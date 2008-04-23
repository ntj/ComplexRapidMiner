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

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.RandomGenerator;

/**
 * Selects a random subset.
 *
 * @author Ingo Mierswa
 * @version $Id: RandomSubsetPreprocessing.java,v 1.3 2007/07/12 08:47:08 stiefelolm Exp $
 */
public class RandomSubsetPreprocessing implements SplitPreprocessing {

	private RandomGenerator random;
	
	private double subsetRatio = 0.2;
	
	public RandomSubsetPreprocessing(double subsetRatio, RandomGenerator random) {
		this.subsetRatio = subsetRatio;
		this.random = random;
	}
	
	public ExampleSet preprocess(ExampleSet inputSet) {
    	ExampleSet exampleSet = (ExampleSet)inputSet.clone();
        
        double usedSubsetRatio = subsetRatio;
        if (usedSubsetRatio < 0.0d) {
            double desiredNumber = Math.floor(Math.log(exampleSet.getAttributes().size()) / Math.log(2) + 1);
            usedSubsetRatio = desiredNumber / exampleSet.getAttributes().size();
        }
    	Iterator<Attribute> i = exampleSet.getAttributes().iterator();
    	while (i.hasNext()) {
    		i.next();
    		if (random.nextDouble() > usedSubsetRatio) {
    			i.remove();
    		}
    	}
    	
    	// ensure that at least one attribute is left
    	if (exampleSet.getAttributes().size() == 0) {
    		int index = random.nextInt(inputSet.getAttributes().size());
    		int counter = 0;
    		for (Attribute attribute : inputSet.getAttributes()) {
    			if (counter == index) {
    				exampleSet.getAttributes().addRegular(attribute);
    				break;
    			}
    			counter++;
    		}
    	}

    	return exampleSet;
	}
}
