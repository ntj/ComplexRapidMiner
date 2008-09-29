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
import java.util.Random;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.DefaultCluster;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;

/**
 * Returns a random clustering. Note that this algorithm does not garantuee that all clusters are non-empty.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: RandomFlatClusterer.java,v 1.8 2008/09/12 10:31:44 tobiasmalbrecht Exp $
 */
public class RandomFlatClusterer extends AbstractFlatClusterer {

	/** The parameter name for &quot;the maximal number of clusters&quot; */
	public static final String PARAMETER_K = "k";
	
	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	public RandomFlatClusterer(OperatorDescription description) {
		super(description);
	}

	public ClusterModel createClusterModel(ExampleSet es) throws OperatorException {
		int k = getParameterAsInt(PARAMETER_K);

		List<String> items = new ArrayList<String>();
		Iterator<Example> er = es.iterator();
		while (er.hasNext()) {
			Example ex = er.next();
			items.add(IdUtils.getIdFromExample(ex));
		}
		
		FlatCrispClusterModel result = new FlatCrispClusterModel();
		for (int i = 0; i < k; i++)
			result.addCluster(new DefaultCluster("" + i));
		
		Random rng = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
		for (int i = 0; i < items.size(); i++) {
			int randomIndex = rng.nextInt(k);
			((DefaultCluster) result.getClusterAt(randomIndex)).addObject(items.get(i));
		}
		
		return result;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_K, "the maximal number of clusters", 2, Integer.MAX_VALUE, 2);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1,
				Integer.MAX_VALUE, -1));
		return types;
	}
}
