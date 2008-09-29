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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.ClusterNode;
import com.rapidminer.operator.learner.clustering.ClusterUtils;
import com.rapidminer.operator.learner.clustering.DefaultClusterNode;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.learner.clustering.SimpleHierarchicalClusterModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * An abstract class supporting TopDown Clustering.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: TopDownClustering.java,v 1.9 2008/09/12 10:30:12 tobiasmalbrecht Exp $
 */
public abstract class TopDownClustering extends OperatorChain {

	/** The parameter name for &quot;the maximal number of items in a cluster leaf&quot; */
	public static final String PARAMETER_MAX_LEAF_SIZE = "max_leaf_size";

	/** The parameter name for &quot;if true, a cluster id is generated as new special attribute &quot; */
	public static final String PARAMETER_ADD_CLUSTER_ATTRIBUTE = "add_cluster_attribute";
	
	private int maxSize = 1;

	private ExampleSet eset;

	public TopDownClustering(OperatorDescription description) {
		super(description);
	}

	/**
	 * Cluster a given set of items.
	 * 
	 * @param items
	 *            the items
	 * @return a List of List
	 */
	protected abstract List<List<String>> clusterItems(List<String> items) throws OperatorException;

	public IOObject[] apply() throws OperatorException {
		maxSize = getParameterAsInt(PARAMETER_MAX_LEAF_SIZE);
		eset = getInput(ExampleSet.class);
		Tools.checkAndCreateIds(eset);
		Tools.isNonEmpty(eset);
		
		List<String> items = new LinkedList<String>();
		Iterator<Example> er = eset.iterator();
		while (er.hasNext())
			items.add(IdUtils.getIdFromExample(er.next()));
		SimpleHierarchicalClusterModel result = new SimpleHierarchicalClusterModel();
		result.setRootNode(recursiveClustering(items, "cl"));
		if (getParameterAsBoolean(PARAMETER_ADD_CLUSTER_ATTRIBUTE)) {
			if (!getParameterAsBoolean("keep_example_set"))
				logWarning("Adding a cluster attribute makes only sense, if you keep the example set.");
			else {
				ClusterUtils.addClusterAttribute(eset, result);
			}
		}
		return new IOObject[] {
			result
		};
	}

	private DefaultClusterNode recursiveClustering(List<String> items, String id) throws OperatorException {
		inApplyLoop();
		DefaultClusterNode result = new DefaultClusterNode(id);
		int numItems = items.size();
		boolean clusteringFailed = false;
		if (items.size() > maxSize) {
			List<List<String>> clusters = clusterItems(items);
			for (int j = 0; j < clusters.size(); j++) {
				List<String> itemsInSubnode = clusters.get(j);
				int numItemsInSubnode = itemsInSubnode.size();
				if ((numItemsInSubnode > 0) && (numItemsInSubnode < numItems)) {
					ClusterNode newChild = recursiveClustering(itemsInSubnode, id + "." + j);
					result.addSubNode(newChild);
				} else
					clusteringFailed = true;
			}
			if (!clusteringFailed)
				return result;
			else {
				for (int i = 0; i < items.size(); i++)
					result.addObject(items.get(i));
				return result;
			}
		}
		for (int i = 0; i < items.size(); i++)
			result.addObject(items.get(i));
		return result;
	}

	protected ExampleSet getExampleSet() {
		return eset;
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ClusterModel.class	};
	}

	public InputDescription getInputDescription(Class cls) {
		if (ExampleSet.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, true, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_MAX_LEAF_SIZE, "The maximal number of items in each cluster leaf", 1, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_ADD_CLUSTER_ATTRIBUTE, "Indicates if a cluster id is generated as new special attribute ", true));
		return types;
	}
}
