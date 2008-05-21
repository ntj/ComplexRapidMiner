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

import java.io.Serializable;

import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;


/**
 * A ParameterType holds information about type, range, and default value of a
 * parameter. Lists of ParameterTypes are provided by operators.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ParameterType.java,v 1.6 2008/05/09 19:22:37 ingomierswa Exp $
 * @see com.rapidminer.operator.Operator#getParameterTypes()
 */
public abstract class ParameterType implements Comparable, Serializable {

	/** The key of this parameter. */
	private String key;

	/** The documentation. Used as tooltip text... */
	private String description;

	/**
	 * Indicates if this is a parameter only viewable in expert mode. Mandatory
	 * parameters are always viewable. The default value is true.
	 */
	private boolean expert = true;
	
	/**
	 * Indicates if this parameter is hidden and is not shown in the GUI.
	 * May be used in conjunction with a configuration wizard which lets the
	 * user configure the parameter.
	 */
	private boolean hidden = false;

	/** Creates a new ParameterType. */
	public ParameterType(String key, String description) {
		this.key = key;
		this.description = description;
	}

	
	/** Returns a human readable description of the range. */
	public abstract String getRange();

	/**
	 * For single parameters the value is a String and must be converted to an
	 * Integer, Double, Boolean or String. For lists, value already is a list.
	 * If value is out of range, a corrected value must be returned.
	 */
	public abstract Object checkValue(Object value);

	/** Returns a value that can be used if the parameter is not set. */
	public abstract Object getDefaultValue();
	
	/** Sets the default value. */
	public abstract void setDefaultValue(Object defaultValue);
	
	/** Copies the value. This is necessary for cloning complex parameter types. */
	public abstract Object copyValue(Object value);
	
	/** Returns true if the values of this parameter type are numerical, i.e. might be parsed 
	 *  by {@link Double#parseDouble(String)}. Otherwise false should be returned. This method
	 *  might be used by parameter logging operators. */
	public abstract boolean isNumerical();
	
	/** Writes an xml representation of the given key-value pair. */
	public abstract String getXML(String indent, String key, Object value, boolean hideDefault);

	
	/** This method will be invoked by the Parameters after a parameter was set.
	 *  The default implementation is empty but subclasses might override this
	 *  method, e.g. for a decryption of passwords. */
	public Object transformNewValue(Object value) {
		return value;
	}
	
	/**
	 * Returns true if this parameter can only be seen in expert mode. The
	 * default implementation returns true if the parameter is optional.
	 */
	public boolean isExpert() {
		return isOptional() && expert;
	}

	/**
	 * Sets if this parameter can be seen in expert mode (true) or beginner mode
	 * (false).
	 */
	public void setExpert(boolean expert) {
		this.expert = expert;
	}

	/**
	 * Returns true if this parameter is hidden and will not be shown in the
	 * GUI. The default implementation returns true which should be the normal case. 
	 */
	public boolean isHidden() {
		return hidden;
	}
	
	/**
	 * Sets if this parameter is hidden (value true) and will not be shown in the GUI.
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	/**
	 * Returns true if this parameter is optional. The default implementation
	 * returns true.
	 */
	public boolean isOptional() {
		return true;
	}

	/** Returns the key. */
	public String getKey() {
		return key;
	}

	/** Returns a short description. */
	public String getDescription() {
		return description;
	}

	/** Sets the short description. */
	public void setDescription(String description) {
		this.description = description;
	}

	/** Returns a string representation of this value. */
	public String toString(Object value) {
		if (value == null)
			return "";
		else
			return Tools.escapeXML(value.toString());
	}

	public String toString() {
		return key + " (" + description + ")";
	}

	/**
	 * Can be called in order to report an illegal parameter value which is
	 * encountered during <tt>checkValue()</tt>.
	 */
	public void illegalValue(Object illegal, Object corrected) {
		LogService.getGlobal().log("Illegal value '" + illegal + "' for parameter '" + key + "' has been corrected to '" + corrected.toString() + "'.", LogService.WARNING);
	}

	/** ParameterTypes are compared by key. */
	public int compareTo(Object o) {
		if (!(o instanceof ParameterType))
			return 0;
		else
			return this.key.compareTo(((ParameterType) o).key);
	}
}
