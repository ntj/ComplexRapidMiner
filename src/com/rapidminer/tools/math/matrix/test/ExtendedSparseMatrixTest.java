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
package com.rapidminer.tools.math.matrix.test;

import com.rapidminer.tools.math.matrix.ExtendedMatrix;
import com.rapidminer.tools.math.matrix.ExtendedSparseMatrix;

import junit.framework.TestCase;

/**
 * A test for the {@link ExtendedSparseMatrix}.
 * 
 * @author Michael Wurst
 * @version $Id: ExtendedSparseMatrixTest.java,v 1.3 2008/05/09 19:23:24 ingomierswa Exp $
 */
public class ExtendedSparseMatrixTest extends TestCase {

    public void testAccess() {
        ExtendedMatrix<String, String> m = new ExtendedSparseMatrix<String, String>();

        m.setEntry("a", "b", 0.4);
        assertEquals(m.getEntry("a", "b"), 0.4);

        m.setEntry("a", "c", 0.0);
        assertEquals(m.getEntry("a", "c"), 0.0);

        assertEquals(m.getEntry("a", "d"), 0.0);
        assertEquals(m.getEntry("d", "a"), 0.0);
        assertEquals(m.getEntry("x", "y"), 0.0);

        m.incEntry("a", "b", 0.4);
        assertEquals(m.getEntry("a", "b"), 0.8);

        m.incEntry("a", "e", 0.4);

        assertEquals(m.getNumXLabels(), 1);
        assertEquals(m.getNumYLabels(), 3);

        assertEquals(m.getNumYEntries("a"), 2);
        assertEquals(m.getNumXEntries("b"), 1);

        m.setEntry("a", "b", 0.0);

        assertEquals(m.getNumYEntries("a"), 1);
        assertEquals(m.getNumXEntries("a"), 0);

        assertEquals(m.getYEntries("a").next(), "e");
    }
}
