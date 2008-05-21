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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;


/**
 * This class is a collection of the parameter values of a single operator.
 * Instances of <code>Parameters</code> are created with respect to the
 * declared list of <code>ParameterTypes</code> of an operator. If parameters
 * are set using the <code>setParameter()</code> method and the value exceeds
 * the range, it is automatically corrected. If parameters are queried that are
 * not set, their default value is returned.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: Parameters.java,v 1.7 2008/05/09 19:22:37 ingomierswa Exp $
 */
public class Parameters implements Cloneable {

	/** Maps parameter keys (i.e. Strings) to their value (Objects). */
	private SortedMap<String, Object> keyToValueMap = new TreeMap<String, Object>();

	/** Maps parameter keys (i.e. Strings) to their <code>ParameterType</code>. */
	private SortedMap<String, ParameterType> keyToTypeMap = new TreeMap<String, ParameterType>();

	/** Creates an empty parameters object without any parameter types. */
	public Parameters() {}

	/**
	 * Constructs an instance of <code>Parameters</code> for the given list of
	 * <code>ParameterTypes</code>. The list might be empty but not null.
	 */
	public Parameters(List<ParameterType> parameterTypes) {
		Iterator<ParameterType> i = parameterTypes.iterator();
		while (i.hasNext()) {
			ParameterType type = i.next();
			keyToTypeMap.put(type.getKey(), type);
		}
	}

	/**
	 * Copies the values and types of the given Parameters object into this one.
	 * Performs a check if both objects contain the same ParameterTypes.
	 * Especially all parameter types of this object must also be defined in the
	 * given Parameters instance. If a parameter type is missing a
	 * NullPointerException will be thrown.
	 */
	public void copy(Parameters parameters) {
		SortedMap<String, Object> newKey2ValueMap = new TreeMap<String, Object>();
		SortedMap<String, ParameterType> newKey2TypeMap = new TreeMap<String, ParameterType>();
		Iterator<String> i = keyToTypeMap.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			ParameterType newType = parameters.keyToTypeMap.get(key);
			if (newType == null)
				throw new NullPointerException("Error during copying a parameters object: the new parameters does not contain '" + key + "'!");
			newKey2TypeMap.put(key, newType);
		}
		i = keyToValueMap.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			try {
				Object newValue = parameters.getParameter(key);
				newKey2ValueMap.put(key, newValue);
			} catch (UndefinedParameterError e) {
                LogService.getGlobal().log("Parameter '" + key + "' is not set and has no default value: using empty parameter value for copied parameters.", LogService.ERROR);
			}
		}
		this.keyToValueMap = newKey2ValueMap;
		this.keyToTypeMap = newKey2TypeMap;
	}

	/** Performs a deep clone on this parameters object. */
	public Object clone() {
		Parameters clone = new Parameters();
		Iterator<String> i = keyToValueMap.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			Object value = keyToValueMap.get(key);
			ParameterType type = keyToTypeMap.get(key);
			if (type != null) {
				clone.keyToValueMap.put(key, type.copyValue(value));				
			}
		}
		i = keyToTypeMap.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			clone.keyToTypeMap.put(key, keyToTypeMap.get(key));
		}
		return clone;
	}

	/** Returns the type of the parameter with the given type. */
	public ParameterType getParameterType(String key) {
		return keyToTypeMap.get(key);
	}

	/** Sets the parameter for the given key after performing a range-check. */
	public void setParameter(String key, Object value) {
		ParameterType type = keyToTypeMap.get(key);
		if (type == null) {
            LogService.getGlobal().log("Illegal key: '" + key + "'", LogService.WARNING);
			if (value instanceof List) {
				type = new ParameterTypeList(key, "guessed", new ParameterTypeString(null, null));
			} else {
				type = new ParameterTypeString(key, "guessed");
			}
			keyToTypeMap.put(key, type);
		}
		Object transformedValue = type.transformNewValue(value);
		keyToValueMap.put(key, type.checkValue(transformedValue));
	}

	/** Sets the parameter without performing a range and type check. */
	public void setParameterWithoutCheck(String key, Object value) {
		if (value == null) {
			keyToValueMap.remove(key);
		} else {
			keyToValueMap.put(key, value);
		}
	}

	/**
	 * Returns the value of the given parameter. If it was not yet set, the
	 * default value is set now and a log message is issued. If the
	 * <code>ParameterType</code> does not provide a default value, this may
	 * result in an error message. In subsequent calls of this method, the
	 * parameter will be set. An OperatorException (UserError) will be thrown if
	 * a non-optional parameter was not set.
	 */
	public Object getParameter(String key) throws UndefinedParameterError {
		if (keyToValueMap.containsKey(key)) {
			return keyToValueMap.get(key);
		} else {
			ParameterType type = keyToTypeMap.get(key);
			if (type == null) {
				return null;
			}
			Object value = type.getDefaultValue();
			if ((value == null) && !type.isOptional()) {
                LogService.getGlobal().log("Parameter '" + key + "' is not set and has no default value.", LogService.ERROR);
				throw new UndefinedParameterError(key);
			} else {
				keyToValueMap.put(key, value);
                LogService.getGlobal().log("Parameter '" + key + "' is not set. Using default ('" + type.toString(value) + "').", LogService.MINIMUM);
			}
			return value;
		}
	}
	
	/** Returns a set view of all parameter keys. */
	public Set<String> getKeys() {
		return keyToValueMap.keySet();
	}

	/**
	 * Returns true if the given parameters are not null and are the same like
	 * this parameters.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof Parameters)) {
			return false;
		} else {
			String thisXML = this.getXML("");
			String otherXML = ((Parameters) o).getXML("");
			if (!thisXML.equals(otherXML)) {
				return false;
			}
			return true;
		}
	}
	
	public int hashCode() {
		return this.getXML("").hashCode();
	}

	/**
	 * Writes a portion of the xml configuration file specifying the parameters
	 * that differ from their default value.
	 */
	public String getXML(String indent) {
		StringBuffer result = new StringBuffer();
		Iterator<String> i = keyToValueMap.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			Object value = keyToValueMap.get(key);
			ParameterType type = keyToTypeMap.get(key);
			if (type != null) {
				result.append(type.getXML(indent, key, value, true));
			} else {
				result.append(indent + "<parameter key=\"" + key + "\"\tvalue=\"" + value.toString() + "\"/>" + Tools.getLineSeparator());
			}
		}
		return result.toString();
	}
}
