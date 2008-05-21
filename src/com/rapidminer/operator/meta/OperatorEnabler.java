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
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SimpleChainInnerOperatorCondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInnerOperator;
import com.rapidminer.parameter.UndefinedParameterError;


/** This operator can be used to enable and disable other operators. The operator which should 
 *  be enabled or disabled must be a child operator of this one. Together with one of the
 *  parameter optimizing or iterating operators this operator can be used to dynamically change
 *  the process setup which might be useful in order to test different layouts, e.g. the
 *  gain by using different preprocessing steps.  
 * 
 * @author Ingo Mierswa
 * @version $Id: OperatorEnabler.java,v 1.6 2008/05/09 19:22:38 ingomierswa Exp $
 */
public class OperatorEnabler extends OperatorChain {

	/** The parameter name for &quot;The name of the operator which should be disabled or enabled&quot; */
	public static final String PARAMETER_OPERATOR_NAME = "operator_name";

	/** The parameter name for &quot;Indicates if the operator should be enabled (true) or disabled (false)&quot; */
	public static final String PARAMETER_ENABLE = "enable";
	
    public OperatorEnabler(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException {
        String operatorName = getParameterAsString(PARAMETER_OPERATOR_NAME);
        Operator operator = getProcess().getOperator(operatorName);
        if (operator == null)
            throw new UserError(this, 109, operatorName);
        operator.setEnabled(getParameterAsBoolean(PARAMETER_ENABLE));
        return super.apply();
    }

    /**
     * Returns true since this operator chain should just return the output of
     * the last inner operator.
     */
    public boolean shouldReturnInnerOutput() {
        return true;
    }

    /** Returns a simple chain condition. */
    public InnerOperatorCondition getInnerOperatorCondition() {
        try {
            String operatorName = getParameterAsString(PARAMETER_OPERATOR_NAME);
            if (operatorName != null) {
                Operator operator = getProcess().getOperator(operatorName);
                if (operator != null) {
                    operator.setEnabled(getParameterAsBoolean(PARAMETER_ENABLE));
                }
            }
        } catch (UndefinedParameterError e) {
        	// does nothing if parameter were not yet defined
        }
        return new SimpleChainInnerOperatorCondition();
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
        return 0;
    }
    
    /**
     * Since the apply methods of the inner operators already add additional
     * output, the handle additional output method should simply return a new
     * container which is build from the additional output objects. Therefore
     * this method returns true.
     */
    public boolean getAddOnlyAdditionalOutput() {
        return true;
    }

    public Class[] getInputClasses() {
        return new Class[0];
    }

    public Class[] getOutputClasses() {
        return new Class[0];
    }

    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeInnerOperator(PARAMETER_OPERATOR_NAME, "The name of the operator which should be disabled or enabled"));
        types.add(new ParameterTypeBoolean(PARAMETER_ENABLE, "Indicates if the operator should be enabled (true) or disabled (false)", false));
        return types;
    }
}
