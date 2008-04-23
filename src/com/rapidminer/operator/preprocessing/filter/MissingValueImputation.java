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
package com.rapidminer.operator.preprocessing.filter;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionCreationException;
import com.rapidminer.example.set.ConditionExampleReader;
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
import com.rapidminer.operator.condition.LastInnerOperatorCondition;
import com.rapidminer.operator.condition.SpecificInnerOperatorCondition;
import com.rapidminer.operator.features.weighting.InfoGainWeighting;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.RandomGenerator;


/**
 * The operator MissingValueImpution imputes missing values by learning models
 * for each attribute (except the label) and applying those models to the data
 * set. To specify a subset of the example set in which the missing values
 * should be imputed (e.g. to limit the imputation to only numerical attributes)
 * arbitrary filters can be used as inner operators. However, the learning
 * scheme which should be used for imputation has to be the last inner operator.
 * 
 * @author Tobias Malbrecht
 * @version $Id: MissingValueImputation.java,v 1.4 2007/06/24 17:20:41 ingomierswa Exp $
 */
public class MissingValueImputation extends OperatorChain {

	/** The parameter name for &quot;Order of attributes in which missing values are estimated.&quot; */
	public static final String PARAMETER_ORDER = "order";

	/** The parameter name for &quot;Impute missing values immediately after having learned the corresponding concept and iterate.&quot; */
	public static final String PARAMETER_ITERATE = "iterate";

	/** The parameter name for &quot;Apply filter to learning set in addition to determination which missing values should be substituted.&quot; */
	public static final String PARAMETER_FILTER_LEARNING_SET = "filter_learning_set";

	/** The parameter name for &quot;Learn concepts to impute missing values only on the basis of complete cases (should be used in case learning approach can not handle missing values).&quot; */
	public static final String PARAMETER_LEARN_ON_COMPLETE_CASES = "learn_on_complete_cases";
	
	// learn and substitute missing values in chronological order
	private static final int CHRONOLOGICAL = 0;

	// learn and substitute missing values in random order
	private static final int RANDOM = 1;

	// learn and substitute missing values in ascending order of information
	// gain
	private static final int INFORMATION_GAIN = 2;

	private static final String[] orderStrategies = { "chronological", "random", "information gain" };

	
	public MissingValueImputation(OperatorDescription description) {
		super(description);
	}

	/** Returns the minimum number of inner operators. */
	public int getMinNumberOfInnerOperators() {
		return 1;
	}

	/** Returns the maximum number of inner operators. */
	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		CombinedInnerOperatorCondition condition = new CombinedInnerOperatorCondition();
		for (int i = 0; i < getNumberOfOperators() - 2; i++) {
			condition.addCondition(new SpecificInnerOperatorCondition("Filter" + i, i, new Class[] { ExampleSet.class }, new Class[] { ExampleSet.class }));
		}
		condition.addCondition(new LastInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[] { Model.class }));
		return condition;
	}

	private Operator getLearner() {
		return getOperator(getNumberOfOperators() - 1);
	}

	/**
	 * Returns the attributes in the order which is specified as parameter.
	 * 
	 * @param exampleSet
	 *            the example set
	 * @param order
	 *            the order criterion in which missing values should be imputed
	 * @return attributes of the example set in the specifed order
	 * @throws OperatorException
	 */
	private Attribute[] getOrderedAttributes(ExampleSet exampleSet, int order)
			throws OperatorException {
		int numberOfAttributes = exampleSet.getAttributes().size();
		Attribute[] orderedAttributes = new Attribute[numberOfAttributes];
		Attribute[] originalAttributes = new Attribute[numberOfAttributes];
		int index = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			originalAttributes[index++] = attribute;
		}
		
		switch (order) {
		case CHRONOLOGICAL:
			for (int i = 0; i < orderedAttributes.length; i++) {
				orderedAttributes[i] = originalAttributes[i];
			}
			break;
		case RANDOM:
			double[] attr_probs = new double[numberOfAttributes];
			for (int j = 0; j < attr_probs.length; j++) {
				attr_probs[j] = 1 / (double) attr_probs.length;
			}
			for (int i = 0; i < numberOfAttributes; i++) {
				int currAttributeIndex = (RandomGenerator.getGlobalRandomGenerator()).randomIndex(attr_probs);
				attr_probs[currAttributeIndex] = 0;
				for (int j = 0; j < attr_probs.length; j++) {
					if (attr_probs[j] != 0) {
						attr_probs[j] = 1 / (double) (attr_probs.length - i - 1);
					}
				}
				orderedAttributes[i] = originalAttributes[currAttributeIndex];
			}
			break;
		case INFORMATION_GAIN:
			if (exampleSet.getAttributes().getLabel() == null) {
				throw new UserError(this, 105);
			}
			AttributeWeights infoGains = null;
			Operator infoGainEvaluator;
			try {
				infoGainEvaluator = OperatorService.createOperator(InfoGainWeighting.class);
			} catch (OperatorCreationException e) {
				throw new OperatorException("Cannot create info gain attribute evaluation.");
			}
			infoGains = infoGainEvaluator.apply(new IOContainer(new IOObject[] { exampleSet })).get(AttributeWeights.class);

			Object[] attribNamesObj = infoGains.getAttributeNames().toArray();
			String[] attributeNames = new String[numberOfAttributes];
			for (int i = 0; i < numberOfAttributes; i++) {
				attributeNames[i] = (String) attribNamesObj[i];
			}
			infoGains.sortByWeight(attributeNames, AttributeWeights.DECREASING, AttributeWeights.ABSOLUTE_WEIGHTS);
			for (int i = 0; i < attributeNames.length; i++) {
				orderedAttributes[i] = exampleSet.getAttributes().get(attributeNames[i]);
			}
			break;
		}

		return orderedAttributes;
	}

	public IOObject[] apply() throws OperatorException {
		boolean iterate = getParameterAsBoolean(PARAMETER_ITERATE);
		int order = getParameterAsInt(PARAMETER_ORDER);
		boolean filterLearningSet = getParameterAsBoolean(PARAMETER_FILTER_LEARNING_SET);
		boolean learnOnCompleteCases = getParameterAsBoolean(PARAMETER_LEARN_ON_COMPLETE_CASES);

		ExampleSet sourceSet = getInput(ExampleSet.class);
		sourceSet.recalculateAllAttributeStatistics();

		ExampleSet originalSet = (ExampleSet) sourceSet.clone();

		// delete original label which should not be learned from
		Attribute labelAttribute = originalSet.getAttributes().getLabel();
		if (labelAttribute != null) {
			originalSet.getAttributes().remove(labelAttribute);
		}

		ExampleSet learningSet = (ExampleSet) originalSet.clone();
		ExampleSet substitutionSet = (ExampleSet) originalSet.clone();

		// filter example set in which missing values should be substituted
		for (int i = 0; i < (getNumberOfOperators() - 1); i++) {
			ExampleSet bufferSet = getOperator(i).apply(new IOContainer(new IOObject[] { originalSet })).get(ExampleSet.class);
			substitutionSet = bufferSet;
		}

		int numberOfAttributes = substitutionSet.getAttributes().size();
		Attribute[][] attributePairs = new Attribute[2][numberOfAttributes];

		substitutionSet.getAttributes().setLabel(labelAttribute);
		attributePairs[0] = getOrderedAttributes(substitutionSet, order);
		substitutionSet.getAttributes().setLabel(null);

		ExampleSet eSet = null;
		for (int i = 0; i < numberOfAttributes; i++) {

			// use original data set
			eSet = (ExampleSet) learningSet.clone();

			// if imputation concepts should be learned only on the filtered
			// data set
			if (filterLearningSet) {
				eSet = (ExampleSet) substitutionSet.clone();
			}

			Attribute currAttribute = attributePairs[0][i];

			// remove current attribute
			eSet.getAttributes().setLabel(currAttribute);

			// sort out examples with missing labels
			Condition condition = null;
			try {
				condition = ConditionExampleReader.createCondition("no_missing_labels", eSet, "");
			} catch (ConditionCreationException e) {
				throw new UserError(this, 904, "no_missing_lables", e.getMessage());
			}
			ExampleSet superSet = new ConditionedExampleSet(eSet, condition);

			// if desired sort out cases with missing attribute values
			if (learnOnCompleteCases) {
				try {
					condition = ConditionExampleReader.createCondition("no_missing_attributes", superSet, "");
				} catch (ConditionCreationException e) {
					throw new UserError(this, 904, "no_missing_attributes", e.getMessage());
				}
				superSet = new ConditionedExampleSet(superSet, condition);
			}

			// learn
			Model model = getLearner().apply(new IOContainer(new IOObject[] { superSet })).get(Model.class);

			// re-add current attribute
			eSet.getAttributes().setLabel(null);
			eSet.getAttributes().addRegular(currAttribute);
			model.apply(eSet);
			attributePairs[1][i] = eSet.getAttributes().getPredictedLabel();

			// if strategy is iterative immediately impute missing values
			// after learning step
			if (iterate) {
				Iterator<Example> iterator = eSet.iterator();
				while (iterator.hasNext()) {
					Example example = iterator.next();
					double value = example.getValue(currAttribute);
					if (Double.isNaN(value)) {
						example.setValue(currAttribute, example.getPredictedLabel());
					}
				}
			}

			eSet.getAttributes().setPredictedLabel(null);
		}

		// if strategy is not iterative impute missing values not before having
		// learned all concepts
		if (!iterate) {
			for (int i = 0; i < numberOfAttributes; i++) {
				Iterator<Example> iterator = eSet.iterator();
				while (iterator.hasNext()) {
					Example example = iterator.next();
					double value = example.getValue(attributePairs[0][i]);
					if (Double.isNaN(value)) {
						example.setValue(attributePairs[0][i], example.getValue(attributePairs[1][i]));
					}
				}
			}
		}

		return new IOObject[] { sourceSet };
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
		types.add(new ParameterTypeBoolean(PARAMETER_ITERATE, "Impute missing values immediately after having learned the corresponding concept and iterate.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_FILTER_LEARNING_SET, "Apply filter to learning set in addition to determination which missing values should be substituted.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_LEARN_ON_COMPLETE_CASES, "Learn concepts to impute missing values only on the basis of complete cases (should be used in case learning approach can not handle missing values).", true));
		return types;
	}
}
