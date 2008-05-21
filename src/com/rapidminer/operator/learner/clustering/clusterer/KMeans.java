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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.DefaultCluster;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.learner.clustering.MutableCluster;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.IterationArrayList;
import com.rapidminer.tools.RandomGenerator;


/**
 * This operator represents a simple implementation of k-means.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: KMeans.java,v 1.8 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class KMeans extends AbstractFlatClusterer {

	/** The parameter name for &quot;the maximal number of clusters&quot; */
	public static final String PARAMETER_K = "k";

	/** The parameter name for &quot;the maximal number of runs of the k method with random initialization that are performed&quot; */
	public static final String PARAMETER_MAX_RUNS = "max_runs";

	/** The parameter name for &quot;the maximal number of iterations performed for one run of the k method&quot; */
	public static final String PARAMETER_MAX_OPTIMIZATION_STEPS = "max_optimization_steps";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	private int numAttributes;

	private int k;

	public KMeans(OperatorDescription description) {
		super(description);
	}

	public ClusterModel createClusterModel(ExampleSet es) throws OperatorException {
		es.remapIds();
		
		k = getParameterAsInt(PARAMETER_K);
		int maxOptimizationSteps = getParameterAsInt(PARAMETER_MAX_OPTIMIZATION_STEPS);
		int maxRuns = getParameterAsInt(PARAMETER_MAX_RUNS);

		// additional checks
		Tools.onlyNumericalAttributes(es, "KMeans");
		Tools.onlyNonMissingValues(es, "KMeans");
		if (es.size() < k) {
			logWarning("number of clusters (k) = " + k + " > number of objects =" + es.size());
			k = es.size();
		}
		
		List<String> ids = new ArrayList<String>();
		Iterator<Example> er = es.iterator();
		while (er.hasNext()) {
			Example ex = er.next();
			ids.add(IdUtils.getIdFromExample(ex));
		}
		
		double max = Double.NEGATIVE_INFINITY;
		KMeansClusterModel bestModel = null;
		for (int iter = 0; iter < maxRuns; iter++) {
			FlatCrispClusterModel oldResult = null;
			// initialize the method
			KMeansClusterModel result = new KMeansClusterModel(initializeCendroids(es, ids), es);
			boolean stableState = false;
			for (int l = 0; (l < maxOptimizationSteps) && !stableState; l++) {
				stableState = true;
				while (result.getNumberOfClusters() > 0)
					result.removeClusterAt(0);
				for (int j = 0; j < k; j++) {
					result.addCluster(new DefaultCluster("" + j));
				}
				// Assign the ids to the cluster centroids
				for (int i = 0; i < ids.size(); i++) {
					String d1 = ids.get(i);
					int maxIndex = bestIndex(d1, es, result);
					log("KMethod: item " + d1 + "assigned to cluster with index " + maxIndex);
					if (maxIndex < 0)
						maxIndex = RandomGenerator.getGlobalRandomGenerator().nextInt(result.getNumberOfClusters());
					((MutableCluster) result.getClusterAt(maxIndex)).addObject(d1);
					if (oldResult == null)
						stableState = false;
					else if (!oldResult.getClusterAt(maxIndex).contains(d1))
						stableState = false;
				}
				// Capture the old result to compare it with the result of the next round
				oldResult = new FlatCrispClusterModel(result);
				// Recalculate the centroids
				recalculateCentroids(es, result);
			}
			double v = evaluateClusterModel(es, result);
			if (v > max) {
				max = v;
				bestModel = result;
			}
		}

		return bestModel;
	}

	protected double[][] initializeCendroids(ExampleSet es, List<String> ids) throws OperatorException {
		numAttributes = es.getAttributes().size();
		double[][] centroids = new double[k][numAttributes];
		List randomIdList = IdUtils.getRandomIdList(ids, k, getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
		for (int j = 0; j < k; j++) {
			String id = (String) randomIdList.get(j);
			Example example = IdUtils.getExampleFromId(es, id);
			int m = 0;
			for (Attribute att : example.getAttributes()) {
				centroids[j][m++] = example.getValue(att);
			}
		}
		return centroids;
	}

	protected double evaluateClusterModel(ExampleSet es, KMeansClusterModel cm) {
		int count = 0;
		double sum = 0.0;
		for (int i = 0; i < cm.getNumberOfClusters(); i++) {
			List<String> objs = new IterationArrayList<String>(cm.getClusterAt(i).getObjects());
			for (int j = 0; j < objs.size(); j++) {
				String d = objs.get(j);
				double v = cm.getDistanceFromCentroid(i, IdUtils.getExampleFromId(es, d));
				sum = sum + v * v;
				count++;
			}
		}
		return -(sum / count);
	}

	protected int bestIndex(String id, ExampleSet es, KMeansClusterModel cl) {
		Example ex = IdUtils.getExampleFromId(es, id);
		double min = Double.MAX_VALUE;
		int maxIndex = 0;
		for (int j = 0; j < k; j++) {
			double d = 0;
			int m = 0;
			for (Attribute att : ex.getAttributes()) {
				d = d + (ex.getValue(att) - cl.getCentroid(j)[m]) * (ex.getValue(att) - cl.getCentroid(j)[m]);
				m++;
			}
			if (d < min) {
				min = d;
				maxIndex = j;
			}
		}
		return maxIndex;
	}

	protected void recalculateCentroids(ExampleSet es, KMeansClusterModel cl) {
		for (int j = 0; j < k; j++) {
			List<String> x = new IterationArrayList<String>(cl.getClusterAt(j).getObjects());
			for (int m = 0; m < numAttributes; m++) {
				cl.getCentroid(j)[m] = 0.0;
			}
			int numExamplesInCluster = x.size();
			for (int i = 0; i < numExamplesInCluster; i++) {
				Example ex = IdUtils.getExampleFromId(es, x.get(i));
				int m = 0;
				for (Attribute att : ex.getAttributes()) {
					cl.getCentroid(j)[m++] += ex.getValue(att) / numExamplesInCluster;
				}
			}
		}
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_K, "The number of clusters which should be detected.", 2, Integer.MAX_VALUE, 2);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeInt(PARAMETER_MAX_RUNS, "The maximal number of runs of k-Means with random initialization that are performed.", 1,
				Integer.MAX_VALUE, 10));
		types.add(new ParameterTypeInt(PARAMETER_MAX_OPTIMIZATION_STEPS, "The maximal number of iterations performed for one run of k-Means.", 1,
				Integer.MAX_VALUE, 100));
		types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1,
				Integer.MAX_VALUE, -1));
		return types;
	}
}
