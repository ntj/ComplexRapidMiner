/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.learner.clustering.clusterer;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.similarity.DistanceSimilarityConverter;
import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.operator.similarity.SimilarityUtil;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.IterationArrayList;


/**
 * Simple implementation of k-medoids.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: KMedoids.java,v 1.3 2007/06/30 23:24:35 ingomierswa Exp $
 */
public class KMedoids extends AbstractKMethod {

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	private SimilarityMeasure sim;

	private String[] medoids;

	public KMedoids(OperatorDescription description) {
		super(description);
	}

	public ClusterModel createClusterModel(ExampleSet es) throws OperatorException {
		int maxK = getParameterAsInt(PARAMETER_K); 
		int maxOptimizationSteps = getParameterAsInt(PARAMETER_MAX_OPTIMIZATION_STEPS); 
		int maxRuns = getParameterAsInt(PARAMETER_MAX_RUNS); 
		
		sim = SimilarityUtil.resolveSimilarityMeasure(getParameters(), getInput(), es);
		if (sim.isDistance())
			sim = new DistanceSimilarityConverter(sim);

		FlatClusterModel result = kmethod(es, maxK, maxOptimizationSteps, maxRuns);

		return result;
	}

	protected void initKMethod(List<String> ids, int k) throws OperatorException {
		medoids = new String[k];
		List<String> randomList = IdUtils.getRandomIdList(ids, k, getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
		for (int j = 0; j < k; j++) {
			medoids[j] = randomList.get(j);
		}
	}

	protected int bestIndex(String id, FlatCrispClusterModel cl, FlatCrispClusterModel cmOld) {
		String d1 = id;
		double max = Double.NEGATIVE_INFINITY;
		int maxIndex = 0;
		int j;
		for (j = 0; (j < medoids.length) && !medoids[j].equals(d1); j++)
			if (sim.isSimilarityDefined(d1, medoids[j]))
				if (sim.similarity(d1, medoids[j]) > max) {
					max = sim.similarity(d1, medoids[j]);
					maxIndex = j;
				}
		if (j < medoids.length)
			maxIndex = j;
		return maxIndex;
	}

	protected void recalculateCentroids(FlatCrispClusterModel cl) {
		for (int j = 0; j < medoids.length; j++) {
			List<String> x = new IterationArrayList<String>(cl.getClusterAt(j).getObjects());
			double max = Double.NEGATIVE_INFINITY;
			String maxId = null;
			for (int i1 = 0; i1 < x.size(); i1++) {
				String d1 = x.get(i1);
				double sum = 0.0;
				for (int i2 = 0; i2 < x.size(); i2++) {
					String d2 = x.get(i2);
					if (sim.isSimilarityDefined(d1, d2))
						sum = sum + sim.similarity(d1, d2);
				}
				if (sum > max) {
					max = sum;
					maxId = d1;
				}
			}
			// For the case, that all similarities are undefined, take the first
			// as mendoid
			if (maxId == null)
				maxId = x.get(0);
			medoids[j] = maxId;
		}
	}

	protected double evaluateClusterModel(FlatCrispClusterModel cm) {
		double sum = 0.0;
		int count = 0;
		for (int i = 0; i < cm.getNumberOfClusters(); i++) {
			List<String> objs = new IterationArrayList<String>(cm.getClusterAt(i).getObjects());
			for (int j = 0; j < objs.size(); j++) {
				String d1 = objs.get(j);
				for (int l = j; l < objs.size(); l++) {
					String d2 = objs.get(l);
					if (sim.isSimilarityDefined(d1, d2)) {
						sum = sum + sim.similarity(d1, d2);
						count++;
					}
				}
			}
		}
		return (sum / count);
	}

	public InputDescription getInputDescription(Class cls) {
		if (SimilarityMeasure.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(SimilarityUtil.generateSimilarityParameter());
		types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1,
				Integer.MAX_VALUE, -1));
		return types;
	}
}
