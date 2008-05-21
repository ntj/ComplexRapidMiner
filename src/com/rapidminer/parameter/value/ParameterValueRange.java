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
package com.rapidminer.parameter.value;

import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterType;


/**
 * Represents a range of numerical parameter values.
 * 
 * @author Tobias Malbrecht
 * @version $Id: ParameterValueRange.java,v 1.2 2008/05/09 19:23:26 ingomierswa Exp $
 */
public class ParameterValueRange extends ParameterValues {
	private double min;
	
	private double max;
	
	public ParameterValueRange(Operator operator, ParameterType type, double min, double max) {
		super(operator, type);
		this.min = min;
		this.max = max;
	}
	
	public void setMin(double min) {
		this.min = min;
	}
	
	public double getMin() {
		return min;
	}
	
	public void setMax(double max) {
		this.max = max;
	}
	
	public double getMax() {
		return max;
	}

	public int getNumberOfValues() {
		return -1;
	}
	
	public String getValuesString() {
		return "[" + Double.toString(min) + 
			   ";" + Double.toString(max) + "]";
	}
	
	public String toString() {
		return "range: " + min + " - " + max;
	}
}
