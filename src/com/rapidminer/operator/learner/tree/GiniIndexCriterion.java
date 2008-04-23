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
 * Calculates the Gini index for the given split.
 * 
 * @author Ingo Mierswa
 * @version $Id: GiniIndexCriterion.java,v 1.1 2007/06/16 03:28:12 ingomierswa Exp $
 */
public class GiniIndexCriterion implements Criterion {
    
    private FrequencyCalculator calculator = new FrequencyCalculator();
    
    public double getBenefit(SplittedExampleSet exampleSet) {
        double[] totalWeights = calculator.getLabelWeights(exampleSet);
        double totalWeight = calculator.getTotalWeight(totalWeights);
        double totalEntropy = getGiniIndex(totalWeights, totalWeight);
        double gain = 0;
        for (int i = 0; i < exampleSet.getNumberOfSubsets(); i++) {
            exampleSet.selectSingleSubset(i);
            double[] partitionWeights = calculator.getLabelWeights(exampleSet);
            double partitionWeight = calculator.getTotalWeight(partitionWeights);
            gain += getGiniIndex(partitionWeights, partitionWeight) * partitionWeight / totalWeight;
        }
        return totalEntropy - gain;
    }
    
    private double getGiniIndex(double[] labelWeights, double totalWeight) {
    	double sum = 0.0d;
    	for (int i = 0; i < labelWeights.length; i++) {
    		double frequency = labelWeights[i] / totalWeight;
    		sum += frequency * frequency;
    	}
    	return 1.0d - sum;
    }
}
