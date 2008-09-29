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
package com.rapidminer.operator.olap.aggregation;

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
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.preprocessing.filter.ExampleFilter;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.container.MultidimensionalArraySet;
import com.rapidminer.tools.container.ValueSet;
import com.rapidminer.tools.math.function.AggregationFunction;
import com.rapidminer.tools.math.function.AbstractAggregationFunction;


/**
 * <p>This operator creates a new example set from the input example set
 * showing the results of arbitrary aggregation functions (as SUM, COUNT
 * etc. known from SQL). Before the values of different rows are aggregated
 * into a new row the rows might be grouped by the values of a multiple
 * attributes (similar to the group-by clause known from SQL). In this case
 * a new line will be created for each group.</p>
 * 
 * <p>Please note that the known HAVING clause from SQL can be simulated
 * by an additional {@link ExampleFilter} operator following this one.</p>
 * 
 * @author Tobias Malbrecht, Ingo Mierswa
 * @version $Id: AggregationOperator.java,v 1.9 2008/08/20 11:09:50 tobiasmalbrecht Exp $
 */
public class AggregationOperator extends Operator {
    
	public static final String PARAMETER_AGGREGATION_ATTRIBUTES = "aggregation_attributes";
	
    public static final String PARAMETER_AGGREGATION_FUNCTIONS = "aggregation_functions";
	
    public static final String PARAMETER_GROUP_BY_ATTRIBUTES = "group_by_attributes";
    
    public static final String PARAMETER_ONLY_DISTINCT = "only_distinct";
    
    public static final String PARAMETER_IGNORE_MISSINGS = "ignore_missings";
    
    private static final String GENERIC_GROUP_NAME = "group";
    
    private static final String GENERIC_ALL_NAME = "all";
    
    public AggregationOperator(OperatorDescription desc) {
        super(desc);
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
        boolean onlyDistinctValues = getParameterAsBoolean(PARAMETER_ONLY_DISTINCT);
        boolean ignoreMissings = getParameterAsBoolean(PARAMETER_IGNORE_MISSINGS);

        List parameterList = this.getParameterList(PARAMETER_AGGREGATION_ATTRIBUTES);
        int aggregations = parameterList.size();
        Attribute[] aggregationAttributes = new Attribute[aggregations];
        String[] aggregationFunctionNames = new String[aggregations];
        int parameterListIndex = 0;
        for (Object entry : parameterList) {
        	Object[] valuePair = (Object[]) entry;
        	String attributeName = (String) valuePair[0];
        	String aggregationFunctionName = (String) valuePair[1];
        	Attribute attribute = exampleSet.getAttributes().get(attributeName);
        	if (attribute == null) {
                throw new UserError(this, 111, attributeName);
        	}
        	aggregationAttributes[parameterListIndex] = attribute;
        	aggregationFunctionNames[parameterListIndex] = aggregationFunctionName;
        	parameterListIndex++;
        }
        
        Attribute weightAttribute = exampleSet.getAttributes().getWeight();
        MemoryExampleTable resultTable = null;
        
        if (isParameterSet(PARAMETER_GROUP_BY_ATTRIBUTES)) {
        	String groupByAttributesRegex = getParameterAsString(PARAMETER_GROUP_BY_ATTRIBUTES);
        	Attribute[] groupByAttributes = getAttributesArrayFromRegex(exampleSet.getAttributes(), groupByAttributesRegex);
        	
        	if (groupByAttributes.length == 0) {
        		throw new UserError(this, 111, groupByAttributesRegex);
        	}
        	
        	int[] mappingSizes = new int[groupByAttributes.length];
        	for (int i = 0; i < groupByAttributes.length; i++) {
        		if (!groupByAttributes[i].isNominal()) {
        			throw new UserError(this, 103, new Object[] { groupByAttributesRegex, "grouping by attribute." });        			
        		}
        		mappingSizes[i] = groupByAttributes[i].getMapping().size();
        	}
        	
        	// create aggregation functions
        	MultidimensionalArraySet<AggregationFunction[]> functionSet = new MultidimensionalArraySet<AggregationFunction[]>(mappingSizes);
        	for (int i = 0; i < functionSet.size(); i++) {
            	AggregationFunction[] functions = new AggregationFunction[aggregations]; 
            	for (int j = 0; j < aggregations; j++) {
            		functions[j] = getAggregationFunction(aggregationFunctionNames[j], ignoreMissings, aggregationAttributes[j]);
            	}
        		functionSet.set(i, functions);
        	}
        	
        	if (onlyDistinctValues) {
        		
        		// initialize distinct value sets
        		MultidimensionalArraySet<ValueSet[]> distinctValueSet = new MultidimensionalArraySet<ValueSet[]>(mappingSizes);
        		for (int i = 0; i < functionSet.size(); i++) {
        			ValueSet[] distinctValues = new ValueSet[aggregations];
        			for (int j = 0; j < aggregations; j++) {
        				distinctValues[j] = new ValueSet();
        			}
        			distinctValueSet.set(i, distinctValues);
        		}
        		
        		// extract distinct values
        		for (Example example : exampleSet) {
        			int[] indices = new int[groupByAttributes.length];
        			for (int i = 0; i < groupByAttributes.length; i++) {
        				indices[i] = (int) example.getValue(groupByAttributes[i]);
        			}
        			double weight = weightAttribute != null ? example.getWeight() : 1.0d;
        			ValueSet[] distinctValues = distinctValueSet.get(indices);
        			for (int i = 0; i < aggregations; i++) {
        				distinctValues[i].add(example.getValue(aggregationAttributes[i]), weight);
        			}
        		}
        		
	        	// compute aggregation function values
        		for (int i = 0; i < functionSet.size(); i++) {
        			AggregationFunction[] functions = functionSet.get(i);
        			ValueSet[] distinctValues = distinctValueSet.get(i);
        			for (int j = 0; j < aggregations; j++) {
        				for (Double value : distinctValues[j]) {
        					functions[j].update(value);
        				}
        			}
        		}
        	} else {
        		
	        	// compute aggregation function values
	        	for (Example example : exampleSet) {
	        		int[] indices = new int[groupByAttributes.length];
	        		for (int i = 0; i < groupByAttributes.length; i++) {
	        			indices[i] = (int) example.getValue(groupByAttributes[i]);
	        		}
		    		double weight = weightAttribute != null ? example.getWeight() : 1.0d;
		    		AggregationFunction[] functions = functionSet.get(indices);
		    		for (int i = 0; i < aggregations; i++) {
		    			functions[i].update(example.getValue(aggregationAttributes[i]), weight);
		    		}
	        	}
        	}

        	// create grouped data table
        	List<Attribute> resultAttributes = new LinkedList<Attribute>();
            Attribute[] resultGroupAttributes = new Attribute[groupByAttributes.length];
            for (int i = 0; i < groupByAttributes.length; i++) {
            	Attribute resultGroupAttribute = AttributeFactory.createAttribute(groupByAttributes[i].getName(), Ontology.NOMINAL);
            	for (int j = 0; j < groupByAttributes[i].getMapping().size(); j++) {
            		resultGroupAttribute.getMapping().mapString(groupByAttributes[i].getMapping().mapIndex(j));
            	}
            	resultAttributes.add(resultGroupAttribute);
            	resultGroupAttributes[i] = resultGroupAttribute;
            }
        	for (int i = 0; i < aggregations; i++) {
        		resultAttributes.add(AttributeFactory.createAttribute(aggregationFunctionNames[i] + "(" + aggregationAttributes[i].getName() + ")", Ontology.REAL));
        	}
            resultTable = new MemoryExampleTable(resultAttributes);
                    	
        	// fill data table
            for (int i = 0; i < functionSet.size(); i++) {
        		double data[] = new double[groupByAttributes.length + aggregations];
        		int[] indices = functionSet.getIndices(i);
        		for (int j = 0; j < groupByAttributes.length; j++) {
        			data[j] = indices[j];
        		}
        		AggregationFunction[] functions = functionSet.get(i);
        		for (int j = 0; j < aggregations; j++) {
        			data[groupByAttributes.length + j] =functions[j].getValue();
        		}
    	    	resultTable.addDataRow(new DoubleArrayDataRow(data));
        	}
        } else {
        	AggregationFunction[] functions = new AggregationFunction[aggregations]; 
        	for (int i = 0; i < aggregations; i++) {
        		functions[i] = getAggregationFunction(aggregationFunctionNames[i], ignoreMissings, aggregationAttributes[i]);
        	}

        	if (onlyDistinctValues) {

        		// initialize distinct value sets
            	ValueSet[] distinctValues = new ValueSet[aggregations];
            	for (int i = 0; i < aggregations; i++) {
            		distinctValues[i] = new ValueSet();
            	}
            	for (Example example : exampleSet) {
            		double weight = weightAttribute != null ? example.getWeight() : 1.0d;
            		for (int i = 0; i < distinctValues.length; i++) {
                		distinctValues[i].add(example.getValue(aggregationAttributes[i]), weight);            			
            		}
            	}
            	
	        	// compute aggregation function values
            	for (int i = 0; i < distinctValues.length; i++) {
	            	for (Double value : distinctValues[i]) {
	            		functions[i].update(value);
	            	}
            	}
            } else {
            	
	        	// compute aggregation function values
            	for (Example example : exampleSet) {
            		double weight = weightAttribute != null ? example.getWeight() : 1.0d;
            		for (int i = 0; i < functions.length; i++) {
                		functions[i].update(example.getValue(aggregationAttributes[i]), weight);            			
            		}
            	}
            }
        	
        	// create data table
        	List<Attribute> resultAttributes = new LinkedList<Attribute>();
        	Attribute resultGroupAttribute = AttributeFactory.createAttribute(GENERIC_GROUP_NAME, Ontology.NOMINAL); 
        	resultAttributes.add(resultGroupAttribute);
        	for (int i = 0; i < aggregations; i++) {
        		resultAttributes.add(AttributeFactory.createAttribute(aggregationFunctionNames[i] + "(" + aggregationAttributes[i].getName() + ")", Ontology.REAL));
        	}
            resultTable = new MemoryExampleTable(resultAttributes);
        	        	
            // fill data table
            double[] data = new double[aggregations + 1];
            data[0] = resultGroupAttribute.getMapping().mapString(GENERIC_ALL_NAME);
            for (int i = 0; i < aggregations; i++) {
            	data[i + 1] = functions[i].getValue();
            }
            resultTable.addDataRow(new DoubleArrayDataRow(data));
        }
        
        ExampleSet resultSet = resultTable.createExampleSet();
        return new IOObject[] { resultSet };
    }
    
    private AggregationFunction getAggregationFunction(String functionName, boolean ignoreMissings, Attribute attribute) throws UserError {
        AggregationFunction function;
        try {
            function = AbstractAggregationFunction.createAggregationFunction(functionName, ignoreMissings);
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
        if (!function.supportsAttribute(attribute)) {
        	throw new UserError(this, 136, attribute.getName());
      	}
        return function;
    }
    
    /** Indicates that the consumption of example sets can be user defined (default: no consumption). */
    public InputDescription getInputDescription(Class cls) {
        if (ExampleSet.class.isAssignableFrom(cls)) {
            return new InputDescription(cls, true, true);
        } else {
            return super.getInputDescription(cls);
        }
    }
    
    public Class<?>[] getInputClasses() {
        return new Class[] { ExampleSet.class };
    }

    public Class<?>[] getOutputClasses() {
        return new Class[] { ExampleSet.class };
    }

    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType functionParameter = new ParameterTypeStringCategory(PARAMETER_AGGREGATION_FUNCTIONS, "The type of the used aggregation function.", AbstractAggregationFunction.KNOWN_AGGREGATION_FUNCTION_NAMES, AbstractAggregationFunction.KNOWN_AGGREGATION_FUNCTION_NAMES[0]);
        types.add(new ParameterTypeList(PARAMETER_AGGREGATION_ATTRIBUTES, "The attributes which should be aggregated.", functionParameter));
        types.add(new ParameterTypeString(PARAMETER_GROUP_BY_ATTRIBUTES, "Performs a grouping by the values of the attributes whose names match the given regular expression."));
        types.add(new ParameterTypeBoolean(PARAMETER_ONLY_DISTINCT, "Indicates if only rows with distinct values for the aggregation attribute should be used for the calculation of the aggregation function.", false));
        types.add(new ParameterTypeBoolean(PARAMETER_IGNORE_MISSINGS, "Indicates if missings should be ignored and aggregation should be based only on existing values or not. In the latter case the aggregated value will be missing in the presence of missing values.", true));        
        return types;
    }
}
