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
 * Returns the value of the Sigmoid kernel of both examples.
 * 
 * @author Ingo Mierswa
 * @version $Id: KernelSigmoid.java,v 1.1 2007/05/29 00:13:03 ingomierswa Exp $
 */
public class KernelSigmoid extends Kernel { 

	private static final long serialVersionUID = 5056175330389455467L;

	/** The parameter a of the sigmoid kernel. */
	private double a = 1.0d;

	/** The parameter b of the sigmoid kernel. */
	private double b = 0.0d;

	/** Constructor(s) */
	public KernelSigmoid(double a, double b) {
		super();
		this.a = a;
		this.b = b;
	}
	
	public KernelSigmoid() {
		super();
	}
	
	public double eval(double[] x, double[] y) {
		// K = tanh(a(x*y)+b)
		double prod = 0;
		for (int i = 0; i < x.length; i++) {
			prod += x[i] * y[i];
		}
		prod = a * prod + b;
		double e1 = Math.exp(prod);
		double e2 = Math.exp(-prod);
		return ((e1 - e2) / (e1 + e2));
	}
	
	public String toString() {
		return "sigmoid kernel [a = " + a + ", b = " + b + "]";
	}
}
