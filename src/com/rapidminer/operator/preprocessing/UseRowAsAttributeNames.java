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
package com.rapidminer.operator.preprocessing;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.preprocessing.filter.NominalNumbers2Numerical;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;

/**
 * <p>This operators uses the values of the specified row of the data set as new
 * attribute names (including both regular and special columns). This might
 * be useful for example after a transpose operation. The row will be deleted 
 * from the data set. Please note, however, that an internally used nominal 
 * mapping will not be removed and following operators like {@link NominalNumbers2Numerical}
 * could possibly not work as expected. In order to correct the value types and 
 * nominal value mappings, one could use the operator {@link GuessValueTypes}
 * after this operator.</p> 
 *
 * @author Ingo Mierswa
 * @version $Id: UseRowAsAttributeNames.java,v 1.2 2008/05/09 19:22:54 ingomierswa Exp $
 */
public class UseRowAsAttributeNames extends Operator {

	public static final String PARAMETER_ROW_NUMBER = "row_number";
	
	public UseRowAsAttributeNames(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		int rowNumber = getParameterAsInt(PARAMETER_ROW_NUMBER) - 1; // counting of parameter starts with 1
		if ((rowNumber < 0) || (rowNumber > exampleSet.size() - 1)) {
			throw new UserError(this, 207, new Object[] { (rowNumber + 1), PARAMETER_ROW_NUMBER, "the value must be between 1 and the number of available examples" });
		}
		
		Example example = exampleSet.getExample(rowNumber);
		Iterator<Attribute> a = exampleSet.getAttributes().allAttributes();
		while (a.hasNext()) {
			Attribute attribute = a.next();
			double value = example.getValue(attribute);
			String newName = value + "";
			if (attribute.isNominal()) {
				newName = attribute.getMapping().mapIndex((int)value);
			}
			attribute.setName(newName);
		}
	
		// remove example
		int[] elements = new int[exampleSet.size()];
		elements[0] = 0;
		for (int i = 1; i < elements.length; i++)
			elements[i] = 1;
		SplittedExampleSet result = new SplittedExampleSet(exampleSet, new Partition(elements, 2));
		result.selectSingleSubset(1);
		
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
		ParameterType type = new ParameterTypeInt(PARAMETER_ROW_NUMBER, "Indicates which row should be used as attribute names. Counting starts with 1.", 1, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
