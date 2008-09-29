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
package com.rapidminer.gui.renderer.cluster;

import java.awt.Component;

import com.rapidminer.datatable.SimpleDataTable;
import com.rapidminer.datatable.SimpleDataTableRow;
import com.rapidminer.gui.plotter.ParallelPlotter;
import com.rapidminer.gui.plotter.PlotterAdapter;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.learner.clustering.CentroidBasedClusterModel;
import com.rapidminer.report.Reportable;

/**
 * A renderer for centroid based cluster models.
 * 
 * @author Ingo Mierswa
 * @version $Id: ClusterModelCentroidPlotRenderer.java,v 1.3 2008/07/18 15:50:46 ingomierswa Exp $
 */
public class ClusterModelCentroidPlotRenderer extends AbstractRenderer {

	public String getName() {
		return "Centroid Plot View";
	}

	private PlotterAdapter createCentroidPlotter(CentroidBasedClusterModel cm, int width, int height) {
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
		plotter.getPlotter().setSize(width, height);
		return plotter;
	}
	
	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		CentroidBasedClusterModel cm = (CentroidBasedClusterModel) renderable;
		return createCentroidPlotter(cm, 800, 600);
	}

	public Reportable createReportable(Object renderable, IOContainer ioContainer, int width, int height) {
		CentroidBasedClusterModel cm = (CentroidBasedClusterModel) renderable;
		return (createCentroidPlotter(cm, width, height));
	}
}
