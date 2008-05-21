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
 * is the default value. Additionally users can define other strings than these
 * given in as pre-defined categories. Operators ask for the defined String with
 * the method
 * {@link com.rapidminer.operator.Operator#getParameterAsString(String)}.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ParameterTypeStringCategory.java,v 2.10 2006/03/21 15:35:49
 *          ingomierswa Exp $
 */
public class ParameterTypeStringCategory extends ParameterTypeSingle {

	private static final long serialVersionUID = 1620216625117563601L;

	private String defaultValue = null;

	private String[] categories = new String[0];

	public ParameterTypeStringCategory(String key, String description, String[] categories) {
		this(key, description, categories, null);
	}

	public ParameterTypeStringCategory(String key, String description, String[] categories, String defaultValue) {
		super(key, description);
		this.categories = categories;
		this.defaultValue = defaultValue;
	}

	public boolean isOptional() {
		if (defaultValue == null) {
			return false;
		} else {
			return true;
		}
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = (String)defaultValue;
	}
	
	public Object checkValue(Object value) {
		return value;
	}

	public String toString(Object value) {
		return (String) value;
	}

	public String[] getValues() {
		return categories;
	}

	/** Returns false. */
	public boolean isNumerical() { return false; }
	
	public String getRange() {
		StringBuffer values = new StringBuffer();
		for (int i = 0; i < categories.length; i++) {
			if (i > 0)
				values.append(", ");
			values.append(categories[i]);
		}
		values.append((defaultValue != null) ? ("; default: '" + defaultValue + "'") : "");
		return values.toString();
	}

}
