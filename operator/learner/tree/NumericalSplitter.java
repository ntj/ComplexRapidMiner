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
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;

/**
 * Calculates the best split point for numerical attributes according to 
 * a given criterion.
 * 
 * @author Ingo Mierswa
 * @version $Id: NumericalSplitter.java,v 1.10 2008/05/09 19:22:53 ingomierswa Exp $
 */
public class NumericalSplitter {
	
    private Criterion criterion;
    
    
    public NumericalSplitter(Criterion criterion) {
        this.criterion = criterion;
    }
    
    public double getBestSplit(ExampleSet inputSet, Attribute attribute) {
        SortedExampleSet exampleSet = new SortedExampleSet((ExampleSet)inputSet.clone(), attribute, SortedExampleSet.INCREASING);
        Attribute labelAttribute = exampleSet.getAttributes().getLabel();
        double oldLabel = Double.NaN;
        double bestSplit = Double.NaN;
        double lastValue = Double.NaN;
        double bestSplitBenefit = Double.NEGATIVE_INFINITY;
        int counter = -1;
        Example lastExample = null;
        if (this.criterion.supportsIncrementalCalculation()) {
        	this.criterion.startIncrementalCalculation(exampleSet);
        }
        for (Example e : exampleSet) {
            counter++;
        	double currentValue = e.getValue(attribute);
        	double label = e.getValue(labelAttribute);            
    		if (this.criterion.supportsIncrementalCalculation()) {
    			if (lastExample != null) this.criterion.swapExample(lastExample);
    			lastExample = e;
    			if ((Double.isNaN(oldLabel)) || (oldLabel != label)) {
	    			double benefit = this.criterion.getIncrementalBenefit();

	    			if (benefit > bestSplitBenefit) {
	        			bestSplitBenefit = benefit;
	        			bestSplit = (lastValue + currentValue) / 2.0d;
	    			}
        		}
    		}
    		else if ((Double.isNaN(oldLabel)) || (oldLabel != label)) {
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
