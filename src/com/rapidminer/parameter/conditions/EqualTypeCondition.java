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
package com.rapidminer.parameter.conditions;

import com.rapidminer.parameter.ParameterHandler;
import com.rapidminer.parameter.UndefinedParameterError;

/**
 * This condition checks if a type parameter (category) has a certain value.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: EqualTypeCondition.java,v 1.4 2008/07/13 16:39:42 ingomierswa Exp $
 */
public class EqualTypeCondition implements ParameterCondition {

	private ParameterHandler handler;
	
	private String conditionParameter;
	
	private int[] types;
	
	private boolean becomeMandatory;
	
	public EqualTypeCondition(ParameterHandler handler, String conditionParameter, boolean becomeMandatory, int... types) {
		this.handler = handler;
		this.conditionParameter = conditionParameter;
		this.types = types;
		this.becomeMandatory = becomeMandatory;
	}

	public boolean dependencyMet() {
		boolean equals = false;
		int isType;
		try {
			isType = handler.getParameterAsInt(conditionParameter);
		} catch (UndefinedParameterError e) {
			return false;
		} 
		for (int type : types) {
			equals |= isType == type;
		}
		return equals;
	}

	public boolean becomeMandatory() {
		return this.becomeMandatory;
	}
}
