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

import com.rapidminer.tools.LogService;

import junit.framework.AssertionFailedError;

/**
 * Extends the JUnit test case by a method for asserting equality of doubles
 * with respect to Double.NaN
 * 
 * @author Simon Fischer
 * @version $Id: TestCase.java,v 1.1 2007/05/27 21:59:04 ingomierswa Exp $
 */
public class TestCase extends junit.framework.TestCase {

	public TestCase() {
		super();
	}

	public TestCase(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		super.setUp();
		LogService.getGlobal().setVerbosityLevel(LogService.WARNING);
	}

	public void assertEqualsNaN(String message, double expected, double actual) {
		if (Double.isNaN(expected)) {
			if (!Double.isNaN(actual)) {
				throw new AssertionFailedError(message + " expected: <" + expected + "> but was: <" + actual + ">");
			}
		} else {
			assertEquals(message, expected, actual, 0.000000001);
		}
	}

}
