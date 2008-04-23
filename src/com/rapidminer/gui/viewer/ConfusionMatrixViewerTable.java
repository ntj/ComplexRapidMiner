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


/**
 * Can be used to display (parts of) a confusion matrix by means of a JTable.
 * 
 * @author Ingo Mierswa
 * @version $Id: ConfusionMatrixViewerTable.java,v 1.2 2007/06/20 12:30:02 ingomierswa Exp $
 */
public class ConfusionMatrixViewerTable extends ExtendedJTable {

    private static final long serialVersionUID = 3799580633476845998L; 
    
	public ConfusionMatrixViewerTable(String[] classNames, int[][] counter) {
        super(new ConfusionMatrixViewerTableModel(classNames, counter), false);
        setAutoResizeMode(AUTO_RESIZE_OFF);
        setTableHeader(null);
    }
    
    public Color getCellColor(int row, int col) {
        if ((row == 0) || (row == (getRowCount() - 1)) || (col == 0) || (col == (getColumnCount() - 1))) {
        	return SwingTools.LIGHTEST_BLUE;
        } else {
        	return SwingTools.LIGHTEST_YELLOW;
        }
    }
}
