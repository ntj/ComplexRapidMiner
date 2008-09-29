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

import java.io.Serializable;
import java.util.Iterator;

/**
 * Represents an individual cluster.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: Cluster.java,v 1.6 2008/09/12 10:30:33 tobiasmalbrecht Exp $
 */
public interface Cluster extends Serializable {

	/**
	 * Get all objects associated to this cluster.
	 * 
	 * @return Iterator of String
	 */
	public Iterator<String> getObjects();

	/**
	 * Get the id of the cluster.
	 * 
	 * @return String
	 */
	public String getId();

	/**
	 * Get the description of the cluster.
	 * 
	 * @return String
	 */
	public String getDescription();

	/**
	 * Check whether the given object is contained in the cluster.
	 * 
	 * @param id
	 *            the id representing the object
	 * @return boolean
	 */
	public boolean contains(String id);

	/**
	 * Returns the number of objects in this cluster
	 * 
	 * @return number of objects
	 */
	public int getNumberOfObjects();
}
