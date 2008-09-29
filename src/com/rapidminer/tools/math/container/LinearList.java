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

import java.util.ArrayList;
import java.util.Collection;

import com.rapidminer.tools.Tupel;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * This class is an implementation of the GeometricDataCollection interface, which
 * searches all datapoints linearly for the next k neighbours. Hence O(n) computations
 * are required for this operation.
 * 
 * @author Sebastian Land
 * @version $Id: LinearList.java,v 1.3 2008/07/13 20:38:24 ingomierswa Exp $
 * 
 * @param <T> This is the type of value with is stored with the points and retrieved on nearest
 * neighbour search
 */
public class LinearList<T> implements GeometricDataCollection<T> {

	DistanceMeasure distance;
	ArrayList<double[]> samples = new ArrayList<double[]>();
	ArrayList<T> storedValues = new ArrayList<T>();
	
	public LinearList(DistanceMeasure distance) {
		this.distance = distance;
	}
	
	public void add(double[] values, T storeValue) {
		this.samples.add(values);
		this.storedValues.add(storeValue);
	}

	public Collection<T> getNearestValues(int k, double[] values) {
		BoundedPriorityQueue<Tupel<Double, T>> queue = new BoundedPriorityQueue<Tupel<Double, T>>(k);
		int i = 0;
		for (double[] sample: this.samples) {
			queue.add(new Tupel<Double, T>(distance.calculateDistance(sample, values), storedValues.get(i)));
			i++;
		}
		
		Collection<T> result = new ArrayList<T>(k);
		for (Tupel<Double, T> tupel: queue) {
			result.add(tupel.getSecond());
		}
		return result;
	}

	public Collection<Tupel<Double, T>> getNearestValueDistances(int k, double[] values) {
		BoundedPriorityQueue<Tupel<Double, T>> queue = new BoundedPriorityQueue<Tupel<Double, T>>(k);
		int i = 0;
		for (double[] sample: this.samples) {
			queue.add(new Tupel<Double, T>(distance.calculateDistance(sample, values), storedValues.get(i)));
			i++;
		}
		
		Collection<Tupel<Double, T>> result = new ArrayList<Tupel<Double, T>>(k);
		for (Tupel<Double, T> tupel: queue) {
			result.add(new Tupel<Double, T>(tupel.getFirst(), tupel.getSecond()));
		}
		return result;
	}
}
