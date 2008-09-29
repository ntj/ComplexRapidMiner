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
import java.util.Iterator;
import java.util.Random;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.SimpleDataTable;
import com.rapidminer.datatable.SimpleDataTableRow;
import com.rapidminer.gui.plotter.HistogramPlotter;
import com.rapidminer.gui.plotter.Plotter;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.report.Reportable;

/**
 * A renderer for the histogram view of a similarity measure.
 * 
 * @author Ingo Mierswa
 * @version $Id: SimilarityHistogramRenderer.java,v 1.3 2008/07/18 15:50:44 ingomierswa Exp $
 */
public class SimilarityHistogramRenderer extends AbstractRenderer {

	public String getName() {
		return "Histogram";
	}
	
	private Plotter createHistogramPlotter(Object renderable, IOContainer ioContainer) {
		SimilarityMeasure sim = (SimilarityMeasure) renderable;
		DataTable dataTable = new SimpleDataTable("Histogram", new String[] { "Histogram" });
        double sampleRatio = Math.min(1.0d, 500.0d / sim.getNumberOfIds());
        
        Random random = new Random();
        Iterator<String> i = sim.getIds();
        while (i.hasNext()) {
            String idX = i.next();
            Iterator<String> j = sim.getIds();
            if (random.nextDouble() < sampleRatio) {
                while (j.hasNext()) {
                    String idY = j.next();
                    if (!(idX.equals(idY)) && (random.nextDouble() < sampleRatio)) {
                        double simValue = sim.similarity(idX, idY);
                        dataTable.add(new SimpleDataTableRow(new double[] { simValue }));
                    }
                }
            }
        }

		HistogramPlotter histogramView = new HistogramPlotter();
		histogramView.setDataTable(dataTable);
		histogramView.setPlotColumn(0, true);
		histogramView.setBinNumber(100);
		
		return histogramView;
	}
	
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int width, int height) {
		Plotter plotter = createHistogramPlotter(renderable, ioContainer);
		plotter.getPlotter().setSize(width, height);
		return plotter;
	}

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		return createHistogramPlotter(renderable, ioContainer).getPlotter();
	}
}
