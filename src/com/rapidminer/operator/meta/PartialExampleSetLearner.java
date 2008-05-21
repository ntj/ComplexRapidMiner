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

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.LastInnerOperatorCondition;
import com.rapidminer.operator.validation.XValidation;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * This operator works similar to the {@link LearningCurveOperator}. 
 * In contrast to this, it just splits the ExampleSet according to the
 * parameter "fraction" and learns a model only on the subset. It can be used, 
 * for example, in conjunction with {@link GridSearchParameterOptimizationOperator} 
 * which sets the fraction parameter to values between 0 and 1. The advantage 
 * is, that this operator can then be used inside of a {@link XValidation},
 * which delivers more stable result estimations.
 * 
 * @author Martin Mauch, Ingo Mierswa
 * @version $Id: PartialExampleSetLearner.java,v 1.5 2008/05/09 19:22:38 ingomierswa Exp $
 */
public class PartialExampleSetLearner extends OperatorChain {


	/** The parameter name for &quot;The fraction of examples which shall be used.&quot; */
	public static final String PARAMETER_FRACTION = "fraction";

	/** The parameter name for &quot;Defines the sampling type (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)&quot; */
	public static final String PARAMETER_SAMPLING_TYPE = "sampling_type";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
    public PartialExampleSetLearner(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException { 
        ExampleSet originalExampleSet = getInput(ExampleSet.class); 
        double fraction = getParameterAsDouble(PARAMETER_FRACTION); 
        if (fraction < 0 || fraction > 1.0) 
            throw new UserError(this, 207, new Object[] { fraction, "fraction", "Cannot use fractions of less than 0.0 or more than 1.0" }); 
        SplittedExampleSet splitted = new SplittedExampleSet(originalExampleSet, fraction, 
                                                             getParameterAsInt(PARAMETER_SAMPLING_TYPE), 
                                                             getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED)); 
        splitted.selectSingleSubset(0); 
        IOContainer input = new IOContainer(new IOObject[] { splitted }); 
        input = getOperator(0).apply(input);  
        return new IOObject[] {input.get(Model.class)}; 
    } 
             
    public InnerOperatorCondition getInnerOperatorCondition() {
        return new LastInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[] { Model.class } );
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
        return new Class[] { Model.class };
    }

    public List<ParameterType> getParameterTypes() { 
        List<ParameterType> types = super.getParameterTypes(); 
        ParameterType type = new ParameterTypeDouble(PARAMETER_FRACTION, "The fraction of examples which shall be used.", 0.0d, 1.0d, 0.05); 
        type.setExpert(false); 
        types.add(type);
        types.add(new ParameterTypeCategory(PARAMETER_SAMPLING_TYPE, "Defines the sampling type (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)", SplittedExampleSet.SAMPLING_NAMES, SplittedExampleSet.STRATIFIED_SAMPLING));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1));
        return types; 
    }
}
