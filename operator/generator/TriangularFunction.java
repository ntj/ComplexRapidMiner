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
package com.rapidminer.operator.generator;

/** The label is att1 - (int)att1. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: TriangularFunction.java,v 1.3 2008/05/09 19:22:50 ingomierswa Exp $
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
