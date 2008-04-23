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
package com.rapidminer.operator;

/**
 * This class is used to describe the required input of an operator and the
 * operator's behavior with respect to consumation of this input object. Input
 * objects can be simply consumed (default) or the consumation behavior can be
 * defined with help of an parameter (user decision).
 * 
 * @author Ingo Mierswa
 * @version $Id: InputDescription.java,v 1.1 2007/05/27 21:59:01 ingomierswa Exp $
 */
public class InputDescription {

	/** The class of the input object. */
	private Class inputType;

	/** The default value for consumation. */
	private boolean keepDefault;

	/**
	 * Indicates if the operator at hand should define a parameter so that the
	 * user can decide if the input should be consumed.
	 */
	private boolean parameter;

	/** The parameter name. */
	private String parameterName;

	/**
	 * Creates a new input description for the given class. The input object is
	 * consumed.
	 */
	public InputDescription(Class inputType) {
		this(inputType, false, false, null);
	}

	/**
	 * Creates a new input description for the given class. The parameter
	 * keepDefault defines if the input object is consumed.
	 */
	public InputDescription(Class inputType, boolean keepDefault) {
		this(inputType, keepDefault, false, null);
	}

	/**
	 * Creates a new input description for the given class. The parameter
	 * keepDefault defines if the input object is consumed per default.
	 * <code>parameter<code> defines if the operator should provide a user parameter.
	 */
	public InputDescription(Class inputType, boolean keepDefault, boolean parameter) {
		this(inputType, keepDefault, parameter, parameter ? convertClass2ParameterName(inputType) : null);
	}

	/**
	 * Creates a new input description for the given class. The parameter
	 * keepDefault defines if the input object is consumed per default.
	 * <code>parameter<code> defines if the operator should provide a user parameter. This parameter utilizes the given name.
	 */
	public InputDescription(Class inputType, boolean keepDefault, boolean parameter, String parameterName) {
		this.inputType = inputType;
		this.keepDefault = keepDefault;
		this.parameter = parameter;
		this.parameterName = parameterName;
	}

	/** Returns the desired class of the input object. */
	public Class getInputType() {
		return inputType;
	}

	/** Indicates if the input should be consumed. */
	public boolean getKeepDefault() {
		return keepDefault;
	}

	/** Indicates if a user parameter should be defined. */
	public boolean showParameter() {
		return parameter;
	}

	/** Returns the name of the user parameter. */
	public String getParameterName() {
		return parameterName;
	}

	/**
	 * Converts the class name into a parameter name following RapidMiner conventions
	 * (lower case, underscores for empty spaces).
	 */
	private static String convertClass2ParameterName(Class inputType) {
		String className = inputType.getName();
		String name = className.substring(className.lastIndexOf(".") + 1);
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < name.length(); i++) {
			char current = name.charAt(i);
			if (Character.isUpperCase(current))
				result.append("_" + Character.toLowerCase(current));
			else
				result.append(current);
		}
		return "keep" + result.toString();
	}

	/** Returns a String representation of this input description. */
	public String toString() {
		return "Input description for " + inputType + " (keep: " + keepDefault + ", parameter: " + parameter + ", name: " + parameterName + ")";
	}
}
