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
package com.rapidminer.operator.learner.clustering.hierarchical.clustersimilarity;

import com.rapidminer.operator.learner.clustering.ClusterNode;

/**
 * This interface represents a similarity between two clusters as needed for agglomerative clustering.
 * 
 * @author Michael Wurst
 * @version $Id: ClusterSimilarity.java,v 1.5 2008/09/12 10:32:12 tobiasmalbrecht Exp $
 */
public interface ClusterSimilarity {

	/**
	 * Calculate the similarity between two cluster nodes x and y of which the first one was create by merging two clusters x1 and x2.
	 * 
	 * @param x1y
	 *            the similairty between x1 and y
	 * @param x2y
	 *            the similarity between x2 and y
	 * @param cx1
	 *            the cluster x1
	 * @param cx2
	 *            the cluster x2
	 * @param cy
	 *            the cluster y
	 * @return the similarity value
	 */
	public double similarity(double x1y, double x2y, ClusterNode cx1, ClusterNode cx2, ClusterNode cy);
}
