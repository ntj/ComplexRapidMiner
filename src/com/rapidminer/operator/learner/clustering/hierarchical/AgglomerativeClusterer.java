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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.clustering.DefaultCluster;
import com.rapidminer.operator.learner.clustering.DefaultClusterNode;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.HierarchicalClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.learner.clustering.SimpleHierarchicalClusterModel;
import com.rapidminer.operator.learner.clustering.hierarchical.clustersimilarity.ClusterSimilarity;
import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.parameter.Parameters;
import com.rapidminer.tools.ClassNameMapper;
import com.rapidminer.tools.IterationArrayList;


/**
 * This class performs generic agglomorative clustering based on a set of ids and a similarity measure. The algorithm implemented here is currently
 * very simple and not very efficient (cubic).
 * 
 * @author Michael Wurst
 * @version $Id: AgglomerativeClusterer.java,v 1.2 2007/06/15 16:58:40 ingomierswa Exp $
 */
public class AgglomerativeClusterer {


	/** The parameter name for &quot;the cluster similarity criterion (class) to use&quot; */
	public static final String PARAMETER_MODE = "mode";
	private static String[] MODES = new String[] {
			"com.rapidminer.operator.learner.clustering.hierarchical.clustersimilarity.SingleLink",
			"com.rapidminer.operator.learner.clustering.hierarchical.clustersimilarity.CompleteLink"
	};

	private static ClassNameMapper MODE_MAP = new ClassNameMapper(MODES);

	public DefaultClusterNode[] cluster(ExampleSet es, SimilarityMeasure sim, ClusterSimilarity csim, int k) throws OperatorException {
		DefaultClusterNode[] nodes;
		double[][] d;
		final int numObjs;
		Set<String> ids_ = new HashSet<String>();
		Iterator<Example> er = es.iterator();
		while (er.hasNext()) {
			Example ex = er.next();
			ids_.add(IdUtils.getIdFromExample(ex));
		}
		List<String> ids = new ArrayList<String>(ids_);
		numObjs = ids.size();
		List<List<String>> objLists = new ArrayList<List<String>>(numObjs);
		// Initialize matrix
		d = new double[numObjs][numObjs];
		for (int i = 0; i < numObjs; i++) {
			for (int j = 0; j < numObjs; j++)
				d[i][j] = sim.similarity(ids.get(i), ids.get(j));
		}
		// Initialize nodes
		nodes = new DefaultClusterNode[numObjs];
		for (int i = 0; i < numObjs; i++) {
			String objId = ids.get(i);
			nodes[i] = new DefaultClusterNode(objId);
			nodes[i].addObject(objId);
			nodes[i].setWeight(Double.POSITIVE_INFINITY);
			List<String> currentList = new LinkedList<String>();
			currentList.add(objId);
			objLists.add(currentList);
		}
		// Main loop
		for (int numClusters = numObjs; numClusters > k; numClusters--) {
			// find maximal pair
			double max = Double.NEGATIVE_INFINITY;
			int x = -1;
			int y = -1;
			for (int i = 0; i < numObjs; i++)
				for (int j = i + 1; j < numObjs; j++)
					if ((nodes[i] != null) && (nodes[j] != null))
						if (d[i][j] > max) {
							max = d[i][j];
							x = i;
							y = j;
						}
			if ((x > -1) || (y > -1)) {
				// Update the matrix
				for (int i = 0; i < numObjs; i++)
					if (nodes[i] != null) {
						d[x][i] = csim.similarity(d[x][i], d[y][i], nodes[x], nodes[y], nodes[i]);
						d[i][x] = d[x][i];
					}
				// Merge the two clusters
				DefaultClusterNode newNode = new DefaultClusterNode("id " + (numClusters + numObjs));
				addSubNode(newNode, nodes[x]);
				addSubNode(newNode, nodes[y]);
				newNode.setWeight(max);
				nodes[x] = newNode;
				objLists.get(x).addAll(objLists.get(y));
				objLists.set(y, null);
				nodes[y] = null;
			}
		}
		return nodes;
	}

	public HierarchicalClusterModel clusterHierarchical(ExampleSet es, SimilarityMeasure sim, ClusterSimilarity csim, int minItems)
			throws OperatorException {
		DefaultClusterNode nodes[] = cluster(es, sim, csim, 1);
		DefaultClusterNode root = null;
		for (int i = 0; (i < nodes.length) && (root == null); i++)
			if (nodes[i] != null)
				root = nodes[i];
		aggregateSmallClusters(root, minItems);
		SimpleHierarchicalClusterModel result = new SimpleHierarchicalClusterModel();
		result.setRootNode(root);
		return result;
	}

	public FlatCrispClusterModel clusterFlat(ExampleSet es, SimilarityMeasure sim, ClusterSimilarity csim, int k) throws OperatorException {
		DefaultClusterNode nodes[] = cluster(es, sim, csim, k);
		double minSimilarity = Double.POSITIVE_INFINITY;
		FlatCrispClusterModel flatResult = new FlatCrispClusterModel();
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] != null) {
				if (nodes[i].getWeight() < minSimilarity)
					minSimilarity = nodes[i].getWeight();
				DefaultCluster cl = new DefaultCluster("id " + i);
				Iterator<String> it = nodes[i].getObjectsInSubtree();
				while (it.hasNext())
					cl.addObject(it.next());
				flatResult.addCluster(cl);
			}
		}
		flatResult.setProperty("min_similarity", minSimilarity);
		return flatResult;
	}

	private List<String> aggregateSmallClusters(DefaultClusterNode cn, int minSize) {
		List<String> localItems = new IterationArrayList<String>(cn.getObjects());
		if (cn.getNumberOfSubNodes() == 0) {
			return localItems;
		} else {
			List<String> itemsLeft = aggregateSmallClusters((DefaultClusterNode) cn.getSubNodeAt(0), minSize);
			List<String> itemsRight = aggregateSmallClusters((DefaultClusterNode) cn.getSubNodeAt(1), minSize);
			if ((itemsLeft.size() < minSize) && (itemsRight.size() < minSize)) {
				if (itemsLeft.size() + itemsRight.size() + localItems.size() >= minSize) {
					while (cn.getNumberOfSubNodes() > 0)
						cn.removeSubNodeAt(0);
					for (String id : itemsLeft)
						cn.addObject(id);
					for (String id : itemsRight)
						cn.addObject(id);
				}
			}
			if ((itemsLeft.size() < minSize) && (itemsRight.size() >= minSize)) {
				cn.removeSubNodeAt(0);
				for (String id : itemsLeft)
					cn.addObject(id);
			}
			if ((itemsLeft.size() >= minSize) && (itemsRight.size() < minSize)) {
				cn.removeSubNodeAt(1);
				for (String id : itemsRight)
					cn.addObject(id);
			}
			localItems.addAll(itemsLeft);
			localItems.addAll(itemsRight);
			return localItems;
		}
	}

	private void addSubNode(DefaultClusterNode node, DefaultClusterNode subNode) {
		node.addSubNode(subNode);
	}

	public static ClusterSimilarity resolveClusterSimilarity(Parameters parameters) throws UserError {
		String csimClassName = (String) parameters.getParameter(PARAMETER_MODE);
		return (ClusterSimilarity) MODE_MAP.getInstantiation(csimClassName);
	}

	public static ParameterType createClusterSimilarityParameter() {
		ParameterType p = new ParameterTypeStringCategory(PARAMETER_MODE, "the cluster similarity criterion (class) to use", MODE_MAP.getShortClassNames(),
				MODE_MAP.getShortClassNames()[0]);
		p.setExpert(false);
		return p;
	}
}
