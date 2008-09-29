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
package com.rapidminer.operator.learner.clustering.hierarchical.upgma;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;

/**
 * @author Michael Wurst
 * @version $Id: EuklidianDistanceMeasure.java,v 1.5 2008/09/12 10:29:49 tobiasmalbrecht Exp $
 */
public class EuklidianDistanceMeasure extends DistanceMeasure {

	public double calculateDistance(Example e1, Example e2) {
		double sum = 0;
		for (Attribute att : e1.getAttributes()) {
			double d = e1.getValue(att) - e2.getValue(att);
			sum += d * d;
		}
		return Math.sqrt(sum);
	}
}
