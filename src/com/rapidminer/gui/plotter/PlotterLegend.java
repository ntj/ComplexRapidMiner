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

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

import com.rapidminer.datatable.DataTable;


/** This plotter legend component can be used by external plotter components. 
 * 
 *  @author Sebastian Land, Ingo Mierswa
 *  @version $Id: PlotterLegend.java,v 1.3 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class PlotterLegend extends JComponent {
	
	private static final long serialVersionUID = -4737111168245916491L;

	private PlotterAdapter adapter;
	
	private transient DataTable dataTable;
	
	private int legendColumn = -1;
	
	public PlotterLegend(PlotterAdapter adapter){
		super();
		this.adapter = adapter;
	}

	public Dimension getPreferredSize() {
		return new Dimension(adapter.getWidth() - 2 * PlotterAdapter.MARGIN, PlotterAdapter.MARGIN);
	}
	
	public void setLegendColumn(DataTable dataTable, int legendColumn) {
		this.dataTable = dataTable;
		this.legendColumn = legendColumn;
		repaint();
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		adapter.drawLegend(g, this.dataTable, legendColumn);
	}
}
