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
package com.rapidminer.operator.meta;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.ModelApplier;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.condition.AllInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;

/**
 * This operator can be used for some basic series prediction operations.
 * The given series must be univariate and must be encoded by
 * examples, i.e. each point of time is encoded by the values in one
 * single example. The values which should be predicted must be defined
 * by the label attribute. Other attributes will be ignored.
 * 
 * The operator creates time windows and learns a model from these
 * windows to predict the value of the label column after a certain amount
 * of values (horizon). After predicting a value, the window is moved with step
 * size 1 and the next value is predicted. All predictions are kept and can
 * be compared afterwards to the actual values in a series plot or with a performance 
 * evaluation operator.
 * 
 * If you want predictions for different horizons, you have to restart
 * this operator with different settings for horizon. This might be useful to
 * get a prediction for 1 to horizon future time steps.
 * 
 * The inner learner must be able to work on numerical regression problems.
 *  
 * @author Ingo Mierswa
 * @version $Id: UnivariateLabelSeriesPrediction.java,v 1.5 2008/05/09 19:22:38 ingomierswa Exp $
 */
public class UnivariateLabelSeriesPrediction extends OperatorChain {

	public static final String PARAMETER_WINDOW_WIDTH = "window_width";
	
	public static final String PARAMETER_MAX_TRAINING_SET_SIZE = "max_training_set_size";
	
	public static final String PARAMETER_HORIZON = "horizon";
	
	
	public UnivariateLabelSeriesPrediction(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		Attribute label = exampleSet.getAttributes().getLabel();
		
		// *** sanity checks
		if (label == null)
			throw new UserError(this, 105);
		if (label.isNominal()) 
			throw new UserError(this, 102, "series predictions", label.getName());
		
		// debug out
		/*
		System.out.println("##### COMPLETE DATA #####");
		for (Example example : exampleSet)
			System.out.print(example + ", ");
		System.out.println();
		*/
		
		// *** create training attributes and training table
		int windowWidth = getParameterAsInt(PARAMETER_WINDOW_WIDTH);
		List<Attribute> attributes = new LinkedList<Attribute>();
		for (int i = 0; i < windowWidth; i++) {
			attributes.add(AttributeFactory.createAttribute("series_" + (i + 1), Ontology.REAL));
		}
		Attribute newLabel = AttributeFactory.createAttribute("label", Ontology.REAL); 
		attributes.add(newLabel);
		MemoryExampleTable trainingTable = new MemoryExampleTable(attributes);
		
		// *** create test attributes and test table
		List<Attribute> testAttributes = new LinkedList<Attribute>();
		for (int i = 0; i < windowWidth; i++) {
			testAttributes.add(AttributeFactory.createAttribute("series_" + (i + 1), Ontology.REAL));
		}
		MemoryExampleTable testTable = new MemoryExampleTable(testAttributes);
		
		
		// *** learn models and create predictions
		
    	Operator modelApplier = null;
		try {
			modelApplier = OperatorService.createOperator(ModelApplier.class);
		} catch (OperatorCreationException e) {
			throw new OperatorException("Cannot create model applier: " + e.getMessage());
		}
    	
		int horizon = getParameterAsInt(PARAMETER_HORIZON);
		int maxTrainingSetSize = getParameterAsInt(PARAMETER_MAX_TRAINING_SET_SIZE);
		double[] predictions = new double[exampleSet.size()];
		for (int i = 0; i < horizon; i++)
			predictions[i] = Double.NaN;
		for (int i = horizon; i < 2 * horizon + windowWidth - 1; i++)
			predictions[i] = exampleSet.getExample(i - horizon).getValue(label);
		
		// debug out
		/*
		System.out.println("+++ First Predictions:");
		for (int i = 0; i < 2 * horizon + windowWidth - 1; i++) {
			System.out.print(predictions[i] + ", ");
		}
		System.out.println();
		*/
		
		// create actually learned predictions
        for (int toPredict = windowWidth + 2 * horizon - 1; toPredict < exampleSet.size(); toPredict++) {
        	if (trainingTable.size() > maxTrainingSetSize - 1) {
        		trainingTable.removeDataRow(0);
        	}
        	double[] trainingData = new double[windowWidth + 1];
        	for (int d = 0; d < windowWidth; d++) {
        		trainingData[d] = exampleSet.getExample(toPredict - 2 * horizon - windowWidth + 1 + d).getValue(label);
        	}
        	trainingData[trainingData.length - 1] = exampleSet.getExample(toPredict - horizon).getValue(label);
        	trainingTable.addDataRow(new DoubleArrayDataRow(trainingData));

        	// create training set and apply inner learner
        	ExampleSet trainingSet = trainingTable.createExampleSet(newLabel);
        	
        	// debug out
        	/*
        	System.out.println("### To Predict: " + toPredict + ", w = " + windowWidth + ", h = " + horizon);
        	System.out.println("+++ Current Training Data:");
        	for (Example example : trainingSet) {
        		for (Attribute attribute : example.getAttributes())
        			System.out.print(example.getValue(attribute) + ", ");
        		System.out.println("L = " + example.getLabel());
        	}
        	*/
        	
        	IOContainer innerResult = getOperator(0).apply(new IOContainer(trainingSet));
        	Model model = innerResult.get(Model.class);
        	
        	// create prediction example and apply model
        	testTable.clear();
        	double[] testData = new double[windowWidth + 1];
        	for (int d = 0; d < windowWidth; d++) {
        		testData[d] = exampleSet.getExample(toPredict - horizon - windowWidth + 1 + d).getValue(label);
        	}
        	testTable.addDataRow(new DoubleArrayDataRow(testData));
        	ExampleSet testSet = testTable.createExampleSet();

        	// debug out
        	/*
        	System.out.println("+++ Current Test Data:");
        	for (Example example : testSet) {
        		for (Attribute attribute : example.getAttributes())
        			System.out.print(example.getValue(attribute) + ", ");
        		System.out.println("L = " + example.getLabel());
        	}
        	*/
        	
        	IOContainer applyResult = modelApplier.apply(new IOContainer(new IOObject[] { model, testSet }));
        	ExampleSet predictionSet = applyResult.get(ExampleSet.class);
        	
        	double predictedValue = predictionSet.getExample(0).getPredictedLabel();
        	
        	// store prediction
        	predictions[toPredict] = predictedValue;
        	PredictionModel.removePredictedLabel(predictionSet);
        	
        	// debug out
        	/*
        	System.out.println("+++ Current Prediction:");
        	System.out.println(toPredict + " --> " + predictedValue);
        	System.out.println();
        	*/
        	
			checkForStop();
        }
		
		Attribute predictedLabel = PredictionModel.createPredictedLabel(exampleSet, label);
		Iterator<Example> e = exampleSet.iterator();
		int counter = 0;
		while (e.hasNext()) {
			Example example = e.next();
			double prediction = predictions[counter];
			example.setValue(predictedLabel, prediction);
			counter++;
		}
		
		return new IOObject[] { exampleSet };
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		return new AllInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[] { Model.class });
	}

	public int getMaxNumberOfInnerOperators() {
		return 1;
	}

	public int getMinNumberOfInnerOperators() {
		return 1;
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeInt(PARAMETER_WINDOW_WIDTH, "The number of values used as indicators for predicting the target value.", 1, Integer.MAX_VALUE, 10));
		types.add(new ParameterTypeInt(PARAMETER_HORIZON, "The gap size used between training windows and prediction value.", 1, Integer.MAX_VALUE, 1));
		types.add(new ParameterTypeInt(PARAMETER_MAX_TRAINING_SET_SIZE, "The maximum number of examples (windows) used for training the prediction model.", 1, Integer.MAX_VALUE, 10));
		return types;
	}
}
