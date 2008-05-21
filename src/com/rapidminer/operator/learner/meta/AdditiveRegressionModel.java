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

import javax.swing.JTabbedPane;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.tools.ExtendedJTabbedPane;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.Tools;


/**
 * The model created by an AdditiveRegression meta learner.
 * 
 * @author Ingo Mierswa
 * @version $Id: AdditiveRegressionModel.java,v 1.7 2008/05/09 19:22:47 ingomierswa Exp $
 */
public class AdditiveRegressionModel extends PredictionModel {

	private static final long serialVersionUID = -8036434608645810089L;

	private Model defaultModel;
	
	private Model[] residualModels;
	
	private double shrinkage;
	
	public AdditiveRegressionModel(ExampleSet exampleSet, Model defaultModel, Model[] residualModels, double shrinkage) {
		super(exampleSet);
		this.defaultModel = defaultModel;
		this.residualModels = residualModels;
		this.shrinkage = shrinkage;
	}
	
	public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
		// apply default model
		exampleSet = defaultModel.apply(exampleSet);
		double[] predictions = new double[exampleSet.size()];
		Iterator<Example> e = exampleSet.iterator();
		int counter = 0;
		while (e.hasNext()) {
			predictions[counter++] = e.next().getPredictedLabel();
		}
		PredictionModel.removePredictedLabel(exampleSet);

		// apply all models to the example set sum up the predictions
		for (int i = 0; i < residualModels.length; i++) {
			exampleSet = residualModels[i].apply(exampleSet);
			e = exampleSet.iterator();
			counter = 0;
			while (e.hasNext()) {
				predictions[counter++] += shrinkage * e.next().getPredictedLabel();
			}
			PredictionModel.removePredictedLabel(exampleSet);
		}
		
		// set final predictions
		e = exampleSet.iterator();
		counter = 0;
		Attribute newPredictedLabel = createPredictedLabel(exampleSet, getLabel());
		while (e.hasNext()) {
			e.next().setValue(newPredictedLabel, predictions[counter++]);
		}
		
		return exampleSet;
	}

	public Component getVisualizationComponent(IOContainer container) {
		JTabbedPane tabPane = new ExtendedJTabbedPane();
		tabPane.add("Default Model", defaultModel.getVisualizationComponent(container));
		int index = 1;
		for (Model residualModel : residualModels) {
			tabPane.add("Model " + index, residualModel.getVisualizationComponent(container));
			index++;
		}
		return tabPane;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer(super.toString());
		result.append("Default model:" + Tools.getLineSeparator() + this.defaultModel.toString() + Tools.getLineSeparator() + Tools.getLineSeparator());
		result.append("Number of base models: " + this.residualModels.length + Tools.getLineSeparator());
		result.append("Shrinkage: " + this.shrinkage + Tools.getLineSeparator());
		for (int i = 0; i < this.residualModels.length; i++) {
			result.append(Tools.getLineSeparator() + Tools.ordinalNumber(i+1) + " Model:" + Tools.getLineSeparator() + this.residualModels[i] + Tools.getLineSeparator());
		}
		return result.toString();
	}
}
