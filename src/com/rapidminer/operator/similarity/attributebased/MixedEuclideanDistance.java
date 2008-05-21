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
package com.rapidminer.operator.similarity.attributebased;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;


/**
 * Euclidean distance for numerical and nominal values. For nomimal values, a distance of one is accounted if both values are not the same. Note: In
 * most cases, you must normalize the numerical values, to obtain sound results.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: MixedEuclideanDistance.java,v 1.4 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class MixedEuclideanDistance extends AbstractValueBasedSimilarity {

	private static final long serialVersionUID = 5160645165398097366L;

	private boolean[] isNominal;

	public double similarity(double[] e1, double[] e2) {
		double sum = 0.0;
		int counter = 0;
		
		for (int i = 0; i < e1.length; i++) {
			if ((!Double.isNaN(e1[i])) && (!Double.isNaN(e2[i]))) {
				if (isNominal[i]) {
					if (e1[i] != e2[i])
						sum = sum + 1.0;
				} else {
					sum = sum + (e1[i] - e2[i]) * (e1[i] - e2[i]);
				}
				counter++;
			}
		}
		double d = Math.sqrt(sum);
		if (counter > 0)
			return d;
		else
			return Double.NaN;
	}

	public void init(ExampleSet exampleSet) throws OperatorException {
		super.init(exampleSet);
		isNominal = new boolean[exampleSet.getAttributes().size()];
		int index = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			isNominal[index++] = attribute.isNominal();
		}
	}

	public boolean isDistance() {
		return true;
	}
}
