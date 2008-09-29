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
package com.rapidminer.operator.meta;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import com.rapidminer.gui.properties.ConfigureParameterOptimizationDialogCreator;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeConfiguration;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeParameterValue;
import com.rapidminer.parameter.value.ParameterValues;
import com.rapidminer.parameter.value.ParameterValueGrid;
import com.rapidminer.parameter.value.ParameterValueList;
import com.rapidminer.parameter.value.ParameterValueRange;


/**
 * Provides an operator chain which operates on given parameters
 * depending on specified values for these parameters.
 * 
 * @author Tobias Malbrecht
 * @version $Id: ParameterIteratingOperatorChain.java,v 1.6 2008/07/04 10:27:17 stiefelolm Exp $
 */
public abstract class ParameterIteratingOperatorChain extends OperatorChain {
	
	/** The parameter name for &quot;Parameters to optimize in the format OPERATORNAME.PARAMETERNAME.&quot; */
	public static final String PARAMETER_PARAMETERS = "parameters";
	
	/** A specification of the parameter values for a parameter.&quot; */
	public static final String PARAMETER_VALUES = "values";
	
	/** Means that the parameter iteration scheme can only handle discrete parameter values (i.e. lists or numerical grids). */
	public static final int VALUE_MODE_DISCRETE = 0;

	/** Means that the parameter iteration scheme can only handle intervals of numerical values. */
	public static final int VALUE_MODE_CONTINUOUS = 1;
	
	private static final int PARAMETER_VALUES_ARRAY_LENGTH_RANGE = 2;
	
	private static final int PARAMETER_VALUES_ARRAY_LENGTH_GRID = 3;
	
	private static final int PARAMETER_VALUES_ARRAY_LENGTH_SCALED_GRID = 4;
	
	
	public ParameterIteratingOperatorChain(OperatorDescription description) {
		super(description);
	}
	
	/**
	 * Has to return one of the predefined modes which indicate whether the
	 * operator takes discrete values or intervals as basis for optimization.
	 * The first option is to be taken for all strategies that iterate over the
	 * given parameters. The latter option is to be taken for strategies such
	 * as an evolutionary one in which allowed ranges of parameters have to be
	 * specified.
	 */
	public abstract int getParameterValueMode();
	
	/**
	 * Parses a parameter list and creates the corresponding data structures.	
	 */
	public List<ParameterValues> parseParameterValues(List parameterList) throws OperatorException {
		List<ParameterValues> parameterValuesList = new LinkedList<ParameterValues>();
		Iterator iterator = parameterList.iterator();
		while (iterator.hasNext()) {
			Object[] parameterListEntry = (Object[]) iterator.next();
			String[] parameter = ((String) parameterListEntry[0]).split("\\.");
            if (parameter.length != 2)
                throw new UserError(this, 907, parameterListEntry[0]);
            Operator operator = getProcess().getOperator(parameter[0]);
            if (operator == null)
                throw new UserError(this, 109, parameter[0]);
            ParameterType parameterType = operator.getParameters().getParameterType(parameter[1]);
            if (parameterType == null) {
                throw new UserError(this, 906, parameter[0] + "." + parameter[1]);
            }
            String parameterValuesString = (String)parameterListEntry[1];
            ParameterValues parameterValues = null;
            try {
	            int startIndex = parameterValuesString.indexOf("["); 
	            if (startIndex >= 0) {
                    int endIndex = parameterValuesString.indexOf("]");
                    if (endIndex > startIndex) {
                        String[] parameterValuesArray = parameterValuesString.substring(startIndex + 1, endIndex).trim().split("[;:,]");
                        switch (parameterValuesArray.length) {
                        case PARAMETER_VALUES_ARRAY_LENGTH_RANGE: {		// value range: [minValue;maxValue]
                        	double min = Double.parseDouble(parameterValuesArray[0]);
                        	double max = Double.parseDouble(parameterValuesArray[1]);
                            parameterValues = new ParameterValueRange(operator, parameterType, min, max);
                        }
                        	break;
                        case PARAMETER_VALUES_ARRAY_LENGTH_GRID: {		// value grid: [minValue;maxValue;stepSize]
                        	double min = Double.parseDouble(parameterValuesArray[0]);
                        	double max = Double.parseDouble(parameterValuesArray[1]);
                        	double stepSize = Double.parseDouble(parameterValuesArray[2]);
                        	if (stepSize == 0) {
                        		throw new Exception("step size of 0 is not allowed");
                        	}
                            if (min <= max + stepSize) {
                                throw new Exception("end value must at least be as large as start value plus step size");
                            }
                            parameterValues = new ParameterValueGrid(operator, parameterType, min, max, stepSize);
                        }
                        	break;
                        case PARAMETER_VALUES_ARRAY_LENGTH_SCALED_GRID: {		// value grid: [minValue;maxValue;noOfSteps;scale]
                        	double min = Double.parseDouble(parameterValuesArray[0]);
                        	double max = Double.parseDouble(parameterValuesArray[1]);
                        	int steps = Integer.parseInt(parameterValuesArray[2]);
                        	if (steps == 0) {
                        		throw new Exception("step size of 0 is not allowed");
                        	}
                        	String scaleName = parameterValuesArray[3];
                        	parameterValues = new ParameterValueGrid(operator, parameterType, min, max, steps, scaleName);
                        }
                        	break;
                        default:
                            throw new Exception("parameter values string could not be parsed (too many arguments)");
                        }
                    } else {
                        throw new Exception("']' was missing");
                    }
	            } else {
	            	int colonIndex = parameterValuesString.indexOf(":");
	            	if (colonIndex >= 0) {
	            		// maintain compatibility for evolutionary parameter optimization (old format: startValue:endValue without parantheses)
	            		String[] parameterValuesArray = parameterValuesString.trim().split(":");
	            		if (parameterValuesArray.length != 2) {
	            			throw new Exception("wrong parameter range format");
	            		} else {
	                    	double min = Double.parseDouble(parameterValuesArray[0]);
	                    	double max = Double.parseDouble(parameterValuesArray[1]);
	                        parameterValues = new ParameterValueRange(operator, parameterType, min, max);
	            		}
	            	} else {
		            	// usual parameter value list: value1,value2,value3,...
	            		if (parameterValuesString.length() != 0) {
	            			String[] values = parameterValuesString.split(",");
		                	parameterValues = new ParameterValueList(operator, parameterType, values);
		                }
	            	}
	            }
            } catch (Throwable e) {
                throw new UserError(this, 116, parameterListEntry[0], "Unknown parameter value specification format: '" + parameterListEntry[1] + "'. Error: " + e.getMessage());
            }
            if (parameterValues != null) {
            	parameterValuesList.add(parameterValues);
            }
		}
		return parameterValuesList;
	}

    /**
     * Returns the highest possible value for the maximum number of inner
     * operators.
     */
    public int getMaxNumberOfInnerOperators() {
        return Integer.MAX_VALUE;
    }

    /** Returns 1 for the minimum number of inner operators. */
    public int getMinNumberOfInnerOperators() {
        return 1;
    }
    
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeConfiguration(ConfigureParameterOptimizationDialogCreator.class, this);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeList(PARAMETER_PARAMETERS, "The parameters.", new ParameterTypeParameterValue(PARAMETER_VALUES, "The value specifications for the parameters."));
		type.setHidden(true);
		types.add(type);
		return types;
	}
}
