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

/** The superclass for all attribute statistics objects. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: UnknownStatistics.java,v 1.6 2008/05/09 19:22:42 ingomierswa Exp $
 */
public class UnknownStatistics implements Statistics {
    
    private static final long serialVersionUID = 217609774484151520L;
    
    private int unknownCounter = 0;
    
    public UnknownStatistics() {}
    
    /** Clone constructor. */
    private UnknownStatistics(UnknownStatistics other) {
        this.unknownCounter = other.unknownCounter;
    }
    
    public Object clone() {
        return new UnknownStatistics(this);
    }
    
    public void startCounting(Attribute attribute) {
        this.unknownCounter = 0;
    }
    
    public void count(double value) {
        if (Double.isNaN(value))
            unknownCounter++;
    }

    public double getStatistics(Attribute attribute, String statisticsName, String parameter) {
        if (UNKNOWN.equals(statisticsName)) {
            return unknownCounter;
        } else {
            LogService.getGlobal().log("Cannot calculate statistics, unknown type: " + statisticsName, LogService.WARNING);
            return Double.NaN;
        }
    }

    public boolean handleStatistics(String statisticsName) {
        return UNKNOWN.equals(statisticsName);
    }
    
    public String toString() {
        return "unknown: " + this.unknownCounter;
    }
}
