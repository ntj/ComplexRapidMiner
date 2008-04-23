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

/** The label is att1 - (int)att1. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: TriangularFunction.java,v 1.1 2007/05/27 21:58:45 ingomierswa Exp $
 */
public class TriangularFunction extends RegressionFunction {

	public double calculate(double[] args) throws FunctionException {
		if (args.length != 1)
			throw new FunctionException("Triangular function", "needs 1 attributes!");
		// double a = 100.0d;
		// double b = 5.0d;
		// return a * (4.0d * Math.abs(Math.atan(Math.tan((args[0]/ b + 1.0d
		// / 4.0d) * Math.PI)) / Math.PI) - 1.0d);
		return args[0] - (int) args[0];
	}
}
