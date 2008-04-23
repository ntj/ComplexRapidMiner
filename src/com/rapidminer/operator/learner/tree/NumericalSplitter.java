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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;

/**
 * 
 * @author Ingo Mierswa
 * @version $Id: NumericalSplitter.java,v 1.5 2007/06/20 00:16:24 ingomierswa Exp $
 */
public class NumericalSplitter {
	
    private Criterion criterion;
    
    private int sampleSize = -1;
    
    public NumericalSplitter(Criterion criterion, int sampleSize) {
        this.criterion = criterion;
        this.sampleSize = sampleSize;
    }
    
    public double getBestSplit(ExampleSet inputSet, Attribute attribute) {
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
        double bestSplitBenefit = Double.NEGATIVE_INFINITY;
        int counter = -1;
        for (Example e : exampleSet) {
            counter++;
        	double currentValue = e.getValue(attribute);
        	double label = e.getValue(labelAttribute);            
        	if ((Double.isNaN(oldLabel)) || (oldLabel != label)) {
        		double splitValue = (lastValue + currentValue) / 2.0d;
        		SplittedExampleSet splitted = SplittedExampleSet.splitByAttribute(exampleSet, attribute, splitValue);
        		double benefit = this.criterion.getBenefit(splitted);
        		if (benefit > bestSplitBenefit) {
        			bestSplitBenefit = benefit;
        			bestSplit = splitValue;
        		}
        		oldLabel = label;
        	}
            lastValue = currentValue;
        }
        return bestSplit;
    }
}
