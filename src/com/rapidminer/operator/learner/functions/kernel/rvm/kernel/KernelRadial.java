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
 * Radial basis function (rbf) kernel:
 * K(x, y) = exp(-lengthScale^{-2} * ||x - y||^2)
 *  
 * @author Piotr Kasprzak, Ingo Mierswa
 * @version $Id: KernelRadial.java,v 1.1 2007/05/29 00:13:03 ingomierswa Exp $
 */
public class KernelRadial extends Kernel {

	private static final long serialVersionUID = 2728320273018583212L;
	
	/** LengthScale parameter */
	protected double lengthScale = 0;
	
	/** Constructor(s) */
	public KernelRadial(double lengthScale) {
		super();
		this.lengthScale = lengthScale;
	}
	
	public KernelRadial() {
		super();
	}
	
	/** evaluate kernel */
	public double eval(double[] x, double[] y) {
		double result 	= Math.exp(-1.0d / (lengthScale * lengthScale) * norm2(x, y));
		return result;
	}
	
	public String toString() {
		return "rbf kernel [lengthScale = " + lengthScale + "]";
	}
}
