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
package com.rapidminer.gui.plotter.charts;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.PieDataset;

/**
 * A simple 3D pie chart plotter.
 * 
 * @author Ingo Mierswa
 * @version $Id: PieChart3DPlotter.java,v 1.1 2007/05/27 22:02:53 ingomierswa Exp $
 */
public class PieChart3DPlotter extends AbstractPieChartPlotter {

	private static final long serialVersionUID = -2107283003284552898L;

	public JFreeChart createChart(PieDataset pieDataSet) {
		JFreeChart chart = ChartFactory.createPieChart3D(
				null,
                pieDataSet,
                true, // legend
                true,
                false);
        
		PiePlot3D plot = (PiePlot3D) chart.getPlot();
		plot.setSectionOutlinesVisible(false);
		plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		plot.setNoDataMessage("No data available");
		plot.setCircular(false);
		plot.setLabelGap(0.02);
		plot.setForegroundAlpha(0.5f);
		plot.setOutlinePaint(Color.WHITE);
		
		return chart;
	}
}
