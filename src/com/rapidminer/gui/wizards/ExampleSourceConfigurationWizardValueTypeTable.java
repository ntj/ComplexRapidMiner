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
package com.rapidminer.gui.wizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.gui.EditorCellRenderer;
import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.att.AttributeDataSource;


/**
 * This table shows only the attribute names and the attribute value types.
 * 
 * @author Ingo Mierswa
 * @version $Id: ExampleSourceConfigurationWizardValueTypeTable.java,v 1.1 2007/05/27 22:02:06 ingomierswa Exp $
 */
public class ExampleSourceConfigurationWizardValueTypeTable extends ExtendedJTable {
    
    private static final long serialVersionUID = -6402806364622312588L;

    private static class ExampleSourceConfigurationWizardValueTypeTableModel extends AbstractTableModel {

        private static final long serialVersionUID = -8459288119418286682L;

		private List<AttributeDataSource> sources;
                
        public ExampleSourceConfigurationWizardValueTypeTableModel(List<AttributeDataSource> sources) {
        	this.sources = sources;
        }
 
        public void guessValueTypes(File originalDataFile, String commentString, String columnSeparators, boolean firstLineAsNames) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(originalDataFile));
                String line = null;
                boolean first = true;
                while ((line = in.readLine()) != null) {
                    if ((commentString != null) && (commentString.trim().length() > 0) && (line.startsWith(commentString)))
                        continue;
                    String[] row = line.trim().split(columnSeparators);
                    if (first) {
                        if (!firstLineAsNames) {
                            updateValueTypes(row);
                        }
                        first = false;
                    } else {
                        updateValueTypes(row);
                    }
                }
                in.close();
            } catch (IOException e) {
                SwingTools.showSimpleErrorMessage("Cannot re-write data: " + e.getMessage(), e);
            }            
        }
        
        private void updateValueTypes(String[] row) {
            for (int c = 0; c < row.length; c++) {
                int valueType = Ontology.INTEGER;
                String value = row[c];
                if ((value != null) && (!value.equals("?"))) {
                    try {
                        double d = Double.parseDouble(value);
                        if ((valueType == Ontology.INTEGER) && ((int) d != d)) {
                            valueType = Ontology.REAL;
                        }
                    } catch (NumberFormatException e) {
                        valueType = Ontology.NOMINAL;
                    }
                }
                setValueAt(Ontology.VALUE_TYPE_NAMES[valueType], 0, c);
            }
        }
        
        public int getColumnCount() {
            return sources.size();
        }

        public int getRowCount() {
            return 1;
        }

        public void setValueAt(Object value, int rowIndex, int columnIndex) {
        	String valueTypeName = (String)value;
        	int valueType = Ontology.NOMINAL;
        	for (int i = 0; i < Ontology.VALUE_TYPE_NAMES.length; i++) {
        		if (Ontology.VALUE_TYPE_NAMES[i].equals(valueTypeName)) {
        			valueType = i;
        			break;
        		}
        	}
            AttributeDataSource source = sources.get(columnIndex);
            Attribute oldAttribute = source.getAttribute();
            source.setAttribute(AttributeFactory.changeValueType(oldAttribute, valueType));        	
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
        	return Ontology.VALUE_TYPE_NAMES[sources.get(columnIndex).getAttribute().getValueType()];
        }
        
        public String getColumnName(int column) {
        	return sources.get(column).getAttribute().getName();
        }
    }
    
    public ExampleSourceConfigurationWizardValueTypeTable(List<AttributeDataSource> sources) {
        super(false);
        setAutoResizeMode(AUTO_RESIZE_OFF);
        setModel(new ExampleSourceConfigurationWizardValueTypeTableModel(sources));
        update();
    }
    
    public void guessValueTypes(File data, String commentString, String columnSeparators, boolean firstLineAsNames) {
    	((ExampleSourceConfigurationWizardValueTypeTableModel)getModel()).guessValueTypes(data, commentString, columnSeparators, firstLineAsNames);	
    }
    
    public void update() {
        ((AbstractTableModel)getModel()).fireTableStructureChanged();
        TableColumnModel columnModel = getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn tableColumn = columnModel.getColumn(i);
            tableColumn.setPreferredWidth(120);
        }
    }
    
	public boolean isCellEditable(int row, int col) {
		return true;
	}

	public TableCellEditor getCellEditor(int row, int column) {
        String[] allValueTypes = Ontology.ATTRIBUTE_VALUE_TYPE.getNames();
		String[] valueTypes = new String[allValueTypes.length - 1];
		System.arraycopy(allValueTypes, 1, valueTypes, 0, valueTypes.length);
		JComboBox typeBox = new JComboBox(valueTypes);
		typeBox.setBackground(javax.swing.UIManager.getColor("Table.cellBackground"));
		return new DefaultCellEditor(typeBox);
	}

	public TableCellRenderer getCellRenderer(int row, int column) {
		return new EditorCellRenderer(getCellEditor(row, column));
	}
}
