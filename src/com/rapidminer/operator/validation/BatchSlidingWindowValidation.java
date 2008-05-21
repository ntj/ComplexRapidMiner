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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.math.AverageVector;


/**
 * <p>
 * The <code>BatchSlidingWindowValidation</code> is similar to the usual 
 * {@link SlidingWindowValidation}. This operator, however, does not 
 * split the data itself in windows of predefined widths but uses the partition
 * defined by the special attribute &quot;batch&quot;. This can be an arbitrary
 * nominal or integer attribute where each possible value occurs at least once
 * (since many learning schemes depend on this minimum number of examples).
 * In each iteration, the next training batch is used for learning and the batch
 * after this for prediction. It is also possible to perform a cumulative batch
 * creation where each test batch will simply be added to the current training
 * batch for the training in the next generation. 
 * </p> 
 * 
 * <p>
 * The first inner operator must accept an
 * {@link com.rapidminer.example.ExampleSet} while the second must accept an
 * {@link com.rapidminer.example.ExampleSet} and the output of the first (which
 * is in most cases a {@link com.rapidminer.operator.Model}) and must produce
 * a {@link com.rapidminer.operator.performance.PerformanceVector}.
 * </p>
 * 
 * @author Ingo Mierswa
 * @version $Id: BatchSlidingWindowValidation.java,v 1.6 2008/05/09 19:22:54 ingomierswa Exp $
 */
public class BatchSlidingWindowValidation extends ValidationChain {
    
	/** The parameter name for &quot;Indicates if each training batch should be added to the old one or should replace the old one.&quot; */
	public static final String PARAMETER_CUMULATIVE_TRAINING = "cumulative_training";

	/** The parameter name for &quot;Indicates if only performance vectors should be averaged or all types of averagable result vectors&quot; */
	public static final String PARAMETER_AVERAGE_PERFORMANCES_ONLY = "average_performances_only";
	
    private int iteration;

    public BatchSlidingWindowValidation(OperatorDescription description) {
        super(description);
        addValue(new Value("iteration", "The number of the current iteration.") {
            public double getValue() {
                return iteration;
            }
        });
    }
    
    public IOObject[] estimatePerformance(ExampleSet inputSet) throws OperatorException {
        // split by attribute
        Attribute batchAttribute = inputSet.getAttributes().getSpecial(Attributes.BATCH_NAME);
        if (batchAttribute == null) {
            throw new UserError(this, 113, Attributes.BATCH_NAME);
        }
        SplittedExampleSet splittedES = SplittedExampleSet.splitByAttribute((ExampleSet)inputSet.clone(), batchAttribute);

        splittedES.clearSelection();
        
        // start window validation
        List<AverageVector> averageVectors = new ArrayList<AverageVector>();
        for (iteration = 0; iteration < splittedES.getNumberOfSubsets() - 1; iteration++) {

            if (getParameterAsBoolean(PARAMETER_CUMULATIVE_TRAINING)) {
                splittedES.clearSelection();
                for (int s = 0; s <= iteration; s++ )
                    splittedES.selectAdditionalSubset(s);
            } else {
                splittedES.selectSingleSubset(iteration);
            }
            learn(splittedES);

            splittedES.selectSingleSubset(iteration + 1);
            IOContainer evalOutput = evaluate(splittedES);
            Tools.handleAverages(evalOutput, averageVectors, getParameterAsBoolean(PARAMETER_AVERAGE_PERFORMANCES_ONLY));

            inApplyLoop();
        }
        // end window validation

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
        types.add(new ParameterTypeBoolean(PARAMETER_CUMULATIVE_TRAINING, "Indicates if each training batch should be added to the old one or should replace the old one.", false));
        types.add(new ParameterTypeBoolean(PARAMETER_AVERAGE_PERFORMANCES_ONLY, "Indicates if only performance vectors should be averaged or all types of averagable result vectors", true));
        return types;
    }
}
