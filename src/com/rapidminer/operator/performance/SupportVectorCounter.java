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

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.learner.functions.kernel.KernelModel;
import com.rapidminer.operator.learner.functions.kernel.SupportVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;


/**
 * Returns a performance vector just counting the number of support vectors 
 * of a given support vector based model (kernel model). Please note that
 * this operator will try to derive the number of support vectors of the
 * first delivered model and might fail on this task if no appropriate
 * kernel based model is delivered. Currently, at least the models delivered 
 * by the operator JMySVM, MyKLR, LibSVM, GPLearner, KernelLogisticRegression,
 * RVM, and the EvoSVM should be supported. 
 * 
 * @author Ingo Mierswa
 * @version $Id: SupportVectorCounter.java,v 1.1 2008/08/25 08:10:40 ingomierswa Exp $
 */
public class SupportVectorCounter extends Operator {

	/** The parameter name for &quot;Indicates if the fitness should for maximal or minimal number of features.&quot; */
	public static final String PARAMETER_OPTIMIZATION_DIRECTION = "optimization_direction";
	
    private double lastCount = Double.NaN;
    
	public SupportVectorCounter(OperatorDescription description) {
		super(description);
        addValue(new ValueDouble("support_vectors", "The number of the currently used support vectors.") {
            public double getDoubleValue() {
                return lastCount; 
            }
        });
	}

	/**
	 * Creates a new performance vector if the given one is null. Adds a new estimated criterion. 
	 * If the criterion was already part of the performance vector before it will be overwritten.
	 */
	private PerformanceVector count(KernelModel model, PerformanceVector performanceCriteria) throws OperatorException {
		if (performanceCriteria == null)
			performanceCriteria = new PerformanceVector();

		this.lastCount = 0;
		int svNumber = model.getNumberOfSupportVectors();
		for (int i = 0; i < svNumber; i++) {
			SupportVector sv = model.getSupportVector(i);
			if (Math.abs(sv.getAlpha()) > 0.0d)
				this.lastCount++;
		}
		EstimatedPerformance svCriterion = new EstimatedPerformance("number_of_support_vectors", lastCount, 1, getParameterAsInt(PARAMETER_OPTIMIZATION_DIRECTION) == MDLCriterion.MINIMIZATION);
		performanceCriteria.addCriterion(svCriterion);
		return performanceCriteria;
	}

	public IOObject[] apply() throws OperatorException {
		Model model = getInput(Model.class);
		if (!(model instanceof KernelModel)) {
			throw new UserError(this, 122, "'support vector based model (kernel model)'");
		}
		
		PerformanceVector inputPerformance = null;
		try {
			inputPerformance = getInput(PerformanceVector.class);
		} catch (MissingIOObjectException e) {
			// tries to use input performance if available
			// no problem if none is given --> create new 
		}
		
		PerformanceVector performance = count((KernelModel)model, inputPerformance);
		return new IOObject[] { performance };
	}

	/** Shows a parameter keep_example_set with default value &quot;false&quot. */
	public InputDescription getInputDescription(Class cls) {
		if (Model.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { Model.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { PerformanceVector.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeCategory(PARAMETER_OPTIMIZATION_DIRECTION, "Indicates if the fitness should be maximal for the maximal or the minimal number of support vectors.", MDLCriterion.DIRECTIONS, MDLCriterion.MINIMIZATION));
		return types;
	}
}
