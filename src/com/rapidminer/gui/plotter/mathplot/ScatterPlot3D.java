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
import java.util.Iterator;

import org.math.plot.Plot3DPanel;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;


/** This plotter can be used to create 3D scatter plots. 
 * 
 *  @author Sebastian Land, Ingo Mierswa
 *  @version $Id: ScatterPlot3D.java,v 1.4 2008/05/09 19:23:21 ingomierswa Exp $
 */
public class ScatterPlot3D extends JMathPlotter3D {
	
	private static final long serialVersionUID = -3741835931346090326L;

	public ScatterPlot3D(){
		super();
	}
	
	public ScatterPlot3D(DataTable dataTable){
		super(dataTable);
	}
	
	public void update() {
		if (getAxis(0)!= -1&& getAxis(1)!= -1) {
			getPlotPanel().removeAllPlots();
            int totalNumberOfColumns = countColumns();
			for (int currentVariable = 0; currentVariable < totalNumberOfColumns; currentVariable ++){
				if (getPlotColumn(currentVariable)) {
					DataTable table = getDataTable();
					synchronized (table) {
						Iterator<DataTableRow> iterator = table.iterator();
						int i = 0;
						double[][] data = new double[getDataTable().getNumberOfRows()][3];
						while (iterator.hasNext()) {
							DataTableRow row = iterator.next();
							data[i][0] = row.getValue(getAxis(0));
							data[i][1] = row.getValue(getAxis(1));
							data[i][2] = row.getValue(currentVariable);		
							i++;
						}
						// PlotPanel construction
                        Color color = getPointColor((double)(currentVariable + 1) / (double)totalNumberOfColumns);
						((Plot3DPanel)getPlotPanel()).addScatterPlot(getDataTable().getColumnName(currentVariable), color, data);
					}
				}
			}
		}
	}
	
	public int getValuePlotSelectionType() {
		return MULTIPLE_SELECTION;
	}
	
	public String getPlotName(){
		return "z-Axis";
	}
}
