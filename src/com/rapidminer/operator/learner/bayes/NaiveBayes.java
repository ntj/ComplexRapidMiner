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
package com.rapidminer.operator.learner.bayes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.learner.MixedDistributionsDistribution;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;


/**
 * NaiveBayes is a learner based on the Bayes theorem. If the attributes
 * are fully independent, it is the theoretically best learner which 
 * could be used. Although this assumption is often not fulfilled it
 * delivers quite predictions. This operator uses normal distributions 
 * in order to estimate real-valued distributions of data.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: NaiveBayes.java,v 1.3 2007/07/13 22:52:15 ingomierswa Exp $
 */
public class NaiveBayes extends AbstractLearner {

	private static final String USE_EXAMPLE_WEIGHTS = "use_example_weights";

	private static final String USE_KERNEL = "use_kernel";

	public NaiveBayes(OperatorDescription description) {
		super(description);
	}

	public Model learn(ExampleSet exampleSet) throws OperatorException {
		// collection data
		int exampleCount = exampleSet.size();
		int classCount = exampleSet.getAttributes().getLabel().getMapping().size();
		int attributeCount = exampleSet.getAttributes().size();
		Attribute classAttribute = exampleSet.getAttributes().getLabel();
		Attribute exampleWeightAttribute = exampleSet.getAttributes().getSpecial("weight");
		Collection<String> classMappings = classAttribute.getMapping().getValues();
		ArrayList<ArrayList<Example>> classes = new ArrayList<ArrayList<Example>>(classCount);
		for (int i = 0; i < classCount; i++) {
			classes.add(new ArrayList<Example>());
		}
		Iterator<Example> exampleReader = exampleSet.iterator();
		while (exampleReader.hasNext()) {
			Example currentExample = exampleReader.next();
			int currentClass = getIndexOfString(classMappings, currentExample.getValueAsString(classAttribute));
			classes.get(currentClass).add(currentExample);
			checkForStop();
		}
		// which attributes to use
		boolean[] numericalAttribute = new boolean[attributeCount];
		boolean[] nominalAttribute = new boolean[attributeCount];
		String[] attributeNames = new String[attributeCount];
		int counter = 0;
		for (Attribute currentAttribute : exampleSet.getAttributes()) {
			if (currentAttribute.isNominal()) {
				nominalAttribute[counter] = true;
			} else {
				numericalAttribute[counter] = true;
			}
			attributeNames[counter] = currentAttribute.getName();
			counter++;
		}
		// counting examples per class for class probabilities
		double[] classProbabilities = new double[classCount];
		for (int i = 0; i < classCount; i++) {
			classProbabilities[i] = ((double) classes.get(i).size() + 1) / (exampleCount + classCount);
		}
		// generating model
		DistributionModel model = new DistributionModel(exampleSet, classCount, classProbabilities, attributeNames);
		boolean useKernels = this.getParameterAsBoolean(USE_KERNEL);
		boolean useExampleWeighting = this.getParameterAsBoolean(USE_EXAMPLE_WEIGHTS) && (exampleWeightAttribute != null);
		for (int i = 0; i < classCount; i++) {
			int j = 0;
			for (Attribute currentAttribute : exampleSet.getAttributes()) {
				if (numericalAttribute[j]) {
					// if using naive bayes algorithm, just using mean and
					// deviation
					if (!useKernels) {
						if (!useExampleWeighting) {
							double mean = getMeanOfList(classes.get(i), currentAttribute);
							double variance = getDeviationOfList(classes.get(i), currentAttribute, mean);
							model.addDistribution(i, j, new NormalDistribution(mean, variance));
						} else {
							// make use of example weights
							double mean = getMeanOfList(classes.get(i), currentAttribute, exampleWeightAttribute);
							double variance = getDeviationOfList(classes.get(i), currentAttribute, mean, exampleWeightAttribute);
							model.addDistribution(i, j, new NormalDistribution(mean, variance));
						}
						// Flexible Bayes using Kernel density estimation,
						// therefore add every datapoint as Distribution
					} else {
						if (!useExampleWeighting) {
							double variance = 1 / (Math.sqrt(classes.get(i).size()));
							MixedDistributionsDistribution distribution = new MixedDistributionsDistribution();
							for (Example example : classes.get(i)) {
								distribution.addDistribution(new NormalDistribution(example.getValue(currentAttribute), variance));
							}
							model.addDistribution(i, j, distribution);
						} else {
							// make use of example weights
							double variance = 1 / (Math.sqrt(classes.get(i).size()));
							MixedDistributionsDistribution distribution = new MixedDistributionsDistribution();
							for (Example example : classes.get(i)) {
								distribution.addDistribution(new WeightedNormalDistribution(example.getValue(currentAttribute), variance, example
										.getValue(exampleWeightAttribute)));
							}
							model.addDistribution(i, j, distribution);
						}
					}
					checkForStop();
				}
				if (nominalAttribute[j]) {
					LinkedHashSet<Double> set = new LinkedHashSet<Double>();
					ArrayList<Double> tupel = new ArrayList<Double>();
					Iterator<Example> iterator = classes.get(i).iterator();
					while (iterator.hasNext()) {
						Double value = iterator.next().getValue(currentAttribute);
						set.add(value);
						tupel.add(value);
					}
					model.addDistribution(i, j, new DiscreteDistribution(currentAttribute, set, tupel));
				}
				j++;
			}
		}
		return model;
	}

	private double getMeanOfList(ArrayList<Example> exampleCollection, Attribute attribute) {
		double accumulatedValue = 0;
		for (Example example : exampleCollection) {
			accumulatedValue += example.getValue(attribute);
		}
		return accumulatedValue / exampleCollection.size();
	}

	/**
	 * calculating mean of examples in respect to example weight
	 */
	private double getMeanOfList(ArrayList<Example> exampleCollection, Attribute attribute, Attribute weightAttribute) {
		double accumulatedValue = 0;
		double totalWeight = 0;
		for (Example example : exampleCollection) {
			double exampleWeight = example.getValue(weightAttribute);
			accumulatedValue += example.getValue(attribute) * exampleWeight;
			totalWeight += exampleWeight;
		}
		return accumulatedValue / totalWeight;
	}

	private double getDeviationOfList(ArrayList<Example> exampleCollection, Attribute attribute, double mean) {
		double accumulatedValue = 0;
		for (Example example : exampleCollection) {
			accumulatedValue += Math.pow(example.getValue(attribute) - mean, 2);
		}
		return Math.sqrt(accumulatedValue / (exampleCollection.size() - 1));
	}

	/**
	 * calculating deviation of examples in respect to example weight
	 */
	private double getDeviationOfList(ArrayList<Example> exampleCollection, Attribute attribute, double mean, Attribute weightAttribute) {
		double accumulatedValue = 0;
		double totalWeight = 0;
		for (Example example : exampleCollection) {
			double exampleWeight = example.getValue(weightAttribute);
			accumulatedValue += exampleWeight * Math.pow(example.getValue(attribute) - mean, 2);
			totalWeight += exampleWeight;
		}
		double size = exampleCollection.size();
		return Math.sqrt(accumulatedValue / (size - (totalWeight / size)));
	}

	private int getIndexOfString(Collection<String> collection, String value) {
		Iterator<String> collectionReader = collection.iterator();
		int index = 0;
		while (collectionReader.hasNext()) {
			if (collectionReader.next().equals(value)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	public boolean supportsCapability(LearnerCapability lc) {
		if (lc == LearnerCapability.POLYNOMINAL_ATTRIBUTES)
			return true;
		if (lc == LearnerCapability.BINOMINAL_ATTRIBUTES)
			return true;
		if (lc == LearnerCapability.NUMERICAL_ATTRIBUTES)
			return true;
		if (lc == LearnerCapability.POLYNOMINAL_CLASS)
			return true;
		if (lc == LearnerCapability.BINOMINAL_CLASS)
			return true;
		if (lc == LearnerCapability.WEIGHTED_EXAMPLES)
			return true;
		return false;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeBoolean(USE_KERNEL, "Using kernels might reduce error", false);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeBoolean(USE_EXAMPLE_WEIGHTS, "Use example weights if exists", true);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
