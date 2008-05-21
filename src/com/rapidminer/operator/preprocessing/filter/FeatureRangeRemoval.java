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
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * This operator removes the attributes of a given range. The first and last
 * attribute of the range will be removed, too. Counting starts with 1.
 * 
 * @author Sebastian Land
 * @version $Id: FeatureRangeRemoval.java,v 1.4 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class FeatureRangeRemoval extends Operator {


	/** The parameter name for &quot;The first attribute of the attribute range which should  be removed&quot; */
	public static final String PARAMETER_FIRST_ATTRIBUTE = "first_attribute";

	/** The parameter name for &quot;The last attribute of the attribute range which should  be removed&quot; */
	public static final String PARAMETER_LAST_ATTRIBUTE = "last_attribute";
	public FeatureRangeRemoval(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		int first = getParameterAsInt(PARAMETER_FIRST_ATTRIBUTE) - 1;
		int last = getParameterAsInt(PARAMETER_LAST_ATTRIBUTE) - 1;
		if (last < first) {
			logWarning("Last attribute is smaller than first. No change performed.");
		}
		
		if (last >= exampleSet.getAttributes().size()) {
			throw new UserError(this, 125, String.valueOf(exampleSet.getAttributes().size()), String.valueOf(last + 1));
		}
		
		Iterator<Attribute> i = exampleSet.getAttributes().iterator();
		int counter = 0;
		while (i.hasNext() && counter <= last) {
			i.next();
			if ((counter >= first) && (counter <= last))
				i.remove();
			checkForStop();
			counter++;
		}
		return new IOObject[] { exampleSet };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();
		ParameterType parameterType = new ParameterTypeInt(PARAMETER_FIRST_ATTRIBUTE, "The first attribute of the attribute range which should  be removed", 1, Integer.MAX_VALUE, false);
		parameterType.setExpert(false);
		parameterTypes.add(parameterType);
		parameterType = new ParameterTypeInt(PARAMETER_LAST_ATTRIBUTE, "The last attribute of the attribute range which should  be removed", 1, Integer.MAX_VALUE, false);
		parameterType.setExpert(false);
		parameterTypes.add(parameterType);
		return parameterTypes;
	}
}
