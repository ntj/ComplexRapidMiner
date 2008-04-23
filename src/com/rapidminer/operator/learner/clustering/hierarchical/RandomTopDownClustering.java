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
package com.rapidminer.operator.learner.clustering.hierarchical;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SimpleChainInnerOperatorCondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.RandomGenerator;


/**
 * Creates a random top down clustering. Used for testing purposes.
 * 
 * @author Michael Wurst
 * @version $Id: RandomTopDownClustering.java,v 1.2 2007/06/15 16:58:40 ingomierswa Exp $
 */
public class RandomTopDownClustering extends TopDownClustering {


	/** The parameter name for &quot;the maximal number of clusters at each level&quot; */
	public static final String PARAMETER_MAX_K = "max_k";
	public RandomTopDownClustering(OperatorDescription description) {
		super(description);
	}

	protected List<List<String>> clusterItems(List<String> items) throws UndefinedParameterError {
		int maxNumClusters = getParameterAsInt(PARAMETER_MAX_K);
		List<List<String>> result = new LinkedList<List<String>>();
		int numClusters = 0;
		if (maxNumClusters > items.size())
			numClusters = items.size();
		else
			numClusters = maxNumClusters;
		int diff = items.size() / numClusters;
		List<String> items_ = new ArrayList<String>(items);
		Random rng = RandomGenerator.getGlobalRandomGenerator();
		int i = 0;
		List<String> subList = null;
		while (i < items.size()) {
			int randomIndex = rng.nextInt(items_.size());
			String item = items_.get(randomIndex);
			items_.remove(randomIndex);
			if ((i % diff) == 0) {
				subList = new ArrayList<String>();
				result.add(subList);
			}
			subList.add(item);
			i++;
		}
		return result;
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		return new SimpleChainInnerOperatorCondition();
	}

	public int getMaxNumberOfInnerOperators() {
		return 0;
	}

	public int getMinNumberOfInnerOperators() {
		return 0;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_MAX_K, "the maximal number of clusters at each level", 2, Integer.MAX_VALUE, 2);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
