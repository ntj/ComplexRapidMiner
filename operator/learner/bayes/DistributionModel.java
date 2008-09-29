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
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.plotter.DistributionPlotter;
import com.rapidminer.gui.tools.JRadioSelectionPanel;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.report.Renderable;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.math.distribution.DiscreteDistribution;
import com.rapidminer.tools.math.distribution.Distribution;
import com.rapidminer.tools.math.distribution.NormalDistribution;


/**
 * DistributionModel is a model for learners which estimate distributions 
 * of attribute values from example sets like NaiveBayes.
 * 
 * Predictions are calculated as product of the conditional probabilities
 * for all attributes times the class probability.
 * 
 * The basic learning concept is to simply count occurances of classes
 * and attribute values. This means no propabilities are calculated during
 * the learning step. This is only done before output. Optionally, this
 * calculation can apply a Laplace correction which means in particular
 * that zero probabilities are avoided which would hide information
 * in distributions of other attributes.
 * 
 * @author Tobias Malbrecht
 * @version $Id: DistributionModel.java,v 1.24 2008/08/07 09:00:59 tobiasmalbrecht Exp $
 */
public class DistributionModel extends PredictionModel implements Renderable {

	private static final long serialVersionUID = -402827845291958569L;

	private static final String UNKNOWN_VALUE_NAME = "unknown";

	private static final int INDEX_VALUE_SUM = 0;
	
	private static final int INDEX_SQUARED_VALUE_SUM = 1;
	
	private static final int INDEX_MISSING_WEIGHTS = 2;
	
	private static final int INDEX_MEAN = 0;
	
	private static final int INDEX_STANDARD_DEVIATION = 1;
	
	/** The number of classes. */
	private int numberOfClasses;
	
	/** The number of attributes. */
	private int numberOfAttributes;
	
	/** Flags indicating which attribute is nominal. */
	private boolean[] nominal;
	
	/** Class name (used for result displaying). */
	private String className;
	
	/** Class values (used for result displaying). */
	private String[] classValues;
	
	/** Attribute names (used for result displaying). */
	private String[] attributeNames;

	/** Nominal attribute values (used for result displaying). */
	private String[][] attributeValues;
	
	/** Total weight (or number) of examples used to build the model. */  
	private double totalWeight;
	
	/** Total weight of examples belonging to the separate classes. */
	private double[] classWeights;
	
	/** 
	 * Specifies the total weight of examples in which the different combinations
	 * of classes and (nominal) attribute values co-occur. In the case of numeric
	 * attributes the (weighted) sum and the (weighted) sum of the squared
	 * attribute values are stored which are needed to calculate the mean and the
	 * standard deviation/variance of the resulting (assumed) normal distribution.  
	 * 
	 * Array dimensions:
	 * 	 1st: attribtues
	 * 	 2nd: classes
	 * 	 3nd: nominal values or value sum (index=0) and squared value sum (index=1)
	 */
	private double[][][] weightSums;
	
	/** Class (a-priori) probabilites. */
	private double[] priors;
	
	/** 
	 * Specifies the a-postiori distributions. Contains the a-postiori probabilites
	 * that certain values occur given the class value for nominal values. Contains
	 * the means and standard deviations for numerical attributes.
	 * 
	 * Array dimensions:
	 * 	 1st: attribtues
	 * 	 2nd: classes
	 * 	 3nd: nominal values or mean (index=0) and standard deviation (index=1)
	 */
	private double[][][] distributionProperties;
	
	/**
	 * Captures if laplace correction should be applied when calculating
	 * probabilities.
	 */
	boolean laplaceCorrectionEnabled;
	
	/**
	 * Indicates if the model has recently been updated and the actual probabilites
	 * have to be calculated.
	 */
	private boolean modelRecentlyUpdated;

	private DistributionPlotter plotter;

	public DistributionModel(ExampleSet exampleSet) {
		this(exampleSet, true);
	}
	
	public DistributionModel(ExampleSet exampleSet, boolean laplaceCorrectionEnabled) {
		super(exampleSet);
		this.laplaceCorrectionEnabled = laplaceCorrectionEnabled;
		Attribute labelAttribute = exampleSet.getAttributes().getLabel();
		numberOfClasses = labelAttribute.getMapping().size();
		numberOfAttributes = exampleSet.getAttributes().size();
		nominal = new boolean[numberOfAttributes];
		attributeNames = new String[numberOfAttributes];
		attributeValues = new String[numberOfAttributes][];
		className = labelAttribute.getName();
		classValues = new String[numberOfClasses];
		for (int i = 0; i < numberOfClasses; i++) {
			classValues[i] = labelAttribute.getMapping().mapIndex(i);
		}
		int attributeIndex = 0;
		weightSums = new double[numberOfAttributes][numberOfClasses][];
		distributionProperties = new double[numberOfAttributes][numberOfClasses][];
		for (Attribute attribute : exampleSet.getAttributes()) {
			attributeNames[attributeIndex] = attribute.getName();
			if (attribute.isNominal()) {
				nominal[attributeIndex] = true;
				int mappingSize = attribute.getMapping().size() + 1;
				attributeValues[attributeIndex] = new String[mappingSize];
				for (int i = 0; i < mappingSize - 1; i++) {
					attributeValues[attributeIndex][i] = attribute.getMapping().mapIndex(i);
				}
				attributeValues[attributeIndex][mappingSize - 1] = UNKNOWN_VALUE_NAME;
				for (int i = 0; i < numberOfClasses; i++) {
					weightSums[attributeIndex][i] = new double[mappingSize];
					distributionProperties[attributeIndex][i] = new double[mappingSize];
				}
			} else {
				nominal[attributeIndex] = false;
				for (int i = 0; i < numberOfClasses; i++) {
					weightSums[attributeIndex][i] = new double[3];
					distributionProperties[attributeIndex][i] = new double[2];
				}
			}
			attributeIndex++;
		}

		//  initialization of total and a priori weight counters
		totalWeight = 0.0d;
		classWeights = new double[numberOfClasses];
		priors = new double[numberOfClasses];

		// update the model
		updateModel(exampleSet);
		
		// calculate the probabilites
		updateDistributionProperties();
	}
	
	public String[] getAttributeNames() {
		return this.attributeNames;
	}
	
	/**
	 * Updates the model by counting the occurances of classes and attribute values
	 * in combination with the class values.
	 * 
	 * ATTENTION: only updates the weight counters, distribution properties are not
	 * updated, call updateDistributionProperties() to accomplish this task
	 */
	public void updateModel(ExampleSet exampleSet) {
		Attribute weightAttribute = exampleSet.getAttributes().getWeight();
		for (Example example : exampleSet) {
			double weight = weightAttribute == null ? 1.0d : example.getWeight();
			totalWeight += weight;
			double labelValue = example.getLabel();
			if (!Double.isNaN(labelValue)) {
				int classIndex = (int) example.getLabel();
				classWeights[classIndex] += weight;
				int attributeIndex = 0;
				for (Attribute attribute : exampleSet.getAttributes()) {
					double attributeValue = example.getValue(attribute);
					if (nominal[attributeIndex]) {					
						if (!Double.isNaN(attributeValue)) {
							weightSums[attributeIndex][classIndex][(int) attributeValue] += weight;
						} else {
							weightSums[attributeIndex][classIndex][weightSums[attributeIndex][classIndex].length - 1] += weight;
						}
					} else {
						if (!Double.isNaN(attributeValue)) {
							weightSums[attributeIndex][classIndex][INDEX_VALUE_SUM] += weight * attributeValue;
							weightSums[attributeIndex][classIndex][INDEX_SQUARED_VALUE_SUM]  += weight * attributeValue * attributeValue;
						} else {
							weightSums[attributeIndex][classIndex][INDEX_MISSING_WEIGHTS] += weight;						
						}
					}
					attributeIndex++;
				}
			}
		}
		modelRecentlyUpdated = true;
	}

	/**
	 * Updates the distribution properties by calculating them on the basis of the
	 * weight counters. 
	 */
	private void updateDistributionProperties() {
		double f = laplaceCorrectionEnabled ? 1 / totalWeight : 0;
		for (int i = 0; i < numberOfClasses; i++) {
			priors[i] = classWeights[i] / totalWeight;
		}
		for (int i = 0; i < numberOfAttributes; i++) {
			if (nominal[i]) {
				for (int j = 0; j < numberOfClasses; j++) {
					for (int k = 0; k < weightSums[i][j].length; k++) {
						distributionProperties[i][j][k] = (weightSums[i][j][k] + f) / (classWeights[j] + f * weightSums[i][j].length); 
					}					
				}
			} else {
				for (int j = 0; j < numberOfClasses; j++) {
					double classWeight = classWeights[j] - weightSums[i][j][INDEX_MISSING_WEIGHTS];
					distributionProperties[i][j][INDEX_MEAN] = weightSums[i][j][INDEX_VALUE_SUM] / classWeight;
					distributionProperties[i][j][INDEX_STANDARD_DEVIATION] = calculateStandardDeviation(weightSums[i][j][INDEX_VALUE_SUM], weightSums[i][j][INDEX_SQUARED_VALUE_SUM], classWeight);
				}
			}
		}
		modelRecentlyUpdated = false;
	}

	/**
	 * Perform predictions based on the distribution properties.
	 */
	public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) {
		if (modelRecentlyUpdated) {
			updateDistributionProperties();
		}
		for (Example example : exampleSet) {
			double[] probabilities = new double[numberOfClasses];
			double maxProbability = Double.NEGATIVE_INFINITY;
			int mostProbableClass = 0;
			double probabilitySum = 0;
			for (int i = 0; i < numberOfClasses; i++) {
				double probability = priors[i];
				int j = 0;
				for (Attribute attribute : exampleSet.getAttributes()) {
					double value = example.getValue(attribute);
					if (nominal[j]) {
						if (!Double.isNaN(value)) {
							probability *= distributionProperties[j][i][(int) value];							
						} else {
							probability *= distributionProperties[j][i][distributionProperties[j][i].length - 1];
						}
					} else {
						if (!Double.isNaN(value)) {
							probability *= NormalDistribution.getProbability(distributionProperties[j][i][INDEX_MEAN], distributionProperties[j][i][INDEX_STANDARD_DEVIATION], value);
						}
					}
					j++;
				}
				probabilities[i] = probability;
				if (probability > maxProbability) {
					maxProbability = probability;
					mostProbableClass = i;
				}
				probabilitySum += probability;

			}
			example.setPredictedLabel(mostProbableClass);
			for (int i = 0; i < numberOfClasses; i++) {
				example.setConfidence(classValues[i], probabilities[i] / probabilitySum);
			}
		}
		return exampleSet;
	}
	
	private double calculateStandardDeviation(double sum, double squaredSum, double totalWeightSum) {
		return Math.sqrt((squaredSum - sum * sum / totalWeightSum) / (totalWeightSum - 1));
	}
	
	public void setLaplaceCorrectionEnabled(boolean laplaceCorrectionEnabled) {
		this.laplaceCorrectionEnabled = laplaceCorrectionEnabled;
	}
	
	public boolean getLaplaceCorrectionEnabled() {
		return laplaceCorrectionEnabled;
	}

	public double getLowerBound(int attributeIndex) {
		if (!nominal[attributeIndex]) {
			double lowerBound = Double.POSITIVE_INFINITY;
			for (int i = 0; i < numberOfClasses; i++) {
				double currentLowerBound = NormalDistribution.getLowerBound(distributionProperties[attributeIndex][i][INDEX_MEAN],
						   													distributionProperties[attributeIndex][i][INDEX_STANDARD_DEVIATION]);
				if (!Double.isNaN(currentLowerBound)) {
					lowerBound = Math.min(lowerBound, currentLowerBound);
				}
			}
			return lowerBound;
		} else {
			return Double.NaN;
		}
	}

	public double getUpperBound(int attributeIndex) {
		if (!nominal[attributeIndex]) {
			double upperBound = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < numberOfClasses; i++) {
				double currentUpperBound = NormalDistribution.getUpperBound(distributionProperties[attributeIndex][i][INDEX_MEAN],
						   													distributionProperties[attributeIndex][i][INDEX_STANDARD_DEVIATION]);
				if (!Double.isNaN(currentUpperBound)) {
					upperBound = Math.max(upperBound, currentUpperBound);
				}
			}
			return upperBound;
		} else {
			return Double.NaN;
		}
	}

	public boolean isDiscrete(int attributeIndex) {
		if (attributeIndex>= 0 && attributeIndex < nominal.length) {
			return nominal[attributeIndex];
		}
		return false;
	}

	public Collection<Integer> getClassIndices() {
		Collection<Integer> classValueIndices = new ArrayList<Integer>(numberOfClasses);
		for (int i = 0; i < numberOfClasses; i++) {
			classValueIndices.add(i);
		}
		return classValueIndices;
	}

	public String getClassName(int index) {
		return classValues[index];
	}
	
	public Distribution getDistribution(int classIndex, int attributeIndex) {
		if (nominal[attributeIndex]) {
			return new DiscreteDistribution(attributeNames[attributeIndex], distributionProperties[attributeIndex][classIndex], attributeValues[attributeIndex]);
		} else {
			return new NormalDistribution(distributionProperties[attributeIndex][classIndex][INDEX_MEAN],
										  distributionProperties[attributeIndex][classIndex][INDEX_STANDARD_DEVIATION]);
		}
	}
	
	public Component getVisualizationComponent(IOContainer container) {
		if (modelRecentlyUpdated) {
			updateDistributionProperties();
		}
		
		JRadioSelectionPanel selectionPanel = new JRadioSelectionPanel();
		
		JPanel graphPanel = new JPanel(new BorderLayout());
		this.plotter = new DistributionPlotter(this);
		graphPanel.add(plotter, BorderLayout.CENTER);
		
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
				plotter.setPlotColumn(combo.getSelectedIndex(), true);
			}
		});
		combo.setSelectedIndex(0);
		selectionPanel.addComponent("Plot View", graphPanel, "Shows a graphical visualisation of the densitiy estimates.");
		selectionPanel.addComponent("Text View", super.getVisualizationComponent(container), "Shows a textual description of the estimated densities.");
		return selectionPanel;
	}
	
    public void prepareRendering() {
    	plotter.prepareRendering();
    }
    
	public int getRenderHeight(int preferredHeight) {
		return plotter.getRenderHeight(preferredHeight);
	}

	public int getRenderWidth(int preferredWidth) {
		return plotter.getRenderWidth(preferredWidth);
	}

	public void render(Graphics graphics, int width, int height) {
		plotter.paintComponent(graphics, width, height);
	}
	
	public String toString() {
		if (modelRecentlyUpdated) {
			updateDistributionProperties();
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("Distribution model for class " + className);
		buffer.append(Tools.getLineSeparators(2));
		for (int i = 0; i < numberOfClasses; i++) {
			String classTitle = "Class " + classValues[i] + " (" + Tools.formatNumber(priors[i]) + ")"; 
			buffer.append(Tools.getLineSeparator());
			buffer.append(classTitle);
			buffer.append(Tools.getLineSeparator());
			buffer.append(getDividerLine("=", classTitle.length()));
			buffer.append(Tools.getLineSeparator());
			for (int j = 0; j < attributeNames.length; j++) {
				String attributeTitle = "Attribute " + attributeNames[j];
				buffer.append(Tools.getLineSeparator());
				buffer.append(attributeTitle);
				buffer.append(Tools.getLineSeparator());
				buffer.append(getDividerLine("-", attributeTitle.length()));
				buffer.append(Tools.getLineSeparator());	
				buffer.append(getDistribution(i, j));
				buffer.append(Tools.getLineSeparator());
			}
			buffer.append(Tools.getLineSeparator());
		}
		return buffer.toString();
	}
	
	private String getDividerLine(String character, int length) {
		StringBuffer deviderLine = new StringBuffer();
		for (int i = 0; i < length; i++) {
			deviderLine.append(character);
		}
		return deviderLine.toString();
	}
}
