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
package com.rapidminer.operator.generator;

/**
 *  The sinc function on R^n for n >= 1
 *   
 *  The Label is f(x) = sin(x) / ||x||, if ||x|| != 0, and 0 else.
 * 
 *  @author Piotr Kasprzak
 *  @version $Id: SincFunction.java,v 1.1 2007/05/27 21:58:46 ingomierswa Exp $
 */
public class SincFunction extends RegressionFunction {

	/* L2 norm on R^n */
	public double norm_l2(double[] vector) {
		double result = 0;
		for (int i = 0; i < vector.length; i++) {
			result += vector[i] * vector[i];
		}
		return Math.sqrt(result);
	}
	
	public double calculate(double[] att) {

		double norm	= norm_l2(att);
		double result;
				
		if (norm <= Double.MIN_VALUE) {
			// Treat as 0
			result = 0;
		} else {
			result = Math.sin(norm) / norm; 
		}
		
		return result;
	}
}
