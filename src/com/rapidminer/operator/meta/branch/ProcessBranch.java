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
package com.rapidminer.operator.meta.branch;

import java.util.List;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SimpleChainInnerOperatorCondition;
import com.rapidminer.operator.preprocessing.filter.ExampleFilter;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.tools.Tools;

/**
 * <p>This operator provides a conditional execution of parts of processes.
 * It has to have two OperatorChains as childs. The first chain is processed
 * if the specified condition is true, the second one is processed if it is false
 * (if-then-else). The second chain may be omitted (if-then). In this case, this
 * operator has only one inner operator.</p>
 * 
 * <p>
 * If the condition &quot;attribute_value_filter&quot; is used, the same attribute
 * value conditions already known from the {@link ExampleFilter} operator can be used.
 * In addition to the known attribute value relation format (e.g. &quot;att1&gt;=0.7&quot;),
 * this operator expects an additional definition for the used example which cam be added in 
 * &quot;[&quot; and &quot;]&quot; after the attribute value condition. The following values
 * are possible:
 * <ul>
 * <li>a fixed number, e.g. &quot;att1&gt;0.7 [7]&quot; meaning that the value for attribute 
 *     &quot;att1&quot; for the example 7 must be greater than 0.7</li>
 * <li>the wildcard &quot;*&quot; meaning that the attribute value condition must be
 *     fulfilled for all examples, e.g. &quot;att4&lt;=5 [*]&quot;</li>
 * <li>no example definition, meaning the same as the wildcard definition [*]</li>
 * </ul>
 * </p>
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: ProcessBranch.java,v 1.10 2008/05/09 22:13:12 ingomierswa Exp $
 */
public class ProcessBranch extends OperatorChain {
	
	public static final String PARAMETER_CONDITION_TYPE = "condition_type";

	public static final String PARAMETER_CONDITION_VALUE = "condition_value";

	public static final String[] CONDITION_NAMES = { 
		"file_exists",
		"min_fitness",
		"max_fitness",
		"min_performance_value",
		"max_performance_value",
		"attribute_value_filter"
	};
	
	public static final Class[]  CONDITION_CLASSES = { 
		FileExistsCondition.class,
		MinFitnessCondition.class,
		MaxFitnessCondition.class,
		MinPerformanceValueCondition.class,
		MaxPerformanceValueCondition.class,
		DataValueCondition.class
	};
	
	
	public ProcessBranch(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		IOContainer input = getInput();
		
		// creating condition
		Class conditionClass = null;
		String selectedConditionName = "";
		
		try {
			selectedConditionName = getParameterAsString(PARAMETER_CONDITION_TYPE);
			for (int i = 0; i < CONDITION_NAMES.length; i++) {
				if (selectedConditionName.toLowerCase().equals(CONDITION_NAMES[i].toLowerCase())) {
					conditionClass = CONDITION_CLASSES[i];
					break;
				}
			}
			
			if (conditionClass == null) {
				try {
					conditionClass = Tools.classForName(selectedConditionName);
				} catch (ClassNotFoundException e) {
					throw new UserError(this, e, 904, new Object[] { selectedConditionName, e });
				}
			}
			ProcessBranchCondition condition = null;
			try {
				condition = (ProcessBranchCondition) conditionClass.newInstance();
			} catch (InstantiationException e) {
				throw new UserError(this, e, 904, new Object[] { selectedConditionName, e });
			} catch (IllegalAccessException e) {
				throw new UserError(this, e, 904, new Object[] { selectedConditionName, e });
			}
	
			if (condition != null) {
				// checking condition
				boolean conditionState = condition.check(this, getParameterAsString(PARAMETER_CONDITION_VALUE));
	
				// execute
				if (conditionState) {
					return getOperator(0).apply(input).getIOObjects();
				} else {
					if (getNumberOfOperators() > 1) {
						return getOperator(1).apply(input).getIOObjects();
					} else {
						return input.getIOObjects();
					}
				}
			} else {
				return input.getIOObjects(); 
			}
		} catch (Exception e) {
			throw new UserError(this, e, 904, new Object[] {selectedConditionName, e });
		}
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return new Class[0];
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		return new SimpleChainInnerOperatorCondition();
	}

	public int getMaxNumberOfInnerOperators() {
		return 2;
	}

	public int getMinNumberOfInnerOperators() {
		return 1;
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeStringCategory(PARAMETER_CONDITION_TYPE, "The condition which is used for the condition check.", CONDITION_NAMES));
		types.add(new ParameterTypeString(PARAMETER_CONDITION_VALUE, "A condition parameter which might be desired for some condition checks.", true));
		return types;
	}
}
