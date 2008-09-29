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

/**
 * Interface for a flat cluster model.
 * 
 * @author Michael Wurst
 * @version $Id: FlatClusterModel.java,v 1.5 2008/09/12 10:30:14 tobiasmalbrecht Exp $
 */
public interface FlatClusterModel extends ClusterModel {

	/**
	 * Get the number of clusters in the model.
	 * 
	 * @return int
	 */
	public int getNumberOfClusters();

	/**
	 * Get a cluster at a given position.
	 * 
	 * @param index
	 * @return Cluster
	 */
	public Cluster getClusterAt(int index);
}
