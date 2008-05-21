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
package com.rapidminer.tools.math.matrix;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A simple sparse matrix implementation based on hash structures. The basic idea is, that every value not explicitely given is assumed to be zero.
 * 
 * @author Michael Wurst
 * @version $Id: SimpleSparseMatrix.java,v 1.5 2008/05/09 19:23:23 ingomierswa Exp $
 * 
 */
public class SimpleSparseMatrix<Ex, Ey> extends AbstractMatrix<Ex, Ey> {

    private static final long serialVersionUID = -5401611723498335593L;

	private Map<Ex, Map<Ey, Double>> objMap = new HashMap<Ex, Map<Ey, Double>>();

    private Set<Ey> yLabels = new HashSet<Ey>();

    public void setEntry(Ex x, Ey y, double v) {

        Map<Ey, Double> objEntries = objMap.get(x);
        if (objEntries == null) {
            objEntries = new HashMap<Ey, Double>();
            objMap.put(x, objEntries);
        }

        yLabels.add(y);

        if (v == 0.0)
            objEntries.remove(y);
        else
            objEntries.put(y, v);

    }

    public void incEntry(Ex x, Ey y, double v) {

        double currentValue = getEntry(x, y);
        setEntry(x, y, currentValue + v);
    }

    public double getEntry(Ex x, Ey y) {

        Map<Ey, Double> objEntries = objMap.get(x);
        if (objEntries != null) {
            Double val = objEntries.get(y);
            if (val != null)
                return val.doubleValue();
            else
                return 0.0;
        } else
            return 0.0;
    }

    public int getNumYEntries(Ex x) {

        Map<Ey, Double> objEntries = objMap.get(x);

        if (objEntries == null)
            return 0;
        else
            return objEntries.keySet().size();
    }

    public int getNumXLabels() {

        return objMap.keySet().size();
    }

    public Iterator<Ex> getXLabels() {

        return objMap.keySet().iterator();
    }

    public Iterator<Ey> getYEntries(Ex x) {

        Map<Ey, Double> objEntries = objMap.get(x);

        if (objEntries == null)
            return new HashSet<Ey>().iterator();
        else
            return objEntries.keySet().iterator();

    }

    public Iterator<Ey> getYLabels() {

        return yLabels.iterator();
    }

    public int getNumYLabels() {

        return yLabels.size();
    }

    
}
