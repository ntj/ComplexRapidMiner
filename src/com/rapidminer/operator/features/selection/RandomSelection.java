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
package com.rapidminer.operator.features.selection;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;

/**
 * This operator selects a randomly chosen number of features randomly from the input example set.
 * This can be useful in combination with a ParameterIteration operator or can be used
 * as a baseline for significance test comparisons for feature selection techniques.
 * 
 * @author Ingo Mierswa
 * @version $Id: RandomSelection.java,v 1.3 2008/05/09 19:23:18 ingomierswa Exp $
 */
public class RandomSelection extends Operator {

	public static final String PARAMETER_NUMBER_OF_FEATURES = "number_of_features";
	
	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	public RandomSelection(OperatorDescription description) {
		super(description);
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		ExampleSet result = (ExampleSet)exampleSet.clone();
		
		RandomGenerator random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
		int number = getParameterAsInt(PARAMETER_NUMBER_OF_FEATURES);
		if (number < 0) {
			number = random.nextIntInRange(1, result.getAttributes().size() + 1);
		} else if (number > result.getAttributes().size()) {
			throw new UserError(this, 125, number, result.getAttributes().size());
		}
		
		while (result.getAttributes().size() > number) {
			int toDeleteIndex = random.nextIntInRange(0, result.getAttributes().size()) - 1;
			Attribute toDeleteAttribute = null;
			int counter = 0;
			for (Attribute attribute : result.getAttributes()) {
				if (counter >= toDeleteIndex) {
					toDeleteAttribute = attribute;
					break;
				}
				counter++;
			}
			if (toDeleteAttribute != null) {
				result.getAttributes().remove(toDeleteAttribute);
			}
		}
		
		return new IOObject[] { result };
	}

	@Override
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	@Override
	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_OF_FEATURES, "The number of features which should randomly selected (-1: use a random number).", -1, Integer.MAX_VALUE, -1);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
