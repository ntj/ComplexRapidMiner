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
package com.rapidminer.operator.learner.rules;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;

/**
 * 
 * @author Ingo Mierswa
 * @version $Id: NumericalSplitter.java,v 1.4 2007/07/15 02:01:43 ingomierswa Exp $
 */
public class NumericalSplitter {
	
    private Criterion criterion;
    
    private int sampleSize = -1;
    
    private double minValue;
    
    public NumericalSplitter(int sampleSize) {
        this.criterion = new AccuracyCriterion();
        this.sampleSize = sampleSize;
        this.minValue = 0.5d;
    }
    
    public Split getBestSplit(ExampleSet inputSet, Attribute attribute, String labelName) {
    	ExampleSet eSet = inputSet;
    	if ((sampleSize > 0) && (eSet.size() > sampleSize)) {
    		eSet = new SplittedExampleSet(eSet, (double)sampleSize / (double)eSet.size(), SplittedExampleSet.STRATIFIED_SAMPLING, -1);
    		((SplittedExampleSet)eSet).selectSingleSubset(0);
    	}
        SortedExampleSet exampleSet = new SortedExampleSet(eSet, attribute, SortedExampleSet.INCREASING);
        Attribute labelAttribute = exampleSet.getAttributes().getLabel();
        double oldLabel = Double.NaN;
        double bestSplit = Double.NaN;
        double lastValue = Double.NaN;
        double bestBenefit = Double.NEGATIVE_INFINITY;
        double bestTotalWeight = 0;
        int bestSplitType = Split.LESS_SPLIT;

        for (Example e : exampleSet) {
        	double currentValue = e.getValue(attribute);
        	double label = e.getValue(labelAttribute);            
        	if ((Double.isNaN(oldLabel)) || (oldLabel != label)) {
        		double splitValue = (lastValue + currentValue) / 2.0d;
        		SplittedExampleSet splitted = SplittedExampleSet.splitByAttribute(exampleSet, attribute, splitValue);
        		
                SplittedExampleSet posSet = (SplittedExampleSet)splitted.clone();
                posSet.selectSingleSubset(0);
                SplittedExampleSet negSet = (SplittedExampleSet)splitted.clone();
                negSet.selectSingleSubset(1);
                
        		double[] benefits = calculateBenefit(posSet, negSet, labelName);
                if ((benefits[0] > minValue) &&
                    (benefits[0] > 0) && (benefits[1] > 0) &&
                    ((benefits[0] > bestBenefit) || 
                    ((benefits[0] == bestBenefit) && (benefits[1] > bestTotalWeight)))) {
        			bestBenefit = benefits[0];
        			bestSplit = splitValue;
        			bestTotalWeight = benefits[1];
        			bestSplitType = Split.LESS_SPLIT;
        		}
                
        		benefits = calculateBenefit(negSet, posSet, labelName);
                if ((benefits[0] > minValue) &&
                    (benefits[0] > 0) && (benefits[1] > 0) &&
                    ((benefits[0] > bestBenefit) || 
                    ((benefits[0] == bestBenefit) && (benefits[1] > bestTotalWeight)))) {
        			bestBenefit = benefits[0];
        			bestSplit = splitValue;
        			bestTotalWeight = benefits[1];
        			bestSplitType = Split.GREATER_SPLIT;
        		}
                
        		oldLabel = label;
        	}
            lastValue = currentValue;
        }
        
        return new Split(bestSplit, new double[] { bestBenefit, bestTotalWeight }, bestSplitType);
    }
    
    private double[] calculateBenefit(ExampleSet posSet, ExampleSet negSet, String labelName) {
        String usedLabelName = labelName;
        if (usedLabelName == null) {
            Attribute posLabel = posSet.getAttributes().getLabel();
            posSet.recalculateAttributeStatistics(posLabel);
            int labelValue = (int)posSet.getStatistics(posLabel, Statistics.MODE); 
        	usedLabelName = posLabel.getMapping().mapIndex(0);
        	if (labelValue >= 0)
        		usedLabelName = posLabel.getMapping().mapIndex(labelValue);
        }
        
		return this.criterion.getBenefit(posSet, negSet, usedLabelName);
    }
}
