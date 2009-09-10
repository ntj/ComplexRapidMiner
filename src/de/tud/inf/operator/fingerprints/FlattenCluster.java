package de.tud.inf.operator.fingerprints;

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


import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.ClusterNode;
import com.rapidminer.operator.learner.clustering.DefaultCluster;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.HierarchicalClusterModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * Creates a flat cluster model from a hierarchical one.
 * 
 * @author Ulrike Fischer
 */
public class FlattenCluster extends Operator {

	/** The parameter name for &quot;the level depth&quot; */
	public static final String PARAMETER_LEVEL = "level";
	
	private double levelDistance;
	private int numberOfClusters;
	
    
	public FlattenCluster(OperatorDescription description) {
		super(description);
		
		levelDistance = 0.0;
		numberOfClusters = 0;

		addValue(new ValueDouble("level distance", "") {
			public double getDoubleValue() {
				return levelDistance;
			}
		});
		
		addValue(new ValueDouble("number cluster", "") {
			public double getDoubleValue() {
				return numberOfClusters;
			}
		});
	}

	public IOObject[] apply() throws OperatorException {
		HierarchicalClusterModel model = getInput(HierarchicalClusterModel.class);
		ClusterNode root = model.getRootNode();
		int level = getParameterAsInt(PARAMETER_LEVEL);

		// creating priorityQueue using reversing comparator
		PriorityQueue<ClusterNode> queue = new PriorityQueue<ClusterNode>(level+1, new Comparator<ClusterNode>() {
			public int compare(ClusterNode o1, ClusterNode o2) {
				int value = Double.compare(o1.getWeight(), o2.getWeight());
				if (value != 0)
					return value;
				else
					return Double.compare(o1.getNumberOfObjectsInSubtree(), o2.getNumberOfObjectsInSubtree());
			}
		});

		LinkedList<String> leafs = new LinkedList<String>();
		int hasLeafs = 0;
		queue.add(root);
		for (int i=0; i<level; i++) {
			ClusterNode topNode = queue.poll();
			levelDistance = -topNode.getWeight();
			
			if (topNode.getNumberOfSubNodes() == 0) {
				Iterator<String> it = topNode.getObjects();
				while (it.hasNext()) {
					String s = it.next();
					leafs.add(s);
					hasLeafs = 1;
				}
			}
			else if (topNode.getNumberOfSubNodes() == 1) {
				queue.add(topNode.getSubNodeAt(0));
				Iterator<String> it = topNode.getObjects();
				while (it.hasNext()) {
					String s = it.next();
					leafs.add(s);
					hasLeafs = 1;
				}
			} else {
				Iterator<ClusterNode> it = topNode.getSubNodes();
				if (it.hasNext()) {
					while (it.hasNext()) {
						ClusterNode cn = it.next();
						queue.add(cn);
					}
				}
			}
			
			if (queue.size() == 0) 
				break;
		}
		
		// construct flat cluster model from nodes
		FlatCrispClusterModel flatModel = new FlatCrispClusterModel();
		int i = 1;
		for (ClusterNode node: queue) {
			DefaultCluster flatCluster = new DefaultCluster(String.valueOf(i));
			Iterator<String> it = node.getObjectsInSubtree();
			while (it.hasNext()) {
				flatCluster.addObject(it.next());
			}
			i++;
			flatModel.addCluster(flatCluster);
		}
		
		// add outliers
		if (hasLeafs > 0) {
			DefaultCluster flatCluster = new DefaultCluster("0");
			for (String s: leafs)
				flatCluster.addObject(s);
			flatModel.addCluster(flatCluster);
		}
		
		numberOfClusters = flatModel.getNumberOfClusters();
	
		return new IOObject[] {flatModel, model};
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { ClusterModel.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ClusterModel.class, ClusterModel.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeInt(PARAMETER_LEVEL, "level", 0, Integer.MAX_VALUE, 2));
		return types;
	}
}
