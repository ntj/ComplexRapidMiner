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
package com.rapidminer.operator.preprocessing.filter;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionCreationException;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.condition.CombinedInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SpecificInnerOperatorCondition;
import com.rapidminer.operator.features.weighting.InfoGainWeighting;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.RandomGenerator;


/**
 * The operator MissingValueImpution imputes missing values by learning models
 * for each attribute (except the label) and applying those models to the data
 * set. The learner which is to be applied has to be given as inner operator.
 * In order to specify a subset of the example set in which the missing values
 * should be imputed (e.g. to limit the imputation to only numerical attributes) an
 * arbitrary filter can be used as the first inner operator. In the case that such
 * a filter is used, the learner has to be the second inner operator.
 * 
 * Please be aware that depending on the ability of the inner operator to handle
 * missing values this operator might not be able to impute all missing values in
 * some cases. This behaviour leads to a warning. It might hence be useful to combine
 * this operator with a subsequent MissingValueReplenishment.
 * 
 * ATTENTION: This operator is currently under development and does not properly
 * work in all cases. We do not recommend the usage of this operator in production 
 * systems.
 * 
 * @author Tobias Malbrecht
 * @version $Id: MissingValueImputation.java,v 1.11 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class MissingValueImputation extends OperatorChain {

	/** The parameter name for &quot;Order of attributes in which missing values are estimated.&quot; */
	public static final String PARAMETER_ORDER = "order";

	/** The parameter name for &quot;Sort direction which is used in order strategy.&quot; */
	public static final String PARAMETER_SORT = "sort";
	
	/** The parameter name for &quot;Impute missing values immediately after having learned the corresponding concept and iterate.&quot; */
	public static final String PARAMETER_ITERATE = "iterate";

	/** The parameter name for &quot;Apply filter to learning set in addition to determination which missing values should be substituted.&quot; */
	public static final String PARAMETER_FILTER_LEARNING_SET = "filter_learning_set";

	/** The parameter name for &quot;Learn concepts to impute missing values only on the basis of complete cases (should be used in case learning approach can not handle missing values).&quot; */
	public static final String PARAMETER_LEARN_ON_COMPLETE_CASES = "learn_on_complete_cases";
	
	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	/** Chronological imputation order. */
	private static final int CHRONOLOGICAL = 0;

	/** Random imputation order. */
	private static final int RANDOM = 1;

	/** Imputation order based on the number of missing values. */
	private static final int NUMBER_OF_MISSING_VALUES = 2;
	
	/** Imputation order based on the information gain of the attributes. */
	private static final int INFORMATION_GAIN = 3;

	/** Order strategies names. */
	private static final String[] orderStrategies = { "chronological", "random", "number of missing values", "information gain" };
	
	/** Ascending sort order. */
	private static final int ASCENDING = 0;
	
	/** Sort strategies names. */
	private static final String[] sortStrategies = { "ascending", "descending" };

	public MissingValueImputation(OperatorDescription description) {
		super(description);
	}

	/** Returns the minimum number of inner operators. */
	public int getMinNumberOfInnerOperators() {
		return 1;
	}

	/** Returns the maximum number of inner operators. */
	public int getMaxNumberOfInnerOperators() {
		return 2;
	}
	
	public InnerOperatorCondition getInnerOperatorCondition() {
		if (getNumberOfOperators() == 1) {
			return new SpecificInnerOperatorCondition("Learner", 0, new Class[] { ExampleSet.class }, new Class[] { Model.class });
		} else {
			CombinedInnerOperatorCondition condition = new CombinedInnerOperatorCondition();
			condition.addCondition(new SpecificInnerOperatorCondition("Filter", 0, new Class[] { ExampleSet.class }, new Class[] { ExampleSet.class }));
			condition.addCondition(new SpecificInnerOperatorCondition("Learner", 1, new Class[] { ExampleSet.class }, new Class[] { Model.class }));
			return condition;
		}
	}

	public Attribute[] getOrderedAttributes(ExampleSet exampleSet, int order, boolean ascending) throws OperatorException {
		Attribute[] sortedAttributes = new Attribute[exampleSet.getAttributes().size()];
		AttributeWeights weights = new AttributeWeights(exampleSet);
		
		switch (order) {
		case CHRONOLOGICAL:
			int index = 0;
			for (Attribute attribute : exampleSet.getAttributes()) {
				weights.setWeight(attribute.getName(), index);
				index++;
			}
			break;
		case RANDOM:
			RandomGenerator randomGenerator = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
			for (Attribute attribute : exampleSet.getAttributes()) {
				weights.setWeight(attribute.getName(), randomGenerator.nextDouble());
			}
			break;
		case NUMBER_OF_MISSING_VALUES:
			exampleSet.recalculateAllAttributeStatistics();
			for (Attribute attribute : exampleSet.getAttributes()) {
				weights.setWeight(attribute.getName(), exampleSet.getStatistics(attribute, Statistics.UNKNOWN));
			}
			break;
		case INFORMATION_GAIN:
			if (exampleSet.getAttributes().getLabel() == null) {
				throw new UserError(this, 105);
			}
			Operator infoGainWeightingOperator;
			try {
				infoGainWeightingOperator = OperatorService.createOperator(InfoGainWeighting.class);
			} catch (OperatorCreationException e) {
				throw new OperatorException("Cannot create info gain weighting operator which is necessary for ordering the attributes.");
			}
			weights = infoGainWeightingOperator.apply(new IOContainer(new IOObject[] { exampleSet })).get(AttributeWeights.class);
			break;
		}

		String[] attributeNames = new String[weights.size()];
		weights.getAttributeNames().toArray(attributeNames);
		int sortingOrder = (ascending ? AttributeWeights.DECREASING : AttributeWeights.INCREASING);
		weights.sortByWeight(attributeNames, sortingOrder, AttributeWeights.ABSOLUTE_WEIGHTS);
		for (int i = 0; i < attributeNames.length; i++) {
			sortedAttributes[i] = exampleSet.getAttributes().get(attributeNames[i]);
		}
		
		return sortedAttributes;
	}
	
	public IOObject[] apply() throws OperatorException {
		boolean iterate = getParameterAsBoolean(PARAMETER_ITERATE);
		int order = getParameterAsInt(PARAMETER_ORDER);
		boolean ascending = (getParameterAsInt(PARAMETER_SORT) == ASCENDING);
		boolean filterLearningSet = getParameterAsBoolean(PARAMETER_FILTER_LEARNING_SET);
		boolean learnOnCompleteCases = getParameterAsBoolean(PARAMETER_LEARN_ON_COMPLETE_CASES);

		// retrieve inner operators
		Operator learner = null;
		Operator filter = null;
		if (getNumberOfOperators() == 1) { 
			learner = getOperator(0);
		} else {
			filter = getOperator(0);
			learner = getOperator(1);
		}

		ExampleSet exampleSet = getInput(ExampleSet.class);

		// delete original label which should not be learned from
		Attribute label = exampleSet.getAttributes().getLabel();
		if (label != null) {
			exampleSet.getAttributes().setLabel(null);
			exampleSet.getAttributes().remove(label);
		}

		ExampleSet imputationSet = (ExampleSet) exampleSet.clone();

		// filter example set in which missing values should be substituted
		if (filter != null) {
			imputationSet = filter.apply(new IOContainer(new IOObject[] { (ExampleSet) exampleSet.clone() })).get(ExampleSet.class);
		}

		int numberOfAttributes = imputationSet.getAttributes().size();
		Attribute[][] attributePairs = new Attribute[2][numberOfAttributes];

		imputationSet.getAttributes().setLabel(label);
		attributePairs[0] = getOrderedAttributes(imputationSet, order, ascending);
		imputationSet.getAttributes().setLabel(null);
		int imputationFailure = 0;

		ExampleSet workingSet = null;
		for (int i = 0; i < numberOfAttributes; i++) {
			// use either filtered set or original (full) set
			if (filterLearningSet) {
				workingSet = (ExampleSet) imputationSet.clone();				
			} else {
				workingSet = (ExampleSet) exampleSet.clone();
			}

			Attribute attribute = attributePairs[0][i];
			workingSet.getAttributes().setLabel(attribute);

			// sort out examples with missing labels
			Condition condition = null;
			try {
				condition = ConditionedExampleSet.createCondition("no_missing_labels", workingSet, "");
			} catch (ConditionCreationException e) {
				throw new UserError(this, 904, "no_missing_lables", e.getMessage());
			}
			ExampleSet learningSet = new ConditionedExampleSet(workingSet, condition);

			// if desired sort out cases with missing attribute values
			if (learnOnCompleteCases) {
				try {
					condition = ConditionedExampleSet.createCondition("no_missing_attributes", learningSet, "");
				} catch (ConditionCreationException e) {
					throw new UserError(this, 904, "no_missing_attributes", e.getMessage());
				}
				learningSet = new ConditionedExampleSet(learningSet, condition);
			}

			log("Learning imputation model for attribute " + attribute.getName() + " on " + learningSet.size() + " examples.");
			
			// learn
			Model model = learner.apply(new IOContainer(new IOObject[] { learningSet })).get(Model.class);

			// re-add current attribute
			workingSet = model.apply(workingSet);
			workingSet.getAttributes().setLabel(null);
			workingSet.getAttributes().addRegular(attribute);
			attributePairs[1][i] = workingSet.getAttributes().getPredictedLabel();

			// if strategy is iterative immediately impute missing values
			// after learning step
			if (iterate) {
				log("Imputating missing values in attribute " + attribute.getName() + ".");
				for (Example example : workingSet) {
					double value = example.getValue(attribute);
					if (Double.isNaN(value)) {
						example.setValue(attribute, example.getPredictedLabel());
						if (Double.isNaN(example.getPredictedLabel())) {
							imputationFailure++;

						}
					}
				}
			}
			if (imputationFailure > 0) {
				logWarning("Unable to impute " + imputationFailure + " missing values in attribute " + attribute.getName() + ".");
				imputationFailure = 0;
			}
			workingSet.getAttributes().setPredictedLabel(null);
		}

		// if strategy is not iterative impute missing values not before having
		// learned all concepts
		if (!iterate) {
			for (int i = 0; i < numberOfAttributes; i++) {
				imputationFailure = 0;
				Attribute attribute = attributePairs[0][i];
				log("Imputating missing values in attribute " + attribute.getName() + ".");
				for (Example example : workingSet) {
					double value = example.getValue(attribute);
					if (Double.isNaN(value)) {
						example.setValue(attribute, example.getValue(attributePairs[1][i]));
						if (Double.isNaN(example.getValue(attributePairs[1][i]))) {
							imputationFailure++;
						}
					}
				}
				if (imputationFailure > 0) {
					logWarning("Unable to impute " + imputationFailure + " missing values in attribute " + attribute.getName() + ".");
					imputationFailure = 0;
				}
			}
		}

		exampleSet.getAttributes().addRegular(label);
		exampleSet.getAttributes().setLabel(label);
		
		return new IOObject[] { exampleSet };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeCategory(PARAMETER_ORDER, "Order of attributes in which missing values are estimated.", orderStrategies, CHRONOLOGICAL));
		types.add(new ParameterTypeCategory(PARAMETER_SORT, "Sort direction which is used in order strategy.", sortStrategies, ASCENDING));
		types.add(new ParameterTypeBoolean(PARAMETER_ITERATE, "Impute missing values immediately after having learned the corresponding concept and iterate.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_FILTER_LEARNING_SET, "Apply filter to learning set in addition to determination which missing values should be substituted.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_LEARN_ON_COMPLETE_CASES, "Learn concepts to impute missing values only on the basis of complete cases (should be used in case learning approach can not handle missing values).", true));
		types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
