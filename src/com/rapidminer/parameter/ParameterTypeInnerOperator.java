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
 * about other inner operator names, e.g. for the definition of the inner operator
 * of the OperatorEnabler operator.
 * 
 * @author Ingo Mierswa
 * @version $Id: ParameterTypeInnerOperator.java,v 1.3 2008/05/09 19:22:37 ingomierswa Exp $
 */
public class ParameterTypeInnerOperator extends ParameterTypeSingle {

	private static final long serialVersionUID = -8428679832770835634L;

	public ParameterTypeInnerOperator(String key, String description) {
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
        return "inner operator names";
    }

    public Object checkValue(Object object) {
        return ((String) object).trim();
    }
    
    /** Returns false. */
    public boolean isNumerical() { return false; }
}
