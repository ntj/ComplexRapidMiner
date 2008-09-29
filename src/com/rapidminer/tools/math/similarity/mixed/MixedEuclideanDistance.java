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
package com.rapidminer.tools.math.similarity.mixed;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * Euclidean distance for numerical and nominal values. For nomimal values, a distance of one is accounted if both values are not the same. Note: In
 * most cases, you must normalize the numerical values, to obtain sound results.
 * 
 * @author Sebastian Land, Michael Wurst
 * @version $Id: MixedEuclideanDistance.java,v 1.1 2008/08/05 09:40:31 stiefelolm Exp $
 */
public class MixedEuclideanDistance extends DistanceMeasure {

	private boolean[] isNominal;
	
	public double calculateDistance(double[] value1, double[] value2) {
		double sum = 0.0;
		int counter = 0;
		for (int i = 0; i < value1.length; i++) {
			if ((!Double.isNaN(value1[i])) && (!Double.isNaN(value2[i]))) {
				if (isNominal[i]) {
					if (value1[i] != value1[i])
						sum = sum + 1.0;
				} else {
					sum = sum + Math.pow(value1[i] - value2[i], 2);
				}
				counter++;
			}
		}
		if (counter > 0)
			return Math.sqrt(sum);
		else
			return Double.NaN;
	}

	public double calculateSimilarity(double[] value1, double[] value2) {
		return -calculateDistance(value1, value2);
	}

	public void init(ExampleSet exampleSet) {
		isNominal = new boolean[exampleSet.getAttributes().size()];
		int index = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			isNominal[index++] = attribute.isNominal();
		}
	}

}
