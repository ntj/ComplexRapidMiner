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

import javax.swing.table.AbstractTableModel;

import com.rapidminer.operator.visualization.dependencies.SymmetricalMatrix;

/** The model for the {@link com.rapidminer.gui.viewer.SymmetricalMatrixViewerTable}. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: SymmetricalMatrixViewerTableModel.java,v 1.1 2008/08/25 08:10:33 ingomierswa Exp $
 */
public class SymmetricalMatrixViewerTableModel extends AbstractTableModel {
    
    private static final long serialVersionUID = 8116530590493627673L;
    
    private transient SymmetricalMatrix matrix;
    
    public SymmetricalMatrixViewerTableModel(SymmetricalMatrix matrix) {
        this.matrix = matrix;
    }
    
    public Class<?> getColumnClass(int column) {
    	Class<?> type = super.getColumnClass(column);
    	if (column == 0) {
    		type = String.class;
    	} else {
    		type = Double.class;
    	}
        return type;
    }
    
    public int getRowCount() {
        return matrix.getNumberOfColumns();
    }

    public int getColumnCount() {
        return matrix.getNumberOfColumns();
    }

    public Object getValueAt(int row, int col) {
    	if (col == 0) {
    		return matrix.getColumnName(row);
    	} else {
    		return matrix.getValue(row, col - 1);
    	}
    }
    
    public String getColumnName(int col) {
        if (col == 0) {
        	return "Name";
        } else {
        	return matrix.getColumnName(col - 1);
        }
    }
}
