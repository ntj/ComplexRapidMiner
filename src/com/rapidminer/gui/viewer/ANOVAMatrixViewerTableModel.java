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

import javax.swing.table.AbstractTableModel;

import com.rapidminer.operator.olap.ANOVAMatrix;
import com.rapidminer.tools.Tools;


/** The model for the {@link com.rapidminer.gui.viewer.ANOVAMatrixViewerTable}. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: ANOVAMatrixViewerTableModel.java,v 1.1 2007/05/27 22:00:40 ingomierswa Exp $
 */
public class ANOVAMatrixViewerTableModel extends AbstractTableModel {
    
	private static final long serialVersionUID = -5732155307505605893L;
	
	private ANOVAMatrix matrix;
    
    public ANOVAMatrixViewerTableModel(ANOVAMatrix matrix) {
    	this.matrix = matrix;
    }
    
    public int getRowCount() {
        return matrix.getAnovaAttributeNames().size();
    }

    public int getColumnCount() {
        return matrix.getGroupingAttributeNames().size() + 1;
    }
    
    public String getColumnName(int col) {
		if (col == 0) {
			return "ANOVA Attribute";
		} else {
			return "group " + matrix.getGroupingAttributeNames().get(col - 1);
		}	
    }
    
    public Object getValueAt(int row, int col) {
    	if (col == 0) {
    		return matrix.getAnovaAttributeNames().get(row);
    	} else {
    		return Tools.formatNumber(matrix.getProbabilities()[row][col - 1]);
    	}	    		
    }
}
