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
 * Helper class for GUI purposes. This parameter type should hold information
 * about other parameter values, e.g. for the definition of the parameters for a
 * parameter optimization.
 * 
 * @author Ingo Mierswa
 * @version $Id: ParameterTypeParameterValue.java,v 2.7 2006/03/21 15:35:49
 *          ingomierswa Exp $
 */
public class ParameterTypeParameterValue extends ParameterTypeSingle {

	private static final long serialVersionUID = 5248919176004016189L;

	public ParameterTypeParameterValue(String key, String description) {
		super(key, description);
	}

    public boolean isOptional() {
        return false;
    }

    /** Returns null. */
    public Object getDefaultValue() {
        return null;
    }
    
    /** Does nothing. */
    public void setDefaultValue(Object defaultValue) {}

    public String getRange() {
        return "parameter values";
    }

    public Object checkValue(Object object) {
        return ((String) object).trim();
    }
    
    /** Returns false. */
    public boolean isNumerical() { return false; }
}
