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
package com.rapidminer.operator.learner.meta;

import java.awt.Component;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTabbedPane;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.tools.ExtendedJTabbedPane;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;


/**
 * A model for the RapidMiner AdaBoost implementation.
 * 
 * @author Martin Scholz
 * @version $Id: AdaBoostModel.java,v 1.6 2008/05/09 19:22:48 ingomierswa Exp $
 */
public class AdaBoostModel extends PredictionModel {

	private static final long serialVersionUID = -4145136493164813582L;

	// Holds the models
	private List<Model> models;

	// Holds the weights
	private List<Double> weights;

	// If set to a value i >= 0 then only the first i models are applied
	private int maxModelNumber = -1;

	private static final String MAX_MODEL_NUMBER = "iteration";

	public AdaBoostModel(ExampleSet exampleSet, List<Model> models, List<Double> weights) {
		super(exampleSet);
		this.models = models;
		this.weights = weights;
		
		for (double i: weights) {
			if (Double.isNaN(i) || Double.isInfinite(i)) {
				logWarning("Found model weight " + i);
			}
		}
	}

	/**
	 * Setting the parameter <code>MAX_MODEL_NUMBER</code> allows to discard
	 * all but the first n models for specified n.
	 */
	public void setParameter(String name, String value) throws OperatorException {
		if (name.equalsIgnoreCase(MAX_MODEL_NUMBER)) {
			try {
				this.maxModelNumber = Integer.parseInt(value);
				return;
			}
			catch (NumberFormatException e) {}
		}
		super.setParameter(name, value);
	}

	/**
	 * Using this setter with a positive value makes the model discard all
	 * but the specified number of base models. A value of -1 turns off this
	 * option.
	 */
	public void setMaxModelNumber(int numModels) {
		this.maxModelNumber = numModels;
	}

	public Component getVisualizationComponent(IOContainer container) {
		JTabbedPane tabPane = new ExtendedJTabbedPane();
		for (int i = 0; i < this.getNumberOfModels(); i++) {
			Model model = this.getModel(i);
			tabPane.add("Model " + (i + 1) + " [w = " + Tools.formatNumber(getWeightForModel(i)) + "]", model.getVisualizationComponent(container));
		}
		return tabPane;
	}
	
	/** @return a <code>String</code> representation of this boosting model. */
	public String toString() {
		StringBuffer result = new StringBuffer(super.toString() + Tools.getLineSeparator() + "Number of inner models: " + this.getNumberOfModels() + Tools.getLineSeparators(2));
		for (int i = 0; i < this.getNumberOfModels(); i++) {
			Model model = this.getModel(i);
			result.append((i > 0 ? Tools.getLineSeparator() : "") + "Embedded model #" + i + " (weight: " + Tools.formatNumber(this.getWeightForModel(i)) + "): "
			       + Tools.getLineSeparator() + model.toResultString());
		}
		return result.toString();
	}

	/** @return the number of embedded models */
	public int getNumberOfModels() {
		if (this.maxModelNumber >= 0) {
			return Math.min(this.maxModelNumber, this.models.size());
		}
		else {
			return this.models.size();
		}
	}

	private double getWeightForModel(int modelNr) {
		return this.weights.get(modelNr);
	}

	/**
	 * Getter method for embedded models
	 * 
	 * @param index
	 *            the number of a model part of this boost model
	 * @return binary or nominal decision model
	 */
	public Model getModel(int index) {
		return this.models.get(index);
	}

	/**
	 * Iterates over all models and returns the class with maximum likelihood.
	 * 
	 * @param origExampleSet
	 *            the set of examples to be classified
	 */
	public ExampleSet performPrediction(ExampleSet origExampleSet, Attribute predictedLabel) throws OperatorException {
		final String attributePrefix = "AdaBoostModelPrediction";
		final int numLabels = predictedLabel.getMapping().size();
		final Attribute[] specialAttributes = new Attribute[numLabels];
		for (int i = 0; i < numLabels; i++) {
			specialAttributes[i] =
				com.rapidminer.example.Tools.createSpecialAttribute(origExampleSet, attributePrefix + i, Ontology.NUMERICAL);
		}

		Iterator<Example> reader = origExampleSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			for (int i = 0; i < specialAttributes.length; i++) {
				example.setValue(specialAttributes[i], 0);
			}
		}

		reader = origExampleSet.iterator();
		for (int modelNr = 0; modelNr < this.getNumberOfModels(); modelNr++) {
			Model model = this.getModel(modelNr);
			ExampleSet exampleSet = (ExampleSet) origExampleSet.clone();
			exampleSet = model.apply(exampleSet);
			this.updateEstimates(exampleSet, modelNr, specialAttributes);
			PredictionModel.removePredictedLabel(exampleSet);
		}

		// Turn prediction weights into confidences and a crisp predcition:
		this.evaluateSpecialAttributes(origExampleSet, specialAttributes);

		// Clean up attributes:
		for (int i = 0; i < numLabels; i++) {
			origExampleSet.getAttributes().remove(specialAttributes[i]);
			origExampleSet.getExampleTable().removeAttribute(specialAttributes[i]);
		}
		
		return origExampleSet;
	}

	private void updateEstimates(ExampleSet exampleSet, int modelNr, Attribute[] specialAttributes) {
		Iterator<Example> reader = exampleSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			int predicted = (int) example.getPredictedLabel();
			
			double oldValue = example.getValue(specialAttributes[predicted]);
			if (Double.isNaN(oldValue)) {
			    logWarning("Found NaN confidence as intermediate prediction.");
				oldValue = 0;
			}

			if ( ! Double.isInfinite(oldValue)) {
				example.setValue(specialAttributes[predicted], oldValue + this.getWeightForModel(modelNr));
			}
		}
	}

	private void evaluateSpecialAttributes(ExampleSet exampleSet, Attribute[] specialAttributes) {
		Attribute exSetLabel = exampleSet.getAttributes().getLabel();
		Iterator<Example> reader = exampleSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			double sum = 0;
			double[] confidences = new double[specialAttributes.length];
			double bestConf = -1;
			int bestLabel = 0;
			for (int n = 0; n < confidences.length; n++) {
				confidences[n] = example.getValue(specialAttributes[n]);
				if (confidences[n] > bestConf) {
					bestConf = confidences[n];
					bestLabel = n;
				}
			}

			example.setValue(example.getAttributes().getPredictedLabel(), exSetLabel.getMapping().mapString(this.getLabel().getMapping().mapIndex(bestLabel)));
			
			for (int n = 0; n < confidences.length; n++) {								
				confidences[n] = Math.exp(confidences[n] - bestConf);
				// remember for normalization:
				sum += confidences[n];
			}

			// Normalize and set confidence values for all classes:
			if (Double.isInfinite(sum) || Double.isNaN(sum)) {
				int best = (int) example.getPredictedLabel();
				for (int k = 0; k < confidences.length; k++) {
					confidences[k] = 0;
				}
				confidences[best] = 1;
			} 
			else {
				for (int k = 0; k < confidences.length; k++) {
					confidences[k] /= sum;
					example.setConfidence(exampleSet.getAttributes().getLabel().getMapping().mapIndex(k), confidences[k]);
				}
			}
		}
	}
}
