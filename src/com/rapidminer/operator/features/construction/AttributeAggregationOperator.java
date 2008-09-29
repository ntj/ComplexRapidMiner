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

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.function.AggregationFunction;
import com.rapidminer.tools.math.function.AbstractAggregationFunction;


/**
 * Allows to generate a new attribute which consists of a function of several
 * other attributes. As functions, several aggregation attributes are available.
 * 
 * @author Tobias Malbrecht
 * @version $Id: AttributeAggregationOperator.java,v 1.1 2008/08/20 11:12:34 tobiasmalbrecht Exp $
 */
public class AttributeAggregationOperator extends Operator {
	
	public static final String PARAMETER_ATTRIBUTE_NAME = "attribute_name";
	
	public static final String PARAMETER_AGGREGATION_ATTRIBUTES = "aggregation_attributes";
	
	public static final String PARAMETER_AGGREGATION_FUNCTION = "aggregation_function";
	
    public static final String PARAMETER_IGNORE_MISSINGS = "ignore_missings";
	
	/** The parameter name for &quot;Indicates if the all old attributes should be kept.&quot; */
	public static final String PARAMETER_KEEP_ALL = "keep_all";
	
	public AttributeAggregationOperator(OperatorDescription description) {
		super(description);
    }	
	
    private Attribute[] getAttributesArrayFromRegex(Attributes attributes, String regex) throws OperatorException {
		Pattern pattern = null;
	    try {
	        pattern = Pattern.compile(regex);
	    } catch (PatternSyntaxException e) {
            throw new UserError(this, 206, regex, e.getMessage());
	    }
	    List<Attribute> attributeList = new LinkedList<Attribute>();
		for (Attribute attribute : attributes) {
			Matcher matcher = pattern.matcher(attribute.getName());
			if (matcher.matches()) {
				attributeList.add(attribute);
			}
		}

		Attribute[] attributesArray = new Attribute[attributeList.size()];
		attributesArray = attributeList.toArray(attributesArray);
		return attributesArray;
    }
	
    public IOObject[] apply() throws OperatorException {    	
    	ExampleSet exampleSet = getInput(ExampleSet.class);
    	Attribute[] attributes = getAttributesArrayFromRegex(exampleSet.getAttributes(), getParameterAsString(PARAMETER_AGGREGATION_ATTRIBUTES));
    	String functionName = AbstractAggregationFunction.KNOWN_AGGREGATION_FUNCTION_NAMES[getParameterAsInt(PARAMETER_AGGREGATION_FUNCTION)];
    	boolean ignoreMissings = getParameterAsBoolean(PARAMETER_IGNORE_MISSINGS);
    	AggregationFunction aggregationFunction = null;
        try {
            aggregationFunction = AbstractAggregationFunction.createAggregationFunction(functionName, ignoreMissings);
        } catch (InstantiationException e) {
            throw new UserError(this, 904, functionName, e.getMessage());
        } catch (IllegalAccessException e) {
            throw new UserError(this, 904, functionName, e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new UserError(this, 904, functionName, e.getMessage());
        } catch (NoSuchMethodException e) {
        	throw new UserError(this, 904, functionName, e.getMessage());
        } catch (InvocationTargetException e) {
        	throw new UserError(this, 904, functionName, e.getMessage());
        }
        
        for (int i = 0; i < attributes.length; i++) {
	        if (!aggregationFunction.supportsAttribute(attributes[i])) {
	        	throw new UserError(this, 136, attributes[i].getName());
	      	}
        }

        // create aggregation attribute
        Attribute newAttribute = AttributeFactory.createAttribute(getParameterAsString(PARAMETER_ATTRIBUTE_NAME), Ontology.REAL);
        exampleSet.getExampleTable().addAttribute(newAttribute);
        exampleSet.getAttributes().addRegular(newAttribute);

        // iterate over examples and aggregate values
        double[] values = new double[attributes.length];
        for (Example example : exampleSet) {
        	for (int i = 0; i < attributes.length; i++) {
        		values[i] = example.getValue(attributes[i]);        			
        	}
        	example.setValue(newAttribute, aggregationFunction.calculate(values));
        }

        // remove old attributes
        if (!getParameterAsBoolean(PARAMETER_KEEP_ALL)) {
        	for (int i = 0; i < attributes.length; i++) {
        		exampleSet.getAttributes().remove(attributes[i]);
        	}
        }
        
        return new IOObject[] { exampleSet };
    }
	
   public Class<?>[] getInputClasses() {
        return new Class[] { ExampleSet.class };
   }

	public Class<?>[] getOutputClasses() {
	    return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_ATTRIBUTE_NAME, "Name of the resulting attributes.", false));
		types.add(new ParameterTypeString(PARAMETER_AGGREGATION_ATTRIBUTES, "Regular expression specifying the attributes that should be aggregated.", false));
		types.add(new ParameterTypeCategory(PARAMETER_AGGREGATION_FUNCTION, "Function for aggregating the attribute values.", AbstractAggregationFunction.KNOWN_AGGREGATION_FUNCTION_NAMES, AbstractAggregationFunction.SUM));
        types.add(new ParameterTypeBoolean(PARAMETER_IGNORE_MISSINGS, "Indicates if missings should be ignored and aggregation should be based only on existing values or not. In the latter case the aggregated value will be missing in the presence of missing values.", true));        
		types.add(new ParameterTypeBoolean(PARAMETER_KEEP_ALL, "Indicates if the all old attributes should be kept.", true));
		return types;
	}
}
