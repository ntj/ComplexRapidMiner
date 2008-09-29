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
package com.rapidminer.tools.math.container;

import java.util.Collection;

import com.rapidminer.tools.Tupel;

/**
 * This interface provides the methods for multidimensional data structures
 * providing efficient search in data space for the next k neighbours and its
 * distances
 * 
 * @author Sebastian Land
 * @version $Id: GeometricDataCollection.java,v 1.5 2008/07/13 23:25:24 ingomierswa Exp $
 * 
 * @param <T> The type of the values stored within each point in data space
 */
public interface GeometricDataCollection<T> {

	/**
	 * This method has to be called in order to insert new values into the data structure 
	 * @param values specifies the geometric coordinates in data space
	 * @param storeValue specifies the value at the given point
	 */
	
	public abstract void add(double[] values, T storeValue);

	/**
	 * This method returns a collection of the stored data values from the k nearest sample points.
	 * @param k   the number of neighbours
	 * @param values the coordinate of the querry point in the sample dimension
	 */
	public abstract Collection<T> getNearestValues(int k, double[] values);
	
	/**
	 * This method returns a collection of data from the k nearest sample points.
	 * This collection consists of Tupels containing the distance from querrypoint
	 * to the samplepoint and in the second component the contained value of the sample
	 * point.
	 * @param k   the number of neighbours
	 * @param values the coordinate of the querry point in the sample dimension
	 */
	public abstract Collection<Tupel<Double, T>> getNearestValueDistances(int k, double[] values);

}
