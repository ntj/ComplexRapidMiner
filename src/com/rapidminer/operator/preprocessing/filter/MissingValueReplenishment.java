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
package com.rapidminer.operator.preprocessing.filter;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.operator.OperatorDescription;


/**
 * Replaces missing values in examples. If a value is missing, it is replaced by
 * one of the functions &quot;minimum&quot;, &quot;maximum&quot;,
 * &quot;average&quot;, and &quot;none&quot;, which is applied to the non
 * missing attribute values of the example set. &quot;none&quot; means, that the
 * value is not replaced. The function can be selected using the parameter list
 * <code>columns</code>. If an attribute's name appears in this list as a
 * key, the value is used as the function name. If the attribute's name is not
 * in the list, the function specified by the <code>default</code> parameter
 * is used. For nominal attributes the mode is used for the average, i.e. the
 * nominal value which occurs most often in the data. For nominal attributes and
 * replacement type zero the first nominal value defined for this
 * attribute is used. The replenishment &quot;value&quot; indicates that the 
 * user defined parameter should be used for the replacement.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: MissingValueReplenishment.java,v 1.7 2006/03/21 15:35:52
 *          ingomierswa Exp $
 */
public class MissingValueReplenishment extends ValueReplenishment {

	private static final int NONE    = 0;

	private static final int MINIMUM = 1;

	private static final int MAXIMUM = 2;

	private static final int AVERAGE = 3;
	
	private static final int ZERO    = 4;
    
    private static final int VALUE   = 5;

	private static final String[] REPLENISHMENT_NAMES = { "none", "minimum", "maximum", "average", "zero", "value" };

	public MissingValueReplenishment(OperatorDescription description) {
		super(description);
	}

	public String[] getFunctionNames() {
		return REPLENISHMENT_NAMES;
	}

	public int getDefaultFunction() {
		return AVERAGE;
	}

	public int getDefaultColumnFunction() {
		return AVERAGE;
	}

	public boolean replenishValue(double currentValue) {
		return Double.isNaN(currentValue);
	}

	public double getReplenishmentValue(int functionIndex, ExampleSet exampleSet, Attribute attribute, double currentValue, String valueString) {
		switch (functionIndex) {
			case NONE:
				return currentValue;
			case MINIMUM:
				return exampleSet.getStatistics(attribute, Statistics.MINIMUM);
			case MAXIMUM:
				return exampleSet.getStatistics(attribute, Statistics.MAXIMUM);
			case AVERAGE:
				if (attribute.isNominal()) {
					return exampleSet.getStatistics(attribute, Statistics.MODE);
				} else {
					return exampleSet.getStatistics(attribute, Statistics.AVERAGE);
				}
			case ZERO:
				return 0.0d;
            case VALUE:
                if (attribute.isNominal()) {
                    return attribute.getMapping().mapString(valueString);
                } else {
                    return Double.parseDouble(valueString);
                }                
			default:
				throw new RuntimeException("Illegal value functionIndex: " + functionIndex);
		}
	}
}
