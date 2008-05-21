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
package com.rapidminer.operator.meta.branch;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeValueFilter;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;

/**
 * This condition tests if the specified datum (attribute value of a specific example)
 * fulfills the specified condition.
 * 
 * @author Ingo Mierswa
 * @version $Id: DataValueCondition.java,v 1.2 2008/05/09 19:23:21 ingomierswa Exp $
 */
public class DataValueCondition implements ProcessBranchCondition {
	
	/**
	 * Constructor used by reflection.
	 */
	public DataValueCondition() {}
	
	/**
	 * This method checks if the file with pathname value exists.
	 */
	public boolean check(ProcessBranch operator, String value) throws OperatorException {
		if (value == null) {
			throw new UserError(operator, 205, ProcessBranch.PARAMETER_CONDITION_VALUE);
		}
		
		int exampleIndex = -1; // -1: test all examples
		
		try {
			int startIndex = value.indexOf('['); 
			if (startIndex >= 0) {
				int endIndex = value.indexOf(']');
				if (endIndex < 0) {
					throw new IllegalArgumentException("The example index must be enclosed in '[' and ']'.");
				}
				if (endIndex < startIndex) {
					throw new IllegalArgumentException("The example index must be enclosed in '[' and ']'.");
				}

				String exampleIndexString = value.substring(startIndex + 1, endIndex);
				if (exampleIndexString.trim().length() == 0) {
					throw new IllegalArgumentException("Empty example index: no number or wildcard is specified between '[' and ']'.");
				}

				if (exampleIndexString.trim().equals("*")) {
					exampleIndex = -1;
				} else {
					try {
						exampleIndex = Integer.parseInt(exampleIndexString);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("No number or wildcard is specified between '[' and ']'.");
					}
				}

				value = value.substring(0, startIndex).trim();
			}
		} catch (IllegalArgumentException e) {
			throw new UserError(operator, 116, new Object[] { ProcessBranch.PARAMETER_CONDITION_VALUE, e } );
		}
		
		ExampleSet exampleSet = operator.getInput().get(ExampleSet.class);
		AttributeValueFilter filter = new AttributeValueFilter(exampleSet, value);

		if (exampleIndex < 0) { // test all
			for (Example example : exampleSet) {
				if (!filter.conditionOk(example))
					return false;
			}
			return true;
		} else {
			Example example = exampleSet.getExample(exampleIndex - 1);
			if (example == null) {
				throw new UserError(operator, 110, exampleIndex);
			}	
			return filter.conditionOk(example);
		}
	}
}
