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
 * Gaussian Combination Kernel
 * 
 * @author Ingo Mierswa
 * @version $Id: KernelGaussianCombination.java,v 1.3 2008/05/09 19:23:21 ingomierswa Exp $
 */
public class KernelGaussianCombination extends Kernel {

	private static final long serialVersionUID = 6080834703694525403L;
	
	private double sigma1 = 1.0d;
	private double sigma2 = 0.0d;
	private double sigma3 = 2.0d;
	
	/** Output as String */
	public String toString() {
		return ("gaussian_combination(s1=" + sigma1 + ",s2=" + sigma2 + ",s3=" + sigma3 + ")");
	};

	/** Class constructor. */
	public KernelGaussianCombination() {}

	public void setParameters(double sigma1, double sigma2, double sigma3) {
		this.sigma1 = sigma1;
		this.sigma2 = sigma2;
		this.sigma3 = sigma3;
	}
	
	/** Calculates kernel value of vectors x and y. */
	public double calculate_K(int[] x_index, double[] x_att, int[] y_index, double[] y_att) {
		double norm2 = norm2(x_index, x_att, y_index, y_att);
		double exp1 = sigma1 == 0.0d ? 0.0d : Math.exp((-1) * norm2 / sigma1);
		double exp2 = sigma2 == 0.0d ? 0.0d : Math.exp((-1) * norm2 / sigma2);
		double exp3 = sigma3 == 0.0d ? 0.0d : Math.exp((-1) * norm2 / sigma3);
		return exp1 + exp2 - exp3;
	}
}
