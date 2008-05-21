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

import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instances;

/**
 * <p>
 * CFS attribute subset evaluator. For more information see: <br/> Hall, M. A.
 * (1998). Correlation-based Feature Subset Selection for Machine Learning.
 * Thesis submitted in partial fulfilment of the requirements of the degree of
 * Doctor of Philosophy at the University of Waikato.
 * </p>
 * 
 * <p>
 * This operator creates a filter based performance measure for a feature
 * subset. It evaluates the worth of a subset of attributes by considering the
 * individual predictive ability of each feature along with the degree of
 * redundancy between them. Subsets of features that are highly correlated with
 * the class while having low intercorrelation are preferred.
 * </p>
 * 
 * <p>
 * This operator can be applied on both numerical and nominal data sets.
 * </p>
 * 
 * @author Ingo Mierswa
 * @version $Id: CFSFeatureSetEvaluator.java,v 1.4 2006/04/05 09:42:02
 *          ingomierswa Exp $
 */
public class CFSFeatureSetEvaluator extends Operator {

	public CFSFeatureSetEvaluator(OperatorDescription description) {
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
			CfsSubsetEval evaluator = new CfsSubsetEval();
			evaluator.buildEvaluator(instances);
			BitSet bitSet = new BitSet(exampleSet.getAttributes().size());
			bitSet.flip(0, exampleSet.getAttributes().size());
			performance = evaluator.evaluateSubset(bitSet);
		} catch (Exception e) {
			throw new UserError(this, e, 905, new Object[] { "CfsSubsetEval", e.getMessage() });
		}
		PerformanceVector result = new PerformanceVector();
		result.addCriterion(new EstimatedPerformance("CorrelationFS", performance, 1, false));
		return new IOObject[] { result };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { PerformanceVector.class };
	}
}
