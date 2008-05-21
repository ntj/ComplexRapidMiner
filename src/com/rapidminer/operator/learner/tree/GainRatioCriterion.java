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
 * The gain ratio divides the information gain by the prior split
 * info in order to prevent id-like attributes to be selected as the best.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: GainRatioCriterion.java,v 1.6 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class GainRatioCriterion extends AbstractCriterion {
    
    private static double LOG_FACTOR = 1d / Math.log(2);
    
    private FrequencyCalculator calculator = new FrequencyCalculator();
    
    private InfoGainCriterion infoGain = new InfoGainCriterion();
    
    public double getBenefit(SplittedExampleSet exampleSet) {
        double[] partitionWeights = calculator.getPartitionWeights((SplittedExampleSet)exampleSet.clone());
        double totalWeight = calculator.getTotalWeight(partitionWeights);
        
        double gain = infoGain.getBenefit(exampleSet);
        double splitInfo = getSplitInfo(partitionWeights, totalWeight);
        if (splitInfo == 0)
            return gain;
        else
            return gain / splitInfo;
    }
    
    protected double getSplitInfo(double[] partitionWeights, double totalWeight) {
        double splitInfo = 0;
        for (double partitionWeight : partitionWeights) {
            if (partitionWeight > 0) {
                double partitionProportion = partitionWeight / totalWeight;
                splitInfo += partitionProportion * Math.log(partitionProportion) * LOG_FACTOR;
            }
        }
        return -splitInfo;
    }
    public boolean supportsIncrementalCalculation() {
    	return true;
    }

	public double getIncrementalBenefit() {
		double gain = infoGain.getEntropy(totalLabelWeights, totalWeight);
		gain -= infoGain.getEntropy(leftLabelWeights, leftWeight) * leftWeight / totalWeight;
		gain -=  infoGain.getEntropy(rightLabelWeights, rightWeight) * rightWeight / totalWeight;
        double splitInfo = getSplitInfo(new double[] {leftWeight, rightWeight}, totalWeight);
        if (splitInfo == 0)
            return gain;
        else
            return gain / splitInfo;
	}
}
