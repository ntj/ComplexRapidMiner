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
package com.rapidminer.operator.performance.test;

import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.test.TestCase;


/**
 * Tests the given performance criterion.
 * 
 * @author Simon Fischer
 * @version $Id: CriterionTestCase.java,v 1.9 2006/03/21 15:35:51 ingomierswa
 *          Exp $
 */
public abstract class CriterionTestCase extends TestCase {

	public void assertAllValuesEqual(String message, PerformanceCriterion expected, PerformanceCriterion actual) {
		message += " " + expected.getName();
		assertEquals(message + " value", expected.getMikroAverage(), actual.getMikroAverage(), 0.000000001);
		assertEqualsNaN(message + " variance", expected.getMikroVariance(), actual.getMikroVariance());
		assertEqualsNaN(message + " makro value", expected.getMakroAverage(), actual.getMakroAverage());
		assertEqualsNaN(message + " makro variance", expected.getMakroVariance(), actual.getMakroVariance());
		assertEquals(message + " name", expected.getName(), actual.getName());
		assertEquals(message + " class", expected.getClass(), actual.getClass());
	}

	public void cloneTest(String message, PerformanceCriterion pc) {
		try {
			assertAllValuesEqual(message, pc, (PerformanceCriterion) pc.clone());
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
