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
package com.rapidminer.generator;

import com.rapidminer.tools.LogService;

/**
 * This class has one numerical input attribute and one output attribute.
 * Calculates the value of the square root of the input attribute.
 * 
 * @author Ingo Mierswa
 * @version $Id: SquareRootGenerator.java,v 2.6 2006/03/21 15:35:40 ingomierswa
 *          Exp $
 */
public class SquareRootGenerator extends SingularNumericalGenerator {

	public SquareRootGenerator() {}

	public FeatureGenerator newInstance() {
		return new SquareRootGenerator();
	}

	public double calculateValue(double value) {
		return Math.sqrt(value);
	}

	public void setFunction(String name) {
		if (!name.equals("sqrt"))
			LogService.getGlobal().log("Illegal function name '" + name + "' for " + getClass().getName() + ".", LogService.ERROR);
	}

	public String getFunction() {
		return "sqrt";
	}
}
