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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An iterator on clusters and objects on top of a cluster model.
 * 
 * @author Michael Wurst
 * @version $Id: ClusterIterator.java,v 1.1 2007/05/27 21:58:38 ingomierswa Exp $
 */
public class ClusterIterator implements Iterator<Cluster> {

	private List<Cluster> clusters;

	private Iterator<Cluster> clusterIterator;

	public ClusterIterator(ClusterNode cn) {
		clusters = new LinkedList<Cluster>();
		collectClusters(cn);
		clusterIterator = clusters.iterator();
	}

	/**
	 * Create a cluster iterator.
	 * 
	 * @param cm
	 *            the underlying cluster model.
	 */
	public ClusterIterator(ClusterModel cm) {
		super();
		if (cm instanceof FlatClusterModel)
			init((FlatClusterModel) cm);
		else if (cm instanceof HierarchicalClusterModel)
			init((HierarchicalClusterModel) cm);
		else
			throw new RuntimeException("Only flat and hierarchical cluster model supported");
	}

	/**
	 * Constructor for ClusterIterator.
	 */
	private void init(HierarchicalClusterModel cm) {
		clusters = new LinkedList<Cluster>();
		collectClusters(cm.getRootNode());
		clusterIterator = clusters.iterator();
	}

	/**
	 * Constructor for ClusterIterator.
	 */
	private void init(FlatClusterModel cm) {
		clusters = new LinkedList<Cluster>();
		for (int i = 0; i < cm.getNumberOfClusters(); i++)
			clusters.add(cm.getClusterAt(i));
		clusterIterator = clusters.iterator();
	}

	/**
	 * Return the next cluster.
	 * 
	 * @return a Cluster
	 */
	public Cluster nextCluster() {
		return clusterIterator.next();
	}

	/**
	 * Are there more clusters to iterate over?
	 * 
	 * @return a boolean
	 */
	public boolean hasMoreClusters() {
		return clusterIterator.hasNext();
	}

	private void collectClusters(ClusterNode cl) {
		clusters.add(cl);
		Iterator<ClusterNode> it = cl.getSubNodes();
		while (it.hasNext()) {
			collectClusters(it.next());
		}
	}

	public boolean hasNext() {
		return hasMoreClusters();
	}

	public Cluster next() {
		return nextCluster();
	}

	public void remove() {
	// Not supported
	}
}
