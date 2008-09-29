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
package com.rapidminer.gui.tools;

import java.awt.Color;

/**
 *  Delivers a color based on the given value scaled.
 *   
 *  @author Ingo Mierswa
 *  @version $Id: CellColorProviderScaled.java,v 1.1 2008/08/25 08:10:36 ingomierswa Exp $
 */
public class CellColorProviderScaled implements CellColorProvider {

	private double min;
	
	private double max;
	
	private boolean absolute;

	private ExtendedJTable table;
	
	public CellColorProviderScaled(ExtendedJTable table, boolean absolute, double min, double max) {
		this.table = table;
		this.absolute = absolute;
		this.min = min;
		this.max = max;
	}
	
	public Color getCellColor(int row, int column) {
    	Object valueObject = table.getValueAt(row, column);
    	try {
    		double value = Double.parseDouble(valueObject.toString());
    		if (absolute)
    			value = Math.abs(value);
    		float scaled = (float)((value - min) / (max - min));
    		Color color = 
    			new Color(1.0f - scaled * 0.2f, 
    					1.0f - scaled * 0.2f, 
    					1.0f);
    		return color;
    	} catch (NumberFormatException e) {
    		return Color.WHITE;
    	}
	}
}
