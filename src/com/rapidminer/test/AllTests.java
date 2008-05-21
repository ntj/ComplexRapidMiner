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

import junit.framework.Test;
import junit.framework.TestSuite;

import com.rapidminer.RapidMiner;
import com.rapidminer.example.test.DataRowTest;
import com.rapidminer.example.test.SparseReaderTest;
import com.rapidminer.operator.performance.test.ClassificationCriterionTest;
import com.rapidminer.operator.performance.test.EstimatedCriterionTest;
import com.rapidminer.operator.performance.test.MeasuredCriterionTest;
import com.rapidminer.tools.LogService;

/**
 * The main test class. Performs all Tests with help of JUnit.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: AllTests.java,v 1.5 2008/05/09 19:22:48 ingomierswa Exp $
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		
		suite.addTest(new TestSuite(DataRowTest.class));
		suite.addTest(new TestSuite(SparseReaderTest.class));
		suite.addTest(new TestSuite(EstimatedCriterionTest.class));
		suite.addTest(new TestSuite(ClassificationCriterionTest.class));
		suite.addTest(new TestSuite(MeasuredCriterionTest.class));

		suite.addTest(new TestSuite(ApplicationTest.class));
		
		suite.addTest(SampleTest.suite());
		
		return suite;
	}

	public static void main(String[] argv) {
		// RapidMiner initialized only once (for performance reasons)
		LogService.getGlobal().setVerbosityLevel(LogService.OFF);
		RapidMiner.init(true, false, false, false);
		LogService.getGlobal().setVerbosityLevel(LogService.OFF);

		junit.textui.TestRunner.run(suite());
	}
}
