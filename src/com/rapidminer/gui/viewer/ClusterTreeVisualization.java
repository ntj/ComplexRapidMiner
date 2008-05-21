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
package com.rapidminer.gui.viewer;

import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import com.rapidminer.ObjectVisualizer;
import com.rapidminer.operator.learner.clustering.Cluster;
import com.rapidminer.operator.learner.clustering.ClusterNode;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.HierarchicalClusterModel;
import com.rapidminer.tools.ObjectVisualizerService;


/**
 * Visualizes clusters as a bookmark like tree.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: ClusterTreeVisualization.java,v 1.3 2008/05/09 19:23:01 ingomierswa Exp $
 * 
 */
public class ClusterTreeVisualization extends JTree implements TreeSelectionListener {

	private static final long serialVersionUID = 3994390578811027103L;

	private static class ClusterTreeLeaf {

		private String title;

		private String id;

		public ClusterTreeLeaf(String title, String id) {
			this.title = title;
			this.id = id;
		}

		public String toString() {
			return title;
		}

		/** Returns the id. */
		public String getId() {
			return id;
		}

		/** Returns the title. */
		public String getTitle() {
			return title;
		}
	}
	
	public ClusterTreeVisualization(HierarchicalClusterModel cm) {
		DefaultTreeModel model = new DefaultTreeModel(generateTreeModel(cm.getRootNode()));
		setModel(model);
		addTreeSelectionListener(this);
	}

	public ClusterTreeVisualization(FlatClusterModel cm) {
		DefaultTreeModel model = new DefaultTreeModel(generateFlatModel(cm));
		setModel(model);
		addTreeSelectionListener(this);
	}

	private DefaultMutableTreeNode generateFlatModel(FlatClusterModel cm) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
		rootNode.setAllowsChildren(true);
		for (int i = 0; i < cm.getNumberOfClusters(); i++) {
			Cluster cl = cm.getClusterAt(i);
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(cl);
			newNode.setAllowsChildren(true);
			rootNode.add(newNode);
			Iterator it = cl.getObjects();
			while (it.hasNext()) {
				String id = (String) it.next();
				newNode.add(createLeaf(id));
			}
		}
		return rootNode;
	}

	private DefaultMutableTreeNode generateTreeModel(ClusterNode cl) {
		DefaultMutableTreeNode result = new DefaultMutableTreeNode(cl);
		result.setAllowsChildren(true);
		Iterator it = cl.getSubNodes();
		// Add sub clusters
		while (it.hasNext()) {
			result.add(generateTreeModel((ClusterNode) it.next()));
		}

		// Add objects
		Iterator it2 = cl.getObjects();
		while (it2.hasNext()) {
			String id = (String) it2.next();
			result.add(createLeaf(id));
		}
		return result;
	}

	private MutableTreeNode createLeaf(String id) {
		ObjectVisualizer viz = ObjectVisualizerService.getVisualizerForObject(id);
		String title = viz.getTitle(id);
		if (title == null)
			title = id;
		DefaultMutableTreeNode newLeaf = new DefaultMutableTreeNode(new ClusterTreeLeaf(title, id));
		newLeaf.setAllowsChildren(false);
		return newLeaf;
	}

	public void valueChanged(TreeSelectionEvent e) {
		TreePath[] paths = getSelectionPaths();
		// If only one item has been selected, then change the text in the
		// description area
		if (paths == null)
			return;
		if (paths.length == 1) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0].getLastPathComponent();
			if (!node.getAllowsChildren()) {
				ClusterTreeLeaf leaf = (ClusterTreeLeaf) node.getUserObject();
				ObjectVisualizer viz = ObjectVisualizerService.getVisualizerForObject(leaf.getId());
				viz.startVisualization(leaf.getId());
			}
		}
	}
}
