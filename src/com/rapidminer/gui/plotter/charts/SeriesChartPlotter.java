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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.plotter.PlotterAdapter;
import com.rapidminer.tools.LogService;


/**
 * This is the deviation chart plotter.
 * 
 * @author Ingo Mierswa
 * @version $Id: SeriesChartPlotter.java,v 1.4 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class SeriesChartPlotter extends PlotterAdapter {

    private static final long serialVersionUID = -8763693366081949249L;

    /** The currently used data table object. */
    private transient DataTable dataTable;
    
    /** The data set used for the plotter. */
    private YIntervalSeriesCollection dataset = null;
    
    /** The column which is used for the values. */
    private boolean[] columns;
    
    /** The axis values for the upper and lower bounds. */
    private int[] axis = new int[] { -1, -1 };
    
    /** Indicates if bounds are plotted. */
    private boolean plotBounds = false;
    
    private List<Integer> plotIndexToColumnIndexMap = new ArrayList<Integer>();
    
    
    public SeriesChartPlotter() {
        super();
        setBackground(Color.white);
    }

    public SeriesChartPlotter(DataTable dataTable) {
        this();
        setDataTable(dataTable);
    }
   
    private JFreeChart createChart(XYDataset dataset, boolean createLegend) {
       
        // create the chart...
        JFreeChart chart = ChartFactory.createXYLineChart(
            null,                      // chart title
            null,                      // x axis label
            null,                      // y axis label
            dataset,                   // data
            PlotOrientation.VERTICAL,
            createLegend,              // include legend
            true,                      // tooltips
            false                      // urls
        );

        chart.setBackgroundPaint(Color.white);
       
        // get a reference to the plot for further customization...
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
       
        DeviationRenderer renderer = new DeviationRenderer(true, false);

        if (plotBounds) {
            for (int i = 0; i < dataset.getSeriesCount() - 1; i++) {
        		LineStyle style = PlotterAdapter.LINE_STYLES[this.plotIndexToColumnIndexMap.get(i) % PlotterAdapter.LINE_STYLES.length];
        		renderer.setSeriesStroke(i, style.getStroke());
        		renderer.setSeriesPaint(i, style.getColor());
        		renderer.setSeriesFillPaint(i, style.getColor());
            }
            
			float[] dashArray = new float[] { 7, 14 };
            renderer.setSeriesStroke(dataset.getSeriesCount() - 1, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dashArray, 0));
            renderer.setSeriesPaint(dataset.getSeriesCount() - 1, Color.GRAY.brighter());
            renderer.setSeriesFillPaint(dataset.getSeriesCount() - 1, Color.GRAY);
        } else {
        	for (int i = 0; i < dataset.getSeriesCount(); i++) {
        		LineStyle style = PlotterAdapter.LINE_STYLES[this.plotIndexToColumnIndexMap.get(i) % PlotterAdapter.LINE_STYLES.length];
        		renderer.setSeriesStroke(i, style.getStroke());
        		renderer.setSeriesPaint(i, style.getColor());
        		renderer.setSeriesFillPaint(i, style.getColor());
        	}
        }
        renderer.setAlpha(0.25f);
        
        plot.setRenderer(renderer);

        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        xAxis.setAutoRange(true);
        xAxis.setAutoRangeStickyZero(false);
        xAxis.setAutoRangeIncludesZero(false);
        
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        yAxis.setAutoRange(true);
        yAxis.setAutoRangeStickyZero(false);
        yAxis.setAutoRangeIncludesZero(false);
        
        return chart;
    }    

    public void setDataTable(DataTable dataTable) {
        super.setDataTable(dataTable);
        this.dataTable = dataTable;
		columns = new boolean[dataTable.getNumberOfColumns()];
        repaint();
    }
    
	public int getValuePlotSelectionType() {
		return MULTIPLE_SELECTION;
	}
	
    public void setPlotColumn(int index, boolean plot) {
    	if ((index >= 0) && (index < columns.length))
    		this.columns[index] = plot;
        repaint();
    }
    
    public boolean getPlotColumn(int index) {
    	return this.columns[index];
    }
    
    public String getPlotName() { return "Plot Series"; }

    public int getNumberOfAxes() {
        return 2;
    }
    
	public String getAxisName(int index) {
		switch (index) {
			case 0:
				return "Lower Bound";
			case 1:
				return "Upper Bound";
			default:
				return "none";
		}
	}
	
	public int getAxis(int index) {
		return axis[index];
	}
	
	public void setAxis(int index, int dimension) {
		if (axis[index] != dimension) {
			axis[index] = dimension;
			repaint();
		}
	}
	
    private int prepareData() {        
        synchronized (dataTable) {
            this.dataset = new YIntervalSeriesCollection();
            this.plotBounds = false;
            this.plotIndexToColumnIndexMap.clear();
            
            int columnCount = 0;
			for (int c = 0; c < dataTable.getNumberOfColumns(); c++) {
				if (getPlotColumn(c)) {
					if (!dataTable.isNominal(c)) {
						YIntervalSeries series = new YIntervalSeries(this.dataTable.getColumnName(c));
						Iterator<DataTableRow> i = dataTable.iterator();
						int index = 1;
						while (i.hasNext()) {
							DataTableRow row = i.next();
							double value = row.getValue(c);
							series.add(index++, value, value, value);
						}
						dataset.addSeries(series);
						plotIndexToColumnIndexMap.add(c);
						columnCount++;
					}
				}
			}
			
			if ((getAxis(0) > -1) && (getAxis(1) > -1)) {
				if ((!dataTable.isNominal(getAxis(0))) && (!dataTable.isNominal(getAxis(1)))) {
					YIntervalSeries series = new YIntervalSeries("Bounds");
					Iterator<DataTableRow> i = dataTable.iterator();
					int index = 1;
					while (i.hasNext()) {
						DataTableRow row = i.next();
						double lowerValue = row.getValue(getAxis(0));
						double upperValue = row.getValue(getAxis(1));
						if (lowerValue > upperValue) {
							double dummy = lowerValue;
							lowerValue = upperValue;
							upperValue = dummy;
						}
						double mean = (upperValue - lowerValue) / 2.0d + lowerValue;
						series.add(index++, mean, lowerValue, upperValue);
					}
					dataset.addSeries(series);
					columnCount++;
					this.plotBounds = true;
				}
			}
    		return columnCount;
        }
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintSeriesChart(g);
    }
    
    public void paintSeriesChart(Graphics graphics) {      
        int categoryCount = prepareData();
		String maxClassesProperty = System.getProperty(MainFrame.PROPERTY_RAPIDMINER_GUI_PLOTTER_COLORS_CLASSLIMIT);
		int maxClasses = 20;
		try {
			if (maxClassesProperty != null)
				maxClasses = Integer.parseInt(maxClassesProperty);
		} catch (NumberFormatException e) {
            LogService.getGlobal().log("Deviation plotter: cannot parse property 'rapidminer.gui.plotter.colors.classlimit', using maximal 20 different classes.", LogService.WARNING);
		}
        boolean createLegend = categoryCount > 0 && categoryCount < maxClasses;
        JFreeChart chart = createChart(this.dataset, createLegend);
        
        // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);
        
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
