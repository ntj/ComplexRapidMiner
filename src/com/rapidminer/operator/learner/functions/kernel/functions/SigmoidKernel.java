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
 * Returns the value of a Sigmoid kernel of both examples.
 * 
 * @author Ingo Mierswa
 * @version $Id: SigmoidKernel.java,v 1.2 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class SigmoidKernel extends Kernel {

	private static final long serialVersionUID = -4503893127271607088L;

	/** The parameter a of the sigmoid kernel. */
	private double a = 1.0d;

	/** The parameter b of the sigmoid kernel. */
	private double b = 0.0d;

	public int getType() {
		return KERNEL_SIGMOID;
	}

	/** Sets the parameters of this Sigmoid kernel to the given values a and b. */
	public void setSigmoidParameters(double a, double b) {
		this.a = a;
		this.b = b;
	}

	/** Subclasses must implement this method. */
	public double calculateDistance(double[] x1, double[] x2) {
		// K = tanh(a(x*y)+b)
		double prod = a * innerProduct(x1, x2) + b;
		double e1 = Math.exp(prod);
		double e2 = Math.exp(-prod);
		return ((e1 - e2) / (e1 + e2));
	}
}
