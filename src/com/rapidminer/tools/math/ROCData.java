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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.tools.Tools;

/**
 * This container holds all ROC data points for a single ROC curve.
 * 
 * @author Ingo Mierswa
 * @version $Id: ROCData.java,v 1.5 2008/05/09 19:23:02 ingomierswa Exp $
 */
public class ROCData implements Iterable<ROCPoint> {

    private List<ROCPoint> points = new ArrayList<ROCPoint>();
    
    private double sumPos;
    
    private double sumNeg;
    
    private double bestIsometricsTP;
    
    public void addPoint(ROCPoint point) {
        points.add(point);
    }
    
    public void removePoint(ROCPoint point) {
        points.remove(point);
    }
    
    public int getNumberOfPoints() {
        return points.size();
    }
    
    public ROCPoint getPoint(int index) {
        return points.get(index);
    }
    
    public double getInterpolatedTruePositives(double d) {
        if (Tools.isZero(d))
            return 0.0d;
        
        if (Tools.isGreaterEqual(d, getTotalPositives()))
            return getTotalPositives();
        
        ROCPoint last = null;
        for (ROCPoint p : this) {
            double fpDivN = p.getFalsePositives() / getTotalNegatives();
            if (Tools.isGreater(fpDivN, d)) {
                if (last == null) {
                    return 0;
                } else {
                    return last.getTruePositives();
                }
            }
            last = p;
        }
        return getTotalPositives();
    }
    
    public Iterator<ROCPoint> iterator() {
        return points.iterator();
    }
    
    public void setTotalPositives(double sumPos) {
        this.sumPos = sumPos;
    }
    
    public double getTotalPositives() {
        return this.sumPos;
    }
    
    public void setTotalNegatives(double sumNeg) {
        this.sumNeg = sumNeg;
    }
    
    public double getTotalNegatives() {
        return this.sumNeg;
    }
    
    public void setBestIsometricsTPValue(double value) {
        this.bestIsometricsTP = value;
    }
    
    public double getBestIsometricsTPValue() {
        return this.bestIsometricsTP;
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer("ROC Points" + Tools.getLineSeparator());
        for (ROCPoint p : points)
            result.append(p + Tools.getLineSeparator());
        return result.toString();
    }
}
