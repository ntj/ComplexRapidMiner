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

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;


/**
 * Abstract superclass for all operators that replenish values, e.g. nan or
 * infinite values.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ValueReplenishment.java,v 1.11 2006/04/05 08:57:27 ingomierswa
 *          Exp $
 */
public abstract class ValueReplenishment extends Operator {


	/** The parameter name for &quot;Function to apply to all columns that are not explicitly specified by parameter 'columns'.&quot; */
	public static final String PARAMETER_DEFAULT = "default";

	/** The parameter name for &quot;List of replacement functions for each column.&quot; */
	public static final String PARAMETER_COLUMNS = "columns";

	/** The parameter name for &quot;This value is used for some of the replenishment types.&quot; */
	public static final String PARAMETER_REPLENISHMENT_VALUE = "replenishment_value";
	public ValueReplenishment(OperatorDescription description) {
		super(description);
	}

	/** Returns true iff the value should be replenished. */
	public abstract boolean replenishValue(double currentValue);

	/** Returns the value of the replenishment function with the given index. */
	public abstract double getReplenishmentValue(int functionIndex, ExampleSet baseExampleSet, Attribute attribute, double currentValue, String valueString);

	/** Returns an array of all replenishment functions. */
	public abstract String[] getFunctionNames();

	/**
	 * Returns the index of the replenishment function which will be used for
	 * attributes not listed in the parameter list &quot;columns&quot;.
	 */
	public abstract int getDefaultFunction();

	/**
	 * Returns the index of the replenishment function which will be used for
	 * attributes listed in the parameter list &quot;columns&quot;.
	 */
	public abstract int getDefaultColumnFunction();

	/**
	 * Iterates over all examples and all attributes makes callbacks to
	 * {@link #getReplenishmentValue(int, ExampleSet, Attribute, double, String)} if
	 * {@link #replenishValue(double)} returns true.
	 */
	public IOObject[] apply() throws OperatorException {
		ExampleSet eSet = getInput(ExampleSet.class);
		eSet.recalculateAllAttributeStatistics();

		int[] replenishmentFunctions = new int[eSet.getAttributes().size()];
		for (int j = 0; j < replenishmentFunctions.length; j++) {
			replenishmentFunctions[j] = getParameterAsInt(PARAMETER_DEFAULT);
		}
		Iterator i = getParameterList(PARAMETER_COLUMNS).iterator();
		while (i.hasNext()) {
			Object[] pair = (Object[]) i.next();
			String name = (String) pair[0];
			Integer replenishmentFunctionIndex = (Integer) pair[1];
			int j = 0;
			for (Attribute attribute : eSet.getAttributes()) {
				if (attribute.getName().equals(name)) {
					replenishmentFunctions[j] = replenishmentFunctionIndex.intValue();
				}
				j++;
			}
		}

		Iterator<Example> reader = eSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			int j = 0;
			for (Attribute attribute : eSet.getAttributes()) {
				double value = example.getValue(attribute);
				if (replenishValue(value)) {
					example.setValue(attribute, getReplenishmentValue(replenishmentFunctions[j], eSet, attribute, value, getParameterAsString(PARAMETER_REPLENISHMENT_VALUE)));
				}
				j++;
			}
			checkForStop();
		}
		return new IOObject[] { eSet };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeCategory(PARAMETER_DEFAULT, "Function to apply to all columns that are not explicitly specified by parameter 'columns'.", getFunctionNames(), getDefaultFunction());
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeList(PARAMETER_COLUMNS, "List of replacement functions for each column.", new ParameterTypeCategory("replace_with", "The key is the attribute name. The value is the name of function used to replace the missing value.", getFunctionNames(), getDefaultColumnFunction())));
        types.add(new ParameterTypeString(PARAMETER_REPLENISHMENT_VALUE, "This value is used for some of the replenishment types.", true));
		return types;
	}
}
