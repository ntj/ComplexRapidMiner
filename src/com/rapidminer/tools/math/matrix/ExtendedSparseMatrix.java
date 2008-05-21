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

import java.util.Iterator;

import com.rapidminer.operator.IOObject;


/**
 * A sparse implementation of an extended matrix.
 * 
 * @author Michael Wurst
 * @version $Id: ExtendedSparseMatrix.java,v 1.3 2008/05/09 19:23:23 ingomierswa Exp $
 * 
 */
public class ExtendedSparseMatrix<Ex, Ey> extends AbstractMatrix<Ex, Ey> implements ExtendedMatrix<Ex, Ey> {

    private static final long serialVersionUID = -4863248877511747800L;

	private SimpleSparseMatrix<Ex, Ey> xIndexedMatrix = new SimpleSparseMatrix<Ex, Ey>();

    private SimpleSparseMatrix<Ey, Ex> yIndexedMatrix = new SimpleSparseMatrix<Ey, Ex>();

    public IOObject copy() {

        ExtendedSparseMatrix<Ex, Ey> result = new ExtendedSparseMatrix<Ex, Ey>();

        Iterator<Ex> xLabels = getXLabels();
        while (xLabels.hasNext()) {

            Ex xLabel = xLabels.next();
            Iterator<Ey> yLabels = getYLabels();
            while (yLabels.hasNext()) {
                Ey yLabel = yLabels.next();
                result.setEntry(xLabel, yLabel, getEntry(xLabel, yLabel));
            }
        }

        return result;
    }

    public int getNumYEntries(Ex x) {

        return xIndexedMatrix.getNumYEntries(x);
    }

    public int getNumXEntries(Ey y) {

        return yIndexedMatrix.getNumYEntries(y);
    }

    public Iterator<Ey> getYEntries(Ex x) {

        return xIndexedMatrix.getYEntries(x);
    }

    public Iterator<Ex> getXEntries(Ey y) {

        return yIndexedMatrix.getYEntries(y);
    }

    public int getNumXLabels() {
        return xIndexedMatrix.getNumXLabels();
    }

    public int getNumYLabels() {
        return yIndexedMatrix.getNumXLabels();
    }

    public Iterator<Ex> getXLabels() {
        return xIndexedMatrix.getXLabels();
    }

    public Iterator<Ey> getYLabels() {
        return yIndexedMatrix.getXLabels();
    }

    public void setEntry(Ex x, Ey y, double val) {
        xIndexedMatrix.setEntry(x, y, val);
        yIndexedMatrix.setEntry(y, x, val);
    }

    public double getEntry(Ex x, Ey y) {

        return xIndexedMatrix.getEntry(x, y);
    }

    public void incEntry(Ex x, Ey y, double val) {

        double currentValue = xIndexedMatrix.getEntry(x, y);
        setEntry(x, y, currentValue + val);

    }

}
