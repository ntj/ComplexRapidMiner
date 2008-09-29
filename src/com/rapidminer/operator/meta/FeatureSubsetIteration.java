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
import java.util.concurrent.atomic.AtomicInteger;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.ValueString;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.LastInnerOperatorCondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;

/**
 * <p>This meta operator iterates through all possible feature subsets within the specified range
 * and applies the inner operators on the feature subsets. This might be useful in combination 
 * with the ProcessLog operator and, for example, a performance evaluation. In contrast
 * to the BruteForce feature selection, which performs a similar task, this iterative approach needs
 * much less memory and can be performed on larger data sets.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: FeatureSubsetIteration.java,v 1.3 2008/07/13 23:25:24 ingomierswa Exp $
 */
public class FeatureSubsetIteration extends OperatorChain {

	public static final String PARAMETER_MAX_NUMBER_OF_ATTRIBUTES = "max_number_of_attributes";
	
	public static final String PARAMETER_MIN_NUMBER_OF_ATTRIBUTES = "min_number_of_attributes";
	
	public static final String PARAMETER_EXACT_NUMBER_OF_ATTRIBUTES = "exact_number_of_attributes";
	
	private int iteration = -1;
	
	private int featureNumber = -1;
	
	private String featureNames = null;
	
	public FeatureSubsetIteration(OperatorDescription description) {
		super(description);

		addValue(new ValueDouble("iteration", "The current iteration.") {
			public double getDoubleValue() {
				return iteration;
			}
		});

		addValue(new ValueDouble("feature_number", "The number of used features in the current iteration.") {
			public double getDoubleValue() {
				return featureNumber;
			}
		});
		
		addValue(new ValueString("feature_names", "The names of the used features in the current iteration.") {
			public String getStringValue() {
				return featureNames;
			}
		});
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
	
		// init
		int minNumberOfFeatures   = getParameterAsInt(PARAMETER_MIN_NUMBER_OF_ATTRIBUTES);
		int maxNumberOfFeatures   = getParameterAsInt(PARAMETER_MAX_NUMBER_OF_ATTRIBUTES);
		int exactNumberOfFeatures = getParameterAsInt(PARAMETER_EXACT_NUMBER_OF_ATTRIBUTES);
		
		// checks
		if (exactNumberOfFeatures > 0) {
			log("Using exact number of features for feature subset iteration (" + exactNumberOfFeatures + "), ignoring possibly defined range for the number of features.");
		} else {
			if ((maxNumberOfFeatures > 0) && (minNumberOfFeatures > maxNumberOfFeatures)) {
				throw new UserError(this, 210, PARAMETER_MAX_NUMBER_OF_ATTRIBUTES, PARAMETER_MIN_NUMBER_OF_ATTRIBUTES);
			} else {
				maxNumberOfFeatures = exampleSet.getAttributes().size();
			}
		}

		// run
		this.iteration = 0;
		this.featureNumber = 0;
		this.featureNames = "?";
		Attribute[] allAttributes = exampleSet.getAttributes().createRegularAttributeArray();
		if (exactNumberOfFeatures > 0) {
			applyOnAllWithExactNumber(exampleSet, allAttributes, exactNumberOfFeatures);
		} else {
			applyOnAllInRange(exampleSet, allAttributes, minNumberOfFeatures, maxNumberOfFeatures);
		}

		return new IOObject[] { exampleSet };
	}

	private void applyInnerOperators(ExampleSet exampleSet) throws OperatorException {
		IOContainer input = new IOContainer(exampleSet);
		for (int i = 0; i < getNumberOfOperators(); i++) {
			input = getOperator(i).apply(input);
		}
	}
	
	/** Add all attribute combinations with a fixed size to the population. */
	private void applyOnAllWithExactNumber(ExampleSet exampleSet, Attribute[] allAttributes, int exactNumberOfFeatures) throws OperatorException {
		ExampleSet workingSet = (ExampleSet)exampleSet.clone();
		this.featureNumber = exactNumberOfFeatures;
		if (exactNumberOfFeatures == 1) {
			for (int i = 0; i < allAttributes.length; i++) {
				workingSet.getAttributes().clearRegular();
				workingSet.getAttributes().addRegular(allAttributes[i]);
				
				// apply inner
				this.iteration++;
				this.featureNames = allAttributes[i].getName();
				applyInnerOperators(workingSet);
			}		
		} else {
			for (int start = 0; start < allAttributes.length - exactNumberOfFeatures + 1; start++) {
				// initialization
				AtomicInteger[] indices = new AtomicInteger[exactNumberOfFeatures];
				for (int i = 0; i < indices.length; i++) {
					indices[i] = new AtomicInteger(start + i);
				}

				for (int c = indices[indices.length - 1].get(); c < allAttributes.length; c++) {
					// create current example set
					StringBuffer nameBuffer = new StringBuffer();
					boolean first = true;
					workingSet.getAttributes().clearRegular();
					for (AtomicInteger index : indices) {
						Attribute attribute = allAttributes[index.get()];
						workingSet.getAttributes().addRegular(attribute);
						if (!first)
							nameBuffer.append(", ");
						nameBuffer.append(attribute.getName());
						first = false;
					}
					
					// apply inner
					this.iteration++;
					this.featureNames = nameBuffer.toString();
					applyInnerOperators(workingSet);

					if (indices[indices.length - 1].get() < allAttributes.length - 1)
						indices[indices.length - 1].incrementAndGet();
				}
			}
		}
	}
	
	/** Recursive method to add all attribute combinations to the population. */
	private void applyOnAllInRange(ExampleSet exampleSet, Attribute[] allAttributes, int minNumberOfFeatures, int maxNumberOfFeatures) throws OperatorException {
		for (int i = minNumberOfFeatures; i <= maxNumberOfFeatures; i++) {
			applyOnAllWithExactNumber(exampleSet, allAttributes, i);
		}
	}
	
	public InnerOperatorCondition getInnerOperatorCondition() {
		return new LastInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[0]);
	}

	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	public int getMinNumberOfInnerOperators() {
		return 1;
	}
	
	public Class<?>[] getInputClasses() {
		return new Class[0];
	}

	public Class<?>[] getOutputClasses() {
		return new Class[0];
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_MIN_NUMBER_OF_ATTRIBUTES, "Determines the minimum number of features used for the combinations.", 1, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_MAX_NUMBER_OF_ATTRIBUTES, "Determines the maximum number of features used for the combinations (-1: try all combinations up to possible maximum)", -1, Integer.MAX_VALUE, -1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_EXACT_NUMBER_OF_ATTRIBUTES, "Determines the exact number of features used for the combinations (-1: use the feature range defined by min and max).", -1, Integer.MAX_VALUE, -1);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
