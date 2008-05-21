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
package com.rapidminer.gui.wizards;

import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.tools.att.AttributeDataSource;


/**
 * This class display a small data view corresponding on the current wizard settings.
 * 
 * @author Ingo Mierswa
 * @version $Id: ExampleSourceConfigurationWizardDataTable.java,v 1.3 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class ExampleSourceConfigurationWizardDataTable extends ExtendedJTable {
   
	private static final long serialVersionUID = -6334023466810899931L;

	private static class ExampleSourceConfigurationWizardDataTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 8548500131446968338L;

		private List<AttributeDataSource> sources;

        private List<String[]> data;
        
        public ExampleSourceConfigurationWizardDataTableModel(List<AttributeDataSource> sources, List<String[]> data) {
        	this.sources = sources;
        	this.data = data;
        }
        
        public int getColumnCount() {
        	return this.sources.size();
        }

        public int getRowCount() {
            return data.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            String[] row = data.get(rowIndex);
            if (columnIndex >= row.length) {
                return "?";
            } else {
                return row[columnIndex];
            }
        }
        
        public String getColumnName(int column) {
        	return sources.get(column).getAttribute().getName();
        }
    }
    
    public ExampleSourceConfigurationWizardDataTable(List<AttributeDataSource> sources, List<String[]> data) {
        super();
        setAutoResizeMode(AUTO_RESIZE_OFF);
        setModel(new ExampleSourceConfigurationWizardDataTableModel(sources, data));
        update();
    }
    
    public void update() {
        ((AbstractTableModel)getModel()).fireTableStructureChanged();
        TableColumnModel columnModel = getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn tableColumn = columnModel.getColumn(i);
            tableColumn.setPreferredWidth(120);
        }
    }
}
