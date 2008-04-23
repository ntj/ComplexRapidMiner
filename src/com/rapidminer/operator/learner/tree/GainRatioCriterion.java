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
public class GainRatioCriterion implements Criterion {
    
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
}
