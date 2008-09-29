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
package com.rapidminer.gui.renderer.similarity;

import java.awt.Component;

import com.rapidminer.gui.plotter.PlotterAdapter;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.viewer.SimilarityKDistanceVisualization;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.similarity.DistanceSimilarityConverter;
import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.report.Reportable;

/**
 * A renderer for the k-distance view of a similarity measure.
 * 
 * @author Ingo Mierswa
 * @version $Id: SimilarityKDistanceRenderer.java,v 1.3 2008/07/18 15:50:45 ingomierswa Exp $
 */
public class SimilarityKDistanceRenderer extends AbstractRenderer {

	public String getName() {
		return "k-distances";
	}

	private PlotterAdapter createKDistancePlotter(Object renderable, IOContainer ioContainer) {
		SimilarityMeasure sim = (SimilarityMeasure) renderable;
		return new SimilarityKDistanceVisualization((sim.isDistance() ? sim : new DistanceSimilarityConverter(sim))); 
	}
	
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int width, int height) {
		PlotterAdapter plotter = createKDistancePlotter(renderable, ioContainer);
		plotter.getPlotter().setSize(width, height);
		return plotter;
	}
	
	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		return createKDistancePlotter(renderable, ioContainer);
	}
}
