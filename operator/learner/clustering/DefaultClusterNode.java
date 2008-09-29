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
package com.rapidminer.operator.learner.clustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.tools.IterationArrayList;

/**
 * A default implementation of a cluster node.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: DefaultClusterNode.java,v 1.6 2008/09/12 10:30:27 tobiasmalbrecht Exp $
 */
public class DefaultClusterNode extends DefaultCluster implements MutableClusterNode {

	private static final long serialVersionUID = -6519137114615619373L;

	private double weight = Double.NaN;

	private final List<ClusterNode> subNodes = new ArrayList<ClusterNode>();

	/**
	 * Constructor for DefaultClusterNode.
	 * 
	 * @param clusterId
	 *            an id
	 */
	public DefaultClusterNode(String clusterId) {
		super(clusterId);
	}

	/**
	 * Copy constructor. A deep copy of all cluster nodes is performend.
	 * 
	 * @param cl
	 *            a cluster node.
	 */
	public DefaultClusterNode(ClusterNode cl) {
		super(cl);
		this.weight = cl.getWeight();
		Iterator<ClusterNode> it = cl.getSubNodes();
		while (it.hasNext()) {
			subNodes.add(new DefaultClusterNode(it.next()));
		}
	}

	/**
	 * Copy constructor.
	 * 
	 * @param cl
	 *            a cluster
	 */
	public DefaultClusterNode(Cluster cl) {
		super(cl);
	}

	public Iterator<ClusterNode> getSubNodes() {
		return subNodes.iterator();
	}

	public double getWeight() {
		return weight;
	}

	public void addSubNode(ClusterNode clusterNode) {
		subNodes.add(clusterNode);
	}

	public boolean containsInSubtree(String objId) {
		boolean containsLocal = super.contains(objId);
		boolean containsSubtree = false;
		Iterator<ClusterNode> it = getSubNodes();
		while (it.hasNext()) {
			if (it.next().containsInSubtree(objId))
				containsSubtree = true;
		}
		return containsLocal || containsSubtree;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public String toString() {
		return getId();
	}

	public Iterator<String> getObjectsInSubtree() {
		List<String> ols = new LinkedList<String>();
		Iterator<String> itObjs = getObjects();
		while (itObjs.hasNext())
			ols.add(itObjs.next());
		Iterator<ClusterNode> it = getSubNodes();
		while (it.hasNext()) {
			ols.addAll(new IterationArrayList<String>(it.next().getObjectsInSubtree()));
		}
		return new LinkedList<String>(ols).iterator();
	}

	public ClusterNode getSubNodeAt(int i) {
		return subNodes.get(i);
	}

	public void removeSubNode(ClusterNode x) {
		subNodes.remove(x);
	}

	public int getNumberOfSubNodes() {
		return subNodes.size();
	}

	public void removeSubNodeAt(int index) {
		subNodes.remove(index);
	}

	public void insertSubNodeAt(ClusterNode clusterNode, int index) {
		subNodes.add(index, clusterNode);
	}

	public int getNumberOfObjectsInSubtree() {
		int result = getNumberOfObjects();
		Iterator<ClusterNode> it = getSubNodes();
		while (it.hasNext())
			result += it.next().getNumberOfObjectsInSubtree();
		return result;
	}
}
