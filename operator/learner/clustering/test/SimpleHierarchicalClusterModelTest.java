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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.operator.learner.clustering.ClusterNode;
import com.rapidminer.operator.learner.clustering.DefaultClusterNode;
import com.rapidminer.operator.learner.clustering.MutableClusterNode;
import com.rapidminer.operator.learner.clustering.SimpleHierarchicalClusterModel;

import junit.framework.TestCase;

/**
 * Test class.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: SimpleHierarchicalClusterModelTest.java,v 1.5 2008/09/12 10:31:49 tobiasmalbrecht Exp $
 */
public class SimpleHierarchicalClusterModelTest extends TestCase {

	SimpleHierarchicalClusterModel model;

	public SimpleHierarchicalClusterModelTest(String arg0) {
		super(arg0);
	}

	public void testAccess() {
		ClusterNode root = model.getRootNode();
		Iterator it1 = root.getSubNodes();
		ClusterNode n1 = (ClusterNode) it1.next();
		ClusterNode n2 = (ClusterNode) it1.next();
		Iterator it2 = n1.getSubNodes();
		ClusterNode n11 = (ClusterNode) it2.next();
		assertTrue(n1.contains("a"));
		assertTrue(n1.containsInSubtree("a"));
		assertTrue(n1.containsInSubtree("b"));
		assertTrue(n2.containsInSubtree("d"));
		assertTrue(n11.containsInSubtree("e"));
		assertTrue(n11.containsInSubtree("a"));
		assertTrue(root.contains("x"));
		assertEquals(n1.getNumberOfObjectsInSubtree(), 4);
		Iterator<String> it = n1.getObjectsInSubtree();
		List<String> objList = new ArrayList<String>();
		while (it.hasNext())
			objList.add(it.next());
		assertTrue(objList.contains("b"));
		assertTrue(objList.contains("e"));
		// check whether a actually occurs twice
		objList.remove("a");
		assertTrue(objList.contains("a"));
	}

	public void testCopy() {
		SimpleHierarchicalClusterModel oldModel = model;
		model = new SimpleHierarchicalClusterModel(model);
		testAccess();
		model = new SimpleHierarchicalClusterModel(model.getRootNode());
		testAccess();
		((DefaultClusterNode) oldModel.getRootNode()).removeObject("x");
		testAccess();
	}

	public void testModify() {
		MutableClusterNode root = (MutableClusterNode) model.getRootNode();
		ClusterNode n1 = root.getSubNodeAt(0);
		root.removeSubNode(n1);
		root.insertSubNodeAt(n1, 0);
		testAccess();
		root.removeObject("x");
		assertFalse(root.contains("x"));
	}

	protected void setUp() throws Exception {
		super.setUp();
		model = new SimpleHierarchicalClusterModel();
		// Add some nodes
		DefaultClusterNode root = new DefaultClusterNode("root");
		DefaultClusterNode n1 = new DefaultClusterNode("1");
		DefaultClusterNode n2 = new DefaultClusterNode("2");
		DefaultClusterNode n11 = new DefaultClusterNode("11");
		root.addSubNode(n1);
		root.addSubNode(n2);
		n1.addSubNode(n11);
		model.setRootNode(root);
		// Add objects
		root.addObject("x");
		n1.addObject("a");
		n1.addObject("b");
		n2.addObject("d");
		n11.addObject("e");
		n11.addObject("a");
	}
}
