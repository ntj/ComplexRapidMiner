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
package com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel;

/**
 * Anova Kernel
 * 
 * @author Ingo Mierswa
 * @version $Id: KernelAnova.java,v 1.1 2007/06/15 18:44:37 ingomierswa Exp $
 */
public class KernelAnova extends Kernel {
	
	private static final long serialVersionUID = -8670034220969832253L;
	
	private double sigma = 1;
	private double degree = 1;

	/** Class constructor. */
	public KernelAnova() {}

	public void setParameters(double sigma, double degree) {
		this.sigma = sigma;
		this.degree = degree;
	}
	
	/** Calculates kernel value of vectors x and y. */
	public double calculate_K(int[] x_index, double[] x_att, int[] y_index, double[] y_att) {
		double result = 0;
		double tmp;
		int xpos = x_index.length - 1;
		int ypos = y_index.length - 1;
		int zeros = dim;
		while ((xpos >= 0) && (ypos >= 0)) {
			if (x_index[xpos] == y_index[ypos]) {
				tmp = x_att[xpos] - y_att[ypos];
				result += Math.exp(-sigma * tmp * tmp);
				xpos--;
				ypos--;
			} else if (x_index[xpos] > y_index[ypos]) {
				tmp = x_att[xpos];
				result += Math.exp(-sigma * tmp * tmp);
				xpos--;
			} else {
				tmp = y_att[ypos];
				result += Math.exp(-sigma * tmp * tmp);
				ypos--;
			}
		    zeros--;
		}
		while (xpos >= 0) {
			tmp = x_att[xpos];
			result += Math.exp(-sigma * tmp * tmp);
			xpos--;
		    zeros--;
		}
		while (ypos >= 0) {
			tmp = y_att[ypos];
			result += Math.exp(-sigma * tmp * tmp);
			ypos--;
		    zeros--;
		}
		result += zeros;
		return Math.pow(result, degree);
	}
	
	/** Output as String */
	public String toString() {
		return ("anova(s = " + sigma + ", d = " + degree + ")");
	}
}
