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
package com.rapidminer.operator.visualization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.condition.AllInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.math.ROCData;
import com.rapidminer.tools.math.ROCDataGenerator;


/**
 * This operator uses its inner operators (each of those must produce a model) and
 * calculates the ROC curve for each of them. All ROC curves together are 
 * plotted in the same plotter. The comparison is based on the average values of a 
 * k-fold cross validation. Alternatively, this operator can use an internal split
 * into a test and a training set from the given data set.
 * 
 * Please note that a former predicted label of the given example set will be removed during 
 * the application of this operator.
 * 
 * @author Ingo Mierswa
 * @version $Id: ROCBasedComparisonOperator.java,v 1.10 2008/05/09 19:23:14 ingomierswa Exp $
 */
public class ROCBasedComparisonOperator extends OperatorChain {

    /** The parameter name for the number of folds. */
    public static final String PARAMETER_NUMBER_OF_FOLDS = "number_of_folds";
    
	/** The parameter name for &quot;Relative size of the training set&quot; */
	public static final String PARAMETER_SPLIT_RATIO = "split_ratio";

	/** The parameter name for &quot;Defines the sampling type of the cross validation (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)&quot; */
	public static final String PARAMETER_SAMPLING_TYPE = "sampling_type";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";

	/** Indicates if example weights should be used. */
	public static final String PARAMETER_USE_EXAMPLE_WEIGHTS = "use_example_weights";
	
    
    public ROCBasedComparisonOperator(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException {
        ExampleSet exampleSet = getInput(ExampleSet.class);
        
        if (exampleSet.getAttributes().getLabel() == null) {
            throw new UserError(this, 105);
        }
        if (!exampleSet.getAttributes().getLabel().isNominal()) {
            throw new UserError(this, 101, "ROC Comparison", exampleSet.getAttributes().getLabel());
        }
        if (exampleSet.getAttributes().getLabel().getMapping().getValues().size() != 2) {
            throw new UserError(this, 114, "ROC Comparison", exampleSet.getAttributes().getLabel());
        }
                
        Map<String, List<ROCData>> rocData = new HashMap<String, List<ROCData>>();
        
        int numberOfFolds = getParameterAsInt(PARAMETER_NUMBER_OF_FOLDS);
        if (numberOfFolds < 0) {
            double splitRatio = getParameterAsDouble(PARAMETER_SPLIT_RATIO);
            SplittedExampleSet eSet = new SplittedExampleSet((ExampleSet)exampleSet.clone(), splitRatio, getParameterAsInt(PARAMETER_SAMPLING_TYPE), getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
            PredictionModel.removePredictedLabel(eSet);

            for (int i = 0; i < getNumberOfOperators(); i++) {
                // learn model on training set
                eSet.selectSingleSubset(0);
                Operator innerOperator = getOperator(i);
                IOContainer result = innerOperator.apply(new IOContainer(eSet));
                Model model = result.remove(Model.class);

                // apply model on test set
                eSet.selectSingleSubset(1);
                ExampleSet resultSet = model.apply(eSet);
                if (resultSet.getAttributes().getPredictedLabel() == null) {
                    throw new UserError(this, 107);
                }

                // calculate ROC values
                ROCDataGenerator rocDataGenerator = new ROCDataGenerator(1.0d, 1.0d);
                ROCData rocPoints = rocDataGenerator.createROCData(resultSet, getParameterAsBoolean(PARAMETER_USE_EXAMPLE_WEIGHTS));
                List<ROCData> dataList = new LinkedList<ROCData>();
                dataList.add(rocPoints);
                rocData.put(innerOperator.getName(), dataList);

                // remove predicted label
                PredictionModel.removePredictedLabel(resultSet);    
            }
        } else {
            SplittedExampleSet eSet = new SplittedExampleSet((ExampleSet)exampleSet.clone(), numberOfFolds, getParameterAsInt(PARAMETER_SAMPLING_TYPE), getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
            PredictionModel.removePredictedLabel(eSet);
            
            // for each inner operator
            for (int i = 0; i < getNumberOfOperators(); i++) {
                Operator innerOperator = getOperator(i);
                List<ROCData> dataList = new LinkedList<ROCData>();
                
                for (int iteration = 0; iteration < numberOfFolds; iteration++) {
                    eSet.selectAllSubsetsBut(iteration);

                    IOContainer result = innerOperator.apply(new IOContainer(eSet));
                    Model model = result.remove(Model.class);

                    eSet.selectSingleSubset(iteration);
                    ExampleSet resultSet = model.apply(eSet);
                    if (resultSet.getAttributes().getPredictedLabel() == null) {
                        throw new UserError(this, 107);
                    }
                    
                    // calculate ROC values
                    ROCDataGenerator rocDataGenerator = new ROCDataGenerator(1.0d, 1.0d);
                    ROCData rocPoints = rocDataGenerator.createROCData(resultSet, getParameterAsBoolean(PARAMETER_USE_EXAMPLE_WEIGHTS));
                    dataList.add(rocPoints);

                    // remove predicted label
                    PredictionModel.removePredictedLabel(resultSet);
       
                    inApplyLoop();
                }                
                
                rocData.put(innerOperator.getName(), dataList);
            }
        }
                
        return new IOObject[] { exampleSet, new ROCComparison(rocData) };
    }
    
    public Class[] getInputClasses() {
        return new Class[] { ExampleSet.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] { ExampleSet.class, ROCComparison.class };
    }
   
    public InnerOperatorCondition getInnerOperatorCondition() {
        return new AllInnerOperatorCondition(new Class[] { ExampleSet.class }, 
                                             new Class[] { Model.class });
    }

    public int getMinNumberOfInnerOperators() {
        return 1;
    }

    public int getMaxNumberOfInnerOperators() {
        return Integer.MAX_VALUE;
    }
    
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_OF_FOLDS, "The number of folds used for a cross validation evaluation (-1: use simple split ratio).", -1, Integer.MAX_VALUE, 10);
        type.setExpert(false);
        types.add(type);
        types.add(new ParameterTypeDouble(PARAMETER_SPLIT_RATIO, "Relative size of the training set", 0.0d, 1.0d, 0.7d));
        types.add(new ParameterTypeCategory(PARAMETER_SAMPLING_TYPE, "Defines the sampling type of the cross validation (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)", SplittedExampleSet.SAMPLING_NAMES, SplittedExampleSet.STRATIFIED_SAMPLING));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1));
        types.add(new ParameterTypeBoolean(PARAMETER_USE_EXAMPLE_WEIGHTS, "Indicates if example weights should be regarded (use weight 1 for each example otherwise).", true));
        return types;
    }
}
