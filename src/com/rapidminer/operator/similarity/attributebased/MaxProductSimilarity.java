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
 * Specialized similarity that takes the maximum product of two feature values. If this value is zero, the similarity is undefined. This similarity
 * measure is used mainly with features extracted from cluster models.
 * 
 * @author Michael Wurst
 * @version $Id: MaxProductSimilarity.java,v 1.1 2007/05/27 21:59:45 ingomierswa Exp $
 */
public class MaxProductSimilarity extends AbstractRealValueBasedSimilarity {

	private static final long serialVersionUID = -7476444724888001751L;

	public double similarity(double[] e1, double[] e2) {
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < e1.length; i++) {
			if ((!Double.isNaN(e1[i])) && (!Double.isNaN(e2[i]))) {
				double v = e2[i] * e1[i];
				if (v > max)
					max = v;
			}
		}
		if (max > 0.0)
			return max;
		else
			return Double.NaN;
	}

	public boolean isDistance() {
		return false;
	}
}
