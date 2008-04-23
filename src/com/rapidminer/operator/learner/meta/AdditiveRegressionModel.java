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
package com.rapidminer.operator.learner.meta;

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.Tools;


/**
 * The model created by an AdditiveRegression meta learner.
 * 
 * @author Ingo Mierswa
 * @version $Id: AdditiveRegressionModel.java,v 1.3 2007/07/13 22:52:11 ingomierswa Exp $
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
	
	public void performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
		// apply default model
		defaultModel.apply(exampleSet);
		double[] predictions = new double[exampleSet.size()];
		Iterator<Example> e = exampleSet.iterator();
		int counter = 0;
		while (e.hasNext()) {
			predictions[counter++] = e.next().getPredictedLabel();
		}
		PredictionModel.removePredictedLabel(exampleSet);

		// apply all models to the example set sum up the predictions
		for (int i = 0; i < residualModels.length; i++) {
			residualModels[i].apply(exampleSet);
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
		Attribute newPredictedLabel = createPredictedLabel(exampleSet);
		while (e.hasNext()) {
			e.next().setValue(newPredictedLabel, predictions[counter++]);
		}
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
