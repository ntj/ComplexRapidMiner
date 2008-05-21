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
package com.rapidminer.tools.math;

import com.rapidminer.tools.Tools;

/**
 * Objects of this class hold all information about a single ROC data point.
 * 
 * @author Ingo Mierswa
 * @version $Id: ROCPoint.java,v 1.3 2008/05/09 19:23:02 ingomierswa Exp $
 */
public class ROCPoint {

    private double falsePositives;
    
    private double truePositives;
    
    private double confidence;
    
    public ROCPoint(double falsePositives, double truePositives, double confidence) {
        this.falsePositives = falsePositives;
        this.truePositives = truePositives;
        this.confidence = confidence;
    }
    
    /** Returns the number of false positives, not the rate. */
    public double getFalsePositives() {
        return falsePositives;
    }
    
    /** Returns the number of true positives, not the rate. */
    public double getTruePositives() {
        return truePositives;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public String toString() {
        return "fp: " + Tools.formatIntegerIfPossible(falsePositives) + ", tp: " + Tools.formatIntegerIfPossible(truePositives) + ", confidence: " + Tools.formatIntegerIfPossible(confidence);
    }
}
