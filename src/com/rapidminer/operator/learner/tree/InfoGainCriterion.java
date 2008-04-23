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
package com.rapidminer.operator.learner.tree;

import com.rapidminer.example.set.SplittedExampleSet;

/**
 * 
 * @author Sebastian Land, Ingo Mierswa
 *
 */
public class InfoGainCriterion implements Criterion {
    
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
    
    private double getEntropy(double[] labelWeights, double totalWeight) {
        double entropy = 0;
        for (int i = 0; i < labelWeights.length; i++) {
            if (labelWeights[i] > 0) {
                double proportion = labelWeights[i] / totalWeight;
                entropy -= (Math.log(proportion) * LOG_FACTOR) * proportion;
            }
        }
        return entropy;
    }
}
