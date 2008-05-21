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
package com.rapidminer.parameter;

/**
 * A parameter type for integer values. Operators ask for the integer value with
 * {@link com.rapidminer.operator.Operator#getParameterAsInt(String)}. For
 * infinite ranges Integer.MIN_VALUE and Integer.MAX_VALUE should be used.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ParameterTypeInt.java,v 2.11 2006/03/21 15:35:49 ingomierswa
 *          Exp $
 */
public class ParameterTypeInt extends ParameterTypeNumber {

	private static final long serialVersionUID = -7360090072467405524L;

	private int defaultValue = -1;

	private int min = Integer.MIN_VALUE;

	private int max = Integer.MAX_VALUE;

	private boolean noDefault = true;

	private boolean optional = true;

	public ParameterTypeInt(String key, String description, int min, int max) {
		this(key, description, min, max, -1);
		this.noDefault = true;
		optional = false;
	}

	public ParameterTypeInt(String key, String description, int min, int max, boolean optional) {
		this(key, description, min, max, -1);
		this.noDefault = true;
		this.optional = optional;
	}

	public ParameterTypeInt(String key, String description, int min, int max, int defaultValue) {
		super(key, description);
		this.defaultValue = defaultValue;
		this.min = min;
		this.max = max;
		this.noDefault = false;
		this.optional = true;
	}

	public double getMinValue() {
		return min;
	}

	public double getMaxValue() {
		return max;
	}

	public int getMinValueInt() {
		return min;
	}

	public int getMaxValueInt() {
		return max;
	}

	public int getDefaultInt() {
		return defaultValue;
	}

	public boolean isOptional() {
		return optional;
	}

	public Object getDefaultValue() {
		if (noDefault)
			return null;
		else
			return Integer.valueOf(defaultValue);
	}
	
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = (Integer)defaultValue;
	}

	/** Returns true. */
	public boolean isNumerical() { return true; }
	
	public Object checkValue(Object value) {
		String string = (String) value;
		Integer intValue = null;
		try {
			intValue = Integer.valueOf(string);
			if (intValue.intValue() < min) {
				intValue = Integer.valueOf(min);
				illegalValue(value, intValue);
			} else if (intValue.intValue() > max) {
				intValue = Integer.valueOf(max);
				illegalValue(value, intValue);
			}
		} catch (NumberFormatException e) {
			intValue = Integer.valueOf(defaultValue);
			illegalValue(value, intValue);
		}
		return intValue;
	}

	public String getRange() {
		String range = "integer; ";
		if (min == Integer.MIN_VALUE)
			range += "-\u221E";
		else
			range += min;
		range += "-";
		if (max == Integer.MAX_VALUE)
			range += "+\u221E";
		else
			range += max;
		if (!noDefault) {
			range += "; default: " + getStringRepresentation(defaultValue);
        }
		return range;
	}

	public String getStringRepresentation(int value) {
        String valueString = value + "";
        if (value == Integer.MAX_VALUE) {
            valueString = "+\u221E";
        } else if (value == Integer.MIN_VALUE) {
            valueString = "-\u221E";
        }
        return valueString;
    }
}
