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
 * Represents the complete link cluster similarity.
 * 
 * @author Michael Wurst
 * @version $Id: CompleteLink.java,v 1.5 2008/09/12 10:32:16 tobiasmalbrecht Exp $
 */
public class CompleteLink implements ClusterSimilarity {

	public double similarity(double x1y, double x2y, ClusterNode cx1, ClusterNode cx2, ClusterNode cy) {
		return Math.min(x1y, x2y);
	}
}
