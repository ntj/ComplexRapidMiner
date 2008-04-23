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
package com.rapidminer.gui.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.rapidminer.gui.graphs.ClusterModelGraphCreator;
import com.rapidminer.gui.graphs.GraphViewer;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.operator.learner.clustering.AbstractClusterModel;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.HierarchicalClusterModel;

/**
 * Pane that contains several visualizations for a cluster model
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: ClusterModelVisualization.java,v 1.1 2007/06/17 15:48:56 ingomierswa Exp $
 * 
 */
public class ClusterModelVisualization extends JPanel {

	private static final long serialVersionUID = 6093518176785862429L;

	public static final String TEXT_VIEW_DESCRIPTION = "Text View";

	public static final String FOLDER_VIEW_DESCRIPTION = "Folder View";

	public static final String GRAPH_VIEW_DESCRIPTION = "Graph View";
	
	private final JPanel togglePanel;

	private final Map<String, JComponent> viewMap = new HashMap<String, JComponent>();

	private final ButtonGroup group;

	public ClusterModelVisualization(AbstractClusterModel cm, final Component textComponent) {
		super();
		group = new ButtonGroup();
		setLayout(new BorderLayout());
		togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(togglePanel, BorderLayout.NORTH);

		final JComponent folderView;
		final JComponent graphView;
		if (cm instanceof HierarchicalClusterModel) {
			folderView = new ExtendedJScrollPane(new ClusterTreeVisualization((HierarchicalClusterModel) cm));
			graphView = new GraphViewer<String,String>(new ClusterModelGraphCreator((HierarchicalClusterModel)cm));
		} else if (cm instanceof FlatClusterModel) {
			folderView = new ExtendedJScrollPane(new ClusterTreeVisualization((FlatClusterModel) cm));
			graphView = new GraphViewer<String,String>(new ClusterModelGraphCreator((FlatClusterModel)cm));
		} else {
			folderView = new JLabel("no visualization supported for this kind of cluster model " + cm.getClass());
			graphView = new JLabel("no visualization supported for this kind of cluster model ");
		}

		addView(TEXT_VIEW_DESCRIPTION, new ExtendedJScrollPane(textComponent));
		addView(FOLDER_VIEW_DESCRIPTION, folderView);
		addView(GRAPH_VIEW_DESCRIPTION, graphView);

	}

	public void addView(String description, JComponent view) {
		if (viewMap.get(description) != null) {
			viewMap.put(description, view);
		} else {
			viewMap.put(description, view);
			final JRadioButton newViewButton = new JRadioButton(description, false);
			togglePanel.add(newViewButton);
			group.add(newViewButton);
			newViewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (newViewButton.isSelected()) {
						remove(1);
						add(viewMap.get(newViewButton.getText()), BorderLayout.CENTER);
						repaint();
					}
				}
			});

			// If this is the first view to be added, select it
			newViewButton.setSelected(viewMap.keySet().size() <= 1);
			if (viewMap.keySet().size() <= 1)
				add(view, BorderLayout.CENTER);
		}
	}
}
