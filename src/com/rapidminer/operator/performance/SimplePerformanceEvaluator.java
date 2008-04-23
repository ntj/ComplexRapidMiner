/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.performance;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.UndefinedParameterError;

/**
 * <p>In contrast to the other performance evaluation methods, this performance 
 * evaluator operator can be used for all types of learning tasks. It will 
 * automatically determine the learning task type and will calculate the most
 * common criteria for this type. For more sophisticated performance calculations,
 * you should check the operators {@link RegressionPerformanceEvaluator},
 * {@link PolynominalClassificationPerformanceEvaluator}, or
 * {@link BinominalClassificationPerformanceEvaluator}. You can even
 * simply write your own performance measure and calculate it with the
 * operator {@link UserBasedPerformanceEvaluator}.</p>
 * 
 * <p>The operator expects a test {@link ExampleSet}
 * as input, whose elements have both true and predicted labels, and delivers as
 * output a list of most commmon performance values for the provided learning
 * task type (regression of (binominal) classification. If an input performance 
 * vector was already given, this is used for keeping the performance values.</p> 
 * 
 * @author Ingo Mierswa
 * @version $Id: SimplePerformanceEvaluator.java,v 1.2 2007/07/15 19:04:34 ingomierswa Exp $
 */
public class SimplePerformanceEvaluator extends AbstractPerformanceEvaluator {

	private ExampleSet testSet = null;
	
	public SimplePerformanceEvaluator(OperatorDescription description) {
		super(description);
	}

	/** Does nothing. */
	protected void checkCompatibility(ExampleSet exampleSet) throws OperatorException {}

	/** Returns null. */
	protected double[] getClassWeights(Attribute label) throws UndefinedParameterError {
		return null;
	}

	/** Uses this example set in order to create appropriate criteria. */
	protected void init(ExampleSet testSet) {
		this.testSet = testSet;
	}
	
	/** Returns false. */
	protected boolean showSkipNaNLabelsParameter() {
		return false;
	}

	/** Returns false. */
	protected boolean showComparatorParameter() {
		return false;
	}
	
	public List<PerformanceCriterion> getCriteria() {
		List<PerformanceCriterion> allCriteria = new LinkedList<PerformanceCriterion>();
		if (this.testSet != null) {
			Attribute label = this.testSet.getAttributes().getLabel();
			if (label.isNominal()) {
				if (label.getMapping().size() == 2) {
					// add most important binominal classification criteria
					allCriteria.add(new MultiClassificationPerformance(MultiClassificationPerformance.ACCURACY));
					allCriteria.add(new BinaryClassificationPerformance(BinaryClassificationPerformance.PRECISION));
					allCriteria.add(new BinaryClassificationPerformance(BinaryClassificationPerformance.RECALL));
					allCriteria.add(new AreaUnderCurve());
				} else {
					// add most important polynominal classification criteria
					allCriteria.add(new MultiClassificationPerformance(MultiClassificationPerformance.ACCURACY));
					allCriteria.add(new MultiClassificationPerformance(MultiClassificationPerformance.KAPPA));
				}
			} else {
				// add most important regression criteria
				allCriteria.add(new RootMeanSquaredError());
				allCriteria.add(new SquaredError());
			}
		}
		this.testSet = null;
		return allCriteria;
	}
}
