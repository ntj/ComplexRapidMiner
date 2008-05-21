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

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.Learner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.learner.lazy.DefaultLearner;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.OperatorService;


/**
 * <p>This operator uses  regression learner as a base learner. The learner starts with a default
 * model (mean or mode) as a first prediction model. In each iteration it learns a 
 * new base model and applies it to the example set. Then, the residuals of the labels are
 * calculated and the next base model is learned. The learned meta model predicts the label
 * by adding all base model predictions.</p>
 *
 * @author Ingo Mierswa
 * @version $Id: AdditiveRegression.java,v 1.8 2008/05/09 19:22:48 ingomierswa Exp $
 */
public class AdditiveRegression extends AbstractMetaLearner {

	/** The parameter name for &quot;The number of iterations.&quot; */
	public static final String PARAMETER_ITERATIONS = "iterations";

	/** The parameter name for &quot;Reducing this learning rate prevent overfitting but increases the learning time.&quot; */
	public static final String PARAMETER_SHRINKAGE = "shrinkage";
	
	public AdditiveRegression(OperatorDescription description) {
		super(description);
	}

	public Model learn(ExampleSet exampleSet) throws OperatorException {
		// create temporary label attribute
		ExampleSet workingExampleSet = (ExampleSet)exampleSet.clone();
		Attribute originalLabel = workingExampleSet.getAttributes().getLabel();
		Attribute workingLabel = AttributeFactory.createAttribute(originalLabel, "working_label");
		workingExampleSet.getExampleTable().addAttribute(workingLabel);
		workingExampleSet.getAttributes().addRegular(workingLabel);
		for (Example example : workingExampleSet) {
			example.setValue(workingLabel, example.getValue(originalLabel));
		}
		workingExampleSet.getAttributes().remove(workingLabel);
		workingExampleSet.getAttributes().setLabel(workingLabel);
		
		// apply default model and calculate residuals
		Learner defaultLearner = null;
		try {
			defaultLearner = OperatorService.createOperator(DefaultLearner.class);
		} catch (OperatorCreationException e) {
			throw new OperatorException(getName() + ": not able to create default classifier!", e);
		}
		Model defaultModel = defaultLearner.learn(workingExampleSet);
		residualReplace(workingExampleSet, defaultModel, false);
		
		// create residual models
		Model[] residualModels = new Model[getParameterAsInt(PARAMETER_ITERATIONS)];
		for (int iteration = 0; iteration < residualModels.length; iteration++) {
			residualModels[iteration] = applyInnerLearner(workingExampleSet);
			residualReplace(workingExampleSet, residualModels[iteration], true);
		}
		
		// clean up working label
		workingExampleSet.getAttributes().remove(workingLabel);
		workingExampleSet.getExampleTable().removeAttribute(workingLabel);
		
		// create and return model
		return new AdditiveRegressionModel(exampleSet, defaultModel, residualModels, getParameterAsDouble(PARAMETER_SHRINKAGE));
	}

	/** This methods replaces the labels of the given example set with the label residuals 
	 *  after using the given model. Please note that the label column will be overwritten
	 *  and the original label should be stored! */
	private void residualReplace(ExampleSet exampleSet, Model model, boolean shrinkage) throws OperatorException {
		ExampleSet resultSet = model.apply(exampleSet);
		Attribute label = exampleSet.getAttributes().getLabel();
		Iterator<Example> originalReader = exampleSet.iterator();
		Iterator<Example> predictionReader = resultSet.iterator();
		while ((originalReader.hasNext()) && (predictionReader.hasNext())) {
			Example originalExample = originalReader.next();
			Example predictionExample = predictionReader.next();
			double prediction = predictionExample.getPredictedLabel();
			if (shrinkage)
				prediction *= getParameterAsDouble(PARAMETER_SHRINKAGE);
			double residual = originalExample.getLabel() - prediction;
			originalExample.setValue(label, residual);
		}
		PredictionModel.removePredictedLabel(resultSet);
	}

	public int getMinNumberOfInnerOperators() {
		return 1;
	}

	public int getMaxNumberOfInnerOperators() {
		return 1;
	}
	
	public boolean supportsCapability(LearnerCapability capability) {
		if (capability.equals(LearnerCapability.BINOMINAL_CLASS))
			return false;
		if (capability.equals(LearnerCapability.POLYNOMINAL_CLASS))
			return false;
		return super.supportsCapability(capability);
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeInt(PARAMETER_ITERATIONS, "The number of iterations.", 1, Integer.MAX_VALUE, 10));
		types.add(new ParameterTypeDouble(PARAMETER_SHRINKAGE, "Reducing this learning rate prevent overfitting but increases the learning time.", 0.0d, 1.0d, 1.0d));
		return types;
	}
}
