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
package com.rapidminer.example;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.generator.FeatureGenerator;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.preprocessing.IdTagging;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.RandomGenerator;


/**
 * Provides some tools for calculation of certain measures and feature
 * generation.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: Tools.java,v 1.7 2008/05/09 19:22:42 ingomierswa Exp $
 */
public class Tools {

	// ================================================================================
	// TABLE CREATION
	// ================================================================================

	/**
	 * After creation of a new MemoryExampleTable with given size all values are
	 * Double.NaN. Use this method to fill the table with random values in the
	 * range specified by minimum and maximum values of the attributes. Please
	 * note that the attributes in the example table must already have proper
	 * minimum and maximum values. This works only for numerical attribute.
	 * Nominal attribute values will be set to 0.
	 */
	public static void fillTableWithRandomValues(ExampleTable exampleTable, ExampleSet baseSet, RandomGenerator random) {
		DataRowReader reader = exampleTable.getDataRowReader();
		Attribute[] attributes = exampleTable.getAttributes();
		while (reader.hasNext()) {
			DataRow dataRow = reader.next();
			for (int i = 0; i < attributes.length; i++) {
				if (attributes[i] != null) {
					if (!attributes[i].isNominal()) {
						double min = baseSet.getStatistics(attributes[i], Statistics.MINIMUM);
						double max = baseSet.getStatistics(attributes[i], Statistics.MAXIMUM);
						if (max > min)
							dataRow.set(attributes[i], random.nextDoubleInRange(min, max));
						else
							dataRow.set(attributes[i], random.nextDouble());
					} else {
						dataRow.set(attributes[i], 0);
					}
				}
			}
		}
	}

	// ================================================================================
	// -------------------- GENERATION --------------------
	// ================================================================================

	public static String[] getRegularAttributeNames(ExampleSet exampleSet) {
		String[] attributeNames = new String[exampleSet.getAttributes().size()];
		int counter = 0;
		for (Attribute attribute : exampleSet.getAttributes())
			attributeNames[counter++] = attribute.getName();
		return attributeNames;
	}
	
	public static Attribute[] createRegularAttributeArray(ExampleSet exampleSet) {
		Attribute[] attributes = new Attribute[exampleSet.getAttributes().size()];
		int counter = 0;
		for (Attribute attribute : exampleSet.getAttributes())
			attributes[counter++] = attribute;
		return attributes;
	}

	public static Attribute[] getRandomCompatibleAttributes(ExampleSet exampleSet, FeatureGenerator generator, int maxDepth, String[] functions, Random random) {
		List inputAttributes = generator.getInputCandidates(exampleSet, maxDepth, functions);
		if (inputAttributes.size() > 0)
			return (Attribute[]) inputAttributes.get(random.nextInt(inputAttributes.size()));
		else
			return null;
	}

	public static Attribute[] getWeightedCompatibleAttributes(AttributeWeightedExampleSet exampleSet, FeatureGenerator generator, int maxDepth, String[] functions, RandomGenerator random) {
		List inputAttributes = generator.getInputCandidates(exampleSet, maxDepth, functions);
		double[] probs = new double[inputAttributes.size()];
		double probSum = 0.0d;
		Iterator i = inputAttributes.iterator();
		int k = 0;
		while (i.hasNext()) {
			Attribute[] candidate = (Attribute[]) i.next();
			for (int j = 0; j < candidate.length; j++) {
				double weight = exampleSet.getWeight(candidate[j]);
				probSum += weight;
				probs[k] = weight;
			}
		}
		for (int j = 0; j < probs.length; j++)
			probs[j] /= probSum;
		return (Attribute[]) inputAttributes.get(random.randomIndex(probs));
	}

	public static Attribute createSpecialAttribute(ExampleSet exampleSet, String name, int valueType) {
		Attribute attribute = AttributeFactory.createAttribute(AttributeFactory.createName(name), valueType);
		exampleSet.getExampleTable().addAttribute(attribute);
		exampleSet.getAttributes().setSpecialAttribute(attribute, name);
		return attribute;
	}

	public static Attribute createWeightAttribute(ExampleSet exampleSet) {
		Attribute weight = exampleSet.getAttributes().getWeight();
		if (weight != null) {
			exampleSet.getLog().logWarning("ExampleSet.createWeightAttribute(): Overwriting old weight attribute!");
		}
		
        weight = AttributeFactory.createAttribute(Attributes.WEIGHT_NAME, Ontology.REAL);
        exampleSet.getExampleTable().addAttribute(weight);
        exampleSet.getAttributes().setWeight(weight);
        
		DataRowReader reader = exampleSet.getExampleTable().getDataRowReader();
		while (reader.hasNext()) {
			DataRow data = reader.next();
			data.set(weight, 1.0d);
		}
		return weight;
	}
	
	public static boolean containsValueType(ExampleSet exampleSet, int valueType) {
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), valueType))
				return true;
		}
		return false;
	}

	/** Replaces the given real value by the new one. Please note that this method will only 
	 *  work for nominal attributes. */
	public static void replaceValue(Attribute attribute, String oldValue, String newValue) {
		if (!attribute.isNominal())
			throw new RuntimeException("Example-Tools: replaceValue is only supported for nominal attributes.");
		NominalMapping mapping = attribute.getMapping();
		int oldIndex = mapping.getIndex(oldValue);
		if (oldIndex < 0)
			throw new RuntimeException("Example-Tools: replaceValue cannot be performed since old value was not defined in the attribute.");
		mapping.setMapping(newValue, oldIndex);
	}

	/**
	 * Replaces the given value by the new one. This method will only work
	 * for nominal attributes.
	 */
	public static void replaceValue(ExampleSet exampleSet, Attribute attribute, String oldValue, String newValue) {
		if (!attribute.isNominal())
			throw new RuntimeException("Example-Tools: replaceValue is only supported for nominal attributes.");
		NominalMapping mapping = attribute.getMapping();
		if (oldValue.equals("?")) {
			for (Example example : exampleSet) {
				if (Double.isNaN(example.getValue(attribute)))
					example.setValue(attribute, mapping.mapString(newValue));
			}
		} else {
			int oldIndex = mapping.getIndex(oldValue);
			if (oldIndex < 0)
				throw new RuntimeException("Example-Tools: replaceValue cannot be performed since old value was not defined in the attribute.");
			if (newValue.equals("?")) {
				for (Example example : exampleSet) {
					int index = mapping.getIndex(example.getValueAsString(attribute));
					if (index == oldIndex) {
						example.setValue(attribute, Double.NaN);
					}
				}
				return;
			}
			int newIndex = mapping.getIndex(newValue);
			if (newIndex >= 0) {
				for (Example example : exampleSet) {
					int index = mapping.getIndex(example.getValueAsString(attribute));
					if (index == oldIndex) {
						example.setValue(attribute, newIndex);
					}
				}
			} else {
				mapping.setMapping(newValue, oldIndex);
			}
		}
	}
	
	/** Replaces the given real value by the new one. Please note that this method will only properly
	 *  work for numerical attributes since for nominal attributes no remapping is performed. Please
	 *  note also that this method performs a data scan. */
	public static void replaceValue(ExampleSet exampleSet, Attribute attribute, double oldValue, double newValue) {
		for (Example example : exampleSet) {
			double value = example.getValue(attribute);
			if (com.rapidminer.tools.Tools.isEqual(value, oldValue)) {
				example.setValue(attribute, newValue);
			}
		}
	}
	

	/**
	 * Returns true if value and block types of the first attribute are subtypes of
	 * value and block type of the second.
	 */
	public static boolean compatible(Attribute first, Attribute second) {
		return (Ontology.ATTRIBUTE_VALUE_TYPE.isA(first.getValueType(), second.getValueType())) && (Ontology.ATTRIBUTE_BLOCK_TYPE.isA(first.getBlockType(), second.getBlockType()));
	}

	// ================================================================================
	// P r o b a b i l t i e s
	// ================================================================================

	public static double getAverageWeight(AttributeWeightedExampleSet exampleSet) {
		int counter = 0;
		double weightSum = 0.0d;
		for (Attribute attribute : exampleSet.getAttributes()) {
			double weight = exampleSet.getWeight(attribute);
			if (!Double.isNaN(weight)) {
				weightSum += Math.abs(weight);
				counter++;
			}
		}
		return weightSum / counter;
	}

	public static double[] getProbabilitiesFromWeights(Attribute[] attributes, AttributeWeightedExampleSet exampleSet) {
		return getProbabilitiesFromWeights(attributes, exampleSet, false);
	}

	public static double[] getInverseProbabilitiesFromWeights(Attribute[] attributes, AttributeWeightedExampleSet exampleSet) {
		return getProbabilitiesFromWeights(attributes, exampleSet, true);
	}

	/**
	 * Calculates probabilities for attribute selection purposes based on the
	 * given weight. Attributes whose weight is not defined in the weight vector
	 * get a probability corresponding to the average weight. Inverse
	 * probabilities can be calculated for cases where attributes with a high
	 * weight should be selected with small probability.
	 */
	public static double[] getProbabilitiesFromWeights(Attribute[] attributes, AttributeWeightedExampleSet exampleSet, boolean inverse) {
		double weightSum = 0.0d;
		int counter = 0;
		for (int i = 0; i < attributes.length; i++) {
			double weight = exampleSet.getWeight(attributes[i]);
			if (!Double.isNaN(weight)) {
				weightSum += Math.abs(weight);
				counter++;
			}
		}
		double weightAverage = weightSum / counter;
		weightSum += (attributes.length - counter) * weightAverage;

		double[] probs = new double[attributes.length];
		for (int i = 0; i < probs.length; i++) {
			double weight = exampleSet.getWeight(attributes[i]);
			if (Double.isNaN(weight)) {
				probs[i] = weightAverage / weightSum;
			} else {
				probs[i] = inverse ? ((2 * weightAverage - Math.abs(weight)) / weightSum) : (Math.abs(weight) / weightSum);
			}
		}
		return probs;
	}

	public static Attribute selectAttribute(Attribute[] attributes, double[] probs, Random random) {
		double r = random.nextDouble();
		double sum = 0.0d;
		for (int i = 0; i < attributes.length; i++) {
			sum += probs[i];
			if (r < sum) {
				return attributes[i];
			}
		}
		return attributes[attributes.length - 1];
	}
	
	public static boolean isDefault(double defaultValue, double value) {
		if (Double.isNaN(defaultValue))
			return Double.isNaN(value);
		/* Don't use infinity.
		if (Double.isInfinite(defaultValue))
			return Double.isInfinite(value);
		*/
		return defaultValue == value;
	}

	/** The data set is not allowed to contain missing values. */
	public static void onlyNonMissingValues(ExampleSet exampleSet, String task) throws OperatorException {
		exampleSet.recalculateAllAttributeStatistics();
		for (Attribute attribute : exampleSet.getAttributes()) {
			double missing = exampleSet.getStatistics(attribute, Statistics.UNKNOWN);
			if (missing > 0) {
				throw new UserError(null, 139, task);
			}
		}
	}
	
	/**
	 * The attributes all have to be numerical.
	 * 
	 * @param es
	 *            the example set
	 * @throws OperatorException
	 */
	public static void onlyNumericalAttributes(ExampleSet es, String task) throws OperatorException {
		for (Attribute attribute : es.getAttributes()) {
			if (!Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), Ontology.NUMERICAL))
				throw new UserError(null, 104, task, attribute);
		}
	}

	/**
	 * The attributes all have to be nominal or binary.
	 * 
	 * @param es
	 *            the example set
	 * @throws OperatorException
	 */
	public static void onlyNominalAttributes(ExampleSet es, String task) throws OperatorException {
		for (Attribute attribute : es.getAttributes()) {
			if (!Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), Ontology.NOMINAL))
				throw new UserError(null, 103, task, attribute);
		}
	}

	/**
	 * The example set has to contain labels.
	 * 
	 * @param es
	 *            the example set
	 * @throws OperatorException
	 */
	public static void isLabelled(ExampleSet es) throws OperatorException {
		if (es.getAttributes().getLabel() == null) {
			throw new UserError(null, 105);
		}
	}

	/**
	 * The example set has to have ids. If no id attribute is available, it will
	 * be automatically created with help of the IDTagging operator.
	 * 
	 * @param es
	 *            the example set
	 * @throws OperatorException
	 */
	public static void checkAndCreateIds(ExampleSet es) throws OperatorException {
		if (es.getAttributes().getId() == null) {
			try {
                // create ids (and visualization)
				Operator idTagging = OperatorService.createOperator(IdTagging.class);
				idTagging.apply(new IOContainer(es));
			} catch (OperatorCreationException e) {
				throw new UserError(null, 113, "id");	
			}
		}
	}

	/**
	 * The example set has to have nominal labels.
	 * 
	 * @param es
	 *            the example set
	 * @throws OperatorException
	 */
	public static void hasNominalLabels(ExampleSet es) throws OperatorException {
		isLabelled(es);
		Attribute a = es.getAttributes().getLabel();
		if (!Ontology.ATTRIBUTE_VALUE_TYPE.isA(a.getValueType(), Ontology.NOMINAL))
			throw new UserError(null, 101, "clustering", a.getName());
	}

	/**
	 * The example set has to contain at least one example.
	 * 
	 * @param es
	 *            the example set
	 * @throws OperatorException
	 */
	public static void isNonEmpty(ExampleSet es) throws OperatorException {
		if (es.size() == 0)
			throw new UserError(null, 117);
	}
}
