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
 * The &quot;KL-divergence &quot;.
 * 
 * @author Regina Fritsch
 * @version $Id: KLDivergence.java,v 1.4 2008/09/12 10:30:43 tobiasmalbrecht Exp $
 */
public class KLDivergence extends AbstractBregmanDivergence {

	public KLDivergence(ExampleSet es) throws InstantiationException {
		super(es);
	}

	public double distance(Example x, double[] y) {
		double result = 0;
		int i = 0;
		for (Attribute attribute : x.getAttributes()) {
			result += x.getValue(attribute) * logXToBaseY(x.getValue(attribute)/y[i],2);
			i++;
		}
		
		return result;
	}

	public boolean isApplicable(Example x) {
		return true;
	}

}
