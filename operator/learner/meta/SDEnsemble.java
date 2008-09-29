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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTabbedPane;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.gui.tools.ExtendedJTabbedPane;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;


/**
 * A subgroup discovery model.
 * 
 * @author Martin Scholz
 * @version $Id: SDEnsemble.java,v 1.11 2008/05/09 19:22:47 ingomierswa Exp $
 */
public class SDEnsemble extends PredictionModel {

	private static final long serialVersionUID = 1320495411014477089L;

	public static final short RULE_COMBINE_ADDITIVE = 1;

	public static final short RULE_COMBINE_MULTIPLY = 2;

	// Holds the models and their weights in array format.
	// Please access with getter methods.
	private List modelInfo;

	// If set to a value i >= 0 then only the first i models are applied
	private int maxModelNumber = -1;

	private static final String MAX_MODEL_NUMBER = "iteration";

	// name of a parameter that allows to specify a file to print predictions to
	private static final String PRED_TO_FILE = "predictions_to_file";

	// the file to print to, null if turned off
	private File predictionsFile = null;

	// if set to true then some statistics are printed to stdout
	private boolean print_to_stdout = false;

	// The classes priors in the training set, starting with index 0.
	private double[] priors;
    
	/**
	 * @param exampleSet
	 *            the example set used for training
	 * @param modelInfo
	 *            a <code>List</code> of <code>Object[2]</code> arrays, each
	 *            entry holding a model and a <code>double[][]</code> array
	 *            containing weights for all prediction/label combinations.
	 * @param priors
	 *            an array of the prior probabilities of labels
	 */
	public SDEnsemble(ExampleSet exampleSet, List modelInfo, double[] priors, short combinationMethod) {
		super(exampleSet);
		this.modelInfo = modelInfo;
		this.priors = priors;
	}

	public Component getVisualizationComponent(IOContainer container) {
		JTabbedPane tabPane = new ExtendedJTabbedPane();
		for (int i = 0; i < this.getNumberOfModels(); i++) {
			Model model = this.getModel(i);
			tabPane.add("Model " + (i + 1), model.getVisualizationComponent(container));
		}
		return tabPane;
	}
	
	/** @return a <code>String</code> representation of the ruleset. */
	public String toString() {
		StringBuffer result = new StringBuffer(super.toString() + (Tools.getLineSeparator() + "Number of inner models: " + this.getNumberOfModels()));
		for (int i = 0; i < this.getNumberOfModels(); i++) {
			Model model = this.getModel(i);
			result.append((i > 0 ? Tools.getLineSeparator() : "")
			// + "Weights: " + this.getFactorForModel(i, true) + ","
					// + this.getFactorForModel(i, false) + " - "
					+ "(Embedded model #" + i + "):" + model.toResultString());
		}
		return result.toString();
	}

	/**
	 * Setting the parameter <code>MAX_MODEL_NUMBER</code> allows to discard
	 * all but the first n models for specified n. <code>PRED_TO_FILE</code>
	 * requires a filename on the local disk system the predictions of the
	 * single classifiers are written to. <code>print_to_stdout</code> prints
	 * some statistics about the base classifiers to the standard output.
	 */
	public void setParameter(String name, String value) throws OperatorException {
		if (name.equalsIgnoreCase("print_to_stdout")) {
			this.print_to_stdout = true;
			return;
		} else if (name.equalsIgnoreCase(PRED_TO_FILE)) {
			if (value != null) {
				String filename = value;
				File file = new File(filename);
				if (file.exists()) {
					boolean result = file.delete();
					if (!result)
						LogService.getGlobal().logError("Cannot delete file: " + file);
				}
				try {
					file.createNewFile();
				} catch (IOException e) {
					throw new UserError(null, 303, filename, e.getMessage());
				}
				this.predictionsFile = file;
				return;
			}
		} else
			try {
				if (name.equalsIgnoreCase(MAX_MODEL_NUMBER)) {
					this.maxModelNumber = Integer.parseInt(value);
					return;
				}
			} catch (NumberFormatException e) {}

		super.setParameter(name, value);
	}

	/** @return the number of embedded models */
	public int getNumberOfModels() {
		if (this.maxModelNumber >= 0)
			return Math.min(this.maxModelNumber, modelInfo.size());
		else
			return modelInfo.size();
	}

	/**
	 * Gets weights for models in the case of general nominal class labels. The
	 * indices are not in RapidMiner format, so add
	 * <code>Attribute.FIRST_CLASS_INDEX</code> before calling this method and
	 * before reading from the returned array.
	 * 
	 * @return a <code>double[]</code> object with the weights to be applied
	 *         for each class if the corresponding rule yields
	 *         <code>predicted</code>.
	 * @param modelNr
	 *            the number of the model
	 * @param predicted
	 *            the predicted label
	 * @return a <code>double[]</code> with one weight per class label.
	 */
	private double[] getWeightsForModel(int modelNr, int predicted) {
		Object[] obj = (Object[]) this.modelInfo.get(modelNr);
		double[][] weight = (double[][]) obj[1];
		return weight[predicted];
	}

	/**
	 * Getter method for prior class probabilities estimated as the relative
	 * frequencies in the training set.
	 * 
	 * @param classIndex
	 *            the index of a class starting with 0 (not the internal representation!)
	 * @return the prior probability of the specified class
	 */
	private double getPriorOfClass(int classIndex) {
		return this.priors[classIndex];
	}

	/**
	 * Getter method for embedded models
	 * 
	 * @param index
	 *            the number of a model part of this boost model
	 * @return binary or nominal decision model for the given classification
	 *         index.
	 */
	public Model getModel(int index) {
		Object[] obj = (Object[]) this.modelInfo.get(index);
		return (Model)obj[0];
	}

	/**
	 * Iterates over all models and returns the class with maximum likelihood.
	 * 
	 * @param exampleSet
	 *            the set of examples to be classified
	 */
	public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabelAttribute) throws OperatorException {
		// If parameter is set than the single predictions are written to file:
		PrintStream predOut = null;
		if (this.predictionsFile != null) {
			try {
				// Create an output stream to write the predictions to:
				predOut = new PrintStream(new BufferedOutputStream(new FileOutputStream(this.predictionsFile)));
			} catch (IOException e) {
				throw new UserError(null, 303, this.predictionsFile.getName(), e.getMessage());
			} finally {
				if (predOut != null) {
					predOut.close();
				}
			}
		}

		// Prepare an ExampleSet for each model.
		ExampleSet[] eSet = new ExampleSet[this.getNumberOfModels()];

		for (int i = 0; i < this.getNumberOfModels(); i++) {
			Model model = this.getModel(i);
			eSet[i] = (ExampleSet) exampleSet.clone();
			eSet[i] = model.apply(eSet[i]);
		}

		// Prepare one ExampleReader per ExampleSet
		List<Iterator<Example>> reader = new ArrayList<Iterator<Example>>(eSet.length);
		for (int r = 0; r < eSet.length; r++) {
			reader.add(eSet[r].iterator());
		}

		// Apply all models:
		Iterator<Example> originalReader = exampleSet.iterator();
		final int posIndex = SDRulesetInduction.getPosIndex(exampleSet.getAttributes().getLabel());

		// <statistics per rule>
		int[] numCovered = new int[this.getNumberOfModels()];
		int[] posCovered = new int[this.getNumberOfModels()];
		int posTotal = 0;
		// </statistics per rule>

		while (originalReader.hasNext()) {
			Example example = originalReader.next();
			double sumPos = 0;
			double sumTotal = 0;
			for (int k = 0; k < reader.size(); k++) {
				Example e = reader.get(k).next();

				if (predOut != null) {
					predOut.print(e.getPredictedLabel() + " ");
				}

				double[] modelWeights;
				int predicted = ((int) e.getPredictedLabel());
				modelWeights = this.getWeightsForModel(k, predicted);
				for (int i = 0; i < modelWeights.length; i++) {
					sumTotal += modelWeights[i];
				}
				sumPos += modelWeights[posIndex];

				if (this.print_to_stdout) {
					// statistics per rule
					int label = ((int) e.getLabel());
					if (k == 0 && label == posIndex) {
						posTotal++;
					}
					// If "posIndex" is the wrong subset this will be corrected
					// later on.
					if (predicted == posIndex) {
						numCovered[k]++;
						if (label == predicted)
							posCovered[k]++;
					}
				}
			} // end of loop evaluating all models for a single example

			if (predOut != null) {
				predOut.println(example.getLabel()); // end line for the
														// predictions of this
														// example
			}

			if (sumTotal > 0) {
				sumPos /= sumTotal;
			} else {
				sumPos = this.getPriorOfClass(posIndex);
			}
			example.setPredictedLabel(sumPos);
		}

		// Closes the file storing the single predictions:
		if (predOut != null) {
			predOut.close();
		}
		
		return exampleSet;
	}

	/**
	 * Creates a predicted label with the given name. If name is null, the name
	 * &quot;prediction(labelname)&quot; is used.
	 */
	protected Attribute createPredictedLabel(ExampleSet exampleSet) {
		Attribute predictedLabel = super.createPredictedLabel(exampleSet, getLabel());
		return exampleSet.getAttributes().replace(predictedLabel, AttributeFactory.changeValueType(predictedLabel, Ontology.REAL));
	}

}
