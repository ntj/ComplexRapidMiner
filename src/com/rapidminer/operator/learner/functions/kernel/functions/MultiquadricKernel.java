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
 * Returns the value of the Multiquadric kernel of both examples.
 * 
 * @author Ingo Mierswa
 * @version $Id: MultiquadricKernel.java,v 1.2 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class MultiquadricKernel extends Kernel {

	private static final long serialVersionUID = -7896178642575555770L;

	/** The parameter sigma of the Multiquadric kernel. */
	private double sigma = 1.0d;
	
	/** The parameter shift of the multiquadric kernel. */
	private double shift = 1.0d;
	
	public int getType() {
		return KERNEL_MULTIQUADRIC;
	}

	public void setSigma(double sigma) {
		this.sigma = sigma;
	}
	
	public double getSigma() {
		return sigma;
	}
	
	public void setShift(double shift) {
		this.shift = shift;
	}
	
	public double getShift() {
		return shift;
	}
	
	/** Calculates kernel value of vectors x and y. */
	public double calculateDistance(double[] x1, double[] x2) {
		return Math.sqrt((norm2(x1, x2) / sigma) + (shift * shift));
	}
}
