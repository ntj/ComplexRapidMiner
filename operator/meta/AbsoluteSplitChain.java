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
package com.rapidminer.operator.meta;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * <p>An operator chain that split an {@link ExampleSet} into two disjunct parts
 * and applies the first child operator on the first part and applies the second
 * child on the second part and the result of the first child. The total result
 * is the result of the second operator.</p>
 * 
 * <p>The input example set will be splitted based on a user defined absolute 
 * numbers.</p>
 * 
 * @author Peter B. Volk, Ingo Mierswa
 * @version $Id: AbsoluteSplitChain.java,v 1.2 2008/05/09 19:22:38 ingomierswa Exp $
 */
public class AbsoluteSplitChain extends AbstractSplitChain {

	/** The parameter name for &quot;Defines the sampling type of this operator.&quot; */
	public static final String PARAMETER_SAMPLING_TYPE = "sampling_type";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	private static final String PARAMETER_NUMBER_TRAINING_EXAMPLES = "number_training_examples";

	private static final String PARAMETER_NUMBER_TEST_EXAMPLES = "number_test_examples";

	
	public AbsoluteSplitChain(OperatorDescription description) {
		super(description);
	}

	protected SplittedExampleSet createSplittedExampleSet(ExampleSet inputSet) throws OperatorException {
		int size = -1;
		if (getParameterAsInt(PARAMETER_NUMBER_TEST_EXAMPLES) == -1) {
			if (getParameterAsInt(PARAMETER_NUMBER_TRAINING_EXAMPLES) == -1) {
				throw new UserError(this, 208, PARAMETER_NUMBER_TEST_EXAMPLES, PARAMETER_NUMBER_TRAINING_EXAMPLES);
			}
			size = getParameterAsInt(PARAMETER_NUMBER_TRAINING_EXAMPLES);
		} else {
			if (getParameterAsInt(PARAMETER_NUMBER_TRAINING_EXAMPLES) != -1) {
				throw new UserError(this, 209, PARAMETER_NUMBER_TEST_EXAMPLES, PARAMETER_NUMBER_TRAINING_EXAMPLES);
			}
			size = inputSet.size() - getParameterAsInt(PARAMETER_NUMBER_TEST_EXAMPLES);
		}

		return new SplittedExampleSet(inputSet, 
				                      (double)size / (double)(inputSet.size()), 
				                      getParameterAsInt(PARAMETER_SAMPLING_TYPE), 
				                      getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_TRAINING_EXAMPLES, "Absolute size of the training set. -1 equal to not defined", -1, Integer.MAX_VALUE, -1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_NUMBER_TEST_EXAMPLES, "Absolute size of the test set. -1 equal to not defined", -1, Integer.MAX_VALUE, -1);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeCategory(PARAMETER_SAMPLING_TYPE, "Defines the sampling type of this operator.", SplittedExampleSet.SAMPLING_NAMES, SplittedExampleSet.SHUFFLED_SAMPLING));
		types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
