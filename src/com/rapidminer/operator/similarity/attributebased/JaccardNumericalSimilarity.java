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
 * A variant of the Jaccard coefficient defined for numeric attributes.
 * 
 * @author Michael Wurst
 * @version $Id: JaccardNumericalSimilarity.java,v 1.1 2007/05/27 21:59:46 ingomierswa Exp $
 */
public class JaccardNumericalSimilarity extends AbstractRealValueBasedSimilarity {

	private static final long serialVersionUID = 1817582690040262790L;

	@Override
	public double similarity(double[] e1, double[] e2) {
		double wxy = 0.0;
		double wx = 0.0;
		double wy = 0.0;
		for (int i = 0; i < e1.length; i++) {
			if ((!Double.isNaN(e1[i])) && (!Double.isNaN(e2[i]))) {
				wx = wx + e1[i];
				wy = wy + e2[i];
				wxy = wxy + e1[i] * e2[i];
			}
		}
		return wxy / (wx + wy - wxy);
	}

	public boolean isDistance() {
		return false;
	}
}
