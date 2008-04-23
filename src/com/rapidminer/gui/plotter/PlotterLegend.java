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
package com.rapidminer.gui.plotter;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

import com.rapidminer.datatable.DataTable;


/** This plotter legend component can be used by external plotter components. 
 * 
 *  @author Sebastian Land, Ingo Mierswa
 *  @version $Id: PlotterLegend.java,v 1.1 2007/05/27 21:59:04 ingomierswa Exp $
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
