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

/** Attribute statistics object for numerical attributes. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: NumericalStatistics.java,v 1.7 2008/05/25 12:08:46 ingomierswa Exp $
 */
public class NumericalStatistics implements Statistics {

    private static final long serialVersionUID = -6283236022093847887L;

    private double sum = 0.0d;

    private double squaredSum = 0.0d;

    private int valueCounter = 0;
    
    public NumericalStatistics() {}
    
    /** Clone constructor. */
    private NumericalStatistics(NumericalStatistics other) {
        this.sum = other.sum;
        this.squaredSum = other.squaredSum;
        this.valueCounter = other.valueCounter;
    }
    
    public Object clone() {
        return new NumericalStatistics(this);
    }
    
    public void startCounting(Attribute attribute) {
        this.sum = 0.0d;
        this.squaredSum = 0.0d;
        this.valueCounter = 0;
    }
    
    public void count(double value) {
        if (!Double.isNaN(value)) {
            sum += value;
            squaredSum += value * value;
            valueCounter++;
        }
    }

    public boolean handleStatistics(String name) {
        return 
            AVERAGE.equals(name) ||
            VARIANCE.equals(name) ||
            SUM.equals(name);
    }
    
    public double getStatistics(Attribute attribute, String name, String parameter) {
    	if (AVERAGE.equals(name)) {
            return this.sum / this.valueCounter;
        } else if (VARIANCE.equals(name)) {
            double average = this.sum / this.valueCounter;
            return squaredSum / valueCounter - (average * average);
        } else if (SUM.equals(name)) {
            return this.sum;
        } else {
            LogService.getGlobal().log("Cannot calculate statistics, unknown type: " + name, LogService.WARNING);
            return Double.NaN;
        }
    }
}
