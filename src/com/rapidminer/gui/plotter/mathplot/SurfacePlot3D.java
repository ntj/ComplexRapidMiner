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
package com.rapidminer.gui.plotter.mathplot;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.math.plot.Plot3DPanel;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;
import com.rapidminer.gui.plotter.conditions.PlotterCondition;
import com.rapidminer.gui.plotter.conditions.RowsPlotterCondition;


/** This plotter can be used to create 3D surface plots of equidistant data. 
 * 
 *  @author Sebastian Land, Ingo Mierswa
 *  @version $Id: SurfacePlot3D.java,v 1.4 2008/05/09 19:23:21 ingomierswa Exp $
 */
public class SurfacePlot3D extends JMathPlotter3D {

	private static final long serialVersionUID = -8086776011628491876L;

	private static final int MAX_NUMBER_OF_ROWS = 50;
	
	public SurfacePlot3D(){
		super();
	}
	
	public SurfacePlot3D(DataTable dataTable){
		super(dataTable);
	}
	
	public void update() {
		if ((getAxis(0) != -1) && (getAxis(1) != -1)) {
			getPlotPanel().removeAllPlots();
            int totalNumberOfColumns = countColumns();
			for (int currentVariable = 0; currentVariable < totalNumberOfColumns; currentVariable++) {
				if (getPlotColumn(currentVariable)) {
					Set<Double> xSet = new TreeSet<Double>();
					Set<Double> ySet = new TreeSet<Double>();
					Map<String, Double> zMap = new HashMap<String, Double>();
					DataTable table = getDataTable();
					synchronized (table) {
						Iterator iterator = table.iterator();
						while (iterator.hasNext()) {
							DataTableRow row = (DataTableRow) iterator.next();
							double x = row.getValue(getAxis(0));
							double y = row.getValue(getAxis(1));
							xSet.add(x);
							ySet.add(y);
							zMap.put(x + "+" + y, row.getValue(currentVariable));
						}
						
						// must the number of values in each dimension be the same?
						int size = Math.max(xSet.size(), ySet.size());
						double[] xArray = new double[size];
						double[] yArray = new double[size];
						double[][] zArray = new double[size][size];
						int xCounter = 0;
						Iterator<Double> x = xSet.iterator();
						double last = 0.0d;
						while (x.hasNext()) {
							xArray[xCounter] = x.next();
							Iterator<Double> y = ySet.iterator();
							int yCounter = 0;
							while (y.hasNext()) {
								yArray[yCounter] = y.next();
								Double value = zMap.get(xArray[xCounter] + "+" + yArray[yCounter]);
								if (value != null) {
									zArray[xCounter][yCounter] = value;
									last = value;
								} else {
									zArray[xCounter][yCounter] = last;
								}
								yCounter++;
							}
							xCounter++;
						}
						// PlotPanel construction
						if ((xArray.length > 0) && (yArray.length > 0) && (zArray.length > 0)) {
	                        Color color = getPointColor((double)(currentVariable + 1) / (double)totalNumberOfColumns);
							((Plot3DPanel) getPlotPanel()).addGridPlot(getDataTable().getColumnName(currentVariable), color, yArray, xArray, zArray);
						}
					}
				}
			}
		}
	}
	
    public PlotterCondition getPlotterCondition() {
        return new RowsPlotterCondition(MAX_NUMBER_OF_ROWS);
    }
    
	public int getValuePlotSelectionType() {
		return MULTIPLE_SELECTION;
	}
	
	public String getPlotName(){
		return "z-Axis";
	}
}

