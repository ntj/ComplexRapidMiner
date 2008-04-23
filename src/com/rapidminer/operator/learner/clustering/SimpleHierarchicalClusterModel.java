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
package com.rapidminer.operator.learner.clustering;

import com.rapidminer.operator.IOObject;
import com.rapidminer.tools.Tools;

/**
 * This class represents a default implementation of hierarchical clustering. Please note: Access via the method getMembership is not supported yet.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: SimpleHierarchicalClusterModel.java,v 1.2 2007/05/28 21:23:34 ingomierswa Exp $
 */
public class SimpleHierarchicalClusterModel extends AbstractClusterModel implements HierarchicalClusterModel {

	private static final long serialVersionUID = 3938052671389438092L;

	private ClusterNode rootNode = null;

	/**
	 * Constructor for SimpleHierarchicalClusterModel.
	 */
	public SimpleHierarchicalClusterModel() {
		super();
		rootNode = new DefaultClusterNode("root");
	}

	/**
	 * Copy constructor. A deep copy of all cluster nodes is performed.
	 * 
	 * @param clusterModel
	 *            a hierarchical cluster model.
	 */
	public SimpleHierarchicalClusterModel(HierarchicalClusterModel clusterModel) {
		super(clusterModel);
		rootNode = new DefaultClusterNode(clusterModel.getRootNode());
	}

	/**
	 * Creates a hierarchical cluster model by deep copying a cluster node.
	 * 
	 * @param node
	 *            the node that is to copied and set as root node of the resulting cluster model.
	 */
	public SimpleHierarchicalClusterModel(ClusterNode node) {
		super();
		rootNode = new DefaultClusterNode(node);
	}

	/**
	 * Creates a hierarchical cluster model by copying a flat one.
	 * 
	 * @param cm
	 *            the cluster model to copy.
	 */
	public SimpleHierarchicalClusterModel(FlatClusterModel cm) {
		super(cm);
		rootNode = new DefaultClusterNode("root");
		for (int i = 0; i < cm.getNumberOfClusters(); i++) {
			((DefaultClusterNode) rootNode).addSubNode(new DefaultClusterNode(cm.getClusterAt(i)));
		}
	}

	public IOObject copy() {
		return new SimpleHierarchicalClusterModel(this);
	}

	public ClusterNode getRootNode() {
		return rootNode;
	}

	public void setRootNode(ClusterNode rootNode) {
		this.rootNode = rootNode;
	}

	public String toString() {
		StringBuffer result = new StringBuffer(super.toString() + Tools.getLineSeparator());
		result.append("Number of clusters :" + getNumberOfClustersRec(rootNode) + Tools.getLineSeparator());
		result.append("Number of items :" + rootNode.getNumberOfObjectsInSubtree());
		return result.toString();
	}

	private int getNumberOfClustersRec(ClusterNode node) {
		if (node != null) {
			int numClustersInSubtrees = 0;
			for (int i = 0; i < node.getNumberOfSubNodes(); i++)
				numClustersInSubtrees += getNumberOfClustersRec(node.getSubNodeAt(i));
			return numClustersInSubtrees + 1;
		} else {
			return 0;
		}
	}
}
