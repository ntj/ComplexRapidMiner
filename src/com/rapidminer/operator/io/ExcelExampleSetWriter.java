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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;

/**
 * <p>This operator can be used to write data into Microsoft Excel spreadsheets. 
 * This operator creates Excel files readable by Excel 95, 97, 2000, XP, 2003 
 * and newer. Missing data values are indicated by empty cells.</p>
 *
 * @author Ingo Mierswa
 * @version $Id: ExcelExampleSetWriter.java,v 1.4 2008/05/09 19:22:37 ingomierswa Exp $
 */
public class ExcelExampleSetWriter extends Operator {

	/** The parameter name for &quot;The Excel spreadsheet file which should be written.&quot; */
	public static final String PARAMETER_EXCEL_FILE = "excel_file";
	
	public ExcelExampleSetWriter(OperatorDescription description) {
		super(description);
	}
	
	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		File file = getParameterAsFile(PARAMETER_EXCEL_FILE);
		
		WorkbookSettings ws = new WorkbookSettings();
		ws.setLocale(Locale.US);
		try {
			WritableWorkbook workbook = 
				Workbook.createWorkbook(file, ws);
			WritableSheet s = workbook.createSheet("RapidMiner Data", 0);
			writeDataSheet(s, exampleSet);
			workbook.write();
			workbook.close(); 
		} catch (Exception e) {
			throw new UserError(this, 303, file.getName(), e.getMessage());
		}
		
		return new IOObject[] { exampleSet };
	}
	
	private static void writeDataSheet(WritableSheet s, ExampleSet exampleSet) throws WriteException {

		// Format the Font
		WritableFont wf = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
		WritableCellFormat cf = new WritableCellFormat(wf);
		
		Iterator<Attribute> a = exampleSet.getAttributes().allAttributes();
		int counter = 0;
		while (a.hasNext()) {
			Attribute attribute = a.next();
			s.addCell(new Label(counter++, 0, attribute.getName(), cf));
		}
		
		NumberFormat nf = new NumberFormat("#.0");
	    WritableCellFormat nfCell = new WritableCellFormat(nf);
		WritableFont wf2 = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
		WritableCellFormat cf2 = new WritableCellFormat(wf2);
		int rowCounter = 1;
		for (Example example : exampleSet) {
			a = exampleSet.getAttributes().allAttributes();
			int columnCounter = 0;
			while (a.hasNext()) {
				Attribute attribute = a.next();
				if (!Double.isNaN(example.getValue(attribute))) {
					if (attribute.isNominal()) {
						s.addCell(new Label(columnCounter, rowCounter, example.getValueAsString(attribute), cf2));
					} else {
					    Number number = new Number(columnCounter, rowCounter, example.getValue(attribute), nfCell);
					    s.addCell(number);
					}
				}
				columnCounter++;
			}
			rowCounter++;
		}
	}
	
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_EXCEL_FILE, "The Excel spreadsheet file which should be written.", "xls", false));
		return types;
	}
}
