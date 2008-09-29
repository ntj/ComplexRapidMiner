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
package com.rapidminer.operator.learner.meta;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.condition.AllInnerOperatorCondition;
import com.rapidminer.operator.condition.CombinedInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.CapabilityCheck;
import com.rapidminer.operator.learner.Learner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.tools.Tools;


/**
 * A <tt>MetaLearner</tt> is an operator that encapsulates one or more
 * learning steps to build its model. New meta learning schemes shoud extend
 * this class to support the same parameters as other learners. The main
 * purpose of this class is to perform some compatibility checks.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractMetaLearner.java,v 1.18 2006/04/05 08:57:26 ingomierswa
 *          Exp $
 */
public abstract class AbstractMetaLearner extends OperatorChain implements Learner {

	public AbstractMetaLearner(OperatorDescription description) {
		super(description);
	}

	/**
	 * Trains a model using an ExampleSet from the input. Uses the method
	 * learn(ExampleSet).
	 */
	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);

		// some checks
		if (exampleSet.getAttributes().getLabel() == null) {
			throw new UserError(this, 105, new Object[0]);
		}
		if (exampleSet.getAttributes().size() == 0) {
			throw new UserError(this, 106, new Object[0]);
		}

		// check capabilities and produce errors if they are not fullfilled
        CapabilityCheck check = new CapabilityCheck(this, Tools.booleanValue(System.getProperty(AbstractLearner.PROPERTY_RAPIDMINER_GENERAL_CAPABILITIES_WARN), true));
        check.checkLearnerCapabilities(this, exampleSet);

		List<IOObject> results = new LinkedList<IOObject>();
		Model model = learn(exampleSet);
		results.add(model);

		// weights must be calculated _after_ learning
		if (shouldCalculateWeights()) {
			AttributeWeights weights = getWeights(exampleSet);
			if (weights != null)
				results.add(weights);
		}

		if (shouldEstimatePerformance()) {
			PerformanceVector perfVector = getEstimatedPerformance();
			if (perfVector != null)
				results.add(perfVector);
		}

		IOObject[] resultArray = new IOObject[results.size()];
		results.toArray(resultArray);
		return resultArray;
	}

	/**
	 * This is a convenience method to apply the inner operators and return the
	 * model which must be output of the last operator.
	 */
	protected Model applyInnerLearner(ExampleSet exampleSet) throws OperatorException {
		IOContainer input = new IOContainer(new IOObject[] { exampleSet });
		for (int i = 0; i < getNumberOfOperators(); i++)
			input = getOperator(i).apply(input);
		return input.remove(Model.class);
	}

	public int getMinNumberOfInnerOperators() {
		return 1;
	}

	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		CombinedInnerOperatorCondition condition = new CombinedInnerOperatorCondition();
		condition.addCondition(new AllInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[] { Model.class }));
		if (shouldEstimatePerformance()) {
            condition.addCondition(new AllInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[] { PerformanceVector.class }));
		}
		if (shouldCalculateWeights()) {
            condition.addCondition(new AllInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[] { AttributeWeights.class }));
		}
		return condition;
	}

	/** Indicates that the consumption of example sets can be user defined. */
	public InputDescription getInputDescription(Class cls) {
		if (ExampleSet.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	/** Returns an array with one element: ExampleSet. */
	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	/**
	 * Returns true if the user wants to estimate the performance (depending on
	 * a parameter). In this case the method getEstimatedPerformance() must also
	 * be overriden and deliver the estimated performance. The default
	 * implementation returns false.
	 */
	public boolean shouldEstimatePerformance() {
		return false;
	}

	/**
	 * Returns true if the user wants to calculate feature weights (depending on
	 * a parameter). In this case the method getWeights() must also be overriden
	 * and deliver the calculated weights. The default implementation returns
	 * false.
	 */
	public boolean shouldCalculateWeights() {
		return false;
	}

	/** The default implementation throws an exception. */
	public PerformanceVector getEstimatedPerformance() throws OperatorException {
		throw new UserError(this, 912, getName(), "estimation of performance not supported.");
	}

	/**
	 * Returns the calculated weight vectors. The default implementation throws
	 * an exception.
	 */
	public AttributeWeights getWeights(ExampleSet exampleSet) throws OperatorException {
		throw new UserError(this, 916, getName(), "calculation of weights not supported.");
	}

	/**
	 * For all meta learners, it checks for the underlying operator to see which
	 * capabilities are supported by them.
	 */
	public boolean supportsCapability(LearnerCapability capability) {
		if (getNumberOfOperators() == 0)
			return false;
		for (int i = 0; i < getNumberOfOperators(); i++) {
			if (getOperator(i) instanceof Learner) {
				return ((Learner) getOperator(i)).supportsCapability(capability);
			}
		}
		return true;
	}

	/**
	 * Depending on the the learner capabilities (performance estimation,
	 * attribute weight calculation) the output classes are generated.
	 */
	public Class<?>[] getOutputClasses() {
		List<Class> classList = new LinkedList<Class>();
		classList.add(Model.class);
		if (shouldEstimatePerformance())
			classList.add(PerformanceVector.class);
		if (shouldCalculateWeights())
			classList.add(AttributeWeights.class);
		Class[] result = new Class[classList.size()];
		classList.toArray(result);
		return result;
	}
}
