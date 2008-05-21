/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2008 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.operator.learner.clustering.constrained;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import Jama.Matrix;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.Cluster;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.DefaultCluster;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.learner.clustering.clusterer.AbstractKMethod;
import com.rapidminer.operator.learner.clustering.constrained.constraints.ClusterConstraintList;
import com.rapidminer.operator.learner.clustering.constrained.constraints.LinkClusterConstraint;
import com.rapidminer.operator.learner.clustering.constrained.constraints.LinkClusterConstraintList;
import com.rapidminer.operator.similarity.attributebased.AbstractRealValueBasedSimilarity;
import com.rapidminer.operator.similarity.attributebased.ParameterizedEuclideanDistance;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;

/**
 * This is an implementation of the "Metric Pairwise Constraints K-Means" algorithm (see "Mikhail Bilenko, Sugato Basu,
 * and Raymond J. Mooney. Integrating constraints and metric learning in semi-supervised clustering. In Proceedings of
 * the 21st International Conference on Machine Learning, ICML, pages 81–88, Banff, Canada, July 2004.") that uses a
 * list of LinkClusterConstraints created from a (possibly partially) labeled ExampleSet to learn a parameterized
 * euklidean distance metric.
 * 
 * @author Alexander Daxenberger
 * @version $Id: MPCKMeans.java,v 1.11 2008/05/09 19:23:27 ingomierswa Exp $
 */
public class MPCKMeans extends AbstractKMethod {

	private static class DoubleComparator implements Comparator<Double>, Serializable {

		private static final long serialVersionUID = 7686843163121077401L;

		public int compare(Double s1, Double s2) {
			if (s1.doubleValue() < s2.doubleValue())
				return -1;
			else if (s1.doubleValue() > s2.doubleValue())
				return 1;
			else if (s1.doubleValue() != s2.doubleValue())
				return 1;
			else
				return 0;
		}
	}
	
	private double[][] centroid;

	private String[][] farthestPair;

	private ParameterizedEuclideanDistance[] metric;

	private LinkClusterConstraintList conList;

	private ExampleSet es;

	private List idList;

	private int mode;

	private int update;

	private RandomGenerator randomGenerator;

	public static final String[] METRIC_UPDATE = { "none", "diagonal", "full" };

	public static final int METRIC_UPDATE_NONE = 0;

	public static final int METRIC_UPDATE_DIA = 1;

	public static final int METRIC_UPDATE_FULL = 2;

	public static final String[] METRIC_MODE = { "single", "multiple" };

	public static final int METRIC_MODE_SINGLE = 0;

	public static final int METRIC_MODE_MULTI = 1;

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	public MPCKMeans(OperatorDescription description) {
		super(description);
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class, ClusterConstraintList.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean("k_from_labels", "set k to the number of unique labels in the example set", false));
		types.add(new ParameterTypeCategory("metric_update", "choose whether to learn diagonal or full or not to learn metric matrices",
				MPCKMeans.METRIC_UPDATE, 0));
		types.add(new ParameterTypeCategory("metric_mode", "use a single metric for all clusters or one metric for each cluster", MPCKMeans.METRIC_MODE, 0));
		types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1));
		return types;
	}
	
	public ClusterModel createClusterModel(ExampleSet exampleSet) throws OperatorException {
		exampleSet.remapIds();
		
		// initializing random generator
		int seed = getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED); 
		if (seed == -1 ) {
			randomGenerator = RandomGenerator.getGlobalRandomGenerator();
		} else {
			randomGenerator = RandomGenerator.getRandomGenerator(seed);
		}

		
		ClusterModel result;
		int maxK = getParameterAsInt("k");
		int maxOptimizationSteps = getParameterAsInt("max_optimization_steps");
		int maxRuns = getParameterAsInt("max_runs");
		
		es = exampleSet;

		Tools.checkAndCreateIds(es);
		Tools.isNonEmpty(es);

		conList = getInput(LinkClusterConstraintList.class);

		if (this.getParameterAsBoolean("k_from_labels")) {
			maxK = es.getAttributes().getLabel().getMapping().size();
		}

		this.update = this.getParameterAsInt("metric_update");
		this.mode = this.getParameterAsInt("metric_mode");

		this.centroid = new double[maxK][];
		farthestPair = new String[maxK][2];
		metric = new ParameterizedEuclideanDistance[maxK];

		result = kmethod(this.es, maxK, maxOptimizationSteps, maxRuns);

		return result;
	}
	
	/**
	 * The k largest and farthest neighborhood sets are used to calculate the initial cluster centroids (weighted
	 * farthest first algorithm) and the list order of the object ids is randomized.
	 * 
	 */
	protected void initKMethod(List<String> ids, int k) throws OperatorException {
		ArrayList<Set<String>> neighSets = conList.getNeighbourhoodSets();
		double[] weight;
		int len = ids.size();
		int rnd;
		String id;
		double[][] centroidData = null;
		this.idList = ids;

		for (int i = 0; i < k; i++) {
			if ((i == 0) || (this.mode == METRIC_MODE_MULTI))
				this.metric[i] = new ParameterizedEuclideanDistance(this.es);
			else
				this.metric[i] = this.metric[0];
			this.farthestPair[i][0] = null;
		}

		if (neighSets.size() > 0) {
			centroidData = new double[neighSets.size()][];
			weight = new double[neighSets.size()];

			for (int i = 0; i < neighSets.size(); i++) {
				DefaultCluster cluster = new DefaultCluster(neighSets.get(i));
				centroidData[i] = this.calculateClusterCentroid(cluster);
				weight[i] = cluster.getNumberOfObjects();
			}

			centroidData = this.getWeightedFarthestFirst(centroidData, weight, this.metric[0]);

			for (int i = 0; i < k; i++) {
				if (i < centroidData.length) {
					this.centroid[i] = centroidData[i];
				} else {
					this.centroid[i] = this.getRandomCentroid(null);
				}
			}
		} else {
			for (int i = 0; i < k; i++) {
				this.centroid[i] = this.getRandomCentroid(null);
			}
		}

		for (int i = 0; i < len; i++) {
			id = ids.get(i);
			rnd = randomGenerator.nextIntInRange(0, len - 1);
			ids.set(i, ids.get(rnd));
			ids.set(rnd, id);
		}
	}

	/**
	 * the E-step
	 */
	protected int bestIndex(String id, FlatCrispClusterModel cm, FlatCrispClusterModel oldCm) {
		int dim = es.getAttributes().size();
		LinkClusterConstraint lcc;
		List constraints;
		Cluster c;
		double targetValue;
		double minTargetValue = Double.MAX_VALUE;
		double[] vals0 = new double[dim];
		double[] vals1 = new double[dim];
		double dist0;
		double dist1;
		int bestIndex = -1;
		int otherClusterId;
		int numObj;

		constraints = this.conList.getLinkConstraintsFor(id);
		this.getValues(id, vals0);

		for (int i = 0; i < cm.getNumberOfClusters(); i++) {

			c = cm.getClusterAt(i);
			numObj = c.getNumberOfObjects();

			dist0 = this.metric[i].similarity(vals0, this.centroid[i]);
			targetValue = (dist0 * dist0) - Math.log(this.metric[i].getDeterminant());

			if (constraints != null) {

				if ((this.farthestPair[i][0] == null) && (numObj > 1))
					this.getFarthestPair(c, this.metric[i], this.farthestPair[i]);

				for (int j = 0; j < constraints.size(); j++) {
					lcc = (LinkClusterConstraint) constraints.get(j);

					if (lcc.getType() == LinkClusterConstraint.MUST_LINK) {
						if (lcc.constraintViolatedIfAdded(id, c)) {
							otherClusterId = this.getClusterIndexForId(lcc.getOtherId(id), cm);
							if (otherClusterId > -1) {
								this.getValues(lcc.getId0(), vals0);
								this.getValues(lcc.getId1(), vals1);
								dist0 = this.metric[i].similarity(vals0, vals1);
								dist1 = this.metric[otherClusterId].similarity(vals0, vals1);
								targetValue += (0.5 * lcc.getConstraintWeight(null) * ((dist0 * dist0) + (dist1 * dist1)));
							}
						}
					} else {
						if ((this.farthestPair[i][0] != null) && (lcc.constraintViolatedIfAdded(id, c))) {
							this.getValues(this.farthestPair[i][0], vals0);
							this.getValues(this.farthestPair[i][1], vals1);
							dist0 = this.metric[i].similarity(vals0, vals1);
							this.getValues(lcc.getId0(), vals0);
							this.getValues(lcc.getId1(), vals1);
							dist1 = this.metric[i].similarity(vals0, vals1);
							targetValue += (lcc.getConstraintWeight(null) * ((dist0 * dist0) - (dist1 * dist1)));
						}
					}
				}
			}

			if (targetValue < minTargetValue) {
				minTargetValue = targetValue;
				bestIndex = i;
			}
		}

		this.farthestPair[bestIndex][0] = null;

		return bestIndex;
	}

	/**
	 * the M-step
	 */
	protected void recalculateCentroids(FlatCrispClusterModel cl) {
		Cluster[] c;
		Matrix m;

		for (int i = 0; i < cl.getNumberOfClusters(); i++) {
			this.centroid[i] = this.calculateClusterCentroid(cl.getClusterAt(i));
		}

		if (this.update != METRIC_UPDATE_NONE) {
			if (this.mode == METRIC_MODE_MULTI) {
				c = new Cluster[1];
				for (int i = 0; i < cl.getNumberOfClusters(); i++) {
					c[0] = cl.getClusterAt(i);
					if (this.update == METRIC_UPDATE_FULL) {
						m = this.learnFullMatrix(c, i);
					} else {
						m = this.learnDiagonalMatrix(c, i);
					}
					if (m != null)
						this.metric[i].setMatrix(m);
				}
			} else {
				c = new Cluster[cl.getNumberOfClusters()];
				for (int i = 0; i < cl.getNumberOfClusters(); i++) {
					c[i] = cl.getClusterAt(i);
				}
				if (this.update == METRIC_UPDATE_FULL) {
					m = this.learnFullMatrix(c, 0);
				} else {
					m = this.learnDiagonalMatrix(c, 0);
				}
				if (m != null)
					this.metric[0].setMatrix(m);
			}
		}

		for (int i = 0; i < cl.getNumberOfClusters(); i++) {
			this.farthestPair[i][0] = null;
		}
	}

	protected double evaluateClusterModel(FlatCrispClusterModel cl) {
		int dim = es.getAttributes().size();
		LinkClusterConstraint lcc;
		List constraints;
		Cluster c;
		// String[] pair = new String[2];
		String id;
		Iterator iter;
		double targetValue;
		double[] vals0 = new double[dim];
		double[] vals1 = new double[dim];
		double dist0;
		double dist1;
		double evalue = 0.0;
		int numObjects = 0;
		int otherClusterId;

		for (int i = 0; i < cl.getNumberOfClusters(); i++) {

			c = cl.getClusterAt(i);
			numObjects += c.getNumberOfObjects();

			if ((this.farthestPair[i][0] == null) && (c.getNumberOfObjects() > 1))
				this.getFarthestPair(c, this.metric[i], this.farthestPair[i]);

			iter = c.getObjects();
			while (iter.hasNext()) {
				id = (String) iter.next();
				constraints = this.conList.getLinkConstraintsFor(id);

				this.getValues(id, vals0);
				dist0 = this.metric[i].similarity(vals0, this.centroid[i]);
				targetValue = (dist0 * dist0) - Math.log(this.metric[i].getDeterminant());

				if (constraints != null) {
					for (int j = 0; j < constraints.size(); j++) {
						lcc = (LinkClusterConstraint) constraints.get(j);

						if (lcc.getType() == LinkClusterConstraint.MUST_LINK) {
							if (lcc.constraintViolated(c)) {
								otherClusterId = this.getClusterIndexForId(lcc.getOtherId(id), cl);
								if (otherClusterId > -1) {
									this.getValues(lcc.getId0(), vals0);
									this.getValues(lcc.getId1(), vals1);
									dist0 = this.metric[i].similarity(vals0, vals1);
									dist1 = this.metric[otherClusterId].similarity(vals0, vals1);
									targetValue += (0.5 * lcc.getConstraintWeight(null) * ((dist0 * dist0) + (dist1 * dist1)));
								}
							}
						} else {
							if ((this.farthestPair[i][0] != null) && (lcc.constraintViolated(c))) {
								this.getValues(this.farthestPair[i][0], vals0);
								this.getValues(this.farthestPair[i][1], vals1);
								dist0 = this.metric[i].similarity(vals0, vals1);
								this.getValues(lcc.getId0(), vals0);
								this.getValues(lcc.getId1(), vals1);
								dist1 = this.metric[i].similarity(vals0, vals1);
								targetValue += (lcc.getConstraintWeight(null) * ((dist0 * dist0) - (dist1 * dist1)));
							}
						}
					}
				}

				evalue += targetValue / numObjects;
			}
		}

		return evalue;
	}

	private double[] calculateClusterCentroid(Cluster c) {
		Example e;
		Iterator<String> iter = c.getObjects();
		int numExamples = c.getNumberOfObjects();
		double[] centroid = new double[es.getAttributes().size()];

		if (numExamples > 0) {
			for (int i = 0; i < centroid.length; i++) {
				centroid[i] = 0.0;
			}
			while (iter.hasNext()) {
				String id = iter.next();
				e = IdUtils.getExampleFromId(this.es, id);
				int i = 0;
				for (Attribute attribute : es.getAttributes()) {
					centroid[i] += e.getValue(attribute);
					i++;
				}
			}
			for (int i = 0; i < centroid.length; i++) {
				centroid[i] = centroid[i] / numExamples;
			}
		} else {
			this.getRandomCentroid(centroid);
		}

		return centroid;
	}

	/*
	 * private double[] getCentroidBasedRandomCentroid(double[][] centroids) { double[] c; double max; double min;
	 * 
	 * c = new double[es.getAttributes().size()];
	 * 
	 * for (int i = 0; i < es.getAttributes().size(); i++) { c[i] = 0.0; min = Double.MAX_VALUE; max =
	 * -Double.MAX_VALUE; for (int j = 0; j < centroids.length; j++) { c[i] += centroids[j][i]; if (centroids[j][i] >
	 * max) max = centroids[j][i]; if (centroids[j][i] < min) min = centroids[j][i]; } c[i] = (c[i] / centroids.length) +
	 * (2.0 * (Math.random() - 0.5) * (max - min)); }
	 * 
	 * return c; }
	 */
	private double[] getRandomCentroid(double[] c) {
		double[] cent;
		int len = this.idList.size();

		if (c != null)
			cent = c;
		else
			cent = new double[es.getAttributes().size()];

		this.getValues((String) idList.get(randomGenerator.nextIntInRange(0, len - 1)), cent);

		return cent;
	}

	/**
	 * Calculates the matrix of the parameterized euklidean distance metric as diagonal matrix for the clusters in the
	 * array 'c'.
	 * 
	 * @param c
	 *            array of clusters to calculate the matrix for
	 * @param start
	 *            index of first cluster in array
	 * @return the learnt matrix
	 */
	private Matrix learnDiagonalMatrix(Cluster[] c, int start) {
		int dim = es.getAttributes().size();
		Matrix ahinv = new Matrix(dim, dim, 0.0);
		Matrix ah;
		LinkClusterConstraint lcc;
		List constraints;
		Iterator iter;
		String id;
		double[] vals0 = new double[dim];
		double[] vals1 = new double[dim];
		int clusterIndex = start;
		int numObjects = 0;

		for (int l = 0; l < c.length; l++) {

			iter = c[l].getObjects();
			while (iter.hasNext()) {
				id = (String) iter.next();
				this.getValues(id, vals0);
				for (int i = 0; i < dim; i++) {
					ahinv.set(i, i, ahinv.get(i, i) + ((vals0[i] - this.centroid[clusterIndex][i]) * (vals0[i] - this.centroid[clusterIndex][i])));
				}
			}

			constraints = this.conList.getLinkConstraintsFor(c[l]);

			if (constraints != null) {

				if ((this.farthestPair[clusterIndex][0] == null) && (c[l].getNumberOfObjects() > 1))
					this.getFarthestPair(c[l], this.metric[clusterIndex], this.farthestPair[clusterIndex]);

				for (int i = 0; i < constraints.size(); i++) {
					lcc = (LinkClusterConstraint) constraints.get(i);
					if (lcc.getType() == LinkClusterConstraint.MUST_LINK) {
						if (lcc.constraintViolated(c[l])) {
							this.getValues(lcc.getId0(), vals0);
							this.getValues(lcc.getId1(), vals1);
							for (int j = 0; j < dim; j++) {
								ahinv.set(j, j, ahinv.get(j, j) + (0.5 * lcc.getConstraintWeight(null) * (vals0[j] - vals1[j]) * (vals0[j] - vals1[j])));
							}
						}
					} else {
						if ((this.farthestPair[clusterIndex][0] != null) && (lcc.constraintViolated(c[l]))) {
							this.getValues(this.farthestPair[clusterIndex][0], vals0);
							this.getValues(this.farthestPair[clusterIndex][1], vals1);
							for (int j = 0; j < dim; j++) {
								ahinv.set(j, j, ahinv.get(j, j) + (lcc.getConstraintWeight(null) * (vals0[j] - vals1[j]) * (vals0[j] - vals1[j])));
							}
							this.getValues(lcc.getId0(), vals0);
							this.getValues(lcc.getId1(), vals1);
							for (int j = 0; j < dim; j++) {
								ahinv.set(j, j, ahinv.get(j, j) - (lcc.getConstraintWeight(null) * (vals0[j] - vals1[j]) * (vals0[j] - vals1[j])));
							}
						}
					}
				}
			}

			numObjects += c[l].getNumberOfObjects();
			clusterIndex++;
		}

		try {
			ah = ahinv.inverse();
		} catch (Exception e) {
			// ahinv = ahinv.plus(Matrix.identity(dim,
			// dim).timesEquals(ahinv.trace() * 0.01));
			// ah = ahinv.inverse();
			return null;
		}

		if (!this.isPositiveDefinite(ah)) {
			return null;
		}

		ah.timesEquals(numObjects);

		return ah;
	}

	/**
	 * Calculates the matrix of the parameterized euklidean distance metric as full covariance matrix for the clusters
	 * in the array 'c'.
	 * 
	 * @param c
	 *            array of clusters to calculate the matrix for
	 * @param start
	 *            index of first cluster in array
	 * @return the learnt matrix
	 */
	private Matrix learnFullMatrix(Cluster[] c, int start) {
		int dim = es.getAttributes().size();
		Matrix ahinv = new Matrix(dim, dim, 0.0);
		Matrix add = new Matrix(dim, dim, 0.0);
		Matrix ah;
		LinkClusterConstraint lcc;
		List constraints;
		Iterator iter;
		String id;
		double[] vals0 = new double[dim];
		double[] vals1 = new double[dim];
		int clusterIndex = start;
		int numObjects = 0;

		for (int l = 0; l < c.length; l++) {

			iter = c[l].getObjects();
			while (iter.hasNext()) {
				id = (String) iter.next();
				this.getValues(id, vals0);
				this.calculateDifferenceProduct(add, vals0, this.centroid[clusterIndex]);
				ahinv.plusEquals(add);
			}

			constraints = this.conList.getLinkConstraintsFor(c[l]);

			if (constraints != null) {

				if ((this.farthestPair[clusterIndex][0] == null) && (c[l].getNumberOfObjects() > 1))
					this.getFarthestPair(c[l], this.metric[clusterIndex], this.farthestPair[clusterIndex]);

				for (int i = 0; i < constraints.size(); i++) {
					lcc = (LinkClusterConstraint) constraints.get(i);
					if (lcc.getType() == LinkClusterConstraint.MUST_LINK) {
						if (lcc.constraintViolated(c[l])) {
							this.getValues(lcc.getId0(), vals0);
							this.getValues(lcc.getId1(), vals1);
							this.calculateDifferenceProduct(add, vals0, vals1);
							add.timesEquals(0.5 * lcc.getConstraintWeight(null));
							ahinv.plusEquals(add);
						}
					} else {
						if ((this.farthestPair[clusterIndex][0] != null) && (lcc.constraintViolated(c[l]))) {
							this.getValues(this.farthestPair[clusterIndex][0], vals0);
							this.getValues(this.farthestPair[clusterIndex][1], vals1);
							this.calculateDifferenceProduct(add, vals0, vals1);
							add.timesEquals(lcc.getConstraintWeight(null));
							ahinv.plusEquals(add);
							this.getValues(lcc.getId0(), vals0);
							this.getValues(lcc.getId1(), vals1);
							this.calculateDifferenceProduct(add, vals0, vals1);
							add.timesEquals(lcc.getConstraintWeight(null));
							ahinv.minusEquals(add);
						}
					}
				}
			}

			numObjects += c[l].getNumberOfObjects();
			clusterIndex++;
		}

		try {
			ah = ahinv.inverse();
		} catch (Exception e) {
			// ahinv = ahinv.plus(Matrix.identity(dim,
			// dim).timesEquals(ahinv.trace() * 0.01));
			// ah = ahinv.inverse();
			return null;
		}

		if (!this.isPositiveDefinite(ah)) {
			return null;
		}

		ah.timesEquals(numObjects);

		return ah;
	}

	/**
	 * calculates ((v1 - v2) * (v1 - v2)^T)
	 * 
	 * @param m
	 *            Matrix
	 * @param v1
	 *            double[]
	 * @param v2
	 *            double[]
	 */
	private void calculateDifferenceProduct(Matrix m, double[] v1, double[] v2) {
		double[] diff = new double[v1.length];

		for (int i = 0; i < diff.length; i++) {
			diff[i] = v1[i] - v2[i];
		}

		for (int i = 0; i < diff.length; i++) {
			for (int j = 0; j < diff.length; j++) {
				m.set(i, j, diff[i] * diff[j]);
			}
		}
	}

	/**
	 * Finds a pair of objects of a cluster with maximal distance.
	 * 
	 * @param c
	 *            the cluster
	 * @param simi
	 *            a similarity measure
	 * @param pair
	 *            a string array of length 2 to store the object ids
	 * 
	 */
	private void getFarthestPair(Cluster c, AbstractRealValueBasedSimilarity simi, String[] pair) {
		ArrayList<String> list = new ArrayList<String>(c.getNumberOfObjects());
		String id = null;
		String old = null;
		String maxId = null;
		String tmp;
		int dim = es.getAttributes().size();
		double[] idValues = new double[dim];
		double[] values = new double[dim];
		double maxDist;
		double dist;

		if (c.getNumberOfObjects() > 1) {
			Iterator<String> iter = c.getObjects();
			while (iter.hasNext()) {
				list.add(iter.next());
			}

			maxId = list.get(0);

			while (!maxId.equals(old)) {
				old = id;
				id = maxId;
				maxDist = 0.0;
				this.getValues(id, idValues);
				for (int i = 0; i < list.size(); i++) {
					tmp = list.get(i);
					this.getValues(tmp, values);
					dist = simi.similarity(idValues, values);
					if (dist > maxDist) {
						maxDist = dist;
						maxId = tmp;
					}
				}
			}
		}

		pair[0] = id;
		pair[1] = maxId;
	}

	/**
	 * Sorts an array of centroids using the "Weighted Farthest First" algorithm.
	 * 
	 * @param centroids
	 *            the centroids to sort
	 * @param weights
	 *            the weights of the centroids
	 * @param simi
	 *            a similarity measure
	 * @return the sorted centroids
	 * 
	 */
	private double[][] getWeightedFarthestFirst(double[][] centroids, double[] weights, AbstractRealValueBasedSimilarity simi) {
		TreeMap<Double, Integer> sortedMap = new TreeMap<Double, Integer>(new DoubleComparator());
		double minDist;
		double dist;

		for (int i = 0; i < centroids.length; i++) {
			minDist = Double.MAX_VALUE;
			for (int j = 0; j < centroids.length; j++) {
				if (i != j) {
					dist = simi.similarity(centroids[i], centroids[j]) * weights[i];
					if (dist < minDist) {
						minDist = dist;
					}
				}
			}
			sortedMap.put(minDist, i);
		}
		
		double[][] sorted = new double[sortedMap.size()][];
		int c = sorted.length - 1;
		Iterator<Integer> iter = sortedMap.values().iterator();
		while (iter.hasNext()) {
			sorted[c--] = centroids[iter.next()];
		}

		return sorted;
	}

	private boolean isPositiveDefinite(Matrix m) {
		double[] eigVals;

		eigVals = m.eig().getRealEigenvalues();

		for (int i = 0; i < eigVals.length; i++) {
			if (eigVals[i] <= 0.0)
				return false;
		}

		return true;
	}

	private void getValues(String id, double[] values) {
		Example e = IdUtils.getExampleFromId(this.es, id);
		int i = 0;
		for (Attribute attribute : e.getAttributes()) {
			values[i] = e.getValue(attribute);
			i++;
		}
	}

	private int getClusterIndexForId(String objectId, FlatCrispClusterModel cm) {
		if (objectId != null) {
			for (int i = 0; i < cm.getNumberOfClusters(); i++) {
				if (cm.getClusterAt(i).contains(objectId))
					return i;
			}
		}
		return -1;
	}
}
