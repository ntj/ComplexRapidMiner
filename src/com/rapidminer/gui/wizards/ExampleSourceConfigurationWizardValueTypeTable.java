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
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.att.AttributeDataSource;


/**
 * This table shows only the attribute names and the attribute value types.
 * 
 * @author Ingo Mierswa
 * @version $Id: ExampleSourceConfigurationWizardValueTypeTable.java,v 1.8 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class ExampleSourceConfigurationWizardValueTypeTable extends ExtendedJTable {
    
    private static final long serialVersionUID = -6402806364622312588L;

    private static class ExampleSourceConfigurationWizardValueTypeTableModel extends AbstractTableModel {

        private static final long serialVersionUID = -8459288119418286682L;

		private List<AttributeDataSource> sources;
                
        public ExampleSourceConfigurationWizardValueTypeTableModel(List<AttributeDataSource> sources) {
        	this.sources = sources;
        }
 
        public void guessValueTypes(File originalDataFile, String commentString, String columnSeparators, char decimalPointCharacter, boolean useQuotes, boolean firstLineAsNames) {
        	BufferedReader in = null;
        	try {
        		in = new BufferedReader(new FileReader(originalDataFile));
        		String line = null;
        		boolean first = true;
        		boolean[] hasToCheck = null;
        		int rowCounter = 1;
        		while ((line = in.readLine()) != null) {
        			if ((commentString != null) && (commentString.trim().length() > 0) && (line.startsWith(commentString)))
        				continue;
        			if (line.trim().length() == 0)
        				continue;

        			String[] row = line.trim().split(columnSeparators);
        			if (useQuotes)
        				row = Tools.mergeQuotedSplits(line, row, "\"");
        			if (first) {
        				hasToCheck = new boolean[row.length];
        				for (int i = 0; i < hasToCheck.length; i++)
        					hasToCheck[i] = true;

        				if (!firstLineAsNames) {
        					updateValueTypes(row, hasToCheck, decimalPointCharacter);
        				}

        				first = false;
        			} else {
        				if (row.length != hasToCheck.length)
        					throw new IOException("Line " + rowCounter + " has a number of columns (" + row.length + ") different from preceding lines (" + hasToCheck.length + ").");
        				updateValueTypes(row, hasToCheck, decimalPointCharacter);
        			}

        			rowCounter++;
        		}
        	} catch (IOException e) {
        		SwingTools.showSimpleErrorMessage("Cannot guess value types: " + e.getMessage(), e);                	
        	} finally {
        		if (in != null) {
        			try {
						in.close();
					} catch (IOException e) {
						SwingTools.showSimpleErrorMessage("Cannot close stream to data file: " + e.getMessage(), e);  
					}
        		}
        	}         
        }
        
        private void updateValueTypes(String[] row, boolean[] hasToCheck, char decimalPointCharacter) {
            for (int c = 0; c < row.length; c++) {
            	if (hasToCheck[c]) {
            		int valueType = Ontology.INTEGER;
            		String value = row[c];

            		if ((value != null) && (value.length() >= 0) && (!value.equals("?"))) {
            			try {
            				String decimalValue = value.replace(decimalPointCharacter, '.');
            				double d = Double.parseDouble(decimalValue);
            				if ((valueType == Ontology.INTEGER) && (!Tools.isEqual(Math.round(d), d))) {
            					valueType = Ontology.REAL;
            					hasToCheck[c] = false;
            				}
            			} catch (NumberFormatException e) {
            				valueType = Ontology.NOMINAL;
            				hasToCheck[c] = false;
            			}
            		}
            		setValueAt(Ontology.VALUE_TYPE_NAMES[valueType], 0, c);
            	}
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
    
    public void guessValueTypes(File data, String commentString, String columnSeparators, char decimalPointCharacter, boolean useQuotes, boolean firstLineAsNames) {
    	((ExampleSourceConfigurationWizardValueTypeTableModel)getModel()).guessValueTypes(data, commentString, columnSeparators, decimalPointCharacter, useQuotes, firstLineAsNames);	
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
