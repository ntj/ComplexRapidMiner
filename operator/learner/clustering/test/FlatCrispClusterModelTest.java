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

import com.rapidminer.operator.learner.clustering.DefaultCluster;
import com.rapidminer.operator.learner.clustering.DefaultClusterNode;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.FlattendClusterModel;
import com.rapidminer.operator.learner.clustering.MutableCluster;
import com.rapidminer.operator.learner.clustering.SimpleHierarchicalClusterModel;

import junit.framework.TestCase;

/**
 * Test case for the crisp flat cluster model.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: FlatCrispClusterModelTest.java,v 1.5 2008/09/12 10:31:48 tobiasmalbrecht Exp $
 */
public class FlatCrispClusterModelTest extends TestCase {

	FlatCrispClusterModel model;

	public FlatCrispClusterModelTest(String arg0) {
		super(arg0);
	}

	public void testAccess() {
		assertTrue(model.getClusterAt(0).contains("a"));
		assertTrue(model.getClusterAt(0).contains("c"));
		assertTrue(model.getClusterAt(1).contains("b"));
	}

	public void testCopy() {
		FlatCrispClusterModel oldModel = model;
		model = new FlatCrispClusterModel(model);
		testAccess();
		oldModel.removeClusterAt(0);
		testAccess();
	}

	public void testCopyFromHierarchical() {
		SimpleHierarchicalClusterModel hmodel = new SimpleHierarchicalClusterModel();
		// Add some nodes
		DefaultClusterNode root = new DefaultClusterNode("root");
		DefaultClusterNode n1 = new DefaultClusterNode("1");
		DefaultClusterNode n2 = new DefaultClusterNode("2");
		DefaultClusterNode n11 = new DefaultClusterNode("11");
		root.addSubNode(n1);
		root.addSubNode(n2);
		n1.addSubNode(n11);
		// Add objects
		n1.addObject("a");
		n1.addObject("b");
		n2.addObject("d");
		n11.addObject("e");
		n11.addObject("a");
		hmodel.setRootNode(root);
		FlatCrispClusterModel model2 = new FlattendClusterModel(hmodel);
		assertTrue(model2.getClusterAt(0).contains("a"));
		assertTrue(model2.getClusterAt(0).contains("e"));
		assertTrue(model2.getClusterAt(0).contains("b"));
		assertTrue(model2.getClusterAt(1).contains("d"));
		assertTrue(model2.getNumberOfClusters() == 2);
	}

	public void testModify() {
		((DefaultCluster) model.getClusterAt(0)).removeObject("a");
		((DefaultCluster) model.getClusterAt(1)).removeObject("b");
		((DefaultCluster) model.getClusterAt(0)).addObject("e");
		assertFalse(model.getClusterAt(0).contains("a"));
		assertTrue(model.getClusterAt(0).contains("e"));
		assertFalse(model.getClusterAt(1).contains("b"));
		assertTrue(((DefaultCluster) model.getClusterAt(1)).getNumberOfObjects() == 0);
	}

	protected void setUp() throws Exception {
		super.setUp();
		model = new FlatCrispClusterModel();
		model.addCluster(new DefaultCluster("0"));
		model.addCluster(new DefaultCluster("1"));
		((MutableCluster) model.getClusterAt(0)).addObject("a");
		((MutableCluster) model.getClusterAt(1)).addObject("b");
		((MutableCluster) model.getClusterAt(0)).addObject("c");
	}
}
