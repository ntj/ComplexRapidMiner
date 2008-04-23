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
 * Laplace kernel:
 * K(x, y) = exp(-sqrt(lengthScale^{-2} * ||x - y||^2))
 *   
 * @author Piotr Kasprzak, Ingo Mierswa
 * @version $Id: KernelLaplace.java,v 1.1 2007/05/29 00:13:03 ingomierswa Exp $
 */
public class KernelLaplace extends KernelRadial {

	private static final long serialVersionUID = -6119888769441823765L;

	/** Constructor(s) */
    
    public KernelLaplace() {}
    
	public KernelLaplace(double lengthScale) {
		super(lengthScale);
	}
		
	/** evaluate kernel */
	public double eval(double[] x, double[] y) {
		
		double result 	= Math.exp(-Math.sqrt(Math.pow(lengthScale, -2) * norm2(x, y)));

		return result;
	}
	
	public String toString() {
		return "laplace kernel [lengthScale = " + lengthScale + "]";
	}
}
