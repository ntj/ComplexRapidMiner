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
package com.rapidminer.operator.learner.tree;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;

/**
 * This criterion class can be used for the incremental calculation of benefits.
 * 
 * @author Sebastian Land
 * @version $Id: AbstractCriterion.java,v 1.5 2008/05/09 19:22:52 ingomierswa Exp $
 */
public abstract class AbstractCriterion implements Criterion {
	
    // data for incremental calculation
    
    protected double leftWeight;
    protected double rightWeight;
    protected double totalWeight;
    protected double[] totalLabelWeights;
    protected double[] leftLabelWeights;
    protected double[] rightLabelWeights;
    protected Attribute labelAttribute;
    protected Attribute weightAttribute;
    
	public abstract double getBenefit(SplittedExampleSet exampleSet);

	public boolean supportsIncrementalCalculation() {
    	return false;
    }

	public void startIncrementalCalculation(ExampleSet exampleSet) {
		FrequencyCalculator calculator = new FrequencyCalculator();
		rightLabelWeights = calculator.getLabelWeights(exampleSet);
		leftLabelWeights = new double[rightLabelWeights.length];
		totalLabelWeights = new double[rightLabelWeights.length];
		System.arraycopy(rightLabelWeights, 0, totalLabelWeights, 0, rightLabelWeights.length);
		leftWeight = 0;
		rightWeight = calculator.getTotalWeight(totalLabelWeights);
		totalWeight = rightWeight;
		
		labelAttribute = exampleSet.getAttributes().getLabel();
		weightAttribute = exampleSet.getAttributes().getWeight();
	}

	public void swapExample(Example example) {
		double weight = 1;
		if (weightAttribute != null) {
			weight = example.getValue(weightAttribute);
		}
		int label = (int)example.getValue(labelAttribute);
		leftWeight += weight;
		rightWeight -= weight;
		leftLabelWeights[label] += weight;
		rightLabelWeights[label] -= weight;
	}
	
	public double getIncrementalBenefit() {
		return 0;
	}
}
