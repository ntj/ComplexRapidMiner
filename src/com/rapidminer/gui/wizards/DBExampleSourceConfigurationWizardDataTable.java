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
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.rapidminer.example.Attributes;
import com.rapidminer.gui.EditorCellRenderer;
import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.tools.jdbc.ColumnIdentifier;


/**
 * This class displays a small data view corresponding on the current wizard settings.
 * 
 * @author Ingo Mierswa
 * @version $Id: DBExampleSourceConfigurationWizardDataTable.java,v 1.5 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class DBExampleSourceConfigurationWizardDataTable extends ExtendedJTable {

    private static final long serialVersionUID = -8058748606383970527L;

    private static class DBExampleSourceConfigurationWizardDataTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 3970716937239185940L;

        private Map<ColumnIdentifier, String> attributeTypeMap;
        
        private List<String[]> data;
        
        private transient ColumnIdentifier[] attributes;
        
        public DBExampleSourceConfigurationWizardDataTableModel(ColumnIdentifier[] attributes, List<String[]> data, Map<ColumnIdentifier, String> attributeTypeMap) {
            this.attributeTypeMap = attributeTypeMap;
            this.data = data;
            this.attributes = attributes;
        }
        
        public int getColumnCount() {
            return attributeTypeMap.keySet().size();
        }

        public int getRowCount() {
            return data.size() + 1;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex == 0) {
                return attributeTypeMap.get(attributes[columnIndex]);
            } else {
                int actualRow = rowIndex - 1;
                String[] row = data.get(actualRow);
                if (columnIndex >= row.length) {
                    return "?";
                } else {
                    return row[columnIndex];
                }
            }
        }
        
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            String type = (String)value;
            attributeTypeMap.remove(attributes[columnIndex]);
            attributeTypeMap.put(attributes[columnIndex], type);
        }

        public String getColumnName(int column) {
            return attributes[column].toString();
        }
    }
    
    public DBExampleSourceConfigurationWizardDataTable() {
        super(false);
        setAutoResizeMode(AUTO_RESIZE_OFF);
    }
    
    public void update(ColumnIdentifier[] identifiers, List<String[]> data, Map<ColumnIdentifier, String> attributeTypeMap) {
        setModel(new DBExampleSourceConfigurationWizardDataTableModel(identifiers, data, attributeTypeMap));
        TableColumnModel columnModel = getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn tableColumn = columnModel.getColumn(i);
            tableColumn.setPreferredWidth(120);
        }
    }
    
    public boolean isCellEditable(int row, int col) {
        return row == 0;
    }

    public TableCellEditor getCellEditor(int row, int column) {
        if (row == 0) {
            JComboBox typeBox = new JComboBox(Attributes.KNOWN_ATTRIBUTE_TYPES);
            typeBox.setBackground(javax.swing.UIManager.getColor("Table.cellBackground"));
            return new DefaultCellEditor(typeBox);
        } else {
            return super.getCellEditor(row, column);
        }
    }

    public TableCellRenderer getCellRenderer(int row, int column) {
        if (row == 0) {
            return new EditorCellRenderer(getCellEditor(row, column));
        } else {
            return super.getCellRenderer(row, column);
        }
    }
}
