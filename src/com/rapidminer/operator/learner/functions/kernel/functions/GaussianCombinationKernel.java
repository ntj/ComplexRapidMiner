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
 * Returns the value of the Gaussian combination kernel of both examples.
 * 
 * @author Ingo Mierswa
 * @version $Id: GaussianCombinationKernel.java,v 1.2 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class GaussianCombinationKernel extends Kernel {

	private static final long serialVersionUID = 542405909968243049L;

	/** The parameter sigma1 of the Gaussian combination kernel. */
	private double sigma1 = 1.0d;

	/** The parameter sigma2 of the Gaussian combination kernel. */
	private double sigma2 = 0.0d;
	
	/** The parameter sigma3 of the Gaussian combination kernel. */
	private double sigma3 = 2.0d;
	
	public int getType() {
		return KERNEL_GAUSSIAN_COMBINATION;
	}

	public void setSigma1(double sigma1) {
		this.sigma1 = sigma1;
	}

	public void setSigma2(double sigma2) {
		this.sigma2 = sigma2;
	}
	
	public void setSigma3(double sigma3) {
		this.sigma3 = sigma3;
	}
	
	public double getSigma1() {
		return sigma1;
	}

	public double getSigma2() {
		return sigma2;
	}
	
	public double getSigma3() {
		return sigma3;
	}
	
	/** Calculates kernel value of vectors x and y. */
	public double calculateDistance(double[] x1, double[] x2) {
		double norm2 = norm2(x1, x2);
		double exp1 = sigma1 == 0.0d ? 0.0d : Math.exp((-1) * norm2 / sigma1);
		double exp2 = sigma2 == 0.0d ? 0.0d : Math.exp((-1) * norm2 / sigma2);
		double exp3 = sigma3 == 0.0d ? 0.0d : Math.exp((-1) * norm2 / sigma3);
		return exp1 + exp2 - exp3;
	}
}
