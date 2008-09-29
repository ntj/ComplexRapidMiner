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
 * Calculates the maximum.
 * 
 * @author Tobias Malbrecht, Ingo Mierswa
 * @version $Id: MaxFunction.java,v 1.3 2008/08/20 11:09:50 tobiasmalbrecht Exp $
 *
 */
public class MaxFunction extends AbstractAggregationFunction {

	private double maxValue;
	
	public MaxFunction() {
		this(DEFAULT_IGNORE_MISSINGS);
	}
	
	public MaxFunction(Boolean ignoreMissings) {
		super(ignoreMissings);
	}
	
    public String getName() { return "maximum"; }

	protected void reset() {
		foundMissing = false;
		maxValue = Double.NEGATIVE_INFINITY;
	}
    
    public void update(double value, double weight) {
    	update(value);
    }
    
    public void update(double value) {
    	if (Double.isNaN(value)) {
    		foundMissing = true;
    		return;
    	}
    	if (value > maxValue) {
	    	maxValue = value;
    	}
    }
    
    public double getValue() {
    	if (foundMissing && !ignoreMissings) {
    		return Double.NaN;
    	}
		return maxValue;
    }    
    
    public boolean supportsAttribute(Attribute attribute) {
        return attribute.isNumerical();
    }
    
}
