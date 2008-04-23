/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.tools.math.matrix;

import java.io.Serializable;
import java.util.Iterator;

/**
 * A matrix with basic functionality.
 * 
 * @author Michael Wurst
 * @version $Id: Matrix.java,v 1.2 2007/05/28 21:23:34 ingomierswa Exp $
 * 
 */
public interface Matrix<Ex, Ey> extends Serializable {

    /**
     * Set an entry.
     * 
     * @param x
     * @param y
     * @param val
     */
    public void setEntry(Ex x, Ey y, double val);

    /**
     * Get an entry.
     * 
     * @param x
     * @param y
     * @return double
     */
    public double getEntry(Ex x, Ey y);

    /**
     * Increase an entry.
     * 
     * @param x
     * @param y
     * @param val
     */
    public void incEntry(Ex x, Ey y, double val);

    /**
     * Get the number of x labels (indices for which a values has been set).
     * 
     * @return int
     */
    public int getNumXLabels();

    /**
     * Get the number of y labels (indices for which a values has been set).
     * 
     * @return int
     */
    public int getNumYLabels();

    /**
     * Get an iteration of all x labels (indices for which a values has been set).
     * 
     * @return Iterator
     */
    public Iterator<Ex> getXLabels();

    /**
     * Get an iteration of all y labels (indices for which a values has been set).
     * 
     * @return Iterator
     */
    public Iterator<Ey> getYLabels();
}
