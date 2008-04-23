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
package com.rapidminer.operator.learner.functions.kernel.rvm;

import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelBasisFunction;

/**
 * Holds the data defining the regression / classification problem to be learned.
 * - All input vectors are assumed to have the same dimension.
 * - All target vectors are assumed to have the same dimension
 * 
 * @author Piotr Kasprzak, Ingo Mierswa
 * @version $Id: Problem.java,v 1.1 2007/05/29 00:13:01 ingomierswa Exp $
 */
public abstract class Problem {

	private double[][] x;						// Input vectors
	
	private KernelBasisFunction[] kernels;		// Kernels to be used
	
	/** Problem types */
	
	
	/** Constructor */
	public Problem(double[][] x, KernelBasisFunction[] kernels) {
		this.x			= x;
		this.kernels	= kernels;
	}
	
	/** Getters */
	public int getProblemSize() {
		return x.length;
	}
	
	public int getInputDimension() {
		return x[0].length;
	}
	
	public double[][] getInputVectors() {
		return x;
	}
	
	public KernelBasisFunction[] getKernels() {
		return kernels;
	}
	
	abstract public int getTargetDimension();	
}
