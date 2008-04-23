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

import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.Distribution;
import com.rapidminer.operator.learner.DistributionPlotter;
import com.rapidminer.operator.learner.SimplePredictionModel;
import com.rapidminer.tools.Tools;


/**
 * DistributionModel is a model for learner, which generate estimated distributions for prediction. For all classes and all attributes at least one
 * distribution must be added. Predicted will be the class with the highest probability. The classprobability is the product of the probability of
 * every attribute. The probability for an attribute is the mean of the probabilities of every distribution.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: DistributionModel.java,v 1.5 2007/07/13 22:52:15 ingomierswa Exp $
 */
public class DistributionModel extends SimplePredictionModel {

	private static final long serialVersionUID = -402827845291958569L;

	private Map<Integer, Map<Integer, Distribution>> classDistributions;

	private int numberOfClasses;

	private double[] classProbabilities;

	/** Only used for result displaying. */
	private String[] attributeNames;

	public DistributionModel(ExampleSet exampleSet, int numberOfClasses, double[] classProbabilities, String[] attributeNames) {
		super(exampleSet);
		this.numberOfClasses = numberOfClasses;
		this.classProbabilities = classProbabilities.clone();
		classDistributions = new HashMap<Integer, Map<Integer, Distribution>>(numberOfClasses);
		this.attributeNames = attributeNames;
	}

	public void addDistribution(int classIndex, int attributeIndex, Distribution distribution) {
		Map<Integer, Distribution> attributes;
		if (classDistributions.containsKey(classIndex)) {
			attributes = classDistributions.get(classIndex);
		} else {
			attributes = new HashMap<Integer, Distribution>();
			classDistributions.put(classIndex, attributes);
		}
		attributes.put(attributeIndex, distribution);
	}

	public double predict(Example example) throws OperatorException {
		double maxProbability = 0;
		int maxProbableClass = 0;
		for (int i = 0; i < numberOfClasses; i++) {
			double probability = getProbabilityForClass(i, example);
			example.setConfidence(getLabel().getMapping().mapIndex(i), probability);
			if (probability > maxProbability) {
				maxProbability = probability;
				maxProbableClass = i;
			}
		}
		return maxProbableClass;
	}

	public double getProbabilityForClass(int classIndex, Example example) {
		double probability = classProbabilities[classIndex];
		int i = 0;
		for (Attribute attribute : example.getAttributes()) {
			probability *= getProbabilityForAttribute(classIndex, attribute, i++, example);
		}
		return probability;
	}

	public double getProbabilityForAttribute(int classIndex, Attribute attribute, int attributeIndex, Example example) {
		Distribution distribution = classDistributions.get(classIndex).get(attributeIndex);
		return distribution.getProbability(example.getValue(attribute));
	}

	public Component getVisualisationComponent(IOContainer container) {
		return new DistributionPlotter(this);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (Integer i : classDistributions.keySet()) {
			String classTitle = "Class " + getLabel().getMapping().mapIndex(i) + " (" + Tools.formatNumber(classProbabilities[i]) + "):"; 
			buffer.append(Tools.getLineSeparator() + classTitle + Tools.getLineSeparator());
			buffer.append(getLine("=", classTitle.length()));
			buffer.append(Tools.getLineSeparator());
			for (Integer j : classDistributions.get(i).keySet()) {
				String attributeTitle = "Attribute " + attributeNames[j]; 
				buffer.append(Tools.getLineSeparator() + attributeTitle + Tools.getLineSeparator());
				buffer.append(getLine("-", attributeTitle.length()) + Tools.getLineSeparator());
				buffer.append(classDistributions.get(i).get(j).toString());
				buffer.append(Tools.getLineSeparator());
			}
			buffer.append(Tools.getLineSeparator());
		}
		return buffer.toString();
	}

	private String getLine(String character, int number) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < number; i++)
			result.append(character);
		return result.toString();
	}
	
	public Iterator<Integer> getClasses() {
		return classDistributions.keySet().iterator();
	}

	public Iterator<Integer> getAttributes(int classIndex) {
		return classDistributions.get(classIndex).keySet().iterator();
	}

	public Distribution getDistribution(int classIndex, int attributeIndex) {
		return classDistributions.get(classIndex).get(attributeIndex);
	}
}
