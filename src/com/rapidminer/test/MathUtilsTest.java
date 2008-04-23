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
package com.rapidminer.test;

import com.rapidminer.tools.math.MathFunctions;

import junit.framework.TestCase;

/**
 * A test for the {@link MathFunctions}.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: MathUtilsTest.java,v 1.1 2007/05/27 21:59:04 ingomierswa Exp $
 */
public class MathUtilsTest extends TestCase {

	public void testVariance() {
		assertEquals(MathFunctions.variance(new double[] { 0.1, 0.1, 0.0, -0.1 }, Double.NEGATIVE_INFINITY), 0.006875, 0.001);
		assertEquals(MathFunctions.variance(new double[] { 0.0, 0.0, 0.0 }, -1.0), 0.0);
	}

	public void testCorrelation() {
		assertEquals(MathFunctions.correlation(new double[] { 0.1, 0.2, -0.3, 0.0 }, new double[] { 0.0, 0.1, 0.1, -0.1 }), -0.161, 0.001);
	}
}
