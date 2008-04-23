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
package com.rapidminer.gui.viewer;

import java.awt.Color;

import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.olap.ANOVAMatrix;


/**
 * Can be used to display (parts of) an ANOVA matrix by means of a JTable.
 * 
 * @author Ingo Mierswa
 * @version $Id: ANOVAMatrixViewerTable.java,v 1.1 2007/05/27 22:00:39 ingomierswa Exp $
 */
public class ANOVAMatrixViewerTable extends ExtendedJTable {
    
	private static final long serialVersionUID = -5076213251038547710L;
	
	private ANOVAMatrix matrix;
	
	public ANOVAMatrixViewerTable(ANOVAMatrix matrix) {
        super(new ANOVAMatrixViewerTableModel(matrix), true);
        this.matrix = matrix;
        setAutoResizeMode(AUTO_RESIZE_OFF);
    }
    
    public Color getCellColor(int row, int col) {
        if (col == 0) {
        	return SwingTools.LIGHTEST_BLUE;
        } else {
        	double value = matrix.getProbabilities()[row][col - 1];
        	if (value < matrix.getSignificanceLevel()) {
        		return SwingTools.LIGHT_YELLOW;
        	} else {
        		return SwingTools.LIGHTEST_YELLOW;
        	}
        }
    }
}
