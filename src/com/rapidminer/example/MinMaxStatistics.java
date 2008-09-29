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
package com.rapidminer.example;

import com.rapidminer.tools.LogService;

/** Attribute statistics object for (pseudo-)numerical attributes like real numerical attributes
 *  or date attributes. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: MinMaxStatistics.java,v 1.1 2008/05/25 12:08:46 ingomierswa Exp $
 */
public class MinMaxStatistics implements Statistics {

	private static final long serialVersionUID = 1027895282018510951L;

	private double minimum = Double.POSITIVE_INFINITY;

    private double maximum = Double.NEGATIVE_INFINITY;
    
    public MinMaxStatistics() {}
    
    /** Clone constructor. */
    private MinMaxStatistics(MinMaxStatistics other) {
        this.minimum = other.minimum;
        this.maximum = other.maximum;
    }
    
    public Object clone() {
        return new MinMaxStatistics(this);
    }
    
	public void count(double value) {
        if (!Double.isNaN(value)) {
            if (minimum > value)
                minimum = value;
            if (maximum < value)
                maximum = value;
        }
	}

	public double getStatistics(Attribute attribute, String name,	String parameter) {
        if (MINIMUM.equals(name)) {
            return this.minimum;
        } else if (MAXIMUM.equals(name)) {
            return this.maximum;    
        } else {
            LogService.getGlobal().log("Cannot calculate statistics, unknown type: " + name, LogService.WARNING);
            return Double.NaN;
        }
	}

	public boolean handleStatistics(String name) {
        return 
        MINIMUM.equals(name) ||
        MAXIMUM.equals(name);
	}

	public void startCounting(Attribute attribute) {
        this.minimum = Double.POSITIVE_INFINITY;
        this.maximum = Double.NEGATIVE_INFINITY;
	}
}
