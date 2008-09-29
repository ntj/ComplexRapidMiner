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
package com.rapidminer.operator.preprocessing.filter;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;

/**
 * This operator simply replaces all values by its absolute values.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbsoluteValueFilter.java,v 1.1 2008/08/28 18:54:02 ingomierswa Exp $
 */
public class AbsoluteValueFilter extends Operator {

	public AbsoluteValueFilter(OperatorDescription description) {
		super(description);
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		for (Example example : exampleSet) {
			for (Attribute attribute : exampleSet.getAttributes()) {
				if (attribute.isNumerical()) {
					double value = example.getValue(attribute);
					value = Math.abs(value);
					example.setValue(attribute, value);
				}
			}
		}
		
		return new IOObject[] { exampleSet };
	}

	@Override
	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	@Override
	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

}
