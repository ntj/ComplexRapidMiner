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

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.LastInnerOperatorCondition;
import com.rapidminer.operator.performance.PerformanceVector;


/**
 * This operator provides basic functions for all other
 * parameter optimization operators.
 * 
 * @author Ingo Mierswa, Helge Homburg, Tobias Malbrecht
 * @version $Id: ParameterOptimizationOperator.java,v 1.13 2006/04/05 08:57:26
 *          ingomierswa Exp $
 */
public abstract class ParameterOptimizationOperator extends ParameterIteratingOperatorChain {

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
    	IOContainer resultContainer = getInput();
    	try {
    		for (int i = 0; i < getNumberOfOperators(); i++) {
    			resultContainer = getOperator(i).apply(resultContainer);
    		}
    		return resultContainer.remove(PerformanceVector.class);
    	} catch (OperatorException e) {
    		logWarning("Cannot evaluate performance for current parameter combination: " + e.getMessage());
    		return null;
    	}
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
}
