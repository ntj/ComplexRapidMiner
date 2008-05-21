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
package com.rapidminer.operator.learner.clustering.constrained.constraints;

import com.rapidminer.operator.learner.clustering.Cluster;
import com.rapidminer.operator.learner.clustering.ClusterModel;

/**
 * The interface for a cluster constraint.
 * 
 * @author Alexander Daxenberger
 * @version $Id: ClusterConstraint.java,v 1.5 2008/05/09 19:23:17 ingomierswa Exp $
 */
public interface ClusterConstraint extends Cloneable {
	
	/**
	 * Returns true, if this constraint is violated for the given clusterModel
	 * 
	 * @param clusterModel
	 */
	public boolean constraintViolated(ClusterModel clusterModel);

	/**
	 * Returns true, if this constraint is violated for the given cluster
	 * 
	 * @param cluster
	 */
	public boolean constraintViolated(Cluster cluster);

	/**
	 * Returns the weight of this constraint (that possibly depends on
	 * the given clusterModel)
	 * @param clusterModel
	 */
	public double getConstraintWeight(ClusterModel clusterModel);

	public ClusterConstraint clone();
}
