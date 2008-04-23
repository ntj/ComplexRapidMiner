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
package com.rapidminer.generator;

import com.rapidminer.tools.LogService;

/**
 * This class has two numerical input attributes and one output attribute.
 * Calculates the power of the input attribute to the second.
 * 
 * @author Ingo Mierswa
 * @version $Id: PowerGenerator.java,v 1.1 2007/05/27 21:58:35 ingomierswa Exp $
 */
public class PowerGenerator extends BinaryNumericalGenerator {

	public PowerGenerator() {}

	public FeatureGenerator newInstance() {
		return new PowerGenerator();
	}

	public boolean isCommutative() {
		return false;
	}

	public boolean isSelfApplicable() {
		return true;
	}

	public double calculateValue(double value1, double value2) {
		return Math.pow(value1, value2);
	}

	public void setFunction(String name) {
		if (!name.equals("pow"))
			LogService.getGlobal().log("Illegal function name '" + name + "' for " + getClass().getName() + ".", LogService.ERROR);
	}

	public String getFunction() {
		return "pow";
	}
}
