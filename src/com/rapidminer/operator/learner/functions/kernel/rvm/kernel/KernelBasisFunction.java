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
 *  A basis function for kernels.
 *
 *  @author Piotr Kasprzak, Ingo Mierswa
 *  @version $Id: KernelBasisFunction.java,v 1.1 2007/05/29 00:13:03 ingomierswa Exp $
 */
public class KernelBasisFunction implements Serializable {

	private static final long serialVersionUID = 6468092358201066159L;

	/** Dimension of the basis vector (= dimension of input vectors) */ 
	protected int dim 		= 0;
	
	/** Vector */
	protected double[] y	= null;
	
	/** The kernel to be used as a basis */
	protected Kernel kernel	= null;
	
	/** Constructor(s) */
    
    private KernelBasisFunction() {}
    
	public KernelBasisFunction(Kernel kernel, double[] y_vector) {
		y	= y_vector;
		dim = y.length;
		
		this.kernel = kernel;
	}

	public KernelBasisFunction(Kernel kernel) {
		this.kernel = kernel;
	}

	
	/** Evaluate KernelBasisFunction */
	public double eval(double[] x) {
		return kernel.eval(x, y);
	}

	/** Get basis vector */
	public double[] getBasisVector() {
		return this.y;
	}
}
