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
package com.rapidminer.parameter.value;

import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterType;


/**
 * Allows the specification of parameter values as a basis of e.g. optimization.
 * 
 * @author Tobias Malbrecht
 * @version $Id: ParameterValues.java,v 1.2 2008/05/09 19:23:26 ingomierswa Exp $
 */
public abstract class ParameterValues {
	protected transient Operator operator;
	
	protected transient ParameterType type;
	
	protected String key;
	
	public ParameterValues(Operator operator, ParameterType type) {
		this.operator = operator;
		this.type = type;
	}
	
	public Operator getOperator() {
		return operator;
	}
	
	public ParameterType getParameterType() {
		return type;
	}
	
	public String getKey() {
		return operator.getName() + "." + type.getKey();
	}
	
	public abstract int getNumberOfValues();
	
	public abstract String getValuesString();
	
	public String[] getValuesArray() {
		return null;
	}
}
