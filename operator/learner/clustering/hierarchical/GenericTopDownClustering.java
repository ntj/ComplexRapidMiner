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
package com.rapidminer.operator.learner.clustering.hierarchical;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.LastInnerOperatorCondition;
import com.rapidminer.operator.learner.clustering.Cluster;
import com.rapidminer.operator.learner.clustering.ClusterIterator;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.tools.IterationArrayList;

/**
 * A top-down generic clustering that can be used with any (flat) clustering as
 * inner operator. Note though, that the outer operator cannot set or get the
 * maximal number of clusters, the inner operator produces. These value has to
 * be set in the inner operator.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: GenericTopDownClustering.java,v 1.2 2007/06/30 23:24:35
 *          ingomierswa Exp $
 */
public class GenericTopDownClustering extends TopDownClustering {

	public GenericTopDownClustering(OperatorDescription description) {
		super(description);
	}

	protected List<List<String>> clusterItems(List<String> items)
			throws OperatorException {
		final Set<String> itemSet = new HashSet<String>(items);
		List<List<String>> result = new LinkedList<List<String>>();
		ConditionedExampleSet fest = new ConditionedExampleSet(getExampleSet(),
				new Condition() {

					private static final long serialVersionUID = 4253578877561302749L;

					public boolean conditionOk(Example example) {
						return itemSet.contains(example
								.getValueAsString(example.getAttributes()
										.getId()));
					}

					 /** 
					  * @deprecated Conditions should not be able to be changed dynamically and hence there is no need for a copy
					  */
					@Deprecated
					public Condition duplicate() {
						return this;
					}
				});
		if (fest.size() > 0) {

			ClusterModel resultModel = null;
			IOContainer container = getInput().append(new IOObject[] { fest });
			container = getOperator(0).apply(container);
			resultModel = container.remove(ClusterModel.class);
			ClusterIterator clIt = new ClusterIterator(resultModel);
			while (clIt.hasMoreClusters()) {
				Cluster oldCluster = clIt.nextCluster();
				List<String> newCluster = new IterationArrayList<String>(
						oldCluster.getObjects());
				result.add(newCluster);
			}
		} else
			result.add(new LinkedList<String>());

		return result;
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		return new LastInnerOperatorCondition(new Class[] { ExampleSet.class },
				new Class[] { ClusterModel.class });
	}

	public int getMaxNumberOfInnerOperators() {
		return 1;
	}

	public int getMinNumberOfInnerOperators() {
		return 1;
	}
}
