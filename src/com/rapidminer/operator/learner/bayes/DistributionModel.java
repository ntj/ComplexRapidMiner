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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.gui.JRadioSelectionPanel;
import com.rapidminer.gui.plotter.DistributionPlotter;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.SimplePredictionModel;
import com.rapidminer.tools.Renderable;
import com.rapidminer.tools.Tools;

/**
 * DistributionModel is a model for learner, which generate estimated
 * distributions for prediction. For all classes and all attributes at least one
 * distribution must be added. Predicted will be the class with the highest
 * probability. The class probability is the product of the probability of every
 * attribute. The probability for an attribute is the mean of the probabilities
 * of every distribution.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: DistributionModel.java,v 1.18 2008/05/10 18:28:58 stiefelolm Exp $
 */
public class DistributionModel extends SimplePredictionModel implements	Renderable {

	private static final long serialVersionUID = -402827845291958569L;

	private Map<Integer, Map<Integer, Distribution>> classDistributions;
	
	private NominalMapping classNameMap;

	private int numberOfClasses;

	private double[] classProbabilities;

	/** Only used for result displaying. */
	private String[] attributeNames;

	private DistributionPlotter plotter;

	public DistributionModel(ExampleSet exampleSet, int numberOfClasses,
			double[] classProbabilities, String[] attributeNames) {
		super(exampleSet);
		this.numberOfClasses = numberOfClasses;
		this.classProbabilities = classProbabilities.clone();
		classDistributions = new LinkedHashMap<Integer, Map<Integer, Distribution>>(
				numberOfClasses);
		classNameMap = exampleSet.getAttributes().getLabel().getMapping();
		this.attributeNames = attributeNames;
	}

	public void addDistribution(int classIndex, int attributeIndex,
			Distribution distribution) {
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
		double probabilitySum = 0;
		double probMax = Double.NEGATIVE_INFINITY;
		int maxProbableClass = 0;
		double[] probabilities = new double[numberOfClasses];
		for (int i = 0; i < numberOfClasses; i++) {
			double probability = getProbabilityForClass(i, example);
			probabilities[i] = probability;
			if (probability > probMax) {
				probMax = probability;
				maxProbableClass = i;
			}
			probabilitySum += probability;
		}
		for (int i = 0; i < numberOfClasses; i++) {
			example.setConfidence(getLabel().getMapping().mapIndex(i),
					probabilities[i] / probabilitySum);
		}
		return maxProbableClass;
	}

	public double getProbabilityForClass(int classIndex, Example example) {
		double probability = classProbabilities[classIndex];
		int i = 0;
		for (Attribute attribute : example.getAttributes()) {
			probability *= getProbabilityForAttribute(classIndex, attribute,
					i++, example);
		}
		return probability;
	}

	public double getLowerBound(int attributeIndex) {
		// iterating over every class to find lowest value of attribute
		double lowerBound = Double.POSITIVE_INFINITY;
		for (Integer key : classDistributions.keySet()) {
			lowerBound = Math.min(lowerBound, classDistributions.get(key).get(
					attributeIndex).getLowerBound());
		}
		return lowerBound;
	}

	public double getUpperBound(int attributeIndex) {
		// iterating over every class to find lowest value of attribute
		double upperBound = Double.NEGATIVE_INFINITY;
		for (Integer key : classDistributions.keySet()) {
			upperBound = Math.max(upperBound, classDistributions.get(key).get(
					attributeIndex).getUpperBound());
		}
		return upperBound;
	}

	public Collection<Double> getValues(int attributeIndex) {
		Collection<Double> values = new HashSet<Double>();
		for (Integer key : classDistributions.keySet()) {
			values.addAll(classDistributions.get(key).get(attributeIndex)
					.getValues());
		}
		return values;
	}

	private double getProbabilityForAttribute(int classIndex,
			Attribute attribute, int attributeIndex, Example example) {
		Distribution distribution = classDistributions.get(classIndex).get(
				attributeIndex);
		double value = example.getValue(attribute);
		if (!Double.isNaN(value))
			return distribution.getProbability(example.getValue(attribute))
				* classProbabilities[classIndex];
		else
			return 1d;
	}
	/**
	 * This method returns the posteriori probability of a value
	 * in given attribute and class. So the value of density of the 
	 * attribute of the given class is weighted by apriori probability of class.
	 * @param classIndex the class
	 * @param attributeIndex the number of the attribute
	 * @param value this values' density will be returned
	 */
	public double getProbabilityForAttribute(int classIndex,
			int attributeIndex, double value) {
		Distribution distribution = classDistributions.get(classIndex).get(
				attributeIndex);
		return distribution.getProbability(value) * classProbabilities[classIndex];
	}

	public Component getVisualizationComponent(IOContainer container) {
		JRadioSelectionPanel selectionPanel = new JRadioSelectionPanel();
		
		JPanel graphPanel = new JPanel(new BorderLayout());
		this.plotter = new DistributionPlotter(this);
		graphPanel.add(plotter, BorderLayout.CENTER);
		Vector<String> attributeNames = new Vector<String>();
		for (int attributeIndex : classDistributions.get(0).keySet()) {
			attributeNames.add(getAttributeName(attributeIndex));
		}
		
		final JComboBox combo = new JComboBox(attributeNames);
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.0d;
		c.weightx = 1.0d;
		c.insets = new Insets(4,4,4,4);
		c.gridwidth = GridBagConstraints.REMAINDER;
		
		JPanel boxPanel = new JPanel(layout);
		JLabel label = new JLabel("Attribute:");
		layout.setConstraints(label, c);
		boxPanel.add(label);
		
		layout.setConstraints(combo, c);
		boxPanel.add(combo);
		
		c.weighty = 1.0d;
		JPanel fillPanel = new JPanel();
		layout.setConstraints(fillPanel, c);
		boxPanel.add(fillPanel);
		
		graphPanel.add(boxPanel, BorderLayout.WEST);
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for (int attributeIndex : classDistributions.get(0).keySet()) {
					if (getAttributeName(attributeIndex).equals(
							combo.getModel().getSelectedItem())) {
						plotter.setPlotColumn(attributeIndex, true);
					}
				}
			}

		});
		
		combo.setSelectedIndex(0);
		
		selectionPanel.addComponent("Graph View", graphPanel,
				"Shows graphical visualisation of densitiy estimates");
		selectionPanel.addComponent("Text View", super
				.getVisualizationComponent(container),
				"Shows textual description of estimated densities");
		return selectionPanel;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (Integer i : classDistributions.keySet()) {
			String classTitle = "Class " + getLabel().getMapping().mapIndex(i)
					+ " (" + Tools.formatNumber(classProbabilities[i]) + "):";
			buffer.append(Tools.getLineSeparator() + classTitle
					+ Tools.getLineSeparator());
			buffer.append(getLine("=", classTitle.length()));
			buffer.append(Tools.getLineSeparator());
			for (Integer j : classDistributions.get(i).keySet()) {
				String attributeTitle = "Attribute " + attributeNames[j];
				buffer.append(Tools.getLineSeparator() + attributeTitle
						+ Tools.getLineSeparator());
				buffer.append(getLine("-", attributeTitle.length())
						+ Tools.getLineSeparator());
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

	public Collection<Integer> getClasses() {
		return classDistributions.keySet();
	}

	public Collection<Integer> getAttributes(int classIndex) {
		return classDistributions.get(classIndex).keySet();
	}

	public Distribution getDistribution(int classIndex, int attributeIndex) {
		return classDistributions.get(classIndex).get(attributeIndex);
	}

	public String getLabelName(int index) {
		return classNameMap.mapIndex(index);
	}

	public String getAttributeName(int index) {
		return attributeNames[index];
	}

	public void render(Graphics graphics, int width, int height) {
		plotter.paintComponent(graphics, width, height);
	}

	public int getRenderHeight(int preferredHeight) {
		return preferredHeight;
	}

	public int getRenderWidth(int preferredWidth) {
		return preferredWidth;
	}
}
