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
package com.rapidminer.gui.plotter.charts;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;

import com.rapidminer.gui.tools.SwingTools;


/**
 * This is the 2D bar chart plotter.
 * 
 * @author Ingo Mierswa
 * @version $Id: BarChart2DPlotter.java,v 1.4 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class BarChart2DPlotter extends AbstractBarChartPlotter {

	private static final long serialVersionUID = -1096632980936041510L;

	public JFreeChart createChart(CategoryDataset categoryDataSet, String groupByName, String valueName, boolean createLegend) {

        JFreeChart chart = ChartFactory.createBarChart(
            null,                     // chart title
            groupByName,              // domain axis label
            valueName,                // range axis label
            categoryDataSet,          // data
            PlotOrientation.VERTICAL, // orientation
            ((createLegend) && (groupByName != null)),      // include legend if group by column is set
            true,                     // tooltips
            false                     // URLs
        );
        
        // get a reference to the plot for further customisation...
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(230, 230, 230));
        plot.setDomainGridlinePaint(Color.white);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.white);
        
        // set up paints for series
        if (groupByName == null) {
        	BarRenderer renderer = (BarRenderer) plot.getRenderer();
        	renderer.setSeriesPaint(0, SwingTools.LIGHT_BLUE);
        	renderer.setSeriesPaint(1, SwingTools.LIGHT_YELLOW);
        }
    
        // domain axis labels
    	CategoryAxis domainAxis = plot.getDomainAxis();
        if (groupByName == null) {
        	domainAxis.setTickLabelsVisible(true);
        	domainAxis.setCategoryLabelPositions(
        			CategoryLabelPositions.createUpRotationLabelPositions(
        					Math.PI / 6.0));
        } else {
        	domainAxis.setTickLabelsVisible(false);
        }

		return chart;
	}
}
