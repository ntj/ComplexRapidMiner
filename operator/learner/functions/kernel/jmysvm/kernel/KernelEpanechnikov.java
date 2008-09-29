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
package com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel;

/**
 * Epanechnikov Kernel
 * 
 * @author Ingo Mierswa
 * @version $Id: KernelEpanechnikov.java,v 1.3 2008/05/09 19:23:21 ingomierswa Exp $
 */
public class KernelEpanechnikov extends Kernel {

	private static final long serialVersionUID = -2375190740988942684L;
	
	private double sigma = 1;
	private double degree = 1;

	/** Output as String */
	public String toString() {
		return ("epanechnikov(s=" + sigma + ",d=" + degree + ")");
	};

	/** Class constructor. */
	public KernelEpanechnikov() {}

	public void setParameters(double sigma, double degree) {
		this.sigma = sigma;
		this.degree = degree;
	}
	
	/** Calculates kernel value of vectors x and y. */
	public double calculate_K(int[] x_index, double[] x_att, int[] y_index, double[] y_att) {
		double expression = norm2(x_index, x_att, y_index, y_att) / sigma;
		if (expression > 1) 
			return 0.0d;
		else {
			double minus = 1.0d - expression;
			return Math.pow(minus, degree);
		}
	}
}
