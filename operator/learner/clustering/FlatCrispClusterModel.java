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
import java.util.HashMap;
import java.util.Map;

import com.rapidminer.operator.IOObject;
import com.rapidminer.tools.Tools;


/**
 * This class represents a stadard implementation of a flat, crisp clustering.
 * 
 * @author Michael Wurst
 * @version $Id: FlatCrispClusterModel.java,v 1.7 2008/09/12 10:30:14 tobiasmalbrecht Exp $
 */
public class FlatCrispClusterModel extends AbstractClusterModel implements FlatClusterModel {

	private static final long serialVersionUID = 5438907384571001278L;

	private final Map<String, DefaultCluster> clusterId2Cluster = new HashMap<String, DefaultCluster>();

	private final java.util.List<DefaultCluster> clusters = new ArrayList<DefaultCluster>();

	public FlatCrispClusterModel() {
		super();
	}

	public FlatCrispClusterModel(ClusterModel cm) {
		super(cm);
	}

	/**
	 * Constructor FlatCrispClusterModel.
	 * 
	 * @param cm
	 *            a flat cluster model.
	 */
	public FlatCrispClusterModel(FlatClusterModel cm) {
		super(cm);
		// Copy clusters
		for (int i = 0; i < cm.getNumberOfClusters(); i++) {
			DefaultCluster cluster = new DefaultCluster(cm.getClusterAt(i));
			clusters.add(cluster);
			clusterId2Cluster.put(cluster.getId(), cluster);
		}
	}

	public IOObject copy() {
		return new FlatCrispClusterModel(this);
	}

	public Cluster getClusterById(String id) {
		return clusterId2Cluster.get(id);
	}

	public int getNumberOfClusters() {
		return clusters.size();
	}

	public Cluster getClusterAt(int index) {
		return clusters.get(index);
	}

	public void addCluster(DefaultCluster cl) {
		clusters.add(cl);
		clusterId2Cluster.put(cl.getId(), cl);
	}

	public void removeClusterAt(int index) {
		DefaultCluster cluster = clusters.remove(index);
		clusterId2Cluster.remove(cluster.getId());
	}

	public void removeCluster(DefaultCluster cl) {
		clusters.remove(cl);
	}

	public String toString() {
		StringBuffer result = new StringBuffer(super.toString() + Tools.getLineSeparator());
		int sum = 0;
		for (int i = 0; i < getNumberOfClusters(); i++) {
			Cluster cl = getClusterAt(i);
			int numObjects = cl.getNumberOfObjects();
			result.append("Cluster " + cl.getId() + " [characterization: " + cl.getDescription() + "]: " + numObjects + " items" + Tools.getLineSeparator());
			sum = sum + numObjects;
		}
		result.append("Total number of items: " + sum + Tools.getLineSeparator());
		return result.toString();
	}
}
