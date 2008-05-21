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

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.condition.AllInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;


/** This operator can be used to employ a single inner operator or operator chain.
 *  Which operator should be used can be defined by the parameter &quot;select_which&quot;.
 *  Together with one of the parameter optimizing or iterating operators this operator 
 *  can be used to dynamically change the process setup which might be useful in order to 
 *  test different layouts, e.g. the gain by using different preprocessing steps or chains
 *  or the quality of a certain learner.  
 * 
 * @author Ingo Mierswa
 * @version $Id: OperatorSelector.java,v 1.3 2008/05/09 22:13:12 ingomierswa Exp $
 */
public class OperatorSelector extends OperatorChain {

	/** The parameter name for &quot;Indicates if the operator which inner operator should be used&quot;. */
	public static final String PARAMETER_SELECT_WHICH = "select_which";
	
    public OperatorSelector(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException {
    	int operatorIndex = getParameterAsInt(PARAMETER_SELECT_WHICH);
    	if ((operatorIndex < 1) || (operatorIndex > getNumberOfOperators())) {
    		throw new UserError(this, 207, new Object[] { operatorIndex, PARAMETER_SELECT_WHICH, "must be between 1 and the number of inner operators."} );
    	}
    	
        Operator operator = getOperator(operatorIndex - 1);
        return operator.apply(getInput()).getIOObjects();
    }

    /** Returns a simple chain condition. */
    public InnerOperatorCondition getInnerOperatorCondition() {
    	try {
    		int operatorIndex = getParameterAsInt(PARAMETER_SELECT_WHICH);
    		if ((operatorIndex < 1) || (operatorIndex > getNumberOfOperators())) {
    			return new AllInnerOperatorCondition(new Class[0], new Class[0]);
    		} else {
    			Operator operator = getOperator(operatorIndex - 1);
    			return new AllInnerOperatorCondition(operator.getInputClasses(), operator.getOutputClasses());
    		}
    	} catch (UndefinedParameterError e) {
			return new AllInnerOperatorCondition(new Class[0], new Class[0]);
    	}
    }

    /**
     * Returns the highest possible value for the maximum number of inner
     * operators.
     */
    public int getMaxNumberOfInnerOperators() {
        return Integer.MAX_VALUE;
    }

    /** Returns 0 for the minimum number of inner operators. */
    public int getMinNumberOfInnerOperators() {
        return 1;
    }

    public Class[] getInputClasses() {
    	try {
    		int operatorIndex = getParameterAsInt(PARAMETER_SELECT_WHICH);
    		if ((operatorIndex < 1) || (operatorIndex > getNumberOfOperators())) {
    			return new Class[0];
    		} else {
    			Operator operator = getOperator(operatorIndex - 1);
    			return operator.getInputClasses();
    		}
    	} catch (Exception e) {
    		return new Class[0];
    	}
    }

    public Class[] getOutputClasses() {
    	try {
    		int operatorIndex = getParameterAsInt(PARAMETER_SELECT_WHICH);
    		if ((operatorIndex < 1) || (operatorIndex > getNumberOfOperators())) {
    			return new Class[0];
    		} else {
    			Operator operator = getOperator(operatorIndex - 1);
    			return operator.getOutputClasses();
    		}
    	} catch (Exception e) {
    		return new Class[0];
    	}
    }

    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeInt(PARAMETER_SELECT_WHICH, "Indicates which inner operator should be currently employed by this operator on the input objects.", 1, Integer.MAX_VALUE, 1);
        type.setExpert(false);
        types.add(type);
        return types;
    }
}
