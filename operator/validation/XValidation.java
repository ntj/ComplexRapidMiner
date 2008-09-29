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

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.visualization.ProcessLogOperator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import com.rapidminer.tools.math.AverageVector;


/**
 * <p>
 * <code>XValidation</code> encapsulates a cross-validation process. The
 * example set {@rapidminer.math S} is split up into <var> number_of_validations</var>
 * subsets {@rapidminer.math S_i}. The inner operators are applied
 * <var>number_of_validations</var> times using {@rapidminer.math S_i} as the test
 * set (input of the second inner operator) and {@rapidminer.math S\backslash S_i}
 * training set (input of the first inner operator).
 * </p>
 * 
 * <p>
 * The first inner operator must accept an
 * {@link com.rapidminer.example.ExampleSet} while the second must accept an
 * {@link com.rapidminer.example.ExampleSet} and the output of the first (which
 * is in most cases a {@link com.rapidminer.operator.Model}) and must produce
 * a {@link com.rapidminer.operator.performance.PerformanceVector}.
 * </p>
 * 
 * <p>
 * Like other validation schemes the RapidMiner cross validation can use several types
 * of sampling for building the subsets. Linear sampling simply divides the
 * example set into partitions without changing the order of the examples.
 * Shuffled sampling build random subsets from the data. Stratifed sampling
 * builds random subsets and ensures that the class distribution in the subsets
 * is the same as in the whole example set.
 * </p>
 * 
 * <p>The cross validation operator provides several values which can be logged 
 * by means of a {@link ProcessLogOperator}. Of course the number of the current
 * iteration can be logged which might be useful for ProcessLog operators wrapped
 * inside a cross validation. Beside that, all performance estimation operators
 * of RapidMiner provide access to the average values calculated during the estimation.
 * Since the operator cannot ensure the names of the delivered criteria, the 
 * ProcessLog operator can access the values via the generic value names:</p>
 * <ul>
 * <li>performance: the value for the main criterion calculated by this validation operator</li>
 * <li>performance1: the value of the first criterion of the performance vector calculated</li>
 * <li>performance2: the value of the second criterion of the performance vector calculated</li>
 * <li>performance3: the value of the third criterion of the performance vector calculated</li>
 * <li>for the main criterion, also the variance and the standard deviation can be 
 *     accessed where applicable.</li>
 * </ul>
 * 
 * @rapidminer.index cross-validation
 * @author Ingo Mierswa
 * @version $Id: XValidation.java,v 1.11 2008/08/25 08:10:35 ingomierswa Exp $
 */
public class XValidation extends ValidationChain {

	/** The parameter name for &quot;Number of subsets for the crossvalidation.&quot; */
	public static final String PARAMETER_NUMBER_OF_VALIDATIONS = "number_of_validations";

	/** The parameter name for &quot;Set the number of validations to the number of examples. If set to true, number_of_validations is ignored&quot; */
	public static final String PARAMETER_LEAVE_ONE_OUT = "leave_one_out";

	/** The parameter name for &quot;Defines the sampling type of the cross validation (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)&quot; */
	public static final String PARAMETER_SAMPLING_TYPE = "sampling_type";

	/** The parameter name for &quot;Indicates if only performance vectors should be averaged or all types of averagable result vectors&quot; */
	public static final String PARAMETER_AVERAGE_PERFORMANCES_ONLY = "average_performances_only";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	private int iteration;

	public XValidation(OperatorDescription description) {
		super(description);
		addValue(new ValueDouble("iteration", "The number of the current iteration.") {
			public double getDoubleValue() {
				return iteration;
			}
		});
	}
	
	public IOObject[] estimatePerformance(ExampleSet inputSet) throws OperatorException {
        int number;
		if (getParameterAsBoolean(PARAMETER_LEAVE_ONE_OUT)) {
			number = inputSet.size();
		} else {
			number = getParameterAsInt(PARAMETER_NUMBER_OF_VALIDATIONS);
		}
		log("Starting " + number + "-fold cross validation");

		// Split training / test set
		int samplingType = getParameterAsInt(PARAMETER_SAMPLING_TYPE);
        int randomSeed = getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED);
		SplittedExampleSet splittedES = new SplittedExampleSet(inputSet, number, samplingType, randomSeed);

		// start crossvalidation
		List<AverageVector> averageVectors = new ArrayList<AverageVector>();
		for (iteration = 0; iteration < number; iteration++) {
			
			splittedES.selectAllSubsetsBut(iteration);
			learn(splittedES);
			
			splittedES.selectSingleSubset(iteration);
			IOContainer evalOutput = evaluate(splittedES);
			
			Tools.handleAverages(evalOutput, averageVectors, getParameterAsBoolean(PARAMETER_AVERAGE_PERFORMANCES_ONLY));
			inApplyLoop();
		}
		// end crossvalidation

		// set last result for plotting purposes. This is an average value and
		// actually not the last performance value!
		PerformanceVector averagePerformance = Tools.getPerformanceVector(averageVectors);
		if (averagePerformance != null)
			setResult(averagePerformance);

		AverageVector[] result = new AverageVector[averageVectors.size()];
		averageVectors.toArray(result);

		return result;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_AVERAGE_PERFORMANCES_ONLY, "Indicates if only performance vectors should be averaged or all types of averagable result vectors", true));
		types.add(new ParameterTypeBoolean(PARAMETER_LEAVE_ONE_OUT, "Set the number of validations to the number of examples. If set to true, number_of_validations is ignored", false));

		ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_OF_VALIDATIONS, "Number of subsets for the crossvalidation.", 2, Integer.MAX_VALUE, 10);
		type.registerDependencyCondition(new BooleanParameterCondition(this, PARAMETER_LEAVE_ONE_OUT, false));
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeCategory(PARAMETER_SAMPLING_TYPE, "Defines the sampling type of the cross validation (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)", SplittedExampleSet.SAMPLING_NAMES, SplittedExampleSet.STRATIFIED_SAMPLING);
		type.registerDependencyCondition(new BooleanParameterCondition(this, PARAMETER_LEAVE_ONE_OUT, false));
		types.add(type);
		
        type = new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1);
        type.registerDependencyCondition(new BooleanParameterCondition(this, PARAMETER_LEAVE_ONE_OUT, false));
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_SAMPLING_TYPE, false, SplittedExampleSet.SHUFFLED_SAMPLING, SplittedExampleSet.STRATIFIED_SAMPLING));
        types.add(type);
        
		return types;
	}
}
