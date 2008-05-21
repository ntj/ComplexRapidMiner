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
package com.rapidminer.operator.io;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.att.AttributeDataSourceCreator;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

/**
 * <p>This operator can be used to load data from Microsoft Excel spreadsheets. 
 * This operator is able to reads data from Excel 95, 97, 2000, XP, and 2003.
 * The user has to define which of the spreadsheets in the workbook should be
 * used as data table. The table must have a format so that each line is an example
 * and each column represents an attribute. Please note that the first line might
 * be used for attribute names which can be indicated by a parameter.</p>
 * 
 * <p>The data table can be placed anywhere on the sheet and is allowed to
 * contain arbitrary formatting instructions, empty rows, and empty columns. Missing data values
 * are indicated by empty cells or by cells containing only &quot;?&quot;.</p>
 *
 * @author Ingo Mierswa
 * @version $Id: ExcelExampleSource.java,v 1.7 2008/05/09 19:22:37 ingomierswa Exp $
 */
public class ExcelExampleSource extends Operator {

	/** The parameter name for &quot;The Excel spreadsheet file which should be loaded.&quot; */
	public static final String PARAMETER_EXCEL_FILE = "excel_file";

	/** The parameter name for &quot;The number of the sheet which should be imported.&quot; */
	public static final String PARAMETER_SHEET_NUMBER = "sheet_number";

	/** The parameter name for &quot;Indicates if the first row should be used for the attribute names.&quot; */
	public static final String PARAMETER_FIRST_ROW_AS_NAMES = "first_row_as_names";

	/** The parameter name for &quot;Indicates which column should be used for the label attribute (0: no label)&quot; */
	public static final String PARAMETER_LABEL_COLUMN = "label_column";

	/** The parameter name for &quot;Indicates which column should be used for the Id attribute (0: no id)&quot; */
	public static final String PARAMETER_ID_COLUMN = "id_column";

	/** The parameter name for &quot;Determines, how the data is represented internally.&quot; */
	public static final String PARAMETER_DATAMANAGEMENT = "datamanagement";

	/** The parameter name for &quot;Character that is used as decimal point.&quot; */
	public static final String PARAMETER_DECIMAL_POINT_CHARACTER = "decimal_point_character";
	
	public ExcelExampleSource(OperatorDescription description) {
		super(description);
	}
	
	public IOObject[] apply() throws OperatorException {
		File file = getParameterAsFile(PARAMETER_EXCEL_FILE);
		
		Workbook workbook = null;
		try {
			workbook = Workbook.getWorkbook(file);
		} catch (Exception e) {
			throw new UserError(this, 302, file, e.getMessage());
		}
		Sheet sheet = workbook.getSheet(getParameterAsInt(PARAMETER_SHEET_NUMBER));
		
		int numberOfColumns = sheet.getColumns();
		int numberOfRows = sheet.getRows();
		
		// determine offsets
		int rowOffset = 0;
		int columnOffset = 0;
		boolean contentFound = false;
		for (int r = 0; r < numberOfRows; r++) {
			for (int c = 0; c < numberOfColumns; c++) {
				Cell cell = sheet.getCell(c, r);
				String content = cell.getContents();
				if ((content != null) && (content.trim().length() > 0)) {
					columnOffset = c;
					contentFound = true;
					break;
				}
			}
			if (contentFound) {
				rowOffset = r;
				break;
			}
		}
		if (!contentFound) {
			throw new UserError(this, 302, file, "spreadsheet seems to be empty");
		}
		
		// determine empty rows
		SortedSet<Integer> emptyRows = new TreeSet<Integer>();
		for (int r = rowOffset; r < numberOfRows; r++) {
			boolean rowEmpty = true;
			for (int c = columnOffset; c < numberOfColumns; c++) {
				Cell cell = sheet.getCell(c, r);
				String content = cell.getContents();
				if ((content != null) && (content.trim().length() > 0)) {
					rowEmpty = false;
					break;
				}
			}
			if (rowEmpty) {
				emptyRows.add(r);
			}
		}
		
		// determine empty columns
		SortedSet<Integer> emptyColumns = new TreeSet<Integer>();
		for (int c = columnOffset; c < numberOfColumns; c++) {
			boolean columnEmpty = true;
			for (int r = rowOffset; r < numberOfRows; r++) {
				Cell cell = sheet.getCell(c, r);
				String content = cell.getContents();
				if ((content != null) && (content.trim().length() > 0)) {
					columnEmpty = false;
					break;
				}
			}
			if (columnEmpty) {
				emptyColumns.add(c);
			}
		}
		
		// attribute names
		String[] attributeNames = new String[numberOfColumns - columnOffset - emptyColumns.size()];
		if (getParameterAsBoolean(PARAMETER_FIRST_ROW_AS_NAMES)) {
			int columnCounter = 0;
			for (int c = columnOffset; c < numberOfColumns; c++) {
				// skip empty columns
				if (emptyColumns.contains(c))
					continue;
				Cell cell = sheet.getCell(c, rowOffset);
				attributeNames[columnCounter++] = cell.getContents();
			}
		} else {
			for (int c = 0; c < numberOfColumns - columnOffset - emptyColumns.size(); c++) {
				attributeNames[c] = file.getName() + " (" + (c + 1) + ")";
			}
		}
		
		// attribute value types
		char decimalPointCharacter = getParameterAsString(PARAMETER_DECIMAL_POINT_CHARACTER).charAt(0);
		int[] valueTypes = new int[numberOfColumns - columnOffset - emptyColumns.size()];
		for (int i = 0; i < valueTypes.length; i++)
			valueTypes[i] = Ontology.INTEGER;
		for (int r = rowOffset; r < numberOfRows; r++) {
			// skip name row
			if ((r == rowOffset) && getParameterAsBoolean(PARAMETER_FIRST_ROW_AS_NAMES))
				continue;
			// skip empty rows
			if (emptyRows.contains(r))
				continue;
			String[] row = new String[numberOfColumns - columnOffset - emptyColumns.size()];
			int columnCounter = 0;
			for (int c = columnOffset; c < numberOfColumns; c++) {
				// skip empty columns
				if (emptyColumns.contains(c))
					continue;
				Cell cell = sheet.getCell(c, r);
				row[columnCounter] = cell.getContents();
				if ((row[columnCounter] == null) || (row[columnCounter].trim().length() == 0))
					row[columnCounter] = "?";
				columnCounter++;
			}
			AttributeDataSourceCreator.guessValueTypes(row, valueTypes, decimalPointCharacter);
		}
		
		// create attributes
		List<Attribute> attributes = new LinkedList<Attribute>();
		for (int c = 0; c < numberOfColumns - columnOffset - emptyColumns.size(); c++) {
            attributes.add(AttributeFactory.createAttribute(attributeNames[c], valueTypes[c]));
        }
		
		// create and fill table
		MemoryExampleTable table = new MemoryExampleTable(attributes);
		DataRowFactory dataRowFactory = new DataRowFactory(getParameterAsInt(PARAMETER_DATAMANAGEMENT), decimalPointCharacter);
		Attribute[] attributeArray = new Attribute[attributes.size()];
		attributes.toArray(attributeArray);
		for (int r = rowOffset; r < numberOfRows; r++) {
			// skip name row
			if ((r == rowOffset) && getParameterAsBoolean(PARAMETER_FIRST_ROW_AS_NAMES))
				continue;
			// skip empty rows
			if (emptyRows.contains(r))
				continue;
			String[] row = new String[numberOfColumns - columnOffset - emptyColumns.size()];
			int columnCounter = 0;
			for (int c = columnOffset; c < numberOfColumns; c++) {
				// skip empty columns
				if (emptyColumns.contains(c))
					continue;
				Cell cell = sheet.getCell(c, r);
				row[columnCounter] = cell.getContents();
				if ((row[columnCounter] == null) || (row[columnCounter].trim().length() == 0))
					row[columnCounter] = "?";
				columnCounter++;
					
			}
			DataRow dataRow = dataRowFactory.create(row, attributeArray);
			table.addDataRow(dataRow);
			checkForStop();
		}
		
		// special attributes
		Attribute labelAttribute = null;
		int labelColumn = getParameterAsInt(PARAMETER_LABEL_COLUMN);
		if (labelColumn != 0) {
			if (labelColumn >= attributes.size() + 1)
				throw new UserError(this, 111, "label_column = " + labelColumn);
			labelAttribute = attributeArray[labelColumn - 1];
		}
		Attribute idAttribute = null;
		int idColumn = getParameterAsInt(PARAMETER_ID_COLUMN);
		if (idColumn != 0) {
			if (idColumn >= attributes.size() + 1)
				throw new UserError(this, 111, "id_column = " + idColumn);
			idAttribute = attributeArray[idColumn - 1];
		}
		
		Map<Attribute, String> specialMap = new HashMap<Attribute, String>();
		specialMap.put(labelAttribute, Attributes.LABEL_NAME);
		specialMap.put(idAttribute, Attributes.ID_NAME);
		ExampleSet exampleSet = table.createExampleSet(specialMap);
		return new IOObject[] { exampleSet };
	}
	
	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_EXCEL_FILE, "The Excel spreadsheet file which should be loaded.", "xls", false));
		types.add(new ParameterTypeInt(PARAMETER_SHEET_NUMBER, "The number of the sheet which should be imported.", 0, Integer.MAX_VALUE, 0));
		ParameterType type = new ParameterTypeBoolean(PARAMETER_FIRST_ROW_AS_NAMES, "Indicates if the first row should be used for the attribute names.", false);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_LABEL_COLUMN, "Indicates which column should be used for the label attribute (0: no label)", 0, Integer.MAX_VALUE, 0);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeInt(PARAMETER_ID_COLUMN, "Indicates which column should be used for the Id attribute (0: no id)", 0, Integer.MAX_VALUE, 0));
		types.add(new ParameterTypeString(PARAMETER_DECIMAL_POINT_CHARACTER, "Character that is used as decimal point.", "."));
		types.add(new ParameterTypeCategory(PARAMETER_DATAMANAGEMENT, "Determines, how the data is represented internally.", DataRowFactory.TYPE_NAMES, DataRowFactory.TYPE_DOUBLE_ARRAY));
		return types;
	}
}
