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
 * The &quot;Generalized I-divergence &quot;.
 * 
 * @author Regina Fritsch
 * @version $Id: GeneralizedIDivergence.java,v 1.2 2008/05/09 19:23:19 ingomierswa Exp $
 */
public class GeneralizedIDivergence extends AbstractBregmanDivergence {

	public GeneralizedIDivergence(ExampleSet es) throws InstantiationException {
		super(es);
	}

	public double distance(Example x, double[] y) {
		double result = 0;
		double result2 = 0;
		int i = 0;
		for (Attribute att : x.getAttributes()) {
			result += x.getValue(att) * Math.log((x.getValue(att) / y[i]));
			result2 += (x.getValue(att) - y[i]);
			i++;
		}
		result = result - result2;
		return result;
	}

	public boolean isApplicable(Example x) {
		for (Attribute att : x.getAttributes()) {
			if (x.getValue(att) <= 0) {
				return false;
			}
		}
		return true;
	}

}
