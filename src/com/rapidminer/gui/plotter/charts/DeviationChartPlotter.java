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
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

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
 * @version $Id: DeviationChartPlotter.java,v 1.7 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class DeviationChartPlotter extends PlotterAdapter {

    private static final long serialVersionUID = -8763693366081949249L;

    /** The currently used data table object. */
    private transient DataTable dataTable;
    
    /** The data set used for the plotter. */
    private YIntervalSeriesCollection dataset = null;
    
    /** The column which is used for the values. */
    private int colorColumn = -1;
    
    
    public DeviationChartPlotter() {
        super();
        setBackground(Color.white);
    }

    public DeviationChartPlotter(DataTable dataTable) {
        this();
        setDataTable(dataTable);
    }
   
    private static JFreeChart createChart(XYDataset dataset, boolean createLegend) {
       
        // create the chart...
        JFreeChart chart = ChartFactory.createXYLineChart(
            null,      // chart title
            null,                      // x axis label
            null,                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            createLegend,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        chart.setBackgroundPaint(Color.white);
       
        // get a reference to the plot for further customization...
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
       
        DeviationRenderer renderer = new DeviationRenderer(true, false);
        Stroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        if (dataset.getSeriesCount() == 1) {
            renderer.setSeriesStroke(0, stroke);
            renderer.setSeriesPaint(0, Color.RED);
            renderer.setSeriesFillPaint(0, Color.RED);            
        } else {
            for (int i = 0; i < dataset.getSeriesCount(); i++) {
                renderer.setSeriesStroke(i, stroke);
                Color color = getPointColor((double)i / (double)(dataset.getSeriesCount() - 1));
                renderer.setSeriesPaint(i, color);
                renderer.setSeriesFillPaint(i, color);
            }
        }
        renderer.setAlpha(0.12f);
        
        plot.setRenderer(renderer);

        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());               
        return chart;
       
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
        return 0;
    }
    
    private int prepareData() {        
        synchronized (dataTable) {
            this.dataset = new YIntervalSeriesCollection();
            if ((colorColumn >= 0) && (dataTable.isNominal(colorColumn))) {
                for (int v = 0; v < dataTable.getNumberOfValues(colorColumn); v++) {
                    String valueName = dataTable.mapIndex(colorColumn, v);
                    YIntervalSeries series = new YIntervalSeries(valueName);
                    for (int column = 0; column < dataTable.getNumberOfColumns(); column++) {
                        if ((!dataTable.isSpecial(column)) && (column != colorColumn)) {
                            Iterator<DataTableRow> i = this.dataTable.iterator();
                            double sum = 0.0d;
                            double squaredSum = 0.0d;
                            int counter = 0;
                            while (i.hasNext()) {
                                DataTableRow row = i.next();
                                if (row.getValue(colorColumn) != v)
                                    continue;
                                double value = row.getValue(column);
                                sum += value;
                                squaredSum += value * value;
                                counter++;
                            }
                            
                            double mean = sum / counter;
                            double deviation = Math.sqrt(squaredSum / counter - (mean * mean));
                            series.add((column + 1), mean, mean - deviation, mean + deviation);
                        }
                    }
                    dataset.addSeries(series);
                }
                return dataTable.getNumberOfValues(colorColumn);
            } else {
                YIntervalSeries series = new YIntervalSeries(dataTable.getName());
                for (int column = 0; column < dataTable.getNumberOfColumns(); column++) {
                    if ((!dataTable.isSpecial(column)) && (column != colorColumn)) {
                        Iterator<DataTableRow> i = this.dataTable.iterator();
                        double sum = 0.0d;
                        double squaredSum = 0.0d;
                        int counter = 0;
                        while (i.hasNext()) {
                            DataTableRow row = i.next();
                            double value = row.getValue(column);
                            sum += value;
                            squaredSum += value * value;
                            counter++;
                        }
                        
                        double mean = sum / counter;
                        double deviation = Math.sqrt(squaredSum / counter - (mean * mean));
                        series.add((column + 1), mean, mean - deviation, mean + deviation);
                    }
                }
                dataset.addSeries(series);
                return 0; 
            }
        }
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintDeviationChart(g);
    }
    
    public void paintDeviationChart(Graphics graphics) {      
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
