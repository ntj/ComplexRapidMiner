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
package com.rapidminer.operator.performance.test;

import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceCriterion;

/**
 * Tests {@link EstimatedPerformance}.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: EstimatedCriterionTest.java,v 1.10 2006/03/21 15:35:51
 *          ingomierswa Exp $
 */
public class EstimatedCriterionTest extends CriterionTestCase {

	private EstimatedPerformance performance10x08, performance20x04;

	public void setUp() throws Exception {
		super.setUp();
		performance10x08 = new EstimatedPerformance("test_performance", 0.8, 10, false);
		performance20x04 = new EstimatedPerformance("test_performance", 0.4, 20, false);
	}

	public void tearDown() throws Exception {
		performance10x08 = performance20x04 = null;
		super.tearDown();
	}

	/**
	 * Tests micro and makro average. Since makro average is implemented in
	 * {@link PerformanceCriterion}, this does not have to be tested for
	 * measured performance criteria.
	 */
	public void testAverage() {
		performance10x08.buildAverage(performance20x04);
		assertEquals("Wrong weighted average", (10 * 0.8 + 20 * 0.4) / (10 + 20), performance10x08.getMikroAverage(), 0.0000001);
		assertEquals("Wrong makro average", (0.8 + 0.4) / 2, performance10x08.getMakroAverage(), 0.0000001);
	}

	public void testClone() {
		cloneTest("Clone of simple criterion", performance10x08);
		performance10x08.buildAverage(performance20x04);
		cloneTest("Clone of averaged criterion", performance10x08);
	}

}
