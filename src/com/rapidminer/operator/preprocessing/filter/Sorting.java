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

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeString;

/**
 * <p>
 * This operator sorts the given example set according to a single attribute. The example
 * set is sorted according to the natural order of the values of this attribute either
 * in increasing or in decreasing direction.
 * </p>
 * 
 * @author Ingo Mierswa
 * @version $Id: Sorting.java,v 1.5 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class Sorting extends Operator {
	
	/** The parameter name for &quot;Indicates the attribute which should be used for determining the sorting.&quot; */
	public static final String PARAMETER_ATTRIBUTE_NAME = "attribute_name";

	/** The parameter name for &quot;Indicates the direction of the sorting.&quot; */
	public static final String PARAMETER_SORTING_DIRECTION = "sorting_direction";
	
	public Sorting(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		int sortingDirection = getParameterAsInt(PARAMETER_SORTING_DIRECTION);
		Attribute sortingAttribute = exampleSet.getAttributes().get(getParameterAsString(PARAMETER_ATTRIBUTE_NAME));
		// some checks
		if (sortingAttribute == null) {
			throw new UserError(this, 111, getParameterAsString(PARAMETER_ATTRIBUTE_NAME));
		}
			
		ExampleSet result = new SortedExampleSet(exampleSet, sortingAttribute, sortingDirection);
		
		return new IOObject[] { result };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_ATTRIBUTE_NAME, "Indicates the attribute which should be used for determining the sorting.", false));
		types.add(new ParameterTypeCategory(PARAMETER_SORTING_DIRECTION, "Indicates the direction of the sorting.", SortedExampleSet.SORTING_DIRECTIONS, SortedExampleSet.INCREASING));
		return types;
	}
}
