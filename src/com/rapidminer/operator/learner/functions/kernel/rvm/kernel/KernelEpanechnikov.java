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
package com.rapidminer.operator.learner.functions.kernel.rvm.kernel;

/**
 * Returns the value of the Epanechnikov kernel of both examples.
 * 
 * @author Ingo Mierswa
 * @version $Id: KernelEpanechnikov.java,v 1.1 2007/05/29 00:13:03 ingomierswa Exp $
 */
public class KernelEpanechnikov extends Kernel { 

	private static final long serialVersionUID = -1706678102534775619L;

	/** The parameter sigma of the Epanechnikov kernel. */
	private double sigma = 1.0d;

	/** The parameter degree of the Epanechnikov kernel. */
	private double degree = 1;

	/** Constructor(s) */
	public KernelEpanechnikov(double sigma, double degree) {
		super();
		this.sigma = sigma;
		this.degree = degree;
	}
	
	public KernelEpanechnikov() {
		super();
	}
	
	public double eval(double[] x, double[] y) {
		double expression = norm2(x, y) / sigma;
		if (expression > 1) 
			return 0.0d;
		else {
			double minus = 1.0d - expression;
			return Math.pow(minus, degree);
		}
	}

	public String toString() {
		return "epanechnikov kernel [sigma = " + sigma + ", degree = " + degree + "]";
	}
}
