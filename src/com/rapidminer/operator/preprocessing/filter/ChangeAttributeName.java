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
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;


/**
 * <p>
 * This operator can be used to rename an attribute of the input example set.
 * If you want to change the attribute type (e.g. from regular to id attribute or from label to regular etc.),
 * you should use the {@link ChangeAttributeType} operator.
 * </p>
 * 
 * @author Ingo Mierswa
 * @version $Id: ChangeAttributeName.java,v 1.4 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class ChangeAttributeName extends Operator {


	/** The parameter name for &quot;The old name of the attribute.&quot; */
	public static final String PARAMETER_OLD_NAME = "old_name";

	/** The parameter name for &quot;The new name of the attribute.&quot; */
	public static final String PARAMETER_NEW_NAME = "new_name";
	public ChangeAttributeName(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		String oldName = getParameterAsString(PARAMETER_OLD_NAME);
		Attribute attribute = exampleSet.getAttributes().get(oldName);
		if (attribute == null) {
			throw new UserError(this, 111, oldName);
		}
		String newName = getParameterAsString(PARAMETER_NEW_NAME);
		attribute.setName(newName);
		return new IOObject[] { exampleSet };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_OLD_NAME, "The old name of the attribute.", false));
		types.add(new ParameterTypeString(PARAMETER_NEW_NAME, "The new name of the attribute.", false));
		return types;
	}
}
