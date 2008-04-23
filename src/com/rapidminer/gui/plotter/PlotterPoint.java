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

import java.awt.Color;

/** Helper class for the plotter point positions and colors. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: PlotterPoint.java,v 1.1 2007/05/27 21:59:04 ingomierswa Exp $
 */
public class PlotterPoint {
	
	private double x;
	private double y;
	private double color;
    private Color borderColor;
		
	public PlotterPoint(double x, double y, double color, Color borderColor) {
		this.x = x;
		this.y = y;
		this.color = color;
        this.borderColor = borderColor;
	}
	
	public double getX() { return x; }
	
	public double getY() { return y; }
	
	public double getColor() { return color; }
    
    public Color getBorderColor() { return borderColor; }
}
