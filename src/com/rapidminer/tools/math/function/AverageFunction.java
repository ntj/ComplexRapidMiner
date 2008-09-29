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
 * Calculates the average.
 * 
 * @author Tobias Malbrecht, Ingo Mierswa
 * @version $Id: AverageFunction.java,v 1.2 2008/08/20 11:09:50 tobiasmalbrecht Exp $
 *
 */
public class AverageFunction extends AbstractAggregationFunction {

	private double valueSum;
	
	private double totalWeightSum;
	
	public AverageFunction() {
		this(DEFAULT_IGNORE_MISSINGS);
	}
	
	public AverageFunction(Boolean ignoreMissings) {
		super(ignoreMissings);
	}
	
	public String getName() { return "average"; }
    
	protected void reset() {
		foundMissing = false;
		valueSum = 0.0d;
		totalWeightSum = 0.0d;		
	}
	
	public void update(double value, double weight) {
		if (Double.isNaN(value)) {
			foundMissing = true;
			return;
		}
		valueSum += value * weight;
		totalWeightSum += weight;
	}
	
	public void update(double value) {
		if (Double.isNaN(value)) {
			foundMissing = true;
			return;
		}
		valueSum += value;
		totalWeightSum++;
	}
	
	public double getValue() {
		if (foundMissing && !ignoreMissings) {
			return Double.NaN;
		}
		return valueSum / totalWeightSum;
	}
	
    public boolean supportsAttribute(Attribute attribute) {
        return attribute.isNumerical();
    }    
}
