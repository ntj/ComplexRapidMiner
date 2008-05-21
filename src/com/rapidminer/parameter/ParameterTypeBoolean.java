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

import com.rapidminer.tools.Tools;

/**
 * A parameter type for boolean parameters. Operators ask for the boolean value
 * with {@link com.rapidminer.operator.Operator#getParameterAsBoolean(String)}.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ParameterTypeBoolean.java,v 2.9 2006/03/21 15:35:49 ingomierswa
 *          Exp $
 */
public class ParameterTypeBoolean extends ParameterTypeSingle {

	private static final long serialVersionUID = 6524969076774489545L;
	
	private boolean defaultValue = false;

	public ParameterTypeBoolean(String key, String description, boolean defaultValue) {
		super(key, description);
		this.defaultValue = defaultValue;
	}

	public boolean getDefault() {
		return defaultValue;
	}

	public Object getDefaultValue() {
		return Boolean.valueOf(defaultValue);
	}
	
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = (Boolean)defaultValue;
	}

	/** Returns false. */
	public boolean isNumerical() { return false; }
	
	public Object checkValue(Object value) {
		String string = ((String) value).toLowerCase().trim();
		for (int i = 0; i < Tools.TRUE_STRINGS.length; i++) {
			if (Tools.TRUE_STRINGS[i].equals(string)) {
				return Boolean.TRUE;
			}
		}
		for (int i = 0; i < Tools.FALSE_STRINGS.length; i++) {
			if (Tools.FALSE_STRINGS[i].equals(string)) {
				return Boolean.FALSE;
			}
		}
		Boolean booleanValue = Boolean.valueOf(defaultValue);
		illegalValue(value, booleanValue);
		return booleanValue;
	}

	public String getRange() {
		return "boolean; default: " + defaultValue;
	}
}
