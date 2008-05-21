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
import java.util.ArrayList;
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
import com.rapidminer.tools.Tools;


/**
 * MultiModels are used for multi class learning tasks. A MultiModel contains a
 * set of Models that can handle only two-class decisions.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: MultiModel.java,v 1.5 2008/05/09 19:22:47 ingomierswa Exp $
 */
public class MultiModel extends PredictionModel {

	private static final long serialVersionUID = -8483834213562711715L;
	
	private Model[] models;

	public MultiModel(ExampleSet exampleSet, Model[] models) {
		super(exampleSet);
		this.models = models;
	}

	public int getNumberOfModels() {
		return models.length;
	}

	/** Returns a binary decision model for the given classification index. */
	public Model getModel(int index) {
		return models[index];
	}

	/**
	 * Iterates over all classes of the label and applies one model for each
	 * class. For each example the predicted label is determined by choosing the
	 * model with the highest confidence.
	 */
	public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {

		ExampleSet[] eSet = new ExampleSet[getNumberOfModels()];

		for (int i = 0; i < getNumberOfModels(); i++) {
			Model model = getModel(i);
			eSet[i] = (ExampleSet) exampleSet.clone();
			eSet[i] = model.apply(eSet[i]);
		}

		List<Iterator<Example>> reader = new ArrayList<Iterator<Example>>(eSet.length);
		for (int r = 0; r < eSet.length; r++)
			reader.add(eSet[r].iterator());

		Iterator<Example> originalReader = exampleSet.iterator();
		while (originalReader.hasNext()) {
			double bestLabel = Double.NaN;
			double highestFunctionValue = Double.NEGATIVE_INFINITY;
			for (int k = 0; k < reader.size(); k++) {
				double functionValue = reader.get(k).next().getPredictedLabel();
				if (functionValue > highestFunctionValue) {
					highestFunctionValue = functionValue;
					bestLabel = k;
				}
			}
			originalReader.next().setPredictedLabel(bestLabel);
		}
		
		return exampleSet;
	}

	public Component getVisualizationComponent(IOContainer container) {
		JTabbedPane tabPane = new ExtendedJTabbedPane();
		for (int i = 0; i < this.getNumberOfModels(); i++) {
			Model model = this.getModel(i);
			tabPane.add("Model " + (i + 1), model.getVisualizationComponent(container));
		}
		return tabPane;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer(super.toString() + Tools.getLineSeparator());
		for (int i = 0; i < models.length; i++)
			result.append((i > 0 ? Tools.getLineSeparator() : "") + models[i].toString());
		return result.toString();
	}
}
