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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;
import com.rapidminer.gui.plotter.PlotterAdapter;
import com.rapidminer.tools.Tools;


/**
 * This is the main bar chart plotter. The plotter is also capable
 * to produce average aggregations based on an additional group-by
 * attribute.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractBarChartPlotter.java,v 1.1 2007/05/27 22:02:53 ingomierswa Exp $
 *
 */
public abstract class AbstractBarChartPlotter extends PlotterAdapter {
    
	/** The currently used data table object. */
	private DataTable dataTable;
	
	/** The pie data set. */
	private DefaultCategoryDataset categoryDataSet = new DefaultCategoryDataset();

	/** The column which is used for the group by attribute. */
	private int groupByColumn = -1;

	/** The column which is used for the legend. */
	private int legendByColumn = -1;
	
	/** The column which is used for the values. */
	private int valueColumn = -1;
	
	
	public AbstractBarChartPlotter() {
		super();
		setBackground(Color.white);
		
	}

	public AbstractBarChartPlotter(DataTable dataTable) {
		this();
		setDataTable(dataTable);
	}
	
	public abstract JFreeChart createChart(CategoryDataset categoryDataSet, String groupByName, String valueName);
	
	public void setDataTable(DataTable dataTable) {
		super.setDataTable(dataTable);
		this.dataTable = dataTable;
		repaint();
	}
	
    public void setPlotColumn(int index, boolean plot) {
		if (plot)
			this.valueColumn = index;
		else
			this.valueColumn = -1;
		repaint();
	}
	
	public boolean getPlotColumn(int index) {
		return valueColumn == index;
	}
	
	public String getPlotName() { return "Value Column"; }

	public int getNumberOfAxes() {
		return 2;
	}

	public void setAxis(int index, int dimension) {
		if (index == 0)
			groupByColumn = dimension;
		else if (index == 1)
			legendByColumn = dimension;
		repaint();
	}

	public int getAxis(int index) {
		if (index == 0)
			return groupByColumn;
		else if (index == 1)
			return legendByColumn;
		else
			return -1;
	}
	
	public String getAxisName(int index) {
		if (index == 0)
			return "Group-By Column";
		else if (index == 1)
			return "Legend Column";
		else
			return "Unknown";
	}
	
	private void prepareData() {		
		synchronized (dataTable) {
			Iterator<DataTableRow> i = this.dataTable.iterator();
			Map<String,Double> categoryValues = new LinkedHashMap<String, Double>();
			Map<String,AtomicInteger> categoryCounters = new LinkedHashMap<String, AtomicInteger>();
			while (i.hasNext()) {
				DataTableRow row = i.next();
				
				double value = Double.NaN;
				if (valueColumn >= 0) {
					value = row.getValue(valueColumn);
				}
				
				if (!Double.isNaN(value)) {
					// name
					String valueString = 
						dataTable.isNominal(valueColumn) ? 
					    dataTable.mapIndex(valueColumn, (int)value) : 
					    Tools.formatIntegerIfPossible(value); 
					String legendName = valueString + "";
					if (legendByColumn >= 0) {
						double nameValue = row.getValue(legendByColumn);
						if (dataTable.isNominal(legendByColumn)) {
							legendName = dataTable.mapIndex(legendByColumn, (int)nameValue) + " (" + valueString + ")";
						} else {
							legendName = Tools.formatIntegerIfPossible(nameValue) + " (" + valueString + ")";
						}	
					}
					
					String groupByName = legendName;
					if (groupByColumn >= 0) {
						double nameValue = row.getValue(groupByColumn);
						if (dataTable.isNominal(groupByColumn)) {
							groupByName = dataTable.mapIndex(groupByColumn, (int)nameValue);
						} else {
							groupByName = Tools.formatIntegerIfPossible(nameValue) + "";
						}	
					}

					// update counters
					AtomicInteger categoryCounter = categoryCounters.get(groupByName);
					if (categoryCounter == null) {
						categoryCounters.put(groupByName, new AtomicInteger(1));
					} else {
						categoryCounter.incrementAndGet();
					}

					// increment values
					Double oldValue = categoryValues.get(groupByName);
					if (oldValue == null) {
						categoryValues.put(groupByName, value);
					} else {
						categoryValues.remove(groupByName);
						categoryValues.put(groupByName, oldValue + value);
					}
				}
			}
			
			// divide by counters and set values
			categoryDataSet.clear();
			if (valueColumn >= 0) {
				Iterator<Map.Entry<String, AtomicInteger>> c = categoryCounters.entrySet().iterator();
				while (c.hasNext()) {
					Map.Entry<String, AtomicInteger> entry = c.next();
					String name = entry.getKey();
					int counter = entry.getValue().intValue();
					double oldValue = categoryValues.get(name).doubleValue();
					if (groupByColumn < 0) {
						categoryDataSet.setValue(oldValue / counter, dataTable.getColumnName(valueColumn), name);
					} else {
						categoryDataSet.setValue(oldValue / counter, name, dataTable.getColumnName(valueColumn));
					}
				}
			}
		}
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintBarChart(g);
	}
	
	public void paintBarChart(Graphics graphics) {		
		prepareData();
        String groupByName = groupByColumn >= 0 ? dataTable.getColumnName(groupByColumn) : null;
        String valueName = valueColumn >= 0 ? dataTable.getColumnName(valueColumn) : null;
		JFreeChart chart = createChart(categoryDataSet, groupByName, valueName);
		
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
