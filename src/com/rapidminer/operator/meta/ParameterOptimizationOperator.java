/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.meta;

import java.util.List;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.LastInnerOperatorCondition;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeParameterValue;


/**
 * This operator provides basic functions for all other
 * parameter optimization operators.
 * 
 * @author Ingo Mierswa, Helge Homburg
 * @version $Id: ParameterOptimizationOperator.java,v 1.13 2006/04/05 08:57:26
 *          ingomierswa Exp $
 */
public abstract class ParameterOptimizationOperator extends OperatorChain {


	/** The parameter name for &quot;Parameters to optimize in the format OPERATORNAME.PARAMETERNAME and either a comma separated list of parameter values or a single value.&quot; */
	public static final String PARAMETER_PARAMETERS = "parameters";
	public ParameterOptimizationOperator(OperatorDescription description) {
		super(description);
        addValue(new Value("performance", "currently best performance") {
            public double getValue() {
                return getCurrentBestPerformance();
            }
        });
	}
    
    public abstract double getCurrentBestPerformance();
    
    /**
     * Applies the inner operator and employs the PerformanceEvaluator for
     * calculating a list of performance criteria which is returned.
     */
    protected PerformanceVector getPerformance() {
    	IOObject[] evalout = null;
    	PerformanceVector result = null;
    	try {
    		evalout = super.apply();
    	} catch (OperatorException e) {
    		logWarning("Cannot evaluate performance for current parameter combination: " + e.getMessage());
    	}
    	if (evalout != null) {
    		IOContainer evalCont = new IOContainer(evalout);
    		try {
    			result = evalCont.remove(PerformanceVector.class); 
    		} catch (MissingIOObjectException e) {
    			logError("Inner operator do not provide performance vector: return empty performance...");        	
    		}
    	}
        return result;
    }

    public InnerOperatorCondition getInnerOperatorCondition() {
        return new LastInnerOperatorCondition(new Class[] { PerformanceVector.class });
    }

    public Class[] getInputClasses() {
        return new Class[0];
    }

    public Class[] getOutputClasses() {
        return new Class[] { ParameterSet.class, PerformanceVector.class };
    }

    /**
     * Returns the highest possible value for the maximum number of innner
     * operators.
     */
    public int getMaxNumberOfInnerOperators() {
        return Integer.MAX_VALUE;
    }

    /** Returns 1 for the minimum number of innner operators. */
    public int getMinNumberOfInnerOperators() {
        return 1;
    }
    
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeList(PARAMETER_PARAMETERS, "Parameters to optimize in the format OPERATORNAME.PARAMETERNAME and either a comma separated list of parameter values or a single value.", new ParameterTypeParameterValue("value(s)", "The value(s) of the parameter (comma-separated if more than one or [start;end;step])"));
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
