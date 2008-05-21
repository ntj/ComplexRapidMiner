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

import com.rapidminer.example.Example;

/**
 * A cluster model that contains a centroid for each cluster.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: CentroidBasedClusterModel.java,v 1.4 2008/05/09 19:23:03 ingomierswa Exp $
 */
public interface CentroidBasedClusterModel extends FlatClusterModel {

	/**
	 * Get the distance from a given example to the centroid with a given index.
	 * 
	 * @param index
	 * @param e
	 * @return distance
	 */
	double getDistanceFromCentroid(int index, Example e);

	/**
	 * Get the distance between two centroids with given indices.
	 * 
	 * @param index1
	 * @param index2
	 * @return the distance
	 */
	double getCentroidDistance(int index1, int index2);
	
	/** Returns the values for the centroid. */
	double[] getCentroid(int index);
	
	/** Returns the column names for the used dimensions. */
	public String[] getDimensionNames();
	
}
