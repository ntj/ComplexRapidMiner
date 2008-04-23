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
package com.rapidminer.operator.learner.functions.kernel.gaussianprocess;

import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.Kernel;

/**
 * Models a Regression-Problem.
 * 
 * @author Piotr Kasprzak
 * @version $Id: RegressionProblem.java,v 1.1 2007/05/29 00:13:03 ingomierswa Exp $
 * 
 */
public class RegressionProblem extends Problem {

	double[][] y; // Target vectors

	public RegressionProblem(double[][] x, double[][] y, Kernel kernel) {
		super(x, kernel);
		this.y = y;
	}

	public int getTargetDimension() {
		return y[0].length;
	}

	public double[][] getTargetVectors() {
		return y;
	}
}
