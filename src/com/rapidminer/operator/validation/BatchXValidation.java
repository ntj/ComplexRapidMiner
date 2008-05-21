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
 * <code>BatchXValidation</code> encapsulates a cross-validation process. The
 * example set {@rapidminer.math S} is split up into <var> number_of_validations</var>
 * subsets {@rapidminer.math S_i}. The inner operators are applied
 * <var>number_of_validations</var> times using {@rapidminer.math S_i} as the test
 * set (input of the second inner operator) and {@rapidminer.math S\backslash S_i}
 * training set (input of the first inner operator).
 * </p>
 * 
 * <p>In contrast to the usual cross validation operator (see {@link XValidation})
 * this operator does not (randomly) split the data itself but uses the partition
 * defined by the special attribute &quot;batch&quot;. This can be an arbitrary
 * nominal or integer attribute where each possible value occurs at least once
 * (since many learning schemes depend on this minimum number of examples). 
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
 * @rapidminer.index cross-validation
 * @author Ingo Mierswa
 * @version $Id: BatchXValidation.java,v 1.6 2008/05/09 19:22:54 ingomierswa Exp $
 */
public class BatchXValidation extends ValidationChain {
    

	/** The parameter name for &quot;Indicates if only performance vectors should be averaged or all types of averagable result vectors&quot; */
	public static final String PARAMETER_AVERAGE_PERFORMANCES_ONLY = "average_performances_only";
    private int iteration;

    public BatchXValidation(OperatorDescription description) {
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
        SplittedExampleSet splittedES = SplittedExampleSet.splitByAttribute(inputSet, batchAttribute);

        // start crossvalidation
        List<AverageVector> averageVectors = new ArrayList<AverageVector>();
        for (iteration = 0; iteration < splittedES.getNumberOfSubsets(); iteration++) {

            splittedES.selectAllSubsetsBut(iteration);
            learn(splittedES);

            splittedES.selectSingleSubset(iteration);
            IOContainer evalOutput = evaluate(splittedES);
            Tools.handleAverages(evalOutput, averageVectors, getParameterAsBoolean(PARAMETER_AVERAGE_PERFORMANCES_ONLY));

            inApplyLoop();
        }
        // end crossvalidation

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
        types.add(new ParameterTypeBoolean(PARAMETER_AVERAGE_PERFORMANCES_ONLY, "Indicates if only performance vectors should be averaged or all types of averagable result vectors", true));
        return types;
    }
}
