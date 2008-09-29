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
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.math.optimization.ec.es.ESOptimization;
import com.rapidminer.tools.math.optimization.ec.es.Individual;


/**
 * <p>This operator uses a set of class weights and also allows a weight for the fact
 * that an example is not classified at all (marked as unknown). Based on the
 * predictions of the model of the inner learner this operator optimized
 * a set of thresholds regarding the defined weights.</p>
 * 
 * <p>
 * This operator might be very useful in cases where it is better to not classify
 * an example then to classify it in a wrong way. This way, it is often possible
 * to get very high accuracies for the remaining examples (which are actually
 * classified) for the cost of having some examples which must still be manually
 * classified. 
 * </p>
 *  
 * @author Ingo Mierswa
 * @version $Id: CostBasedThresholdLearner.java,v 1.7 2008/05/09 19:22:46 ingomierswa Exp $
 */
public class CostBasedThresholdLearner extends AbstractMetaLearner {


	/** The parameter name for &quot;The weights for all classes (first column: class names, second column: weight), empty: using 1 for all classes. The costs for not classifying at all are defined with class name '?'.&quot; */
	public static final String PARAMETER_CLASS_WEIGHTS = "class_weights";

	/** The parameter name for &quot;Use this cost value for predicting an example as unknown (-1: use same costs as for correct class).&quot; */
	public static final String PARAMETER_PREDICT_UNKNOWN_COSTS = "predict_unknown_costs";

	/** The parameter name for &quot;Use this amount of input data for model learning and the rest for threshold optimization.&quot; */
	public static final String PARAMETER_TRAINING_RATIO = "training_ratio";

	/** The parameter name for &quot;Defines the number of optimization iterations.&quot; */
	public static final String PARAMETER_NUMBER_OF_ITERATIONS = "number_of_iterations";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
    public CostBasedThresholdLearner(OperatorDescription description) {
        super(description);
    }

    public Model learn(ExampleSet exampleSet) throws OperatorException {
        Attribute label = exampleSet.getAttributes().getLabel();
        List classWeights = getParameterList(PARAMETER_CLASS_WEIGHTS);
        
        // some checks
        if (!exampleSet.getAttributes().getLabel().isNominal()) {
            throw new UserError(this, 101, getName(), label.getName());
        }
       
        if (classWeights.size() == 0) {
            throw new UserError(this, 205, "class_weights");
        }
        
        // derive possible class weights
        double unknownWeight = getParameterAsDouble(PARAMETER_PREDICT_UNKNOWN_COSTS);
        double[] weights = new double[label.getMapping().size()];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = 1.0d;
        }
       
        Iterator i = classWeights.iterator();
        while (i.hasNext()) {
            Object[] classWeightArray = (Object[])i.next();
            String className = (String)classWeightArray[0];
            double classWeight = (Double)classWeightArray[1];
            int index = label.getMapping().getIndex(className);
            weights[index] = classWeight;
        }

        // logging
        List<String> weightList = new LinkedList<String>();
        for (double d : weights)
            weightList.add(Tools.formatIntegerIfPossible(d));
        log("Used class weights --> " + weightList + ", unknown weight: " + Tools.formatIntegerIfPossible(unknownWeight));

        
        return calculateThresholdModel(exampleSet, weights, unknownWeight);
    }
    
    private Model calculateThresholdModel(ExampleSet exampleSet, final double[] classWeights, final double unknownWeight) throws OperatorException {
        SplittedExampleSet trainingSet = new SplittedExampleSet(exampleSet, getParameterAsDouble(PARAMETER_TRAINING_RATIO), SplittedExampleSet.STRATIFIED_SAMPLING, getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
        trainingSet.selectSingleSubset(0);
        Model innerModel = applyInnerLearner(trainingSet);
        trainingSet.selectSingleSubset(1);
        final ExampleSet appliedTrainingSet = innerModel.apply(trainingSet);
        final Attribute label = appliedTrainingSet.getAttributes().getLabel();
        
        int numberOfGenerations = getParameterAsInt(PARAMETER_NUMBER_OF_ITERATIONS);
        ESOptimization optimization = new ESOptimization(0.0d, 1.0d, 5, classWeights.length, 
                                                         ESOptimization.INIT_TYPE_RANDOM, 
                                                         numberOfGenerations, Math.max(1, numberOfGenerations / 10), 
                                                         ESOptimization.TOURNAMENT_SELECTION,
                                                         0.4, true, ESOptimization.GAUSSIAN_MUTATION, 
                                                         0.9, false,
                                                         RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED)),
                                                         this) {
            public PerformanceVector evaluateIndividual(Individual individual) throws OperatorException {
                double costs = 0.0d;
                double[] thresholds = individual.getValues();
                for (Example example : appliedTrainingSet) {
                    int predictionIndex = (int)example.getPredictedLabel();
                    String className = label.getMapping().mapIndex(predictionIndex);
                    double confidence = example.getConfidence(className);
                    // confident...
                    if (confidence > thresholds[predictionIndex]) {
                        // wrong -> malus
                        if (example.getLabel() != example.getPredictedLabel()) {
                            costs += classWeights[(int)example.getLabel()];
                        } else {
                            // correct -> bonus
                            //costs -= classWeights[(int)example.getLabel()];
                        }
                    // not so confident...
                    } else {
                        double usedWeight = unknownWeight;
                        if (unknownWeight < 0.0d) {
                            usedWeight = classWeights[(int)example.getLabel()];
                        }
                        // correct -> malus
                        if (example.getLabel() == example.getPredictedLabel()) {
                            costs += usedWeight; 
                        } else {
                            // wrong -> bonus
                            //costs -= usedWeight;
                        }
                    }
                }
                PerformanceVector performanceVector = new PerformanceVector();
                performanceVector.addCriterion(new EstimatedPerformance("Costs", costs, 1, true));
                return performanceVector;
            }        
        };

        optimization.optimize();
        PredictionModel.removePredictedLabel(appliedTrainingSet);
        
        double[] bestValues = optimization.getBestValuesEver();

        return new ThresholdModel(appliedTrainingSet, innerModel, bestValues);
    }
    
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeList(PARAMETER_CLASS_WEIGHTS, "The weights for all classes (first column: class names, second column: weight), empty: using 1 for all classes. The costs for not classifying at all are defined with class name '?'.", new ParameterTypeDouble("weight", "The weight for the specified class.", 0.0d, Double.POSITIVE_INFINITY, 1.0d));
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeDouble(PARAMETER_PREDICT_UNKNOWN_COSTS, "Use this cost value for predicting an example as unknown (-1: use same costs as for correct class).", -1.0d, Double.POSITIVE_INFINITY, -1.0d);
        type.setExpert(false);
        types.add(type);
        
        types.add(new ParameterTypeDouble(PARAMETER_TRAINING_RATIO, "Use this amount of input data for model learning and the rest for threshold optimization.", 0.0d, 1.0d, 0.7d));
        types.add(new ParameterTypeInt(PARAMETER_NUMBER_OF_ITERATIONS, "Defines the number of optimization iterations.", 1, Integer.MAX_VALUE, 200));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1));
        return types;
    }
}
