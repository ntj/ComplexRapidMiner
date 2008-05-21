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
package com.rapidminer.test;

import com.rapidminer.tools.math.MathFunctions;

import junit.framework.TestCase;

/**
 * A test for the {@link MathFunctions}.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: MathUtilsTest.java,v 1.3 2008/05/09 19:22:48 ingomierswa Exp $
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
