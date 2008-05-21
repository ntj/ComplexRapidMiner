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

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.math.AverageVector;


/**
 * This is a special validation chain which can only be used for series predictions where 
 * the time points are encoded as examples. It uses a certain window of examples for
 * training and uses another window (after horizon examples, i.e. time points) for testing.
 * The window is moved across the example set and all performance measurements are 
 * averaged afterwards. The parameter &quot;cumulative_training&quot; indicates if all
 * former examples should be used for training (instead of only the current window).
 * 
 * @author Ingo Mierswa
 * @version $Id: SlidingWindowValidation.java,v 1.8 2008/05/09 19:22:54 ingomierswa Exp $
 */
public class SlidingWindowValidation extends ValidationChain {

	/** The parameter name for &quot;Number of examples in the window which is used for training&quot; */
	public static final String PARAMETER_TRAINING_WINDOW_WIDTH = "training_window_width";

	/** The parameter name for &quot;Number of examples the window is moved after each iteration (-1: same as test window width)&quot; */
	public static final String PARAMETER_TRAINING_WINDOW_STEP_SIZE = "training_window_step_size";

	/** The parameter name for &quot;Number of examples which are used for testing (following after 'horizon' examples after the training window end)&quot; */
	public static final String PARAMETER_TEST_WINDOW_WIDTH = "test_window_width";

	/** The parameter name for &quot;Number of examples which are between the training and testing examples&quot; */
	public static final String PARAMETER_HORIZON = "horizon";

	/** The parameter name for &quot;Indicates if each training window should be added to the old one or should replace the old one.&quot; */
	public static final String PARAMETER_CUMULATIVE_TRAINING = "cumulative_training";

	/** The parameter name for &quot;Indicates if only performance vectors should be averaged or all types of averagable result vectors&quot; */
	public static final String PARAMETER_AVERAGE_PERFORMANCES_ONLY = "average_performances_only";

    public SlidingWindowValidation(OperatorDescription description) {
        super(description);
    }

    public IOObject[] estimatePerformance(ExampleSet inputSet) throws OperatorException {
        int trainingWindowWidth = getParameterAsInt(PARAMETER_TRAINING_WINDOW_WIDTH);
        int testWindowWidth = getParameterAsInt(PARAMETER_TEST_WINDOW_WIDTH);
        int stepSize = getParameterAsInt(PARAMETER_TRAINING_WINDOW_STEP_SIZE);
        if (stepSize < 0)
            stepSize = testWindowWidth;
        int horizon = getParameterAsInt(PARAMETER_HORIZON) - 1;
        
        int[] partition = new int[inputSet.size()];
        
        int neededSize = trainingWindowWidth + horizon + testWindowWidth;
        if (neededSize > partition.length) {
            String reason = "(" + trainingWindowWidth + "+" + horizon + "+" + testWindowWidth + "=" + neededSize + ")";
            throw new UserError(this, 110, reason);
        }
        
        // evaluation loop
        List<AverageVector> averageVectors = new ArrayList<AverageVector>();
        for (int trainingStart = 0; trainingStart < partition.length; trainingStart += stepSize) {
            if ((trainingStart + trainingWindowWidth + horizon + testWindowWidth) > partition.length)
                break;
            
            // 0: training
            // 1: testing
            // 2: rest
            int actualTrainingStart = trainingStart;
            if (getParameterAsBoolean(PARAMETER_CUMULATIVE_TRAINING)) {
                actualTrainingStart = 0;
            }
            for (int i = 0; i < partition.length; i++) {
                if ((i >= actualTrainingStart) && (i < trainingStart + trainingWindowWidth)) {
                    partition[i] = 0;
                } else if ((i >= trainingStart + trainingWindowWidth + horizon) &&
                           (i < trainingStart + trainingWindowWidth + horizon + testWindowWidth)) {
                    partition[i] = 1;
                } else {
                    partition[i] = 2;
                }
            }
            
            // train
            SplittedExampleSet splittedES = new SplittedExampleSet(inputSet, new Partition(partition, 3));
            splittedES.selectSingleSubset(0);
            learn(splittedES);

            
            // evaluate
            splittedES.selectSingleSubset(1);
            IOContainer evalOutput = evaluate(splittedES);
            Tools.handleAverages(evalOutput, averageVectors, getParameterAsBoolean(PARAMETER_AVERAGE_PERFORMANCES_ONLY));

            inApplyLoop();
        }
        // end evaluation loop

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
        ParameterType type = new ParameterTypeInt(PARAMETER_TRAINING_WINDOW_WIDTH, "Number of examples in the window which is used for training", 1, Integer.MAX_VALUE, 100);
        type.setExpert(false);
        types.add(type);
        
        type = new ParameterTypeInt(PARAMETER_TRAINING_WINDOW_STEP_SIZE, "Number of examples the window is moved after each iteration (-1: same as test window width)", -1, Integer.MAX_VALUE, -1);
        types.add(type);
        
        type = new ParameterTypeInt(PARAMETER_TEST_WINDOW_WIDTH, "Number of examples which are used for testing (following after 'horizon' examples after the training window end)", 1, Integer.MAX_VALUE, 100);
        type.setExpert(false);
        types.add(type);
        
        type = new ParameterTypeInt(PARAMETER_HORIZON, "Number of examples which are between the training and testing examples", 1, Integer.MAX_VALUE, 1);
        types.add(type);
        
        types.add(new ParameterTypeBoolean(PARAMETER_CUMULATIVE_TRAINING, "Indicates if each training window should be added to the old one or should replace the old one.", false));
        
        types.add(new ParameterTypeBoolean(PARAMETER_AVERAGE_PERFORMANCES_ONLY, "Indicates if only performance vectors should be averaged or all types of averagable result vectors", true));
        
        return types;
    }
}
