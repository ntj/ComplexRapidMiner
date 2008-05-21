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

import java.util.List;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.condition.CombinedInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SpecificInnerOperatorCondition;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * Operator chain that splits an {@link ExampleSet} into a training and test
 * sets similar to XValidation, but returns the test set predictions instead of
 * a performance vector. The inner two operators must be a learner returning a
 * {@link Model} and an operator or operator chain that can apply this model
 * (usually a model applier)
 * 
 * @author Stefan Rueping, Ingo Mierswa
 * @version $Id: XVPrediction.java,v 1.5 2008/05/09 19:22:38 ingomierswa Exp $
 */
public class XVPrediction extends OperatorChain {

	/** The parameter name for &quot;Number of subsets for the crossvalidation.&quot; */
	public static final String PARAMETER_NUMBER_OF_VALIDATIONS = "number_of_validations";

	/** The parameter name for &quot;Set the number of validations to the number of examples. If set to true, number_of_validations is ignored.&quot; */
	public static final String PARAMETER_LEAVE_ONE_OUT = "leave_one_out";

	/** The parameter name for &quot;Defines the sampling type of the cross validation.&quot; */
	public static final String PARAMETER_SAMPLING_TYPE = "sampling_type";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
    
	private static final Class[] INPUT_CLASSES = { ExampleSet.class };

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	private int number;

	private int iteration;

	public XVPrediction(OperatorDescription description) {
		super(description);
		addValue(new Value("iteration", "The number of the current iteration.") {

			public double getValue() {
				return iteration;
			}
		});
	}

    public IOObject[] apply() throws OperatorException {
        ExampleSet inputSet = getInput(ExampleSet.class);
        if (getParameterAsBoolean(PARAMETER_LEAVE_ONE_OUT)) {
            number = inputSet.size();
        } else {
            number = getParameterAsInt(PARAMETER_NUMBER_OF_VALIDATIONS);
        }
        log("Starting " + number + "-fold cross validation prediction");

        // Split training / test set
        int samplingType = getParameterAsInt(PARAMETER_SAMPLING_TYPE);
        SplittedExampleSet splittedES = new SplittedExampleSet(inputSet, number, samplingType, getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
        
        double[] res = new double[inputSet.size()];
        double[][] confidences = null;
        if (inputSet.getAttributes().getLabel().isNominal())
            confidences = new double[inputSet.size()][inputSet.getAttributes().getLabel().getMapping().size()];
        
        ExampleSet predictionSet = null;
        for (iteration = 0; iteration < number; iteration++) {
            splittedES.selectAllSubsetsBut(iteration);
            IOContainer learnResult = getLearner().apply(new IOContainer(new IOObject[] { splittedES }));
            splittedES.selectSingleSubset(iteration);
            IOContainer applyResult = getApplier().apply(learnResult.append(new IOObject[] { splittedES }));
            predictionSet = applyResult.get(ExampleSet.class);

            for (int i = 0; i < splittedES.size(); i++) {
                Example e = splittedES.getExample(i);
                double val = e.getPredictedLabel();
                int index = splittedES.getActualParentIndex(i);
                res[index] = val;
                if (confidences != null) {
                    int counter = 0;
                    for (String s : inputSet.getAttributes().getLabel().getMapping().getValues()) {
                        confidences[index][counter++] = e.getConfidence(s);
                    }
                }
            }
            inApplyLoop();
        }

        // the values must be set here since the model will create new 
        // predicted label attributes in each iteration
        int index = 0;
        PredictionModel.copyPredictedLabel(predictionSet, inputSet);
        for (Example e : inputSet) {
            e.setValue(e.getAttributes().getPredictedLabel(), res[index]);
            if (confidences != null) {
                int counter = 0;
                for (String s : inputSet.getAttributes().getLabel().getMapping().getValues()) {
                    e.setConfidence(s, confidences[index][counter++]);
                }
            }
            index++;
        }
        
        return (new IOObject[] { inputSet });
    }
    
	/** Returns the maximum number of innner operators. */
	public int getMaxNumberOfInnerOperators() {
		return 2;
	}

	/** Returns the minimum number of innner operators. */
	public int getMinNumberOfInnerOperators() {
		return 2;
	}

	/** returns the the classes this operator provides as output. */
	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	/** returns the the classes this operator expects as input. */
	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		CombinedInnerOperatorCondition condition = new CombinedInnerOperatorCondition();
		condition.addCondition(new SpecificInnerOperatorCondition("Training", 0, new Class[] { ExampleSet.class }, new Class[] { Model.class }));
		condition.addCondition(new SpecificInnerOperatorCondition("Testing", 1, new Class[] { ExampleSet.class, Model.class }, new Class[] { ExampleSet.class }));
		return condition;
	}

	/**
	 * Returns the first encapsulated inner operator (or operator chain), i.e.
	 * the learning operator (chain).
	 */
	private Operator getLearner() {
		return getOperator(0);
	}

	/**
	 * Returns the second encapsulated inner operator (or operator chain), i.e.
	 * the application and evaluation operator (chain)
	 */
	private Operator getApplier() {
		return getOperator(1);
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_OF_VALIDATIONS, "Number of subsets for the crossvalidation.", 2, Integer.MAX_VALUE, 10);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_LEAVE_ONE_OUT, "Set the number of validations to the number of examples. If set to true, number_of_validations is ignored.", false));
		types.add(new ParameterTypeCategory(PARAMETER_SAMPLING_TYPE, "Defines the sampling type of the cross validation.", SplittedExampleSet.SAMPLING_NAMES, SplittedExampleSet.STRATIFIED_SAMPLING));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
