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
package com.rapidminer.operator.learner.functions.kernel.functions;

/**
 * Returns the value of a Polynomial kernel of both examples.
 * 
 * @author Ingo Mierswa
 * @version $Id: PolynomialKernel.java,v 1.2 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class PolynomialKernel extends Kernel {

	private static final long serialVersionUID = 1296800822192183260L;

	/** The parameter degree of the polynomial kernel. */
	private double degree = 3;

	/** The parameter shift of the polynomial kernel. */
	private double shift = 1.0d;

	public int getType() {
		return KERNEL_POLYNOMIAL;
	}

	public double getDegree() {
		return degree;
	}

	/** Sets the used polynomial parameters. */
	public void setPolynomialParameters(double degree, double shift) {
		this.degree = degree;
		this.shift = shift;
	}

	/** Subclasses must implement this method. */
	public double calculateDistance(double[] x1, double[] x2) {
		double prod = innerProduct(x1, x2) + shift;
		double result = prod;
		for (int i = 1; i < degree; i++) {
			result *= prod;
		}
		return result;
	}
}
