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
package com.rapidminer.operator.preprocessing.sampling;

import java.util.List;
import java.util.Random;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.MappedExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.validation.IteratingPerformanceAverage;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;


/**
 * This operator constructs a bootstrapped sample from the given example set. That means
 * that a sampling with replacement will be performed. The usual sample size is the number 
 * of original examples. This operator also offers the possibility to create the inverse
 * example set, i.e. an example set containing all examples which are not part of the 
 * bootstrapped example set. This inverse example set might be used for a bootstrapped
 * validation (together with an {@link IteratingPerformanceAverage} operator.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractBootstrapping.java,v 1.4 2008/05/09 19:23:16 ingomierswa Exp $
 */
public abstract class AbstractBootstrapping extends Operator {


	/** The parameter name for &quot;This ratio determines the size of the new example set.&quot; */
	public static final String PARAMETER_SAMPLE_RATIO = "sample_ratio";

	/** The parameter name for &quot;Local random seed for this operator (-1: use global random seed).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
    public AbstractBootstrapping(OperatorDescription description) {
        super(description);
    }

    public abstract int[] createMapping(ExampleSet exampleSet, int size, Random random) throws OperatorException;
    
    public IOObject[] apply() throws OperatorException {
        ExampleSet exampleSet = getInput(ExampleSet.class);
        Random random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
        int[] mapping = createMapping(exampleSet, (int)Math.round(exampleSet.size() * getParameterAsDouble(PARAMETER_SAMPLE_RATIO)), random);
        MappedExampleSet bootstrappedExampleSet = new MappedExampleSet(exampleSet, mapping, true);
        return new IOObject[] { bootstrappedExampleSet };
    }
    
    public Class[] getInputClasses() {
        return new Class[] { ExampleSet.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] { ExampleSet.class };
    }
    
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeDouble(PARAMETER_SAMPLE_RATIO, "This ratio determines the size of the new example set.", 0.0d, Double.POSITIVE_INFINITY, 1.0d));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Local random seed for this operator (-1: use global random seed).", -1, Integer.MAX_VALUE, -1));
        return types;
    }
}
