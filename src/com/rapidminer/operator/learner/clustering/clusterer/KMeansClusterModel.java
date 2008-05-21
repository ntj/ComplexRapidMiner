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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.learner.clustering.CentroidBasedClusterModel;
import com.rapidminer.operator.learner.clustering.Cluster;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.tools.Tools;


/**
 * A cluster model used for the k-means clustering.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: KMeansClusterModel.java,v 1.6 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class KMeansClusterModel extends FlatCrispClusterModel implements CentroidBasedClusterModel {

	private static final long serialVersionUID = 3162433985759604081L;

	private double[][] centroids;

	private ExampleSet es;

	public KMeansClusterModel(double[][] centroids, ExampleSet es) {
		// Call copy constructor
		super();
		// Assign centroids
		this.centroids = centroids;
		this.es = es;
	}

	public double getCentroidDistance(int index1, int index2) {
		double sum = 0.0;
		for (int i = 0; i < centroids[0].length; i++)
			sum = sum + (centroids[index1][i] - centroids[index2][i]) * (centroids[index1][i] - centroids[index2][i]);
		return Math.sqrt(sum);
	}

	public double getDistanceFromCentroid(int index, Example e) {
		double sum = 0.0;
		int i = 0;
		for (Attribute att : e.getAttributes()) {
			sum = sum + (centroids[index][i] - e.getValue(att)) * (centroids[index][i] - e.getValue(att));
			i++;
		}
		return Math.sqrt(sum);
	}

	public void setCentroid(int index, double[] values) {
		centroids[index] = values;
	}
	
	public double[] getCentroid(int index) {
		return centroids[index];
	}

	public String[] getDimensionNames() {
		return com.rapidminer.example.Tools.getRegularAttributeNames(es);
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer(super.toString() + Tools.getLineSeparator());
		result.append("Cluster centroids:" + Tools.getLineSeparator());
		for (int i = 0; i < getNumberOfClusters(); i++) {
			Cluster cl = getClusterAt(i);
			result.append("Cluster " + cl.getId() + ":\t" + centroidToString(i) + Tools.getLineSeparator());
		}
		return result.toString();
	}

	public String toResultString() {
		return toString();
	}

	private String centroidToString(int index) {
		StringBuffer s = new StringBuffer();
		int i = 0;
		for (Attribute att : es.getAttributes()) {
			s.append(att.getName() + " = " + Tools.formatNumber(centroids[index][i++]) + " ");
		}
		return s.toString();
	}
}
