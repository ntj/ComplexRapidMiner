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
package com.rapidminer.operator.learner.clustering.clusterer;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.similarity.bregmandivergences.*;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.IterationArrayList;

/**
 * This operator represents an implementation of the Bregman Hard Clustering.
 * 
 * @author Regina Fritsch
 * @version $Id: BregmanHardClustering.java,v 1.3 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class BregmanHardClustering extends AbstractKMethod {

	/** The parameter name for &quot;Bregman Divergence&quot; */
	public static final String PARAMETER_BREGMAN_DIVERGENCE = "bregman_divergence";

	public static final String[] DISTANCES = { "Squared Euclidean distance",
			"KL-divergence", "Generalized I-divergence",
			"Itakura-Saito distance", "Logistic loss", "Mahalanobis distance",
			"Squared loss", "Logarithmic loss" };

	public static final int SQUARED_EUCLIDEAN_DISTANCE = 0;

	public static final int KL_DIVERGENCE = 1;

	public static final int GENERALIZED_I_DIVERGENCE = 2;

	public static final int ITAKURA_SAITO_DISTANCE = 3;

	public static final int LOGISTIC_LOSS = 4;

	public static final int MAHALANOBIS_DISTANCE = 5;

	public static final int SQUARED_LOSS = 6;

	public static final int LOGARITHMIC_LOSS = 7;

	/**
	 * The parameter name for &quot;Use the given random seed instead of global
	 * random numbers (-1: use global).&quot;
	 */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";

	private double[][] centroids;

	private ExampleSet exampleSet;

	private int numberOfAttr;

	private int k;

	private BregmanDivergence div;

	public BregmanHardClustering(OperatorDescription description) {
		super(description);
	}

	public ClusterModel createClusterModel(ExampleSet exampleSet) throws OperatorException {
		this.exampleSet = exampleSet;
		k = getParameterAsInt(PARAMETER_K);
		int maxOptimizationSteps = getParameterAsInt(PARAMETER_MAX_OPTIMIZATION_STEPS);
		int maxRuns = getParameterAsInt(PARAMETER_MAX_RUNS);

		// create model
		FlatClusterModel model = kmethod(exampleSet, k, maxOptimizationSteps,
				maxRuns);

		return model;
	}

	// initialize cluster centroids randomly
	protected void initKMethod(List<String> ids, int k) throws OperatorException {
		numberOfAttr = this.exampleSet.getAttributes().size();
		centroids = new double[k][numberOfAttr];
		List<String> randomList = IdUtils.getRandomIdList(ids, k, getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
		for (int i = 0; i < k; i++) {
			String id = randomList.get(i);
			Example ex = IdUtils.getExampleFromId(exampleSet, id);
			int j = 0;
			for (Attribute attribute : ex.getAttributes()) {
				centroids[i][j] = ex.getValue(attribute);
				j++;
			}
		}
		
		// which bregman divergence is to use:
		try {
			int divergence = getParameterAsInt(PARAMETER_BREGMAN_DIVERGENCE);
			try {
				switch (divergence) {
				case 0:
					div = new SquaredEuclideanDistance(exampleSet);
					break;
				case 1:
					div = new KLDivergence(exampleSet);
					break;
				case 2:
					div = new GeneralizedIDivergence(exampleSet);
					break;
				case 3:
					div = new ItakuraSaitoDistance(exampleSet);
					break;
				case 4:
					div = new LogisticLoss(exampleSet);
					break;
				case 5:
					div = new MahalanobisDistance(exampleSet);
					break;
				case 6:
					div = new SquaredLoss(exampleSet);
					break;
				case 7:
					div = new LogarithmicLoss(exampleSet);
					break;
				default:
					div = new SquaredEuclideanDistance(exampleSet);
					break;
				}
			} catch (InstantiationException ie) {
				try {
					div = new SquaredEuclideanDistance(exampleSet);
					setParameter(PARAMETER_BREGMAN_DIVERGENCE, "Squared Euclidean distance");
					logWarning(ie.getMessage());
				} catch (Exception e) {
				}
			}
		} catch (UndefinedParameterError e) {
		}
	}

	// the best cluster for the example
	protected int bestIndex(String id, FlatCrispClusterModel cm, FlatCrispClusterModel oldCm) {
		Example toAllocate = IdUtils.getExampleFromId(exampleSet, id);
		int bestIndex = -1;
		double shortestDistance = Double.POSITIVE_INFINITY;
		for (int i = 0; i < k; i++) {
			double[] centroid = centroids[i];
			
			double divergence = div.computeDistance(toAllocate, centroid);
			if (shortestDistance > divergence) {
				shortestDistance = divergence;
				bestIndex = i;
			}
		}
		return bestIndex;
	}

	// compute new cluster centroids
	protected void recalculateCentroids(FlatCrispClusterModel cl) {
		// if no weights available, initialise weights
		if (exampleSet.getAttributes().getWeight() == null) {
			com.rapidminer.example.Tools.createWeightAttribute(exampleSet);
		}
		// clear centroids
		centroids = new double[k][numberOfAttr];

		// compute new centroids for every cluster i
		for (int i = 0; i < k; i++) {
			// over all objects in cluster i
			List<String> iter = new IterationArrayList<String>(cl.getClusterAt(i).getObjects());
			double probMeasureSum = 0;
			for (int j = 0; j < iter.size(); j++) {
				Example ex = IdUtils.getExampleFromId(exampleSet, iter.get(j));
				probMeasureSum += ex.getWeight();
				int a = 0;
				for (Attribute attribute : ex.getAttributes()) {
					centroids[i][a] += (ex.getWeight() * ex.getValue(attribute));
					a++;
				}
			}
			for (int x = 0; x < centroids[i].length; x++) {
				centroids[i][x] = centroids[i][x] / probMeasureSum;
			}
		}
	}

	protected double evaluateClusterModel(FlatCrispClusterModel cl) {
		// according to KMeans
		double sum = 0;
		int count = 0;
		for (int i = 0; i < cl.getNumberOfClusters(); i++) {
			List<String> objectsIter = new IterationArrayList<String>(cl.getClusterAt(i).getObjects());
			for (int j = 0; j < objectsIter.size(); j++) {
				Example ex = IdUtils.getExampleFromId(exampleSet, objectsIter.get(j));
				double z = (div.computeDistance(ex, centroids[i]));
				sum += (z * z);
				count++;
			}
		}
		return -(sum / count);
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		types.add(new ParameterTypeCategory(PARAMETER_BREGMAN_DIVERGENCE, "The Bregman Divergence", DISTANCES, SQUARED_EUCLIDEAN_DISTANCE));
		types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "The local random seed (-1: use global random seed)", -1, Integer.MAX_VALUE, -1));
		return types;
	}

}
