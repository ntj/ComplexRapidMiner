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
package com.rapidminer.operator.validation;

import java.util.BitSet;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.tools.WekaInstancesAdaptor;
import com.rapidminer.tools.WekaTools;

import weka.attributeSelection.ConsistencySubsetEval;
import weka.core.Instances;

/**
 * <p>
 * Consistency attribute subset evaluator. For more information see: <br/> Liu,
 * H., and Setiono, R., (1996). A probabilistic approach to feature selection -
 * A filter solution. In 13th International Conference on Machine Learning
 * (ICML'96), July 1996, pp. 319-327. Bari, Italy.
 * </p>
 * 
 * <p>
 * This operator evaluates the worth of a subset of attributes by the level of
 * consistency in the class values when the training instances are projected
 * onto the subset of attributes. Consistency of any subset can never be lower
 * than that of the full set of attributes, hence the usual practice is to use
 * this subset evaluator in conjunction with a Random or Exhaustive search which
 * looks for the smallest subset with consistency equal to that of the full set
 * of attributes.
 * </p>
 * 
 * <p>
 * This operator can only be applied for classification data sets, i.e. where
 * the label attribute is nominal.
 * </p>
 * 
 * @author Ingo Mierswa
 * @version $Id: ConsistencyFeatureSetEvaluator.java,v 1.4 2006/04/05 09:42:02
 *          ingomierswa Exp $
 */
public class ConsistencyFeatureSetEvaluator extends Operator {

	public ConsistencyFeatureSetEvaluator(OperatorDescription description) {
		super(description);
	}

	/** Shows a parameter keep_example_set with default value &quot;false&quot. */
	public InputDescription getInputDescription(Class cls) {
		if (ExampleSet.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		Instances instances = WekaTools.toWekaInstances(exampleSet, "TempInstances", WekaInstancesAdaptor.LEARNING);
		double performance = 0.0d;
		try {
			ConsistencySubsetEval evaluator = new ConsistencySubsetEval();
			evaluator.buildEvaluator(instances);
			BitSet bitSet = new BitSet(exampleSet.getAttributes().size());
			bitSet.flip(0, exampleSet.getAttributes().size());
			performance = evaluator.evaluateSubset(bitSet);
		} catch (Exception e) {
			throw new UserError(this, e, 905, new Object[] { "ConsistencySubsetEval", e.getMessage() });
		}
		PerformanceVector result = new PerformanceVector();
		result.addCriterion(new EstimatedPerformance("ConsistencyFS", performance, 1, false));
		return new IOObject[] { result };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { PerformanceVector.class };
	}
}
