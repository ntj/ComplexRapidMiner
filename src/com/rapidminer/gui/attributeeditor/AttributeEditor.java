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
package com.rapidminer.gui.attributeeditor;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.FileDataRowReader;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.example.table.RapidMinerLineReader;
import com.rapidminer.gui.EditorCellRenderer;
import com.rapidminer.gui.attributeeditor.actions.GuessAllTypesAction;
import com.rapidminer.gui.attributeeditor.actions.GuessTypeAction;
import com.rapidminer.gui.attributeeditor.actions.RemoveColumnAction;
import com.rapidminer.gui.attributeeditor.actions.RemoveRowAction;
import com.rapidminer.gui.attributeeditor.actions.UseRowAsNamesAction;
import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.io.ExampleSource;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.att.AttributeDataSource;
import com.rapidminer.tools.att.AttributeDataSources;
import com.rapidminer.tools.att.AttributeSet;


/**
 * A table for creating an attribute description file. Data can be read from
 * files as single columns or as a value series. The value types are guessed and
 * can be edited by the user.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: AttributeEditor.java,v 1.20 2008/05/09 19:23:21 ingomierswa Exp $
 */
public class AttributeEditor extends ExtendedJTable implements MouseListener, DataControlListener {

	private static final long serialVersionUID = -3312532913749370288L;

	private class DataCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = -7231941979925919248L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (!checkData(value, row, column)) {
				c.setBackground(java.awt.Color.red);
			} else {
				c.setBackground(java.awt.Color.white);
			}
			return c;
		}
	}
    
	private class AttributeTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 6911819468492570763L;

		public int getColumnCount() {
			return Math.min(sourceList.size(), lastColumn - (firstColumn - 1));
		}

		public int getRowCount() {
			return Math.min(rowCount, (lastRow - (firstRow - 1))) + NUM_OF_HEADER_ROWS;
		}

		public String getColumnName(int _column) {
			int column = _column + (firstColumn - 1);
			AttributeDataSource source = getDataSource(column);
			return source.getFile().getName() + " (" + (source.getColumn() + 1) + ")";
		}

		public Object getValueAt(int _row, int _column) {
			int row = _row;
			int column = _column + (firstColumn - 1);
			if (row < NUM_OF_HEADER_ROWS) {
				AttributeDataSource source = getDataSource(column);
				switch (row) {
					case NAME_ROW:
						return source.getAttribute().getName();
					case TYPE_ROW:
						return source.getType();
					case VALUE_TYPE_ROW:
						return Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(source.getAttribute().getValueType());
					case BLOCK_TYPE_ROW:
						return Ontology.ATTRIBUTE_BLOCK_TYPE.mapIndex(source.getAttribute().getBlockType());
					case SEPARATOR_ROW:
						return null;
					default:
						return "This cannot happen!";
				}
			} else {
				row = _row + (firstRow - 1);
				return getDatum(row - NUM_OF_HEADER_ROWS, column);
			}
		}

		public void setValueAt(Object value, int row, int column) {
			if (row < NUM_OF_HEADER_ROWS) {
				AttributeDataSource source = getDataSource(column);
				switch (row) {
					case NAME_ROW:
						source.getAttribute().setName((String) value);
						break;
					case TYPE_ROW:
						source.setType((String) value);
						break;
					case VALUE_TYPE_ROW:
						source.setAttribute(AttributeFactory.changeValueType(source.getAttribute(), Ontology.ATTRIBUTE_VALUE_TYPE.mapName((String) value)));
						break;
					case BLOCK_TYPE_ROW:
						source.getAttribute().setBlockType(Ontology.ATTRIBUTE_BLOCK_TYPE.mapName((String) value));
						break;
					case SEPARATOR_ROW:
						break;
				}
			} else {
				setDatum(row - NUM_OF_HEADER_ROWS, column, (String) value);
			}
		}
	}

	public static final int LOAD_DATA = 0;

	public static final int LOAD_SERIES_DATA = 1;

	private static final int COLUMN_WIDTH = 120;

	protected transient Action REMOVE_COLUMN_ACTION_24 = new RemoveColumnAction(this, IconSize.SMALL);
	protected transient Action REMOVE_COLUMN_ACTION_32 = new RemoveColumnAction(this, IconSize.MIDDLE);

	protected transient Action REMOVE_ROW_ACTION_24 = new RemoveRowAction(this, IconSize.SMALL);
	protected transient Action REMOVE_ROW_ACTION_32 = new RemoveRowAction(this, IconSize.MIDDLE);
    
    protected transient Action USE_ROW_AS_NAMES_ACTION_24 = new UseRowAsNamesAction(this, IconSize.SMALL);
    protected transient Action USE_ROW_AS_NAMES_ACTION_32 = new UseRowAsNamesAction(this, IconSize.MIDDLE);

	protected transient Action GUESS_TYPE_ACTION_24 = new GuessTypeAction(this, IconSize.SMALL);
	protected transient Action GUESS_TYPE_ACTION_32 = new GuessTypeAction(this, IconSize.MIDDLE);
    
    protected transient Action GUESS_ALL_TYPES_ACTION_24 = new GuessAllTypesAction(this, IconSize.SMALL);
    protected transient Action GUESS_ALL_TYPES_ACTION_32 = new GuessAllTypesAction(this, IconSize.MIDDLE);

	private static final int NAME_ROW = 0;

	private static final int TYPE_ROW = 1;

	private static final int VALUE_TYPE_ROW = 2;

	private static final int BLOCK_TYPE_ROW = 3;

	private static final int SEPARATOR_ROW = 4;

	private static final int NUM_OF_HEADER_ROWS = 5;

	private transient CellEditors cellEditors = new CellEditors(NUM_OF_HEADER_ROWS);

	private transient CellRenderers cellRenderers = new CellRenderers(NUM_OF_HEADER_ROWS);;

	private transient TableCellRenderer dataRenderer = new DataCellRenderer();

	private ArrayList<AttributeDataSource> sourceList = new ArrayList<AttributeDataSource>();

	private File file;

	private transient Operator exampleSource;

	private AttributeTableModel model;

	private int rowCount = 0;

	private int firstRow = 1;

	private int lastRow = 10;

	private int firstColumn = 1;

	private int lastColumn = 10;

    private boolean dataChanged = false;
    
    private boolean metaDataChanged = false;
    
	private Vector<Vector<String>> dataColumnVector = new Vector<Vector<String>>();

	private DataControl dataControl;

	public AttributeEditor(Operator exampleSource, DataControl dataControl) {
		super(null, false);
		this.dataControl = dataControl;
		setModel(model = new AttributeTableModel());
		this.exampleSource = exampleSource;
		setRowHeight(getRowHeight() + SwingTools.TABLE_WITH_COMPONENTS_ROW_EXTRA_HEIGHT);
		getTableHeader().setReorderingAllowed(false);
		setAutoResizeMode(AUTO_RESIZE_OFF);
		addMouseListener(this);
	}

	protected Object readResolve() {
		this.cellEditors = new CellEditors(NUM_OF_HEADER_ROWS);
		this.cellRenderers = new CellRenderers(NUM_OF_HEADER_ROWS);;
		this.dataRenderer = new DataCellRenderer();
		return this;
	}
	
    public boolean hasDataChanged() {
        return dataChanged;
    }

    public boolean hasMetaDataChanged() {
        return metaDataChanged;
    }
    
	private AttributeDataSource getDataSource(int i) {
		return sourceList.get(i);
	}

	private String getDatum(int row, int column) {
		Vector col = dataColumnVector.get(column);
		if (row >= col.size())
			return "?";
		return (String) col.get(row);
	}

	private void setDatum(int row, int column, String value) {
		Vector<String> col = dataColumnVector.get(column);
		if (row >= col.size()) {
			col.addElement(value);
			if (row > rowCount)
				rowCount = row + 1;
		} else {
			col.setElementAt(value, row);
		}
		dataControl.setMaxRows(Math.max(dataControl.getMaxRows(), col.size()));
		dataControl.setMaxColumns(Math.max(dataControl.getMaxColumns(), dataColumnVector.size()));
	}

	private int getDefaultMaximumNumber(String limitName, int defaultNumber) {
		String max = System.getProperty("rapidminer.gui.attributeeditor." + limitName);
		if (max != null) {
			try {
				int number = Integer.parseInt(max);
				if (number == -1)
					return defaultNumber;
				else
					return number;
			} catch (NumberFormatException e) {
				LogService.getGlobal().log("Value of rapidminer.gui.attributeeditor." + limitName + " must be an integer!", LogService.ERROR);
				return defaultNumber;
			}
		} else {
			// not defined --> return default number;
			return defaultNumber;
		}
	}

	private void createNewColumn() {
		// name
		JTextField nameField = new JTextField();
		nameField.setToolTipText("The name of the attribute.");
		cellEditors.add(NAME_ROW, new DefaultCellEditor(nameField));

		// type
		JComboBox typeBox = new JComboBox(Attributes.KNOWN_ATTRIBUTE_TYPES);
		typeBox.setEditable(true);
		typeBox.setToolTipText("The type of the attribute ('attribute' for regular learning attributes or a special attribute name).");
		cellEditors.add(TYPE_ROW, new DefaultCellEditor(typeBox));

		// value type
		String[] allValueTypes = Ontology.ATTRIBUTE_VALUE_TYPE.getNames();
		String[] valueTypes = new String[allValueTypes.length - 1];
		System.arraycopy(allValueTypes, 1, valueTypes, 0, valueTypes.length);
		JComboBox valueTypeBox = new JComboBox(valueTypes);
		valueTypeBox.setToolTipText("The value type of the attribute.");
		cellEditors.add(VALUE_TYPE_ROW, new DefaultCellEditor(valueTypeBox));

		// block type
		JComboBox blockTypeBox = new JComboBox(Ontology.ATTRIBUTE_BLOCK_TYPE.getNames());
		blockTypeBox.setToolTipText("The block type of this attribute.");
		cellEditors.add(BLOCK_TYPE_ROW, new DefaultCellEditor(blockTypeBox));

		// separator
		JTextField separator = new JTextField();
		separator.setToolTipText("Separates meta data from data.");
		separator.setEditable(false);
		cellEditors.add(SEPARATOR_ROW, new DefaultCellEditor(separator));

		for (int i = 0; i < cellRenderers.getSize(); i++) {
			cellRenderers.add(i, new EditorCellRenderer(cellEditors.get(i, cellEditors.getSize(i) - 1)));
		}
		dataColumnVector.add(new Vector<String>());
        this.dataChanged = true;
        this.metaDataChanged = true;
	}

	public void columnAdded(TableColumnModelEvent e) {
		super.columnAdded(e);
		// bigger default size
		TableColumn column = getColumnModel().getColumn(getColumnModel().getColumnCount() - 1);
		column.setPreferredWidth(COLUMN_WIDTH);
	}

	private void addColumn(File file, int index, int valueType) {
		String name = file.getName() + " (" + (index + 1) + ")";
		AttributeDataSource source = new AttributeDataSource(AttributeFactory.createAttribute(name, valueType), file, index, "attribute");
		createNewColumn();
		sourceList.add(source);
        this.dataChanged = true;
        this.metaDataChanged = true;
	}

	public void clear() {
		sourceList.clear();
		dataColumnVector.clear();
		rowCount = 0;
		dataControl.setMaxRows(0);
		dataControl.setMaxColumns(0);
		dataControl.update();
        this.dataChanged = false;
        this.metaDataChanged = false;
	}

	/**
	 * Loads data from a file. The dataType defines if the data should be loaded
	 * as series data. Must be one out of LOAD_DATA and LOAD_SERIES_DATA.
	 */
	public void readData(File file, int dataType) throws IOException {
		int columnOffset = sourceList.size();
		int numberOfNewColumns = 0;
		int currentRow = -1;

		BufferedReader in = new BufferedReader(new FileReader(file));
		RapidMinerLineReader reader = null;
		try {
			reader = new RapidMinerLineReader(exampleSource.getParameterAsString(ExampleSource.PARAMETER_COLUMN_SEPARATORS), exampleSource.getParameterAsString(ExampleSource.PARAMETER_COMMENT_CHARS).toCharArray(), exampleSource.getParameterAsBoolean(ExampleSource.PARAMETER_USE_QUOTES)); 
		} catch (UndefinedParameterError e) {
			 // cannot happen since all	parameters are optional
			throw new IOException("Cannot create RapidMiner line reader: " + e.getMessage());
		}

		ArrayList<Object> valueTypes = new ArrayList<Object>();

		int expectedNumberOfColumns = -1;
		while (true) {
			String[] columns = reader.readLine(in, expectedNumberOfColumns);
			if (columns == null)
				break; // eof;
			expectedNumberOfColumns = columns.length;
			currentRow++;

			for (int currentColumn = 0; currentColumn < columns.length; currentColumn++) {
				int valueType = Ontology.INTEGER;
				String value = columns[currentColumn];
				if ((!value.equals("?")) && (value.length() > 0)) {
					try {
						double d = Double.parseDouble(value);
						if (Tools.isEqual(Math.round(d), d)) {
							valueType = Ontology.INTEGER;
						} else {
							valueType = Ontology.REAL;
						}
					} catch (NumberFormatException e) {
						valueType = Ontology.NOMINAL;
					}
				}

				if (currentColumn >= numberOfNewColumns) {
					addColumn(file, currentColumn, valueType);
					numberOfNewColumns++;
					valueTypes.add(Integer.valueOf(valueType));
				} else {
					int soFar = ((Integer) valueTypes.get(currentColumn)).intValue();
					if (soFar != valueType) {
						if ((soFar == Ontology.NOMINAL) || (valueType == Ontology.NOMINAL)) {
							valueTypes.set(currentColumn, Integer.valueOf(Ontology.NOMINAL));
						} else { // 1 real, 1 integer
							valueTypes.set(currentColumn, Integer.valueOf(Ontology.REAL));
						}
					}
				}

				setDatum(currentRow, currentColumn + columnOffset, value);
			}
		}
		in.close();

		for (int i = 0; i < valueTypes.size(); i++) {
			getDataSource(i + columnOffset).setAttribute(AttributeFactory.changeValueType(getDataSource(i + columnOffset).getAttribute(), ((Integer) valueTypes.get(i)).intValue()));
		}

		// series data?
		if (dataType == LOAD_SERIES_DATA) {
			getDataSource(columnOffset).getAttribute().setBlockType(Ontology.VALUE_SERIES_START);
			for (int i = 1; i < valueTypes.size() - 1; i++) {
				getDataSource(i + columnOffset).getAttribute().setBlockType(Ontology.VALUE_SERIES);
			}
			getDataSource((valueTypes.size() - 1) + columnOffset).getAttribute().setBlockType(Ontology.VALUE_SERIES_END);
		}		
		update();
		guessAllColumnTypes();
        this.dataChanged = false;
        this.metaDataChanged = true;
	}

	public void guessColumnType() {
		int column = getSelectedColumn();
		if (column != -1)
			autoSetValueType(column);	
	}
	
	public void guessAllColumnTypes() {
        for (int i = 0; i < getColumnCount(); i++)
            autoSetValueType(i);	
	}
	
	private void autoSetValueType(int column) {
		char decimalPointCharacter = '.';
		try {
			decimalPointCharacter = exampleSource.getParameterAsString(ExampleSource.PARAMETER_DECIMAL_POINT_CHARACTER).charAt(0);
		} catch (UndefinedParameterError e) {
			// cannot happen
		}
		int valueType = Ontology.INTEGER;
		AttributeDataSource source = getDataSource(column);
		for (int i = 0; i < rowCount; i++) {
			String value = getDatum(i, column);
			if ((value != null) && (!value.equals("?")) && (value.trim().length() > 0)) {
				try {
					String valueString = value.replace(decimalPointCharacter, '.');
					double d = Double.parseDouble(valueString);
					if ((valueType == Ontology.INTEGER) && (!Tools.isEqual(Math.round(d), d))) {
						valueType = Ontology.REAL;
					}
				} catch (NumberFormatException e) {
					valueType = Ontology.NOMINAL;
					break;
				}
			}
		}
		source.setAttribute(AttributeFactory.changeValueType(source.getAttribute(), valueType));
		model.fireTableCellUpdated(VALUE_TYPE_ROW, column);
        this.metaDataChanged = true;
	}

	public boolean isCellEditable(int row, int col) {
		return true;
	}

	public TableCellEditor getCellEditor(int row, int column) {
		if (row >= NUM_OF_HEADER_ROWS) {
			return super.getCellEditor(row, column);
		} else {
			return cellEditors.get(row, column);
		}
	}

	public TableCellRenderer getCellRenderer(int row, int column) {
		if (row >= NUM_OF_HEADER_ROWS) {
			return dataRenderer;
		} else {
			return cellRenderers.get(row, column);
		}
	}

	private boolean checkData(Object value, int row, int column) {
		return true;
	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mouseClicked(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {
		evaluatePopup(e);
	}

	public void mousePressed(MouseEvent e) {
        int row = rowAtPoint(e.getPoint());
        int column = columnAtPoint(e.getPoint());
        setRowSelectionInterval(row, row);
        setColumnSelectionInterval(column, column);
		evaluatePopup(e);
	}

	private void evaluatePopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			createPopupMenu(columnAtPoint(e.getPoint())).show(this, e.getX(), e.getY());
		}
	}

	public JPopupMenu createPopupMenu(final int column) {
		JPopupMenu menu = new JPopupMenu();
		menu.add(GUESS_TYPE_ACTION_24);
        menu.add(GUESS_ALL_TYPES_ACTION_24);
		menu.add(REMOVE_COLUMN_ACTION_24);
		menu.add(REMOVE_ROW_ACTION_24);
        menu.add(USE_ROW_AS_NAMES_ACTION_24);
		return menu;
	}

	public void useRowAsNames() {
        int row = getSelectedRow() - NUM_OF_HEADER_ROWS;
        if (row >= 0) {
            useRowAsNames(row);
        }	
	}
	
	public void removeColumn() {	
		int column = getSelectedColumn();
		if (column != -1)
			removeColumn(column);
	}
	
	public void removeColumn(int column) {
		sourceList.remove(column);
		dataColumnVector.removeElementAt(column);
		rowCount = 0;
		if (dataColumnVector.size() > 0)
			rowCount = dataColumnVector.get(0).size();
		dataControl.setMaxRows(rowCount);
		dataControl.setMaxColumns(Math.max(0, dataControl.getMaxColumns() - 1));
		dataControl.update();
        this.dataChanged = true;
        this.metaDataChanged = true;
	}

	public void removeRow() {
		int row = getSelectedRow() - NUM_OF_HEADER_ROWS;
		if (row != -1)
			removeRow(row);
	}
	
	public void removeRow(int row) {
		if ((rowCount == 0) || (row < 0))
		    return;

		Iterator<Vector<String>> i = dataColumnVector.iterator();
		while (i.hasNext()) {
			Vector<String> c = i.next();
			c.remove(row);
		}
		rowCount--;
		dataControl.setMaxRows(rowCount);
		dataControl.update();
        this.dataChanged = true;
	}
    
    public void useRowAsNames(int row) {
        if ((rowCount == 0) || (row < 0))
            return;
        
        Iterator<Vector<String>> i = dataColumnVector.iterator();
        int column = 0;
        while (i.hasNext()) {
            Vector<String> c = i.next();
            String name = c.remove(row);
            setValueAt(name, NAME_ROW, column);
            column++;
        }
        rowCount--;
        dataControl.setMaxRows(rowCount);
        dataControl.update();
        this.dataChanged = true;
        this.metaDataChanged = true;
    }

	private void ensureAttributeTypeIsUnique(String type) {
		List<AttributeDataSource> columns = new LinkedList<AttributeDataSource>();
		List<Integer> columnNumbers = new LinkedList<Integer>();
		Iterator<AttributeDataSource> i = sourceList.iterator();
		int j = 0;
		while (i.hasNext()) {
			AttributeDataSource source = i.next();
			if ((source.getType() != null) && source.getType().equals(type)) {
				columns.add(source);
				columnNumbers.add(j);
			}
			j++;
		}
		if (columns.size() > 1) {
			String[] names = new String[columns.size()];
			i = columns.iterator();
			j = 0;
			while (i.hasNext()) {
				names[j++] = i.next().getAttribute().getName();
			}
			javax.swing.JTextArea message = new javax.swing.JTextArea("The special attribute " + type + " is multiply defined. Please select one of the data columns (others will be changed to regular attributes). Press \"Cancel\" to ignore.", 4, 40);
			message.setEditable(false);
			message.setLineWrap(true);
			message.setWrapStyleWord(true);
			message.setBackground(new javax.swing.JLabel("").getBackground());
			String selection = (String) JOptionPane.showInputDialog(this, message, type + " multiply defined", JOptionPane.WARNING_MESSAGE, null, names, names[0]);
			if (selection != null) {
				i = columns.iterator();
				Iterator k = columnNumbers.iterator();
				while (i.hasNext()) {
					AttributeDataSource source = i.next();
					Integer number = (Integer) k.next();
					if (!source.getAttribute().getName().equals(selection)) {
						source.setType("attribute");
						model.fireTableCellUpdated(TYPE_ROW, number.intValue());
					}
				}
			}
		}
	}

	public void writeData(File file) throws IOException {
		if (sourceList.size() == 0)
			return;

    	Charset encoding = Tools.getDefaultEncoding();
    	try {
    		encoding = exampleSource.getEncoding();
    	} catch (Exception e) {
    		// do nothing and use default encoding
    	}
    	
		PrintWriter out = null;
		try {
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));

			for (int i = 0; i < sourceList.size(); i++) {
				AttributeDataSource source = sourceList.get(i);
				source.setSource(file, i);
			}

			for (int row = 0; row < rowCount; row++) {
				for (int col = 0; col < sourceList.size(); col++) {
					if (col != 0)
						out.print("\t");
					String value = getDatum(row, col); 
					out.print(value);
					Attribute attribute = sourceList.get(col).getAttribute();
					if (attribute.isNominal()) {
						if ((value != null) && (value.length() != 0) && (!value.equals("?")))
							attribute.getMapping().mapString(value);
					}
				}
				out.println();
			}
			
			this.dataChanged = false;
		} catch (IOException e) {
			throw e;
		} finally {
			if (out != null) {
				out.close();		
			}
		}
	}

	public void openAttributeFile() {
		File file = SwingTools.chooseFile(this, null, true, "aml", "attribute description file");
		if (file != null) {
			openAttributeFile(file);
		}
	}

	public void openAttributeFile(File file) {
		AttributeDataSources attributeDataSources = null;
		try {
			attributeDataSources = AttributeDataSource.createAttributeDataSources(file, true, LogService.getGlobal());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Could not open '" + file + "':" + Tools.getLineSeparator() + e, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (attributeDataSources != null) {
			this.file = file;
			clear();

			DataRowReader reader = null;
			try {
				char[] commentCharacters = null;
				if (exampleSource.getParameterAsBoolean(ExampleSource.PARAMETER_USE_COMMENT_CHARACTERS)) {
					commentCharacters = exampleSource.getParameterAsString(ExampleSource.PARAMETER_COMMENT_CHARS).toCharArray(); 
				}
				reader = 
					new FileDataRowReader(new DataRowFactory(
							exampleSource.getParameterAsInt(ExampleSource.PARAMETER_DATAMANAGEMENT), 
							exampleSource.getParameterAsString(ExampleSource.PARAMETER_DECIMAL_POINT_CHARACTER).charAt(0)), 
							attributeDataSources.getDataSources(), 
							1.0d, 
							-1, 
							exampleSource.getParameterAsString(ExampleSource.PARAMETER_COLUMN_SEPARATORS), 
							commentCharacters, 
							exampleSource.getParameterAsBoolean(ExampleSource.PARAMETER_USE_QUOTES), 
							exampleSource.getEncoding(), 
							RandomGenerator.getRandomGenerator(-1)); 
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Cannot open data file: " + e, "Error", JOptionPane.ERROR_MESSAGE);
				return;
			} catch (UndefinedParameterError e) {} // cannot happen since used parameters are optional
			
			if (reader != null) {
				sourceList.addAll(attributeDataSources.getDataSources());
				for (int j = 0; j < attributeDataSources.getDataSources().size(); j++)
					createNewColumn();

				ExampleTable table = null;
				try {
					table = new MemoryExampleTable(new AttributeSet(attributeDataSources).getAllAttributes(), reader);
				} catch (UserError e) {
					SwingTools.showSimpleErrorMessage("Cannot load attribute descriptions.", e);
				}
				if (table != null) {
					Iterator<Example> e = table.createExampleSet().iterator();
					rowCount = 0;
					while (e.hasNext()) {
						Example example = e.next();
						Iterator adsIterator = sourceList.iterator();
						int n = 0;
						while (adsIterator.hasNext()) {
							AttributeDataSource ads = (AttributeDataSource) adsIterator.next();
							setDatum(rowCount, n++, example.getValueAsString(ads.getAttribute()));
						}
						rowCount++;
					}
				}
				update();
				this.metaDataChanged = false;
				this.dataChanged = false;
			}
		}
	}

	public void saveAttributeFile() {
		for (int i = 1; i < Attributes.KNOWN_ATTRIBUTE_TYPES.length; i++)
			ensureAttributeTypeIsUnique(Attributes.KNOWN_ATTRIBUTE_TYPES[i]);

		File file = SwingTools.chooseFile(this, null, false, "aml", "attribute description file");
		if (file != null) {
			this.file = file;
			try {
	        	Charset encoding = Tools.getDefaultEncoding();
	        	try {
	        		encoding = exampleSource.getEncoding();
	        	} catch (Exception e) {
	        		// do nothing and use default encoding
	        	}
				writeXML(file, encoding);
			} catch (java.io.IOException e) {
				JOptionPane.showMessageDialog(this, e.toString(), "Error saving attribute file " + file, JOptionPane.ERROR_MESSAGE);
			}
		}
        this.metaDataChanged = false;
	}

    private void writeXML(File attFile, Charset encoding) throws IOException {
        if (sourceList.size() == 0)
            return;
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(attFile), encoding));
        File defaultSource = getDataSource(0).getFile();
        String relativePath = Tools.getRelativePath(defaultSource, attFile);
        out.println("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>");
        out.println("<attributeset default_source=\"" + relativePath + "\">");
        Iterator i = sourceList.iterator();
        int c = 0;
        while (i.hasNext()) {
            AttributeDataSource ads = (AttributeDataSource) i.next();
            /*
            Attribute attribute = ads.getAttribute();
            if (attribute.isNominal()) {
                for (String nominalValue : dataColumnVector.get(c)) {
                    attribute.getMapping().mapString(nominalValue);
                }
            }
            */
            ads.writeXML(out, defaultSource);
            c++;
        }
        out.println("</attributeset>");
        out.close();
    }
    
	public File getFile() {
		return file;
	}

	/**
	 * This method should be invoked after data changes. It defines new ranges
	 * for the data control object and invokes the update method of data
	 * control.
	 */
	private void update() {
		dataControl.setFirstRow(1);
		dataControl.setLastRow(Math.min(dataControl.getMaxRows(), getDefaultMaximumNumber("rowlimit", dataControl.getMaxRows())));
		dataControl.setFirstColumn(1);
		dataControl.setLastColumn(Math.min(dataControl.getMaxColumns(), getDefaultMaximumNumber("columnlimit", dataControl.getMaxColumns())));
		dataControl.update();
	}

	/** Sets the new view data and fire a table structure changed event. */
	public void update(int firstRow, int lastRow, int firstColumn, int lastColumn, int what) {
		this.firstRow = firstRow;
		this.lastRow = lastRow;
		this.firstColumn = firstColumn;
		this.lastColumn = lastColumn;
		model.fireTableStructureChanged();
	}
}
