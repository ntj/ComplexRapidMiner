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
 * A parameter type for categories. These are several Strings and one of these
 * is the default value. Operators ask for the index of the selected value with
 * {@link com.rapidminer.operator.Operator#getParameterAsInt(String)}.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ParameterTypeCategory.java,v 2.11 2006/03/21 15:35:49
 *          ingomierswa Exp $
 */
public class ParameterTypeCategory extends ParameterTypeSingle {

	private static final long serialVersionUID = 5747692587025691591L;

	private int defaultValue = 0;

	private String[] categories = new String[0];

	public ParameterTypeCategory(String key, String description, String[] categories, int defaultValue) {
		super(key, description);
		this.categories = categories;
		this.defaultValue = defaultValue;
	}

	public int getDefault() {
		return defaultValue;
	}

	public Object getDefaultValue() {
		if (defaultValue == -1) {
			return null;
		} else {
			return Integer.valueOf(defaultValue);
		}
	}
	
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = (Integer)defaultValue;
	}

	/** Returns false. */
	public boolean isNumerical() { return false; }
	
	public Object checkValue(Object value) {
		String string = (String) value;
		for (int i = 0; i < categories.length; i++) {
			if (categories[i].equals(string)) {
				return Integer.valueOf(i);
			}
		}
        // if string was not found: try to parse index
        try {
            return Integer.valueOf(Integer.parseInt(string));
        } catch (NumberFormatException e) {}
		illegalValue(value, categories[defaultValue]);
		return Integer.valueOf(defaultValue);
	}

	public String toString(Object value) {
		return categories[((Integer) value).intValue()];
	}

	public String[] getValues() {
		return categories;
	}

	public String getRange() {
		StringBuffer values = new StringBuffer();
		for (int i = 0; i < categories.length; i++) {
			if (i > 0)
				values.append(", ");
			values.append(categories[i]);
		}
		return values.toString() + "; default: " + categories[defaultValue];
	}

	public String getCategory(int index) {
		return categories[index];
	}

	public int getNumberOfCategories() {
		return categories.length;
	}
}
