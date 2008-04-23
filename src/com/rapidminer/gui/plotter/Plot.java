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

import java.util.LinkedList;

import com.rapidminer.gui.plotter.PlotterAdapter.LineStyle;


/**
 * This collection consists of all {@link com.rapidminer.gui.plotter.ColorPlotterPoint}s for a plot.
 * 
 * @author Ingo Mierswa
 * @version $Id: Plot.java,v 1.1 2007/05/27 21:59:04 ingomierswa Exp $
 */
class Plot extends LinkedList<ColorPlotterPoint> {
	
	private static final long serialVersionUID = 3408030697850939063L;
	
    private String name;
    
	private int styleIndex;

	public Plot(String name, int styleIndex) {
        this.name = name;
		this.styleIndex = styleIndex;
	}

	public LineStyle getLineStyle() {
		return PlotterAdapter.LINE_STYLES[styleIndex % PlotterAdapter.LINE_STYLES.length];
	}
    
    public String getName() {
        return name;
    }
}
