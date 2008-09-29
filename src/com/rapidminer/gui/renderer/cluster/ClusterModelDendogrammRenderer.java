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

import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.viewer.DendrogramPlotter;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.learner.clustering.HierarchicalClusterModel;
import com.rapidminer.report.Reportable;

/**
 * A renderer for the dendogram of a hierarchical cluster models.
 * 
 * @author Ingo Mierswa
 * @version $Id: ClusterModelDendogrammRenderer.java,v 1.3 2008/07/18 15:50:46 ingomierswa Exp $
 */
public class ClusterModelDendogrammRenderer extends AbstractRenderer {

	public String getName() {
		return "Dendogram View";
	}

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		HierarchicalClusterModel cm = (HierarchicalClusterModel) renderable;
		return new DendrogramPlotter(cm);
	}

	public Reportable createReportable(Object renderable, IOContainer ioContainer, int width, int height) {
		HierarchicalClusterModel cm = (HierarchicalClusterModel) renderable;
		DendrogramPlotter plotter = new DendrogramPlotter(cm);
		plotter.setSize(width, height);
		return plotter;
	}
}
