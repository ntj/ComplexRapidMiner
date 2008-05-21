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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
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

import com.rapidminer.gui.plotter.PlotterAdapter;
import com.rapidminer.tools.Renderable;
import com.rapidminer.tools.math.ROCData;


/**
 * This is the ROC chart plotter.
 * 
 * @author Ingo Mierswa
 * @version $Id: ROCChartPlotter.java,v 1.5 2008/05/09 19:22:59 ingomierswa Exp $
 */
public class ROCChartPlotter extends JPanel implements Renderable {

    private static final long serialVersionUID = -5819082000307077237L;
    
    private static final int NUMBER_OF_POINTS = 500;
    
    /** The data set used for the plotter. */
    private YIntervalSeriesCollection dataset = null;
        
    private Map<String,List<ROCData>> rocDataLists = new HashMap<String, List<ROCData>>();
    
    public ROCChartPlotter() {
        super();
        setBackground(Color.white);
    }
   
    public void addROCData(String name, ROCData singleROCData) {
        List<ROCData> tempList = new LinkedList<ROCData>();
        tempList.add(singleROCData);
        addROCData(name, tempList);
    }
    
    public void addROCData(String name, List<ROCData> averageROCData) {
        rocDataLists.put(name, averageROCData);
    }
    
    private static JFreeChart createChart(XYDataset dataset) {
        // create the chart...
        JFreeChart chart = ChartFactory.createXYLineChart(
            null,      // chart title
            null,                      // x axis label
            null,                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        chart.setBackgroundPaint(Color.white);
       
        // get a reference to the plot for further customisation...
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
                Color color = PlotterAdapter.getPointColor((double)i / (double)(dataset.getSeriesCount() - 1));
                renderer.setSeriesPaint(i, color);
                renderer.setSeriesFillPaint(i, color);
            }
        }
        renderer.setAlpha(0.12f);        
        plot.setRenderer(renderer);
           
        return chart;
    }    

    private void prepareData() {        
        this.dataset = new YIntervalSeriesCollection();

        Iterator<Map.Entry<String, List<ROCData>>> r = rocDataLists.entrySet().iterator();
        while (r.hasNext()) {
            Map.Entry<String, List<ROCData>> entry = r.next();
            YIntervalSeries series = new YIntervalSeries(entry.getKey());
            List<ROCData> dataList = entry.getValue();
            for (int i = 0; i <= NUMBER_OF_POINTS; i++) {
                double sum = 0.0d;
                double squaredSum = 0.0d;
                for (ROCData data : dataList) {
                    double value = data.getInterpolatedTruePositives(i / (double)NUMBER_OF_POINTS) / data.getTotalPositives();
                    sum += value;
                    squaredSum += value * value;               
                }
                double mean = sum / dataList.size();
                double deviation = Math.sqrt(squaredSum / dataList.size() - (mean * mean));
                series.add(i / (double)NUMBER_OF_POINTS, mean, mean - deviation, mean + deviation);            
            }
            dataset.addSeries(series);
        }
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintDeviationChart(g);
    }
    
    public void paintDeviationChart(Graphics graphics) {      
        prepareData();
        
        JFreeChart chart = createChart(this.dataset);
        
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

	public int getRenderHeight(int preferredHeight) {
		return getHeight();
	}

	public int getRenderWidth(int preferredWidth) {
		return getWidth();
	}

	public void render(Graphics graphics, int width, int height) {
		paintComponent(graphics);
	}
}
