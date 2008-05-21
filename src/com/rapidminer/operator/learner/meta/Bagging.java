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

import java.util.List;
import java.util.Vector;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * This Bagging implementation can be used with all learners available in RapidMiner, not only
 * the ones which originally are part of the Weka package.
 * 
 * @author Martin Scholz
 * @version $Id: Bagging.java,v 1.7 2008/05/09 19:22:47 ingomierswa Exp $
 */
public class Bagging extends AbstractMetaLearner {

	/**
	 * Name of the variable specifying the maximal number of iterations of the
	 * learner.
	 */
	public static final String PARAMETER_ITERATIONS = "iterations";

	/** Name of the flag indicating internal bootstrapping. */
	public static final String PARAMETER_SAMPLE_RATIO = "sample_ratio";

	/** Name of the flag indicating internal bootstrapping. */
	public static final String PARAMETER_AVERAGE_CONFIDENCES = "average_confidences";

	// field for visualizing performance
	protected int currentIteration;

	/** Constructor. */
	public Bagging(OperatorDescription description) {
		super(description);
		addValue(new Value("iteration", "The current iteration.") {
			public double getValue() {
				return currentIteration;
			}
		});
	}

	/**
	 * Overrides the method of the super class.
	 */
	public boolean supportsCapability(LearnerCapability lc) {
		if (lc == LearnerCapability.NUMERICAL_CLASS)
			return false;
		else
			return super.supportsCapability(lc);
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeDouble(PARAMETER_SAMPLE_RATIO, "Fraction of examples used for training. Must be greater than 0 and should be lower than 1.", 0, 1, 0.9)); 
		types.add(new ParameterTypeInt(PARAMETER_ITERATIONS, "The number of iterations (base models).", 1, Integer.MAX_VALUE, 10)); 
		types.add(new ParameterTypeBoolean(PARAMETER_AVERAGE_CONFIDENCES, "Specifies whether to average available prediction confidences or not.", true)); 
		return types;
	}

	/**
	 * Constructs a <code>Model</code> repeatedly running a base learner on subsamples.
	 */
	public Model learn(ExampleSet exampleSet) throws OperatorException {
		final double splitRatio = this.getParameterAsDouble(PARAMETER_SAMPLE_RATIO);
		final int numInterations = this.getParameterAsInt(PARAMETER_ITERATIONS);

		Vector<Model> modelList = new Vector<Model>();
		for (this.currentIteration = 0; this.currentIteration < numInterations; this.currentIteration++) {
			SplittedExampleSet splitted = new SplittedExampleSet(exampleSet, splitRatio, SplittedExampleSet.SHUFFLED_SAMPLING, -1);
			splitted.selectSingleSubset(0);
			modelList.add(applyInnerLearner(splitted));
			inApplyLoop();
		}

		if (this.getParameterAsBoolean(PARAMETER_AVERAGE_CONFIDENCES)) {
			return new BaggingModel(exampleSet, modelList);
		} else {
			List<Double> weights = new Vector<Double>();
			for (int i=0; i<modelList.size(); i++) {
				weights.add(1.0d);
			}
			return new AdaBoostModel(exampleSet, modelList, weights);
		}
	}

}
