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
package com.rapidminer.tools.math.function;

import java.lang.reflect.InvocationTargetException;


/**
 * Superclass for aggregation functions providing some generic functions.
 * 
 * @author Tobias Malbrecht
 * @version $Id: AbstractAggregationFunction.java,v 1.2 2008/08/20 11:09:50 tobiasmalbrecht Exp $
 *
 */
public abstract class AbstractAggregationFunction implements AggregationFunction {

    public static final Class[] KNOWN_AGGREGATION_FUNCTIONS = {
        AverageFunction.class,
        VarianceFunction.class,
        StandardDeviationFunction.class,
        CountFunction.class,
        MinFunction.class,
        MaxFunction.class,
        SumFunction.class
    };
    
    public static final String[] KNOWN_AGGREGATION_FUNCTION_NAMES = {
        "average",
        "variance",
        "standard_deviation",
        "count",
        "minimum",
        "maximum",
        "sum"
    };
    
    public static final int AVERAGE = 0;
    
    public static final int VARIANCE = 1;
    
    public static final int STANDARD_DEVIATION = 2;
    
    public static final int COUNT = 3;
    
    public static final int MINIMUM = 4;
    
    public static final int MAXIMUM = 5;
    
    public static final int SUM = 6;
    
    public static final boolean DEFAULT_IGNORE_MISSINGS = true;
    
    protected boolean ignoreMissings = DEFAULT_IGNORE_MISSINGS;
    
    protected boolean foundMissing = false;

    public static AggregationFunction createAggregationFunction(String functionName, boolean ignoreMissings) throws InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        int typeIndex = -1;
        for (int i = 0; i < KNOWN_AGGREGATION_FUNCTION_NAMES.length; i++) {
            if (KNOWN_AGGREGATION_FUNCTION_NAMES[i].equals(functionName)) {
                typeIndex = i;
                break;
            }
        }
        Class<?> clazz = null;
        if (typeIndex < 0) {
            clazz = Class.forName(functionName);
        } else {
            clazz = KNOWN_AGGREGATION_FUNCTIONS[typeIndex];
        }
        return (AggregationFunction) clazz.getConstructor(new Class[] { Boolean.class }).newInstance(ignoreMissings);
    }
    
    public static AggregationFunction createAggregationFunction(String functionName) throws InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
    	return createAggregationFunction(functionName, true);
    }
    
    public static AggregationFunction createAggregationFunction(int typeIndex, boolean ignoreMissings) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	if (typeIndex >= 0 && typeIndex < KNOWN_AGGREGATION_FUNCTION_NAMES.length) {
    		Class<?> clazz = KNOWN_AGGREGATION_FUNCTIONS[typeIndex];
            return (AggregationFunction) clazz.getConstructor(new Class[] { Boolean.class }).newInstance(ignoreMissings);
    	} else {
    		throw new InstantiationException();
    	}
    }
    
    public static AggregationFunction createAggregationFunction(int typeIndex) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	return createAggregationFunction(typeIndex, true);
    }
    
	public AbstractAggregationFunction() {
		this(true);
	}

	public AbstractAggregationFunction(Boolean ignoreMissings) {
		this.ignoreMissings = ignoreMissings;
		this.foundMissing = false;
		reset();
	}
	
	/**
	 * Reset the counters.
	 */
	protected abstract void reset();

	/**
	 * Resets the counters and computes the aggregation function
	 * solely based on the given values.
	 */
	public double calculate(double[] values) {
		reset();
		for (double value : values) {
			update(value);
		}
		return getValue();
	}
}
