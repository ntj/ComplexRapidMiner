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
package com.rapidminer.operator.similarity.bregmandivergences;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

/**
 * Abstract class that represents a bregman divergence.
 * 
 * @author Regina Fritsch
 * @version $Id: AbstractBregmanDivergence.java,v 1.2 2008/05/09 19:23:19 ingomierswa Exp $
 */
public abstract class AbstractBregmanDivergence implements BregmanDivergence {

	public AbstractBregmanDivergence(ExampleSet es) throws InstantiationException {
		for (Example ex : es) {
			if (isApplicable(ex) == false) {
				throw new InstantiationException("The bregman divergence you've choosen is not applicable for the dataset! Proceeding with the 'Squared Euclidean distance' bregman divergence.");
			}
		}
	}

	public abstract double distance(Example x, double[] y);

	public double computeDistance(Example x, double[] y) {
		return distance(x, y);
	}

	public double[] vectorSubtraction(Example x, double[] y) {
		if (x.getAttributes().size() != y.length) {
			throw new RuntimeException(
					"Cannot substract vectors: incompatible numbers of attributes ("
							+ x.getAttributes().size() + " != " + y.length
							+ ")!");
		}
		double[] result = new double[x.getAttributes().size()];
		int i = 0;
		for (Attribute att : x.getAttributes()) {
			result[i] = x.getValue(att) - y[i];
			i++;
		}
		return result;
	}

	public double logXToBaseY(double number, double base) {
		return Math.log(number) / Math.log(base);
	}

}
