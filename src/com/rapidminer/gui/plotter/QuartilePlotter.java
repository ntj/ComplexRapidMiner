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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.tools.math.MathFunctions;


/** This plotter can be used to create quartile plots for several columns of the data table. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: QuartilePlotter.java,v 1.4 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class QuartilePlotter extends ColorQuartilePlotter {

    private static final long serialVersionUID = -5115095967846809152L;
    
	private static Icon[] RECTANGLE_STYLE_ICONS;
	
	static {
		RECTANGLE_STYLE_ICONS = new RectangleStyleIcon[RectangleStyle.NUMBER_OF_STYLES];
		for (int i = 0; i < RECTANGLE_STYLE_ICONS.length; i++) {
			RECTANGLE_STYLE_ICONS[i] = new RectangleStyleIcon(i);
		}
	}

	/**
	 * Defines the icon which is plotted before the attribute in the selection
	 * list (legend or key).
	 */
	private static class RectangleStyleIcon implements Icon {

		private RectangleStyle style;

		private RectangleStyleIcon(int index) {
			this.style = new RectangleStyle(index);
		}

		public int getIconWidth() {
			return 16;
		}

		public int getIconHeight() {
			return 16;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			style.set((Graphics2D) g);
			g.fillRect(x, y, 16, 16);
			g.setColor(Color.black);
			g.drawRect(x, y, 16, 16);
		}
	}
    
    private boolean[] columns = null;
    
    public QuartilePlotter() {
        super();
    }
    
    public QuartilePlotter(DataTable dataTable) {
    	super(dataTable);
    }
    
    public void setDataTable(DataTable dataTable) {
        this.columns = new boolean[dataTable.getNumberOfColumns()];
        super.setDataTable(dataTable);
    }
    
    public int getNumberOfAxes() {
        return 0;
    }
    
    public String getPlotName() {
        return "Dimensions";
    }
    
	public int getValuePlotSelectionType() {
		return MULTIPLE_SELECTION;
	}
	
    public void setPlotColumn(int index, boolean plot) {
    	columns[index] = plot;
        repaint();
    }

    public boolean getPlotColumn(int index) {
        return columns[index];
    }
    
	public Icon getIcon(int index) {
		return RECTANGLE_STYLE_ICONS[index % RECTANGLE_STYLE_ICONS.length];
	}
	
    protected void prepareData() {
        allQuartiles.clear();
        this.globalMin = Double.POSITIVE_INFINITY;
        this.globalMax = Double.NEGATIVE_INFINITY;
        
        if (columns != null) {
        	for (int i = 0; i < this.dataTable.getNumberOfColumns(); i++) {
        		if (columns[i]) {
                    Quartile quartile = Quartile.calculateQuartile(this.dataTable, i); 
                    quartile.setColor(new RectangleStyle(i).getColor());
                    allQuartiles.add(quartile); 
                    this.globalMin = MathFunctions.robustMin(this.globalMin, quartile.getMin());
                    this.globalMax = MathFunctions.robustMax(this.globalMax, quartile.getMax());
        		}
        	}
        }
    }
}
