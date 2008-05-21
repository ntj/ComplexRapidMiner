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

import java.util.Random;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.MappedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;


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
 * @version $Id: WeightedBootstrappingValidation.java,v 1.3 2008/05/09 19:22:54 ingomierswa Exp $
 */
public class WeightedBootstrappingValidation extends AbstractBootstrappingValidation {

    public WeightedBootstrappingValidation(OperatorDescription description) {
        super(description);
    }

    protected int[] createMapping(ExampleSet exampleSet, int size, Random random) throws OperatorException {
        return MappedExampleSet.createWeightedBootstrappingMapping(exampleSet, size, random);
    }
}
