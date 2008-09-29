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
package com.rapidminer.operator.meta;

import java.util.Map;

import com.rapidminer.Process;
import com.rapidminer.operator.Operator;
import com.rapidminer.tools.LogService;

/**
 * The parameter values used by the class {@link ParameterSet}.
 * 
 * @author Ingo Mierswa
 * @version $Id: ParameterValue.java,v 1.3 2008/05/09 19:22:38 ingomierswa Exp $
 */
public class ParameterValue {

	private static final long serialVersionUID = -6847818423564185071L;

	private String operator;

	private String parameterKey;

	private String parameterValue;

	public ParameterValue(String operator, String parameterKey, String parameterValue) {
		this.operator = operator;
		this.parameterKey = parameterKey;
		this.parameterValue = parameterValue;
	}

    public String getOperator() { return operator; }
    
    public String getParameterKey() { return parameterKey; }
    
    public String getParameterValue() { return parameterValue; }
    
	public String toString() {
		return operator + "." + parameterKey + "\t= " + parameterValue;
	}

	public void apply(Process process, Map nameMap) {
		String opName = (String) nameMap.get(operator);
		if (opName == null)
			opName = operator;
		process.getLog().log("Setting parameter '" + parameterKey + "' of operator '" + opName + "' to '" + parameterValue + "'.", LogService.STATUS);
		Operator operator = process.getOperator(opName);
		if (operator == null) {
			process.getLog().log("No such operator: '" + opName + "'.", LogService.STATUS);
		} else {
			operator.getParameters().setParameter(parameterKey, parameterValue);
		}
	}
}
