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
package com.rapidminer.operator.learner.functions.kernel.hyperhyper;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;

/**
 * This is a minimal SVM implementation. The model is built with only one positive
 * and one negative example. Typically this operater is used in combination with a
 * boosting method.
 * 
 * @author Regina Fritsch
 * @version $Id: HyperHyper.java,v 1.4 2008/05/09 19:23:26 ingomierswa Exp $
 */
public class HyperHyper extends AbstractLearner {
	
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";

	public HyperHyper(OperatorDescription description) {
		super(description);
	}
	
	public Model learn(ExampleSet exampleSet) throws OperatorException {
		return createModel(exampleSet);
	}
	
	
	private Model createModel(ExampleSet exampleSet) throws OperatorException {
		// if no weights available, initialize weights
		if (exampleSet.getAttributes().getWeight() == null) {
			com.rapidminer.example.Tools.createWeightAttribute(exampleSet);
		}
		
		double weightSum = 0;
		for (Example e : exampleSet) {
			weightSum += e.getWeight();
		}
		
		Attribute label = exampleSet.getAttributes().getLabel();
		
		Example x1 = this.rejectionSampling(exampleSet, weightSum);	
		Example x2 = null;
		int tries = 0;
		do {
			x2 = this.rejectionSampling(exampleSet, weightSum);
			tries += 1;
			
			// if one class is much smaller in the exampleSet, split it up from the rest
			if (tries >= 10) {
				Vector<Example> examplesWithWantedLabel = new Vector<Example>();
				for (Example ex : exampleSet) {
					if (ex.getValue(label) != x1.getValue(label)) {
						examplesWithWantedLabel.add(ex);
					}
				}
				Random random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
				boolean doSampling = true;
				while (doSampling == true) {
					int index = random.nextInt(examplesWithWantedLabel.size());
					if (random.nextDouble() < examplesWithWantedLabel.get(index).getWeight() / weightSum ) {
						x2 = examplesWithWantedLabel.get(index);
						doSampling = false;
					}
				}								
			}
			
		} while (x1.getValue(label) == x2.getValue(label));
		
		
		// compute w	
		double[] w = new double[x1.getAttributes().size()];
		int i = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			w[i] = x1.getValue(attribute) - x2.getValue(attribute);
			i++;
		}
		
		// compute b
		double bx1 = 0;
		double bx2 = 0;
		i = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			bx1 += x1.getValue(attribute) * w[i];
			bx2 += x2.getValue(attribute) * w[i];
			i++;
		}
		double b = (bx1 + bx2) * -0.5;
		
		double[] x1Values = new double[exampleSet.getAttributes().size()];
		int counter = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			x1Values[counter++] = x1.getValue(attribute);
		}
		
		double[] x2Values = new double[exampleSet.getAttributes().size()];
		counter = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			x1Values[counter++] = x2.getValue(attribute);
		}
		
		return new HyperModel(exampleSet, b, w, x1Values, x2Values);
	}
	
	private Example rejectionSampling(ExampleSet exampleSet, double weightSum) throws OperatorException {
		Example example = null;
		Random random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
		boolean doSampling = true;
		while (doSampling == true) {
			int index = random.nextInt(exampleSet.size());
			if (random.nextDouble() < exampleSet.getExample(index).getWeight() / weightSum ) {
				example = exampleSet.getExample(index);
				doSampling = false;
			}
		}
		return example;	
	}
	
	public boolean supportsCapability(LearnerCapability capability) {
		if (capability == LearnerCapability.NUMERICAL_ATTRIBUTES)
			return true;
		if (capability == LearnerCapability.BINOMINAL_CLASS)
			return true;
		if (capability == LearnerCapability.NUMERICAL_CLASS)
			return true;
		if (capability == LearnerCapability.WEIGHTED_EXAMPLES)
			return true;
		return false;
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "The local random seed (-1: use global random seed)", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
