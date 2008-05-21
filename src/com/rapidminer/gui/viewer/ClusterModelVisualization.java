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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.rapidminer.datatable.SimpleDataTable;
import com.rapidminer.datatable.SimpleDataTableRow;
import com.rapidminer.gui.graphs.ClusterModelGraphCreator;
import com.rapidminer.gui.graphs.GraphViewer;
import com.rapidminer.gui.plotter.ParallelPlotter;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.operator.learner.clustering.AbstractClusterModel;
import com.rapidminer.operator.learner.clustering.CentroidBasedClusterModel;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.HierarchicalClusterModel;
import com.rapidminer.tools.Renderable;

/**
 * Pane that contains several visualizations for a cluster model
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: ClusterModelVisualization.java,v 1.6 2008/05/09 19:23:01 ingomierswa Exp $
 * 
 */
public class ClusterModelVisualization extends JPanel implements Renderable {

	private static final long serialVersionUID = 6093518176785862429L;

	public static final String TEXT_VIEW_DESCRIPTION = "Text View";

	public static final String FOLDER_VIEW_DESCRIPTION = "Folder View";

	public static final String GRAPH_VIEW_DESCRIPTION = "Graph View";

	public static final String CENTROID_PLOT_VIEW_DESCRIPTION = "Centroid Plot View";
	
	public static final String DENDROGRAM_PLOT_VIEW_DESCRIPTION = "Dendrogram";
	
	private final JPanel togglePanel;

	private final Map<String, JComponent> viewMap = new HashMap<String, JComponent>();

	private final ButtonGroup group;
	
	private Renderable renderer;

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
			GraphViewer viewer = new GraphViewer<String,String>(new ClusterModelGraphCreator((HierarchicalClusterModel)cm));
			renderer = viewer;
			graphView = viewer;
		} else if (cm instanceof FlatClusterModel) {
			folderView = new ExtendedJScrollPane(new ClusterTreeVisualization((FlatClusterModel) cm));
			GraphViewer viewer = new GraphViewer<String,String>(new ClusterModelGraphCreator((FlatClusterModel)cm));
			renderer = viewer;
			graphView = viewer;
		} else {
			folderView = new JLabel("no visualization supported for this kind of cluster model " + cm.getClass());
			graphView = new JLabel("no visualization supported for this kind of cluster model ");
		}
		addView(TEXT_VIEW_DESCRIPTION, new ExtendedJScrollPane(textComponent));
		addView(FOLDER_VIEW_DESCRIPTION, folderView);
		addView(GRAPH_VIEW_DESCRIPTION, graphView);
		if (cm instanceof CentroidBasedClusterModel) {
			JComponent centroidPlotterView = createCentroidPlotter((CentroidBasedClusterModel)cm);
			addView(CENTROID_PLOT_VIEW_DESCRIPTION, centroidPlotterView);
		}

		if (cm instanceof HierarchicalClusterModel) {
			JComponent dendrogramPlotterView = new DendrogramPlotter((HierarchicalClusterModel) cm);
			addView(DENDROGRAM_PLOT_VIEW_DESCRIPTION, dendrogramPlotterView);
		}

	}

	private JComponent createCentroidPlotter(CentroidBasedClusterModel cm) {
		String[] dimensionNames = cm.getDimensionNames();
		String[] columnNames = new String[dimensionNames.length + 1];
		System.arraycopy(dimensionNames, 0, columnNames, 0, dimensionNames.length);
		columnNames[columnNames.length - 1] = "Cluster";
		SimpleDataTable dataTable = new SimpleDataTable("Centroid Positions", columnNames);
		for (int i = 0; i < cm.getNumberOfClusters(); i++) {
			double[] centroidValues = cm.getCentroid(i);
			String clusterName = cm.getClusterAt(i).getId();
			double[] values = new double[centroidValues.length + 1];
			System.arraycopy(centroidValues, 0, values, 0, centroidValues.length);
			values[values.length - 1] = dataTable.mapString(values.length - 1, clusterName);
			dataTable.add(new SimpleDataTableRow(values));
		}
		ParallelPlotter plotter = new ParallelPlotter(dataTable);
		plotter.setPlotColumn(columnNames.length - 1, true);
		plotter.setLocalNormalization(false);
		return plotter;
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

	public int getRenderHeight(int preferredHeight) {
		return renderer.getRenderHeight(preferredHeight);
	}

	public int getRenderWidth(int preferredWidth) {
		return renderer.getRenderWidth(preferredWidth);
	}

	public void render(Graphics graphics, int width, int height) {
		renderer.render(graphics, width, height);
	}
}
