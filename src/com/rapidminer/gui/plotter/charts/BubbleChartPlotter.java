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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;
import com.rapidminer.gui.plotter.PlotterAdapter;
import com.rapidminer.tools.math.MathFunctions;


/**
 * This is the bubble chart plotter.
 * 
 * @author Ingo Mierswa
 * @version $Id: BubbleChartPlotter.java,v 1.5 2008/05/09 19:22:58 ingomierswa Exp $
 *
 */
public class BubbleChartPlotter extends PlotterAdapter {
    
	private static final long serialVersionUID = 4568273282283350833L;

	/** The axis names. */
	private static final String[] axisNames = {
		"x-Axis",
		"y-Axis",
		"Bubble Size"
	};
	
	private static final int X_AXIS = 0;
	private static final int Y_AXIS = 1;
	private static final int BUBBLE_SIZE_AXIS = 2;
	
	/** The currently used data table object. */
	private transient DataTable dataTable;
	
	/** The pie data set. */
	private DefaultXYZDataset xyzDataSet = new DefaultXYZDataset();
	
	/** The columns which are used for the axes. */
	private int[] axis = new int[] { -1, -1, -1 };
	
	/** The column which is used for the color. */
	private int colorColumn = -1;
	
	private double bubbleSizeMin = 0;
	
	private double bubbleSizeMax = 1;
	
	private double xAxisMin = 0;
	
	private double xAxisMax = 1;

	private double yAxisMin = 0;
	
	private double yAxisMax = 1;
	
	public BubbleChartPlotter() {
		super();
		setBackground(Color.white);
		
	}

	public BubbleChartPlotter(DataTable dataTable) {
		this();
		setDataTable(dataTable);
	}
	
	public void setDataTable(DataTable dataTable) {
		super.setDataTable(dataTable);
		this.dataTable = dataTable;
		repaint();
	}
	
    public void setPlotColumn(int index, boolean plot) {
		if (plot)
			this.colorColumn = index;
		else
			this.colorColumn = -1;
		repaint();
	}
	
	public boolean getPlotColumn(int index) {
		return colorColumn == index;
	}
	
	public String getPlotName() { return "Color Column"; }

	public int getNumberOfAxes() {
		return 3;
	}

	public void setAxis(int index, int dimension) {
		axis[index] = dimension;
		repaint();
	}

	public int getAxis(int index) {
		return axis[index];
	}
	
	public String getAxisName(int index) {
		return axisNames[index];
	}
	
	private void prepareData() {
		xyzDataSet = new DefaultXYZDataset();
		this.bubbleSizeMin = Double.POSITIVE_INFINITY;
		this.bubbleSizeMax = Double.NEGATIVE_INFINITY;
		this.xAxisMin = Double.POSITIVE_INFINITY;
		this.xAxisMax = Double.NEGATIVE_INFINITY;
		this.yAxisMin = Double.POSITIVE_INFINITY;
		this.yAxisMax = Double.NEGATIVE_INFINITY;
		
		Map<String,List<double[]>> dataCollection = new LinkedHashMap<String, List<double[]>>();
		
		synchronized (dataTable) {
			Iterator<DataTableRow> i = this.dataTable.iterator();
			while (i.hasNext()) {
				DataTableRow row = i.next();
				
				double xValue = Double.NaN;
				if (axis[X_AXIS] >= 0) {
					xValue = row.getValue(axis[X_AXIS]);
				}
				
				double yValue = Double.NaN;
				if (axis[Y_AXIS] >= 0) {
					yValue = row.getValue(axis[Y_AXIS]);
				}
				
				double bubbleSizeValue = Double.NaN;
				if (axis[BUBBLE_SIZE_AXIS] >= 0) {
					bubbleSizeValue = row.getValue(axis[BUBBLE_SIZE_AXIS]);
				}
				
				double colorValue = Double.NaN;
				if (colorColumn >= 0) {
					colorValue = row.getValue(colorColumn);
				}
				
				if (!Double.isNaN(xValue) && !Double.isNaN(yValue) && !Double.isNaN(bubbleSizeValue)) {
					addPoint(dataCollection, xValue, yValue, bubbleSizeValue, colorValue);
				}
			}
		}
		
		Iterator<Map.Entry<String,List<double[]>>> i = dataCollection.entrySet().iterator();
		double scaleFactor = Math.min(this.xAxisMax - this.xAxisMin, this.yAxisMax - this.yAxisMin) / 4.0d;
		while (i.hasNext()) {
			Map.Entry<String,List<double[]>> entry = i.next();
			String seriesName = entry.getKey();
			List<double[]> dataList = entry.getValue();
			double[][] data = new double[3][dataList.size()];
			int listCounter = 0;
			Iterator<double[]> j = dataList.iterator();
			while (j.hasNext()) {
				double[] current = j.next();
				data[X_AXIS][listCounter] = current[X_AXIS];
				data[Y_AXIS][listCounter] = current[Y_AXIS];
				data[BUBBLE_SIZE_AXIS][listCounter] = ((current[BUBBLE_SIZE_AXIS] - bubbleSizeMin) / (bubbleSizeMax - bubbleSizeMin) + 0.1) * scaleFactor;
				listCounter++;
			}
			xyzDataSet.addSeries(seriesName, data);
		}
	}
	
	private void addPoint(Map<String,List<double[]>> dataCollection, double x, double y, double z, double color) {
		List<double[]> dataList = null;
		if (Double.isNaN(color)) {
			dataList = dataCollection.get("All");
			if (dataList == null) {
				dataList = new LinkedList<double[]>();
				dataCollection.put("All", dataList);
			}
		} else {
			String name = color + "";
			if (dataTable.isNominal(colorColumn)) {
				name = dataTable.mapIndex(colorColumn, (int)color);
			}
			dataList = dataCollection.get(name);
			if (dataList == null) {
				dataList = new LinkedList<double[]>();
				dataCollection.put(name, dataList);
			}
		}
		this.bubbleSizeMin = MathFunctions.robustMin(this.bubbleSizeMin, z);
		this.bubbleSizeMax = MathFunctions.robustMax(this.bubbleSizeMax, z);
		this.xAxisMin = MathFunctions.robustMin(this.xAxisMin, x);
		this.yAxisMin = MathFunctions.robustMin(this.yAxisMin, y);
		this.xAxisMax = MathFunctions.robustMax(this.xAxisMax, x);
		this.yAxisMax = MathFunctions.robustMax(this.yAxisMax, y);
		dataList.add(new double[] { x, y, z });
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintBubbleChart(g);
	}
	
	public void paintBubbleChart(Graphics graphics) {		
		prepareData();

        JFreeChart chart = ChartFactory.createBubbleChart(
                null,                     // chart title
                null,                     // domain axis label
                null,                     // range axis label
                xyzDataSet,               // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips
                false                     // URLs
            );
		
        // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);
        chart.getPlot().setForegroundAlpha(0.7f);
        
        // legend settings
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
        	legend.setPosition(RectangleEdge.TOP);
        	legend.setFrame(BlockBorder.NONE);
        	legend.setHorizontalAlignment(HorizontalAlignment.LEFT);
        }
        
		Rectangle2D drawRect = new Rectangle2D.Double(0, 0, getWidth(), getHeight());
		chart.draw((Graphics2D)graphics, drawRect);
	}
}
