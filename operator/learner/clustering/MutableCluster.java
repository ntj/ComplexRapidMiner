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
 * Represents a mutable cluster.
 * 
 * @author Michael Wurst
 * @version $Id: MutableCluster.java,v 1.5 2008/09/12 10:30:27 tobiasmalbrecht Exp $
 */
public interface MutableCluster extends Cluster {

	/**
	 * Add a new object to the cluster.
	 * 
	 * @param objId
	 *            an object id
	 */
	public void addObject(String objId);

	/**
	 * Remove an object from the cluster.
	 * 
	 * @param objId
	 *            an object id
	 */
	public void removeObject(String objId);

	/**
	 * Sets the description.
	 * 
	 * @param description
	 *            The description to set
	 */
	public void setDescription(String description);
}
