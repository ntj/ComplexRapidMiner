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
 * The Camberra distance measure.
 * 
 * @author Michael Wurst
 * @version $Id: CamberraNumericalDistance.java,v 1.1 2007/05/27 21:59:45 ingomierswa Exp $
 */
public class CamberraNumericalDistance extends AbstractRealValueBasedSimilarity {

	private static final long serialVersionUID = 1265980488303010943L;

	public double similarity(double[] e1, double[] e2) {
		double sum = 0.0;
		for (int i = 0; i < e1.length; i++) {
			double v1 = e1[i];
			double v2 = e2[i];
			if ((!Double.isNaN(v1)) && (!Double.isNaN(v2))) {
				sum = sum + Math.abs(v1 - v2) / Math.abs(v1 + v2);
			}
		}
		return sum;
	}

	public boolean isDistance() {
		return true;
	}
}
