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

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.DefaultCluster;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.learner.clustering.MutableCluster;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;

/**
 * Abstract class for all k-methods.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: AbstractKMethod.java,v 1.8 2008/09/12 10:31:45 tobiasmalbrecht Exp $
 */
public abstract class AbstractKMethod extends AbstractFlatClusterer {

	/** The parameter name for &quot;the maximal number of clusters&quot; */
	public static final String PARAMETER_K = "k";

	/** The parameter name for &quot;the maximal number of runs of the k method with random initialization that are performed&quot; */
	public static final String PARAMETER_MAX_RUNS = "max_runs";

	/** The parameter name for &quot;the maximal number of iterations performed for one run of the k method&quot; */
	public static final String PARAMETER_MAX_OPTIMIZATION_STEPS = "max_optimization_steps";
	
	public AbstractKMethod(OperatorDescription description) {
		super(description);
	}

	protected abstract void initKMethod(List<String> ids, int k) throws OperatorException;

	protected abstract int bestIndex(String id, FlatCrispClusterModel cm, FlatCrispClusterModel oldCm);

	protected abstract void recalculateCentroids(FlatCrispClusterModel cl);

	protected abstract double evaluateClusterModel(FlatCrispClusterModel cl);

	
	protected FlatCrispClusterModel kmethod(ExampleSet es, int k, int maxOptimizationSteps, int maxRuns) throws OperatorException {
		List<String> ids = new ArrayList<String>();
		Iterator<Example> er = es.iterator();
		while (er.hasNext()) {
			Example ex = er.next();
			ids.add(IdUtils.getIdFromExample(ex));
		}
		if (es.size() < k) {
			logWarning("number of clusters (k) = " + k + " > number of objects =" + es.size());
			k = es.size();
		}
		double max = Double.NEGATIVE_INFINITY;
		FlatCrispClusterModel bestModel = null;
		for (int iter = 0; iter < maxRuns; iter++) {
			FlatCrispClusterModel result = new FlatCrispClusterModel();
			FlatCrispClusterModel oldResult = null;
			// initialize the method
			initKMethod(ids, k);
			boolean stableState = false;
			for (int l = 0; (l < maxOptimizationSteps) && !stableState; l++) {
				log("KMethod run " + l);
				stableState = true;
				result = new FlatCrispClusterModel();
				for (int j = 0; j < k; j++) {
					result.addCluster(new DefaultCluster("" + j));
				}
				// Assign the ids to the cluster centroids
				for (int i = 0; i < ids.size(); i++) {
					String d1 = ids.get(i);
					int maxIndex = bestIndex(d1, result, oldResult);
					log("KMethod: item " + d1 + "assigned to cluster with index " + maxIndex);
					if (maxIndex < 0)
						maxIndex = RandomGenerator.getGlobalRandomGenerator().nextInt(result.getNumberOfClusters());
					((MutableCluster) result.getClusterAt(maxIndex)).addObject(d1);
					if (oldResult == null)
						stableState = false;
					else if (!oldResult.getClusterAt(maxIndex).contains(d1))
						stableState = false;
				}
				oldResult = result;
				// Recalculate the centroids
				recalculateCentroids(result);
			}
			double v = evaluateClusterModel(result);
			if (v > max) {
				max = v;
				bestModel = result;
			}
		}
		return bestModel;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_K, "The number of clusters which should be found.", 2, Integer.MAX_VALUE, 2);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeInt(PARAMETER_MAX_RUNS, "The maximal number of runs of this operator with random initialization that are performed.", 1,
				Integer.MAX_VALUE, 5));
		types.add(new ParameterTypeInt(PARAMETER_MAX_OPTIMIZATION_STEPS, "The maximal number of iterations performed for one run of this operator.", 1,
				Integer.MAX_VALUE, 100));
		return types;
	}
}
