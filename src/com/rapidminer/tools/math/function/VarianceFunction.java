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
 * Calculates the variance.
 * 
 * @author Tobias Malbrecht, Ingo Mierswa
 * @version $Id: VarianceFunction.java,v 1.2 2008/08/20 11:09:50 tobiasmalbrecht Exp $
 *
 */
public class VarianceFunction extends AbstractAggregationFunction {

	private double valueSum;
	
	private double squaredValueSum;
	
	private double totalWeightSum;
	
	public VarianceFunction() {
		this(DEFAULT_IGNORE_MISSINGS);
	}
	
	public VarianceFunction(Boolean ignoreMissings) {
		super(ignoreMissings);
	}
	
    public String getName() { return "variance"; }
    
	protected void reset() {
		foundMissing = false;
		valueSum = 0.0d;
		squaredValueSum = 0.0d;
		totalWeightSum = 0.0d;
	}

	public void update(double value, double weight) {
		if (Double.isNaN(value)) {
			foundMissing = true;
			return;
		}
		valueSum += weight * value;
		squaredValueSum += weight * value * value;
		totalWeightSum += weight;
    }
    
    public void update(double value) {
    	if (Double.isNaN(value)) {
    		foundMissing = true;
    		return;
    	}
    	valueSum += value;
    	squaredValueSum += value * value;
    	totalWeightSum++;
    }
    
    public double getValue() {
    	if (foundMissing && !ignoreMissings) {
    		return Double.NaN;
    	}
    	if (totalWeightSum <= 1.0d) {
    		return 0.0d;
    	} else {
    		return (squaredValueSum - valueSum * valueSum / totalWeightSum) / (totalWeightSum - 1);
    	}
    }
    
    public boolean supportsAttribute(Attribute attribute) {
        return attribute.isNumerical();
    }
}
