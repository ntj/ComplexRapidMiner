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
package com.rapidminer.operator.features.construction;

import java.util.Iterator;
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
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.Ontology;


/**
 * <p>This operator applies a linear combination for each vector of the input ExampleSet, i.e.
 * it creates a new feature containing the sum of all values of each row.</p>
 * 
 * @author Thomas Harzer, Ingo Mierswa
 * @version $Id: LinearCombinationOperator.java,v 1.4 2008/05/09 19:22:54 ingomierswa Exp $
 */
public class LinearCombinationOperator extends Operator {
	

	/** The parameter name for &quot;Indicates if the all old attributes should be kept.&quot; */
	public static final String PARAMETER_KEEP_ALL = "keep_all";
	public LinearCombinationOperator(OperatorDescription description) {
		super(description);
    }	
	
    public IOObject[] apply() throws OperatorException {    	
    	// get Example Set
    	ExampleSet exampleSet = getInput(ExampleSet.class);
        // throw error if an attribute is not numeric 
        for (Attribute attribute : exampleSet.getAttributes()) {
        	if (attribute.isNominal()) {
            	throw new UserError(this, 104, new Object[] { "Linear Combination", attribute.getName() });
            }
        } 
        
        // create linear combination attribute
        Attribute newAttribute = AttributeFactory.createAttribute("linear_combination", Ontology.REAL);
        exampleSet.getExampleTable().addAttribute(newAttribute);
        exampleSet.getAttributes().addRegular(newAttribute);
                  
        Iterator i = exampleSet.iterator();
        // go through the object attributes and sum them up
        while (i.hasNext()) {
        	Example example = (Example) i.next();
        	double valueSum = 0.0d;
        	for (Attribute attribute : example.getAttributes()) { 
        		if (!attribute.equals(newAttribute))
        			valueSum += example.getValue(attribute);        
        	}
        	example.setValue(newAttribute, valueSum);
        }

        // remove old attributes
        if (!getParameterAsBoolean(PARAMETER_KEEP_ALL)) {
            exampleSet.getAttributes().clearRegular();
            exampleSet.getAttributes().addRegular(newAttribute);
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
		types.add(new ParameterTypeBoolean(PARAMETER_KEEP_ALL, "Indicates if the all old attributes should be kept.", false));
		return types;
	}
}
