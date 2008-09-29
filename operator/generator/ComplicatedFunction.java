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

/** The label is att1*att1*att2 - att1*att2 + max(att1,att2) - exp(att3).
 *  
 *  @author Ingo Mierswa
 *  @version $Id: ComplicatedFunction.java,v 1.3 2008/05/09 19:22:50 ingomierswa Exp $
 */
public class ComplicatedFunction extends RegressionFunction {

	public double calculate(double[] att) throws FunctionException {
		if (att.length < 3)
			throw new FunctionException("Complicated function", "needs at least 3 attributes!");
		return (att[0] * att[0] * att[1] + att[1] * att[2] + Math.max(att[0], att[1]) - Math.exp(att[2]));
	}
}
