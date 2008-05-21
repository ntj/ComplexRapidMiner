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
package com.rapidminer.operator.learner.functions.kernel.functions;

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

/** Stores all distances in a matrix (attention: should only be used for smaller data sets).
 * 
 *  @author Ingo Mierswa
 *  @version $Id: FullCache.java,v 1.2 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class FullCache implements KernelCache {

	private double[][] distances;
	
	public FullCache(ExampleSet exampleSet, Kernel kernel) {
		int size = exampleSet.size();
		this.distances = new double[size][size];
		Iterator<Example> reader = exampleSet.iterator();
		int i = 0;
		while (reader.hasNext()) {
			Example example1 = reader.next();
			double[] x1 = new double[exampleSet.getAttributes().size()];
			int x = 0;
			for (Attribute attribute : exampleSet.getAttributes())
				x1[x++] = example1.getValue(attribute);
			Iterator<Example> innerReader = exampleSet.iterator();
			int j = 0;
			while (innerReader.hasNext()) {
				Example example2 = innerReader.next();
				double[] x2 = new double[exampleSet.getAttributes().size()];
				x = 0;
				for (Attribute attribute : exampleSet.getAttributes())
					x2[x++] = example2.getValue(attribute);
				double distance = kernel.calculateDistance(x1, x2);
				this.distances[i][j] = distance;
				this.distances[j][i] = distance;
				j++;
			}
			i++;
		}
	}
	
	public double get(int i, int j) {
		return this.distances[i][j];
	}

	public void store(int i, int j, double value) {
		this.distances[i][j] = value;
	}
}
