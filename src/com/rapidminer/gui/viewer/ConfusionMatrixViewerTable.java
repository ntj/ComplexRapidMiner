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
package com.rapidminer.gui.viewer;

import java.awt.Color;

import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.gui.tools.SwingTools;


/**
 * Can be used to display (parts of) a confusion matrix by means of a JTable.
 * 
 * @author Ingo Mierswa
 * @version $Id: ConfusionMatrixViewerTable.java,v 1.6 2008/05/09 19:23:00 ingomierswa Exp $
 */
public class ConfusionMatrixViewerTable extends ExtendedJTable {

    private static final long serialVersionUID = 3799580633476845998L; 
    
	public ConfusionMatrixViewerTable(String[] classNames, double[][] counter) {
        super(new ConfusionMatrixViewerTableModel(classNames, counter), false);
        setAutoResizeMode(AUTO_RESIZE_OFF);
        setTableHeader(null);
    }
    
    public Color getCellColor(int row, int col) {
        if ((row == 0) || (row == (getRowCount() - 1)) || (col == 0) || (col == (getColumnCount() - 1))) {
        	return SwingTools.LIGHTEST_BLUE;
        } else {
        	if (row == col)
        		return SwingTools.LIGHT_YELLOW;
        	else
        		return SwingTools.LIGHTEST_YELLOW;
        }
    }
}
