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
package com.rapidminer.operator.learner.bayes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Tupel;


/**
 * NaiveBayes is a learner based on the Bayes theorem. If the attributes
 * are fully independent, it is the theoretically best learner which 
 * could be used. Although this assumption is often not fulfilled it
 * delivers quite predictions. This operator uses normal distributions 
 * in order to estimate real-valued distributions of data.
 * Numerical missing values will be ignored, nominal missing values will
 * be treated as nominal category
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: NaiveBayes.java,v 1.13 2008/05/10 18:28:58 stiefelolm Exp $
 */
public class NaiveBayes extends AbstractLearner {

	public static final String PARAMETER_USE_EXAMPLE_WEIGHTS = "use_example_weights";

	public static final String PARAMETER_USE_KERNEL = "use_kernel";

	public static final String PARAMETER_NUMBER_OF_KERNELS = "use_number_of_kernels";
	
	public NaiveBayes(OperatorDescription description) {
		super(description);
	}

	public Model learn(ExampleSet exampleSet) throws OperatorException {
		// collection data
		int exampleCount = exampleSet.size();
		int classCount = exampleSet.getAttributes().getLabel().getMapping().size();
		int attributeCount = exampleSet.getAttributes().size();
		Attribute labelAttribute = exampleSet.getAttributes().getLabel();
		Attribute exampleWeightAttribute = exampleSet.getAttributes().getSpecial(Attributes.WEIGHT_NAME);
		SplittedExampleSet labelSets = SplittedExampleSet.splitByAttribute(exampleSet, labelAttribute); 

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
			labelSets.selectSingleSubset(i);
			classProbabilities[i] = ((double) labelSets.size()) / (exampleCount + classCount);
		}
		// generating model
		DistributionModel model = new DistributionModel(exampleSet, classCount, classProbabilities, attributeNames);
		boolean useKernels = this.getParameterAsBoolean(PARAMETER_USE_KERNEL);
		boolean useExampleWeighting = this.getParameterAsBoolean(PARAMETER_USE_EXAMPLE_WEIGHTS) && (exampleWeightAttribute != null);
		int maximalNumberOfKernels = this.getParameterAsInt(PARAMETER_NUMBER_OF_KERNELS);
		for (int i = 0; i < classCount; i++) {
			// selecting single class set
			labelSets.selectSingleSubset(i);
			// recalculating statistics for label set
			labelSets.recalculateAllAttributeStatistics();
			int j = 0;
			for (Attribute currentAttribute : exampleSet.getAttributes()) {
				if (numericalAttribute[j]) {
					// if using naive bayes algorithm, just using mean and
					// deviation
					if (!useKernels) {
						if (!useExampleWeighting) {
							double mean = getMean(labelSets, currentAttribute);
							double variance = getVariance(labelSets, currentAttribute, mean);
							model.addDistribution(i, j, new NormalDistribution(mean, variance));
						} else {
							// make use of example weights
							double mean = getWeightedMean(labelSets, currentAttribute);
							double variance = getWeightedVariance(labelSets, currentAttribute, mean);
							model.addDistribution(i, j, new NormalDistribution(mean, variance));
						}
						// Flexible Bayes using Kernel density estimation,
						// therefore building bags for given number of kernels
					} else {
						HashMap<Distribution, ArrayList<Double>> kernelBags = new HashMap<Distribution, ArrayList<Double>>();
						HashMap<Distribution, ArrayList<Tupel<Double, Double>>> weightedKernelBags = new HashMap<Distribution, ArrayList<Tupel<Double, Double>>>();
						MixedDistributionsDistribution distribution = new MixedDistributionsDistribution();
						for (Example example : labelSets) {
							int labelSetSize = labelSets.size();
							if (kernelBags.size() < maximalNumberOfKernels || maximalNumberOfKernels == 0) {
								// generate new kernel
								double variance = 1 / (Math.sqrt(labelSetSize));
								double currentValue = example.getValue(currentAttribute);
								if (!useExampleWeighting) {
									ArrayList<Double> bagValues = new ArrayList<Double>();
									bagValues.add(currentValue);
									kernelBags.put(new NormalDistribution(currentValue, variance), bagValues);
								} else {
									ArrayList<Tupel<Double, Double>> bagValues = new ArrayList<Tupel<Double, Double>>();
									double currentWeight = example.getValue(exampleWeightAttribute);
									bagValues.add(new Tupel<Double, Double>(currentValue, currentWeight));
									weightedKernelBags.put(new WeightedNormalDistribution(currentValue, variance, currentWeight), bagValues);
								}
							} else {
								// find kernel with highest probability for this example
								double minVariance = 1 / (Math.sqrt(labelSetSize));
								double bestProbability = Double.NEGATIVE_INFINITY;
								double currentValue = example.getValue(currentAttribute);
								if (!Double.isNaN(currentValue)) {
									Distribution bestDistribution = null;
									if (!useExampleWeighting) {
										for (Distribution currentDistribution: kernelBags.keySet()) {
											double currentProbability = currentDistribution.getProbability(currentValue);
											if (currentProbability > bestProbability) {
												bestProbability = currentProbability;
												bestDistribution = currentDistribution;
											}
										}
									} else {
										for (Distribution currentDistribution: weightedKernelBags.keySet()) {
											double currentProbability = currentDistribution.getProbability(currentValue);
											if (currentProbability > bestProbability) {
												bestProbability = currentProbability;
												bestDistribution = currentDistribution;
											}
										}	
									}
									// building new distribution for this kernel using all added examples
									if (!useExampleWeighting) {
										ArrayList<Double> bagValues = kernelBags.get(bestDistribution);
										kernelBags.remove(bestDistribution);
										bagValues.add(currentValue);
										double mean = getMean(bagValues, currentAttribute);
										double variance = Math.max(getVariance(bagValues, currentAttribute, mean), minVariance);
										kernelBags.put(new NormalDistribution(mean, variance), bagValues);
									} else {
										ArrayList<Tupel<Double, Double>> bagValues = weightedKernelBags.get(bestDistribution);
										weightedKernelBags.remove(bestDistribution);
										double currentWeight = example.getValue(exampleWeightAttribute);
										bagValues.add(new Tupel<Double, Double>(currentValue, currentWeight));
										double mean = getWeightedMean(bagValues, currentAttribute);
										double variance = Math.max(getWeightedVariance(bagValues, currentAttribute, mean), minVariance);
										double weight = getWeightOfList(bagValues);
										weightedKernelBags.put(new WeightedNormalDistribution(mean, variance, weight), bagValues);
									}
								}
							}
						}
						if (!useExampleWeighting) {
							distribution.addDistributions(kernelBags.keySet());
						} else {
							distribution.addDistributions(weightedKernelBags.keySet());
						}
						model.addDistribution(i, j, distribution);
					}
					checkForStop();
				}
				if (nominalAttribute[j]) {
					LinkedHashMap<Double, Double> valueFrequencies = new LinkedHashMap<Double, Double>();
					// adding all values with weight 0
					NominalMapping mapping = currentAttribute.getMapping();
					for (String string: mapping.getValues()) {
						valueFrequencies.put(new Double(mapping.mapString(string)), 0.0d);
					}
					
					// running through examples
					double totalWeight = 0;
					for (Example example: labelSets) {
						double value = example.getValue(currentAttribute);
						double weight = 1;
						if (useExampleWeighting) {
							weight = example.getWeight();
						}
						totalWeight += weight;
						// since all possible values has been added, no check needed!
						valueFrequencies.put(value, weight + valueFrequencies.get(value));
					}
					model.addDistribution(i, j, new DiscreteDistribution(currentAttribute, valueFrequencies, totalWeight));
				}
				j++;
			}
		}
		return model;
	}



	private double getWeightedVariance(ExampleSet exampleSet, Attribute currentAttribute, double mean) {
		double accumulatedValue = 0;
		double totalWeight = 0;
		for (Example example: exampleSet) {
			double exampleWeight = example.getWeight();
			double value = example.getValue(currentAttribute);
			if (!Double.isNaN(value)) 
				accumulatedValue += exampleWeight * Math.pow(value - mean, 2);
			totalWeight += exampleWeight;
		}
		double size = exampleSet.size();
		return Math.sqrt(accumulatedValue / (totalWeight - (totalWeight / size)));
	}

	private double getWeightedMean(ExampleSet exampleSet, Attribute currentAttribute) {
		double accumulatedValue = 0;
		double totalWeight = 0;
		for (Example example: exampleSet) {
			double exampleWeight = example.getWeight();
			double value = example.getValue(currentAttribute);
			if (!Double.isNaN(value)) 
				accumulatedValue += value * exampleWeight;
			totalWeight += exampleWeight;
		}
		return accumulatedValue / totalWeight;
	}

	private double getMean(ExampleSet exampleSet, Attribute currentAttribute) {
		double accumulatedValue = 0;
		for (Example example : exampleSet) {
			double value = example.getValue(currentAttribute);
			if (!Double.isNaN(value)) 
				accumulatedValue += value;
		}
		return accumulatedValue / exampleSet.size();
	}

	private double getVariance(ExampleSet exampleSet, Attribute currentAttribute, double mean) {
		double accumulatedValue = 0;
		for (Example example: exampleSet) {
			double value = example.getValue(currentAttribute);
			if (!Double.isNaN(value)) 
				accumulatedValue += Math.pow(value - mean, 2);
		}
		return Math.sqrt(accumulatedValue / (exampleSet.size() - 1));
	}

	private double getWeightOfList(ArrayList<Tupel<Double, Double>> bagValues) {
		double weightSum = 0;
		for(Tupel<Double, Double> tupel: bagValues) {
			weightSum += tupel.getSecond().doubleValue();
		}
		return weightSum / bagValues.size();
	}

	private double getMean(ArrayList<Double> valueCollection, Attribute attribute) {
		double accumulatedValue = 0;
		for (Double value : valueCollection) {
			accumulatedValue += value.doubleValue();
		}
		return accumulatedValue / valueCollection.size();
	}

	/**
	 * calculating mean of examples in respect to example weight
	 */
	private double getWeightedMean(ArrayList<Tupel<Double, Double>> valueCollection, Attribute attribute) {
		double accumulatedValue = 0;
		double totalWeight = 0;
		for (Tupel<Double, Double> tupel: valueCollection) {
			double exampleWeight = tupel.getSecond().doubleValue();
			accumulatedValue += tupel.getFirst().doubleValue() * exampleWeight;
			totalWeight += exampleWeight;
		}
		return accumulatedValue / totalWeight;
	}

	private double getVariance(ArrayList<Double> valueCollection, Attribute attribute, double mean) {
		double accumulatedValue = 0;
		for (Double value: valueCollection) {
			accumulatedValue += Math.pow(value - mean, 2);
		}
		return Math.sqrt(accumulatedValue / (valueCollection.size() - 1));
	}

	/**
	 * calculating deviation of examples in respect to example weight
	 */
	private double getWeightedVariance(ArrayList<Tupel<Double, Double>> tupelCollection, Attribute attribute, double mean) {
		double accumulatedValue = 0;
		double totalWeight = 0;
		for (Tupel<Double, Double> tupel: tupelCollection) {
			double exampleWeight = tupel.getSecond().doubleValue();
			accumulatedValue += exampleWeight * Math.pow(tupel.getFirst().doubleValue() - mean, 2);
			totalWeight += exampleWeight;
		}
		double size = tupelCollection.size();
		return Math.sqrt(accumulatedValue / (totalWeight - (totalWeight / size)));
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
		ParameterType type = new ParameterTypeBoolean(PARAMETER_USE_KERNEL, "Using kernels might reduce error", false);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeBoolean(PARAMETER_USE_EXAMPLE_WEIGHTS, "Use example weights if exists", true);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_NUMBER_OF_KERNELS, "maximal number of kernels. 0 for infinite",0,Integer.MAX_VALUE, 200);
		type.setExpert(true);
		types.add(type);
		return types;
	}
}
