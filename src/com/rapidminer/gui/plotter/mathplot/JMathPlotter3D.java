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

import org.math.plot.Plot3DPanel;
import org.math.plot.PlotPanel;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.gui.plotter.conditions.MissingValuesPlotterCondition;
import com.rapidminer.gui.plotter.conditions.PlotterCondition;


/** The abstract super class for all 3D plotters using the JMathPlot library. 
 *  
 *  @author Ingo Mierswa
 *  @version $Id: JMathPlotter3D.java,v 1.5 2008/05/09 19:23:21 ingomierswa Exp $
 */
public abstract class JMathPlotter3D extends JMathPlotter {

	public JMathPlotter3D() {
		super();
	}
	
	public JMathPlotter3D(DataTable dataTable) {
		super(dataTable);
	}
	
	public PlotPanel createPlotPanel() { return new Plot3DPanel(); }
	
    public PlotterCondition getPlotterCondition() {
        return new MissingValuesPlotterCondition();
    }
    
	public int getNumberOfOptionIcons() {
		return 5;
	}
}
