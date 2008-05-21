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

import java.util.List;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SimpleChainInnerOperatorCondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * Performs its inner operators for the defined number of times. The input of this
 * operator will be the input of the first operator in the first iteration. 
 * The output of each children operator is the input for the following one, the output
 * of the last inner operator will be the input for the first child in the next iteration.
 * The output of the last operator in the last iteration will be the output of this
 * operator.
 * 
 * @author Ingo Mierswa
 * @version $Id: IteratingOperatorChain.java,v 1.12 2006/04/05 08:57:26
 *          ingomierswa Exp $
 */
public class IteratingOperatorChain extends OperatorChain {

	/** The parameter name for &quot;Number of iterations&quot; */
	public static final String PARAMETER_ITERATIONS = "iterations";
    
	/** The parameter name for &quot;Timeout in minutes (-1: no timeout)&quot; */
	public static final String PARAMETER_TIMEOUT = "timeout";
    
	private int currentIteration = 0;
	
	public IteratingOperatorChain(OperatorDescription description) {
		super(description);
		addValue(new Value("iteration", "The iteration currently performed by this looping operator.") {
			public double getValue() {
				return currentIteration;
			}
		});
	}

	public IOObject[] apply() throws OperatorException {
		int maxIterations = getParameterAsInt(PARAMETER_ITERATIONS);
		int timeOut = getParameterAsInt(PARAMETER_TIMEOUT);
        long stoptime = Long.MAX_VALUE;
		if (timeOut >= 0) {
			stoptime = System.currentTimeMillis() + 60L * 1000 * timeOut;
		}
        
		IOContainer input = getInput();
		for (this.currentIteration = 0; this.currentIteration < maxIterations; this.currentIteration++) {
			for (int j = 0; j < getNumberOfOperators(); j++) {
				input = getOperator(j).apply(input);
			}
			if ((timeOut >= 0) && (System.currentTimeMillis() > stoptime))
				break;
            inApplyLoop();
		}
        
		return input.getIOObjects();
	}

	/** Returns a simple chain condition. */
	public InnerOperatorCondition getInnerOperatorCondition() {
		return new SimpleChainInnerOperatorCondition();
	}

	/** Returns the maximum number of inner operators. */
	public int getMinNumberOfInnerOperators() {
		return 0;
	}

	/** Returns the minimum number of inner operators. */
	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	public Class[] getOutputClasses() {
	    return new Class[0];
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_ITERATIONS, "Number of iterations", 0, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_TIMEOUT, "Timeout in minutes (-1: no timeout)", -1, Integer.MAX_VALUE, -1);
		type.setExpert(true);
		types.add(type);
		return types;
	}
}
