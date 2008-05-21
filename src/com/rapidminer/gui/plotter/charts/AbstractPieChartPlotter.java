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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.plotter.PlotterAdapter;
import com.rapidminer.operator.olap.aggregation.AggregationFunction;
import com.rapidminer.operator.olap.aggregation.AggregationOperator;
import com.rapidminer.operator.olap.aggregation.AverageFunction;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;


/**
 * This is the main pie chart plotter.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractPieChartPlotter.java,v 1.6 2008/05/09 19:22:58 ingomierswa Exp $
 *
 */
public abstract class AbstractPieChartPlotter extends PlotterAdapter {
    
	/** The maximal number of printable categories. */
	private static final int MAX_CATEGORIES = 50;
	
	/** The currently used data table object. */
	private DataTable dataTable;
	
	/** The pie data set. */
	private DefaultPieDataset pieDataSet = new DefaultPieDataset();
	
	/** The column which is used for the piece names (or group-by statements). */
	private int groupByColumn = -1;
	
	/** The column which is used for the legend. */
	private int legendByColumn = -1;

	/** The column which is used for the values. */
	private int valueColumn = -1;
	
	/** Indicates if only distinct values should be used for aggregation functions. */
	private JCheckBox useDistinct = new JCheckBox("Use Only Distinct", false);
	
	/** The used aggregation function. */
	private JComboBox aggregationFunction = null;
	
	public AbstractPieChartPlotter() {
		super();
		setBackground(Color.white);
		
		useDistinct.setToolTipText("Indicates if only distinct values should be used for aggregation functions.");
		useDistinct.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		
		String[] allFunctions = new String[AggregationOperator.KNOWN_AGGREGATION_FUNCTION_NAMES.length + 1];
		allFunctions[0] = "none";
		System.arraycopy(AggregationOperator.KNOWN_AGGREGATION_FUNCTION_NAMES, 0, allFunctions, 1, AggregationOperator.KNOWN_AGGREGATION_FUNCTION_NAMES.length);
		aggregationFunction = new JComboBox(allFunctions);
		aggregationFunction.setToolTipText("Select the type of the aggregation function which should be used for grouped values.");
		aggregationFunction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaint();
			}		
		});
	}

	public AbstractPieChartPlotter(DataTable dataTable) {
		this();
		setDataTable(dataTable);
	}
	
	public abstract JFreeChart createChart(PieDataset pieDataSet, boolean createLegend);
	
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
	
	private int prepareData() {
		synchronized (dataTable) {
			AggregationFunction aggregation = null;
			if (aggregationFunction.getSelectedIndex() > 0) {
				try {
					aggregation = AggregationOperator.createAggregationFunction(AggregationOperator.KNOWN_AGGREGATION_FUNCTION_NAMES[aggregationFunction.getSelectedIndex() - 1]);
				} catch (Exception e) {
					LogService.getGlobal().logWarning("Cannot instantiate aggregation function '" + aggregationFunction.getSelectedItem() + "', using 'average' as default.");
					aggregation = new AverageFunction();
				}
			}
			Iterator<DataTableRow> i = this.dataTable.iterator();
			Map<String,Collection<Double>> categoryValues = new LinkedHashMap<String, Collection<Double>>();
			
			pieDataSet.clear();
			
			if ((groupByColumn >= 0) && (!dataTable.isNominal(groupByColumn)))
				return 0;
			
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
							legendName = nameValue + " (" + valueString + ")";
						}	
					}
					
					String groupByName = legendName;
					if (groupByColumn >= 0) {
						double nameValue = row.getValue(groupByColumn);
						if (dataTable.isNominal(groupByColumn)) {
							groupByName = dataTable.mapIndex(groupByColumn, (int)nameValue);
						} else {
							groupByName = nameValue + "";
						}	
					}

					// increment values
					Collection<Double> values = categoryValues.get(groupByName);
					if (values == null) {
						if (useDistinct.isSelected()) {
							values = new TreeSet<Double>();
						} else {
							values = new LinkedList<Double>();
						}
						categoryValues.put(groupByName, values);
					}
					values.add(value);
				}
			}
			
			
			// calculate aggregation and set values
			if (valueColumn >= 0) {
				if (aggregation != null) {
					Iterator<Map.Entry<String, Collection<Double>>> c = categoryValues.entrySet().iterator();
					while (c.hasNext()) {
						Map.Entry<String, Collection<Double>> entry = c.next();
						String name = entry.getKey();
						Collection<Double> values = entry.getValue();
						double[] valueArray = new double[values.size()];
						Iterator<Double> v = values.iterator();
						int valueIndex = 0;
						while (v.hasNext()) {
							valueArray[valueIndex++] = v.next();
						}
						double value = aggregation.calculate(valueArray);
						if (legendByColumn >= 0) {
							pieDataSet.setValue(name, value);
						} else {
							pieDataSet.setValue(name + " (" + Tools.formatIntegerIfPossible(value) + ")", value);
						}
					}
				} else {
					Iterator<Map.Entry<String, Collection<Double>>> c = categoryValues.entrySet().iterator();
					while (c.hasNext()) {
						Map.Entry<String, Collection<Double>> entry = c.next();
						String name = entry.getKey();
						Collection<Double> values = entry.getValue();
						Iterator<Double> v = values.iterator();
						while (v.hasNext()) {
							double value = v.next();
							if (legendByColumn >= 0) {
								pieDataSet.setValue(name, value);
							} else {
								pieDataSet.setValue(name + " (" + Tools.formatIntegerIfPossible(value) + ")", value);
							}
						}
					}
				}
			}
			
			return categoryValues.size();
		}
	}
    
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintPieChart(g);
	}
	
	public void paintPieChart(Graphics graphics) {		
		int categoryCount = prepareData();
		String maxClassesProperty = System.getProperty(MainFrame.PROPERTY_RAPIDMINER_GUI_PLOTTER_COLORS_CLASSLIMIT);
		int maxClasses = 20;
		try {
			if (maxClassesProperty != null)
				maxClasses = Integer.parseInt(maxClassesProperty);
		} catch (NumberFormatException e) {
            LogService.getGlobal().log("Pie Chart plotter: cannot parse property 'rapidminer.gui.plotter.colors.classlimit', using maximal 20 different classes.", LogService.WARNING);
		}
        boolean createLegend = categoryCount > 0 && categoryCount < maxClasses;
        
        if (categoryCount <= MAX_CATEGORIES) {
        	JFreeChart chart = createChart(pieDataSet, createLegend);

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
        } else {
        	graphics.drawString("Too many columns (" + categoryCount + "), this chart is only able to plot up to " + MAX_CATEGORIES + " different categories", MARGIN, MARGIN);
        }
	}
	
	public JComponent getOptionsComponent(int index) {
		switch (index) {
			case 0:
				JLabel label = new JLabel("Aggregation");
                label.setToolTipText("Select the type of the aggregation function which should be used for grouped values.");
                return label;
			case 1:
				return aggregationFunction;
			case 2:
                return useDistinct;
		}
		return null;
	}
}
