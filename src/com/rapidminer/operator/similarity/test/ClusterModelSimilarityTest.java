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
package com.rapidminer.operator.similarity.test;

import com.rapidminer.operator.learner.clustering.DefaultClusterNode;
import com.rapidminer.operator.learner.clustering.SimpleHierarchicalClusterModel;
import com.rapidminer.operator.similarity.clustermodel.TreeDistance;

import junit.framework.TestCase;

/**
 * A test for the cluster model similarities.
 * 
 * @author Michael Wurst
 * @version $Id: ClusterModelSimilarityTest.java,v 1.3 2008/05/09 19:22:44 ingomierswa Exp $
 */
public class ClusterModelSimilarityTest extends TestCase {

	protected void setUp() throws Exception {
	// No setup necessary
	}

	public void testSimilarity() throws Exception {
		SimpleHierarchicalClusterModel model = new SimpleHierarchicalClusterModel();
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
		n11.addObject("g");
		TreeDistance sim = new TreeDistance();
		sim.init(model);
		assertEquals(0.0, sim.similarity("e", "g"));
		assertEquals(2.0, sim.similarity("b", "d"));
		assertEquals(0.0, sim.similarity("a", "a"));
		assertEquals(1.0, sim.similarity("b", "g"));
		assertEquals(2.0, sim.similarity("x", "g"));
	}
}
