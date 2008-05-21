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

import com.rapidminer.example.set.SplittedExampleSet;

/**
 * This criterion implements the well known information gain in 
 * order to calculate the benefit of a split. The information gain
 * is defined as the change in entropy from a prior state to a 
 * state that takes some information as given:
 * 
 * IG(E_x,a) = H(E_x) − H(E_x | a)
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: InfoGainCriterion.java,v 1.6 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class InfoGainCriterion extends AbstractCriterion {
    
    private static double LOG_FACTOR = 1d / Math.log(2);
    
    private FrequencyCalculator calculator = new FrequencyCalculator();
    
    public double getBenefit(SplittedExampleSet exampleSet) {
        double[] totalWeights = calculator.getLabelWeights(exampleSet);
        double totalWeight = calculator.getTotalWeight(totalWeights);
        double totalEntropy = getEntropy(totalWeights, totalWeight);
        double gain = 0;
        for (int i = 0; i < exampleSet.getNumberOfSubsets(); i++) {
            exampleSet.selectSingleSubset(i);
            double[] partitionWeights = calculator.getLabelWeights(exampleSet);
            double partitionWeight = calculator.getTotalWeight(partitionWeights);
            gain += getEntropy(partitionWeights, partitionWeight) * partitionWeight / totalWeight;
        }
        return totalEntropy - gain;
    }
    
    public double getEntropy(double[] labelWeights, double totalWeight) {
        double entropy = 0;
        for (int i = 0; i < labelWeights.length; i++) {
            if (labelWeights[i] > 0) {
                double proportion = labelWeights[i] / totalWeight;
                entropy -= (Math.log(proportion) * LOG_FACTOR) * proportion;
            }
        }
        return entropy;
    }
    
    public boolean supportsIncrementalCalculation() {
    	return true;
    }
    
	public double getIncrementalBenefit() {
		 double totalEntropy = getEntropy(totalLabelWeights, totalWeight);
		 double gain = getEntropy(leftLabelWeights, leftWeight) * leftWeight / totalWeight;
		 gain += getEntropy(rightLabelWeights, rightWeight) * rightWeight / totalWeight;
		 return totalEntropy - gain;
	}
}
