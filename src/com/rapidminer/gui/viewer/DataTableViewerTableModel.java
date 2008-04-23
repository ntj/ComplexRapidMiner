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

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;


/** The model for the {@link com.rapidminer.gui.viewer.DataTableViewerTable}. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: DataTableViewerTableModel.java,v 1.1 2007/05/27 22:00:37 ingomierswa Exp $
 */
public class DataTableViewerTableModel extends AbstractTableModel {
    
    private static final long serialVersionUID = 8116530590493627673L;
    
    private transient DataTable dataTable;
    
    public DataTableViewerTableModel(DataTable dataTable) {
        this.dataTable = dataTable;
    }
    
    public Class<?> getColumnClass(int column) {
        if (dataTable.isNominal(column))
            return String.class;
        else
            return Double.class;
    }
    
    public int getRowCount() {
        return dataTable.getNumberOfRows();
    }

    public int getColumnCount() {
        return dataTable.getNumberOfColumns();
    }

    public Object getValueAt(int row, int col) {
        DataTableRow tableRow = dataTable.getRow(row);
        if (dataTable.isNominal(col)) {
            return dataTable.getValueAsString(tableRow, col);
        } else {
            return tableRow.getValue(col);
        }
    }
    
    public String getColumnName(int col) {
        return dataTable.getColumnName(col);
    }
}
