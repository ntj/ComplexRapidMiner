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
package com.rapidminer.gui.plotter;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.math.MathFunctions;


/** This plotter can be used to create 2D histogram plots for a single column colorized by
 *  another column.
 * 
 *  @author Ingo Mierswa
 *  @version $Id: ColorHistogramPlotter.java,v 1.6 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class ColorHistogramPlotter extends HistogramPlotter {
	
	private static final long serialVersionUID = -2185573642487757891L;

	private int columnIndex = -1;
	
	private int colorIndex = -1;
	
	public ColorHistogramPlotter() {
		super();
	}

	public ColorHistogramPlotter(DataTable dataTable) {
		super(dataTable);
	}
	
	public int getNumberOfAxes() {
		return 1;
	}
	
	public int getAxis(int axis) { 
		return columnIndex; 
	}
	
	public String getAxisName(int index) {
		if (index == 0)
			return "Histogram";
		else
			return "empty";
	}
	
    public void setAxis(int index, int dimension) {
        if (this.columnIndex != dimension) {
            this.columnIndex = dimension;
            this.currentXPlotterColumn = this.columnIndex;
            repaint();
        }
    }
    
	public void setPlotColumn(int index, boolean plot) {
		colorIndex = index;
		repaint();
	}

    public String getPlotName() {
        return "Color";
    }
    
	public boolean getPlotColumn(int index) {
		return index == colorIndex;
	}
	
    /** Overrides the method of the super type HistogramPlotter which allows for multiple plot selections. */
    public int getValuePlotSelectionType() {
        return SINGLE_SELECTION;
    }
    
	public void prepareData() {		
		minX = Double.POSITIVE_INFINITY;
		maxX = Double.NEGATIVE_INFINITY;
		minY = Double.POSITIVE_INFINITY;
		maxY = Double.NEGATIVE_INFINITY;

		allPlots.clear();

		if ((columnIndex != -1) && (colorIndex != -1)) {
			// create value map
			Map<Double,List<Double>> valueMap = new TreeMap<Double,List<Double>>();
			synchronized (dataTable) {
				Iterator<DataTableRow> i = dataTable.iterator();
				while (i.hasNext()) {
					DataTableRow row = i.next();
					double columnValue = row.getValue(columnIndex);
					this.minX = MathFunctions.robustMin(minX, columnValue);
					this.maxX = MathFunctions.robustMax(maxX, columnValue);
					double colorValue  = row.getValue(colorIndex);
					List<Double> values = valueMap.get(colorValue);
					if (values == null) {
						values = new LinkedList<Double>();
						values.add(columnValue);
						valueMap.put(colorValue, values);
					} else {
						values.add(columnValue);
					}
				}
			
				String maxClassesProperty = System.getProperty(MainFrame.PROPERTY_RAPIDMINER_GUI_PLOTTER_COLORS_CLASSLIMIT);
				int maxClasses = 10;
				try {
					if (maxClassesProperty != null)
						maxClasses = Integer.parseInt(maxClassesProperty);
				} catch (NumberFormatException e) {
                    LogService.getGlobal().log("Color histogram: cannot parse property 'rapidminer.gui.plotter.colors.classlimit', using maximal 10 different classes.", LogService.WARNING);
				}
				if (valueMap.size() <= maxClasses) {
					// collect actual data and create a histogram for each different color value
					Iterator<Map.Entry<Double,List<Double>>>
							it = valueMap.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry<Double,List<Double>> e = it.next();
						Double key = e.getKey();
						int colorValue = (int)key.doubleValue();
						Color color = getPointColor(colorValue / (valueMap.size() - 1.0d));
						color = new Color(color.getRed(), color.getGreen(), color.getBlue(), RectangleStyle.ALPHA);
						RectangleStyle style = new RectangleStyle(color);
						Bins bins = new Bins(style, minX, maxX, this.binNumber);
						allPlots.put(colorValue, bins);
						List<Double> values = e.getValue();
						Iterator<Double> v = values.iterator();
						while (v.hasNext()) {
							bins.addPoint(v.next());
							this.maxY = Math.max(bins.getMaxCounter(), this.maxY);
						}
					}
				} else {
					// too many classes --> super method in order to create usual non-colored histogram
					super.prepareData();
                    LogService.getGlobal().log("Color histogram: cannot create colored histogram since the number of different values (" + 
							valueMap.size() + ") is too large. Allowed are " + maxClassesProperty +
							" different values (edit this limit in the properties dialog).", LogService.WARNING);
				}
			} 
		} else {
			// no plots selected --> do nothing
			this.maxY = 1;
		}
		this.minY = 0;

		if (dataTable.getNumberOfRows() == 0) {
			minX = 0;
			maxX = 1;
			minY = 0;
			maxY = 1;
		}

		if (minX == maxX) {
			minX -= 0.5;
			maxX += 0.5;
		}
		if (minY == maxY) {
			minY -= 0.5;
			maxY += 0.5;
		}
		
		xTicSize = getTicSize(dataTable, columnIndex, minX, maxX);
		yTicSize = getNumericalTicSize(minY, maxY);
		minX = Math.floor(minX / xTicSize) * xTicSize;
		maxX = Math.ceil(maxX / xTicSize) * xTicSize;
		minY = Math.floor(minY / yTicSize) * yTicSize;
		maxY = Math.ceil(maxY / yTicSize) * yTicSize;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (drawLegend && (allPlots.size() > 0))
			drawLegend(g, dataTable, colorIndex, 50, RectangleStyle.ALPHA);
	}
}
