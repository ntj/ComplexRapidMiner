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

/**
 * Cosine similarity that supports feature weights. If both vectors are empty or null vectors, NaN is returned.
 * 
 * @author Michael Wurst
 * @version $Id: CosineSimilarity.java,v 1.3 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class CosineSimilarity extends AbstractRealValueBasedSimilarity {

	private static final long serialVersionUID = 2856052490402674777L;

	public double similarity(double[] e1, double[] e2) {
		double sum = 0.0;
		double sum1 = 0.0;
		double sum2 = 0.0;
		for (int i = 0; i < e1.length; i++) {
			double v1 = e1[i];
			double v2 = e2[i];
			if ((!Double.isNaN(v1)) && (!Double.isNaN(v2))) {
				sum = sum + v2 * v1;
				sum1 = sum1 + v1 * v1;
				sum2 = sum2 + v2 * v2;
			}
		}
		if ((sum1 > 0) && (sum2 > 0))
			return sum / (Math.sqrt(sum1) * Math.sqrt(sum2));
		else
			return Double.NaN;
	}

	public boolean isDistance() {
		return false;
	}
}
