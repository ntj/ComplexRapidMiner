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
import java.awt.event.MouseEvent;

import javax.swing.table.JTableHeader;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.gui.tools.SwingTools;


/**
 * Can be used to display (parts of) the data by means of a JTable.
 * 
 * @author Ingo Mierswa
 * @version $Id: DataViewerTable.java,v 1.3 2007/06/22 22:56:21 ingomierswa Exp $
 */
public class DataViewerTable extends ExtendedJTable {
    
    private static final long serialVersionUID = 5535239693801265693L;

    private int numberOfSpecialAttributes = 0;
    
    
    public DataViewerTable() { 
        setAutoResizeMode(AUTO_RESIZE_OFF);
    }

    public void setExampleSet(ExampleSet exampleSet) {
        setModel(new DataViewerTableModel(exampleSet));
        this.numberOfSpecialAttributes = exampleSet.getAttributes().specialSize();
    }
    
    /** This method ensures that the correct tool tip for the current column is delivered. */
    protected JTableHeader createDefaultTableHeader() {
      return new JTableHeader(columnModel) {
    	  
		private static final long serialVersionUID = 1L;

		public String getToolTipText(MouseEvent e) {
          java.awt.Point p = e.getPoint();
          int index = columnModel.getColumnIndexAtX(p.x);
          int realColumnIndex = convertColumnIndexToModel(index);
          return DataViewerTable.this.getHeaderToolTipText(realColumnIndex);
        }
      };
    }
    
    private String getHeaderToolTipText(int realColumnIndex) {
        if (realColumnIndex == 0) {
        	return "The position of the example in the (filtered) view on the example table.";
        } else {
        	return "The data for the attribute '" + 
        	  getModel().getColumnName(realColumnIndex) + "'.";
        }	
    }
    
    public Color getCellColor(int row, int column) {
    	int col = convertColumnIndexToModel(column);
        if ((col > 0) && (col < (numberOfSpecialAttributes + 1))) {
            if (row % 2 == 0) {
                return Color.WHITE;
            } else {
            	return SwingTools.LIGHTEST_YELLOW;
            }
        } else {
            if (row % 2 == 0) {
            	return Color.WHITE;
            } else {
            	return SwingTools.LIGHTEST_BLUE;
            }
        }
    }
}
