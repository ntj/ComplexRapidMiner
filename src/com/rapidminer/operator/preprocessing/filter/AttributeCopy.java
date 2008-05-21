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
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;


/**
 * Adds a copy of a single attribute to the given example set.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeCopy.java,v 1.4 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class AttributeCopy extends Operator {


	/** The parameter name for &quot;The name of the nominal attribute to which values should be added.&quot; */
	public static final String PARAMETER_ATTRIBUTE_NAME = "attribute_name";

	/** The parameter name for &quot;The name of the new (copied) attribute. If this parameter is missing, simply the same name with an appended number is used.&quot; */
	public static final String PARAMETER_NEW_NAME = "new_name";
    public AttributeCopy(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException {
        ExampleSet exampleSet = getInput(ExampleSet.class);
        Attribute attribute = exampleSet.getAttributes().get(getParameterAsString(PARAMETER_ATTRIBUTE_NAME));
        
        // some checks
        if (attribute == null) {
            throw new UserError(this, 111, getParameterAsString(PARAMETER_ATTRIBUTE_NAME));
        }

        // copy attribute
        Attribute newAttribute = AttributeFactory.createAttribute(attribute);
        String newName = getParameterAsString(PARAMETER_NEW_NAME);
        if (newName != null)
            newAttribute.setName(AttributeFactory.createName(newName));
        exampleSet.getExampleTable().addAttribute(newAttribute);
        exampleSet.getAttributes().addRegular(newAttribute);
        
        // copy data
        for (Example e : exampleSet) {
            e.setValue(newAttribute, e.getValue(attribute));
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
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeString(PARAMETER_ATTRIBUTE_NAME, "The name of the nominal attribute to which values should be added.", false));
        types.add(new ParameterTypeString(PARAMETER_NEW_NAME, "The name of the new (copied) attribute. If this parameter is missing, simply the same name with an appended number is used.", true));
        return types;
    }
}
