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
import java.awt.Graphics2D;

/** Defines the style of plotted rectangles, i.e. the color.
 *  
 *  @author Ingo Mierswa
 *  @version $Id: RectangleStyle.java,v 1.3 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class RectangleStyle {

	public static final int ALPHA = 30;
	
    public static final int NUMBER_OF_STYLES = 10;
    
	private int styleNumber = 0;

    private Color color = null;
    
	public RectangleStyle(int styleNumber) {
	    this.styleNumber = styleNumber % NUMBER_OF_STYLES;
	}

    public RectangleStyle(Color color) {
        this.color = color;
    }
    
	public void set(Graphics2D g) {
		g.setColor(getColor());
	}
	
	public Color getColor() { 
        if (color != null) {
            return color;
        } else {
            return PlotterAdapter.getPointColor((double)styleNumber / (double)NUMBER_OF_STYLES, ALPHA);
        }
	}
}
