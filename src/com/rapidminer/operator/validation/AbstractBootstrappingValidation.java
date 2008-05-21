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
package com.rapidminer.operator.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.MappedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.AverageVector;


/**
 * <p>This validation operator performs several bootstrapped samplings (sampling with replacement)
 * on the input set and trains a model on these samples. The remaining samples, i.e. those which
 * were not sampled, build a test set on which the model is evaluated. This process is repeated
 * for the specified number of iterations after which the average performance is calculated.</p>
 * 
 * <p>The basic setup is the same as for the usual cross validation operator. The first inner 
 * operator must provide a model and the second a performance vector. Please note that this operator
 * does not regard example weights, i.e. weights specified in a weight column.</p> 
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractBootstrappingValidation.java,v 1.3 2008/05/09 19:22:54 ingomierswa Exp $
 */
public abstract class AbstractBootstrappingValidation extends ValidationChain {

    public static final String PARAMETER_NUMBER_OF_VALIDATIONS = "number_of_validations";
    
    public static final String PARAMETER_SAMPLE_RATIO = "sample_ratio";
    
    public static final String PARAMETER_AVERAGE_PERFORMANCES_ONLY = "average_performances_only";
    
    public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	private int number;

    private int iteration;

    public AbstractBootstrappingValidation(OperatorDescription description) {
        super(description);
        addValue(new Value("iteration", "The number of the current iteration.") {
            public double getValue() {
                return iteration;
            }
        });
    }

    protected abstract int[] createMapping(ExampleSet exampleSet, int size, Random random) throws OperatorException;
    
    public IOObject[] estimatePerformance(ExampleSet inputSet) throws OperatorException {
        number = getParameterAsInt(PARAMETER_NUMBER_OF_VALIDATIONS);

        // start bootstrapping loop
        Random random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
        List<AverageVector> averageVectors = new ArrayList<AverageVector>();
        for (iteration = 0; iteration < number; iteration++) {
            int[] mapping = createMapping(inputSet, (int)Math.round(inputSet.size() * getParameterAsDouble(PARAMETER_SAMPLE_RATIO)), random);
            MappedExampleSet trainingSet = new MappedExampleSet((ExampleSet)inputSet.clone(), mapping, true);
            learn(trainingSet);
            
            MappedExampleSet inverseExampleSet = new MappedExampleSet((ExampleSet)inputSet.clone(), mapping, false);
            IOContainer evalOutput = evaluate(inverseExampleSet);
            Tools.handleAverages(evalOutput, averageVectors, getParameterAsBoolean(PARAMETER_AVERAGE_PERFORMANCES_ONLY));
            inApplyLoop();
        }
        // end loop

        // set last result for plotting purposes. This is an average value and
        // actually not the last performance value!
        PerformanceVector averagePerformance = Tools.getPerformanceVector(averageVectors);
        if (averagePerformance != null)
            setResult(averagePerformance.getMainCriterion());

        AverageVector[] result = new AverageVector[averageVectors.size()];
        averageVectors.toArray(result);

        return result;
    }
    
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_OF_VALIDATIONS, "Number of subsets for the crossvalidation.", 2, Integer.MAX_VALUE, 10);
        type.setExpert(false);
        types.add(type);
        types.add(new ParameterTypeDouble(PARAMETER_SAMPLE_RATIO, "This ratio of examples will be sampled (with replacement) in each iteration.", 0.0d, Double.POSITIVE_INFINITY, 1.0d));
        types.add(new ParameterTypeBoolean(PARAMETER_AVERAGE_PERFORMANCES_ONLY, "Indicates if only performance vectors should be averaged or all types of averagable result vectors.", true));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
        return types;
    }
}
