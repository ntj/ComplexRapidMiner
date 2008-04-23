/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.similarity.attributebased;

/**
 * Cosine similarity that supports feature weights. If both vectors are empty or null vectors, NaN is returned.
 * 
 * @author Michael Wurst
 * @version $Id: CosineSimilarity.java,v 1.1 2007/05/27 21:59:45 ingomierswa Exp $
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
