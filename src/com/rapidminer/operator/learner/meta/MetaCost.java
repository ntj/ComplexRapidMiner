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
import java.util.Random;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.MappedExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeMatrix;
import com.rapidminer.tools.RandomGenerator;


/**
 * This operator uses a given cost matrix to compute label predictions
 * according to classification costs. The method used by this operator
 * is similar to MetaCost as described by Pedro Domingos.  
 *  
 * @author Helge Homburg
 * @version $Id: MetaCost.java,v 1.8 2008/05/09 19:22:48 ingomierswa Exp $
 */
public class MetaCost extends AbstractMetaLearner {

	/** The parameter name for &quot;The cost matrix in Matlab single line format&quot; */
	public static final String PARAMETER_COST_MATRIX = "cost_matrix";

	/** The parameter name for &quot;File&quot; */
	public static final String PARAMETER_COST_MATRIX_FILE_LOCATION = "cost_matrix_file_location";

	/** The parameter name for &quot;Fraction of examples used for training. Must be greater than 0 and should be lower than 1.&quot; */
	public static final String PARAMETER_USE_SUBSET_FOR_TRAINING = "use_subset_for_training";

	/** The parameter name for &quot;The number of iterations (base models).&quot; */
	public static final String PARAMETER_ITERATIONS = "iterations";

	/** The parameter name for &quot;Use sampling with replacement (true) or without (false)&quot; */
	public static final String PARAMETER_SAMPLING_WITH_REPLACEMENT = "sampling_with_replacement";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	
	public MetaCost(OperatorDescription description) {
		super(description);
	}		
	
	public Model learn(ExampleSet inputSet) throws OperatorException {
		int iterations = getParameterAsInt(PARAMETER_ITERATIONS);
		double subsetRatio = getParameterAsDouble(PARAMETER_USE_SUBSET_FOR_TRAINING);
		Model[] models = new Model[iterations]; 
		
		//get cost matrix
		double[][] costMatrix = getParameterAsMatrix(PARAMETER_COST_MATRIX);		
		
		//perform bagging operation			
		if (getParameterAsBoolean(PARAMETER_SAMPLING_WITH_REPLACEMENT)) {	
			//sampling with replacement
			int randomSeed = getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED);
			Random randomGenerator = RandomGenerator.getRandomGenerator(randomSeed);	
			int size = (int)(inputSet.size()*subsetRatio);		
			for (int i = 0; i < iterations; i++) {		
				ExampleSet exampleSet = (ExampleSet)inputSet.clone();
				int[] mapping = MappedExampleSet.createBootstrappingMapping(exampleSet, size, randomGenerator);
				MappedExampleSet currentSampleSet = new MappedExampleSet(exampleSet, mapping);
				models[i] = applyInnerLearner(currentSampleSet);				
				inApplyLoop();
			}
		} else {
			//sampling without replacement
			for (int i = 0; i < iterations; i++) {			
				SplittedExampleSet splitted = new SplittedExampleSet((ExampleSet)inputSet.clone(), subsetRatio, SplittedExampleSet.SHUFFLED_SAMPLING, -1);
				splitted.selectSingleSubset(0);
				models[i] = applyInnerLearner(splitted);
				inApplyLoop();
			}
		}
		
		return new MetaCostModel(inputSet, models, costMatrix);
	}

	/**
	 * Support polynominal labels. For all other capabilities, it checks for the underlying 
	 * operator to see which capabilities are supported by them.
	 */
	public boolean supportsCapability(LearnerCapability capability) {
		if (getNumberOfOperators() == 0)
			return false;
		if (capability == LearnerCapability.POLYNOMINAL_CLASS)
			return true;
		if (capability == LearnerCapability.BINOMINAL_CLASS)
			return true;
		return super.supportsCapability(capability);
	}
	
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();		
		types.add(new ParameterTypeMatrix(PARAMETER_COST_MATRIX, "The cost matrix in Matlab single line format", true, false));
		//types.add(new ParameterTypeFile(PARAMETER_COST_MATRIX_FILE_LOCATION,"File",null,true));
		types.add(new ParameterTypeDouble(PARAMETER_USE_SUBSET_FOR_TRAINING, "Fraction of examples used for training. Must be greater than 0 and should be lower than 1.", 0, 1, 1.0));
		types.add(new ParameterTypeInt(PARAMETER_ITERATIONS, "The number of iterations (base models).", 1, Integer.MAX_VALUE, 10));
		types.add(new ParameterTypeBoolean(PARAMETER_SAMPLING_WITH_REPLACEMENT, "Use sampling with replacement (true) or without (false)", true));
		types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
