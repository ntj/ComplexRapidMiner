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
package com.rapidminer.operator.performance;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;


/**
 * Returns a performance vector just counting the number of attributes currently
 * used for the given example set.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeCounter.java,v 1.5 2008/05/09 19:22:43 ingomierswa Exp $
 */
public class AttributeCounter extends Operator {


	/** The parameter name for &quot;Indicates if the fitness should for maximal or minimal number of features.&quot; */
	public static final String PARAMETER_OPTIMIZATION_DIRECTION = "optimization_direction";
    private double lastCount = Double.NaN;
    
	public AttributeCounter(OperatorDescription description) {
		super(description);
        addValue(new Value("attributes", "The currently selected number of attributes.") {
            public double getValue() {
                return lastCount; 
            }
        });
	}

	/**
	 * Creates a new performance vector if the given one is null. Adds a MDL
	 * criterion. If the criterion was already part of the performance vector
	 * before it will be overwritten.
	 */
	private PerformanceVector count(ExampleSet exampleSet, PerformanceVector performanceCriteria) throws OperatorException {
		if (performanceCriteria == null)
			performanceCriteria = new PerformanceVector();

		MDLCriterion mdlCriterion = new MDLCriterion(getParameterAsInt(PARAMETER_OPTIMIZATION_DIRECTION));
		mdlCriterion.startCounting(exampleSet, true);
        this.lastCount = mdlCriterion.getAverage();
		performanceCriteria.addCriterion(mdlCriterion);
		return performanceCriteria;
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		PerformanceVector inputPerformance = null;
		try {
			inputPerformance = getInput(PerformanceVector.class);
		} catch (MissingIOObjectException e) {
			// tries to use input performance if available
			// no problem if none is given --> create new 
		}
		PerformanceVector performance = count(exampleSet, inputPerformance);
		return new IOObject[] { performance };
	}

	/** Shows a parameter keep_example_set with default value &quot;false&quot. */
	public InputDescription getInputDescription(Class cls) {
		if (ExampleSet.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { PerformanceVector.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeCategory(PARAMETER_OPTIMIZATION_DIRECTION, "Indicates if the fitness should for maximal or minimal number of features.", MDLCriterion.DIRECTIONS, MDLCriterion.MINIMIZATION));
		return types;
	}
}
