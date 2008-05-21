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

import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;

/**
 * This operator changes the attribute roles of two input attributes. This could for 
 * example be useful to exchange the roles of a label with a regular attribute (and 
 * vice versa), or a label with a batch attribute, a label with a cluster etc.
 * 
 * @author Ingo Mierswa
 * @version $Id: ExchangeAttributeRoles.java,v 1.2 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class ExchangeAttributeRoles extends Operator {

	public static final String PARAMETER_FIRST_ATTRIBUTE = "first_attribute";
	
	public static final String PARAMETER_SECOND_ATTRIBUTE = "second_attribute";
	
	
	public ExchangeAttributeRoles(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		String firstName = getParameterAsString(PARAMETER_FIRST_ATTRIBUTE);
		String secondName = getParameterAsString(PARAMETER_SECOND_ATTRIBUTE);
		
		AttributeRole firstRole = exampleSet.getAttributes().getRole(firstName);
		AttributeRole secondRole = exampleSet.getAttributes().getRole(secondName);
		
		if (firstRole == null)
			throw new UserError(this, 111, firstName);
		
		if (secondRole == null)
			throw new UserError(this, 111, secondName);
		
		String dummyRoleName = secondRole.getSpecialName();
		secondRole.setSpecial(firstRole.getSpecialName());
		exampleSet.getAttributes().setSpecialAttribute(secondRole.getAttribute(), secondRole.getSpecialName());
		firstRole.setSpecial(dummyRoleName);
		exampleSet.getAttributes().setSpecialAttribute(firstRole.getAttribute(), firstRole.getSpecialName());
		
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
		types.add(new ParameterTypeString(PARAMETER_FIRST_ATTRIBUTE, "The name of the first attribute for the attribute role exchange.", false));
		types.add(new ParameterTypeString(PARAMETER_SECOND_ATTRIBUTE, "The name of the first attribute for the attribute role exchange.", false));
		return types;
	}
}
