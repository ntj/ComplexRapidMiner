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
package com.rapidminer.operator.learner.clustering.test;

import java.io.File;

import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.test.SampleTest;
import com.rapidminer.test.SimpleSampleTest;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.ParameterService;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for the clustering package.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: ClusteringTestSuite.java,v 1.6 2008/09/12 10:31:49 tobiasmalbrecht Exp $
 */
public abstract class ClusteringTestSuite extends SampleTest {

	private String file;

	public ClusteringTestSuite(String file) {
		super("clustering test");
		this.file = file;
	}

	public String getName() {
		return "Sample '" + file + "'";
	}

	public void setUp() throws Exception {
		super.setUp();
	}

	public void sampleTest() throws Exception {
		File processFile = new File(ParameterService.getRapidMinerHome(), "plugins" + File.separator + "clustering" + File.separator + "sample"
				+ File.separator + file);
		Process process = RapidMiner.readProcessFile(processFile);
		process.getLog().setVerbosityLevel(LogService.WARNING);
		IOContainer output = process.run();
		checkOutput(output);
	}

	public abstract void checkOutput(IOContainer output) throws MissingIOObjectException;

	public static Test suite() {
		TestSuite suite = new TestSuite("Sample test");
		// suite.addTest(new SimpleSampleTest("simple_kmeans.xml"));
		suite.addTest(new SimpleSampleTest("dbscan_synth.xml"));
		suite.addTest(new SimpleSampleTest("simple_agglomorative.xml"));
		return suite;
	}
}
