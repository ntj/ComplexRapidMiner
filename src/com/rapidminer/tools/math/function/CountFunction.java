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
package com.rapidminer.tools.math.function;

import com.rapidminer.example.Attribute;


/**
 * Calculates the number of values.
 * 
 * @author Tobias Malbrecht, Ingo Mierswa
 * @version $Id: CountFunction.java,v 1.2 2008/08/20 11:09:50 tobiasmalbrecht Exp $
 *
 */
public class CountFunction extends AbstractAggregationFunction {

	double totalWeightSum;
	
	public CountFunction() {
		this(DEFAULT_IGNORE_MISSINGS);
	}
	
	public CountFunction(Boolean ignoreMissings) {
		super(ignoreMissings);
	}
	
	public String getName() { return "count"; }

	protected void reset() {
		foundMissing = false;
		totalWeightSum = 0.0d;
	}
	
    public void update(double value, double weight) {
    	if (Double.isNaN(value)) {
    		foundMissing = true;
    		return;
    	}
    	totalWeightSum += weight;
    }
    
    public void update(double value) {
    	if (Double.isNaN(value)) {
    		foundMissing = true;
    		return;
    	}
   		totalWeightSum++;
    }
    
    public double getValue() {
    	if (foundMissing && !ignoreMissings) {
    		return Double.NaN;
    	}
    	return totalWeightSum;
    }
	
    public boolean supportsAttribute(Attribute attribute) {
        return true;
    }
}
