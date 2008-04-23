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
package com.rapidminer.tools.math.matrix.test;

import com.rapidminer.tools.math.matrix.Matrix;
import com.rapidminer.tools.math.matrix.SimpleSparseMatrix;

import junit.framework.TestCase;

/**
 * Tests the simple sparse matrix.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: SimpleSparseMatrixTest.java,v 1.1 2007/05/27 22:03:40 ingomierswa Exp $
 */
public class SimpleSparseMatrixTest extends TestCase {

    public void testAccess() {
        Matrix<String, String> m = new SimpleSparseMatrix<String, String>();

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
    }
}
