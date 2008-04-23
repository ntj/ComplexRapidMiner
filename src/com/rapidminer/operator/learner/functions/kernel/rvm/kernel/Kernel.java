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

import java.io.Serializable;

/**
 * Abstract base class for all RVM / GP kernels. Please note that all kernel functions must have
 * a zero argument constructor.
 *  
 * @author Piotr Kasprzak, Ingo Mierswa
 * @version $Id: Kernel.java,v 1.1 2007/05/29 00:13:03 ingomierswa Exp $
 *
 */
public abstract class Kernel implements Serializable {
		
	/** Constructor(s) */	
	public Kernel() {
	}
	
	/** Evaluate kernel */
	public abstract double eval(double[] x, double[] y);
	
	/** Calculates l2-norm(x, y)^2 = ||x - y||^2 */
	public double norm2(double[] x, double[] y) {
		double result = 0, diff;
		for (int i = 0; i < x.length; i++) {
			diff	= x[i] - y[i];
			result += diff * diff; 
		}
		return result;
	}	
}
