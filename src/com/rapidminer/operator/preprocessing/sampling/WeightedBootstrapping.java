/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.preprocessing.sampling;

import java.util.Random;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.MappedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;


/**
 * This operator constructs a bootstrapped sample from the given example set which must provide 
 * a weight attribute. If no weight attribute was provided this operator will stop the process
 * with an error message. See the operator {@link Bootstrapping} for more information.
 * 
 * @author Ingo Mierswa
 * @version $Id: WeightedBootstrapping.java,v 1.2 2007/06/07 17:12:24 ingomierswa Exp $
 */
public class WeightedBootstrapping extends AbstractBootstrapping {

    public WeightedBootstrapping(OperatorDescription description) {
        super(description);
    }

    public int[] createMapping(ExampleSet exampleSet, int size, Random random) throws OperatorException {
        if (exampleSet.getAttributes().getWeight() == null)
            throw new UserError(this, 113, Attributes.WEIGHT_NAME);
        return MappedExampleSet.createWeightedBootstrappingMapping(exampleSet, size, random);
    } 
}
