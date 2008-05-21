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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeString;

/**
 * <p>This operator can be used to write data into CSV files (Comma Separated Values). 
 * The values and columns are separated by &quot;;&quot;. Missing data values are 
 * indicated by empty cells.</p>
 *
 * @author Ingo Mierswa
 * @version $Id: CSVExampleSetWriter.java,v 1.4 2008/05/09 19:22:37 ingomierswa Exp $
 */
public class CSVExampleSetWriter extends Operator {

	/** The parameter name for &quot;The CSV file which should be written.&quot; */
	public static final String PARAMETER_CSV_FILE = "csv_file";

	/** The parameter name for the column separator parameter. */
	public static final String PARAMETER_COLUMN_SEPARATOR = "column_separator";

	/** Indicates if the attribute names should be written as first row. */
	public static final String PARAMETER_WRITE_ATTRIBUTE_NAMES = "write_attribute_names";
	
	public CSVExampleSetWriter(OperatorDescription description) {
		super(description);
	}
	
	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		String columnSeparator = getParameterAsString(PARAMETER_COLUMN_SEPARATOR);
		File file = getParameterAsFile(PARAMETER_CSV_FILE);
		PrintWriter out = null;
		try {
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), getEncoding()));
			
			// write column names
			if (getParameterAsBoolean(PARAMETER_WRITE_ATTRIBUTE_NAMES)) {
				Iterator<Attribute> a = exampleSet.getAttributes().allAttributes();
				boolean first = true;
				while (a.hasNext()) {
					if (!first)
						out.print(columnSeparator);
					Attribute attribute = a.next();
					out.print("\"" + attribute.getName() + "\"");
					first = false;
				}
				out.println();
			}
			
			// write data
			for (Example example : exampleSet) {
				Iterator<Attribute> a = exampleSet.getAttributes().allAttributes();
				boolean first = true;
				while (a.hasNext()) {
					Attribute attribute = a.next();
					if (!first)
						out.print(columnSeparator);
					if (!Double.isNaN(example.getValue(attribute))) {
						if (attribute.isNominal()) {
							out.print("\"" + example.getValueAsString(attribute) + "\"");
						} else {
						    out.print(example.getValue(attribute));
						}
					}
					first = false;
				}
				out.println();
			}
		} catch (FileNotFoundException e) {
			throw new UserError(this, 301, file.getName());
		} finally {
			if (out != null) {
				out.close();
			}
		}
		
		return new IOObject[] { exampleSet };
	}
	
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_CSV_FILE, "The CSV file which should be written.", "csv", false));
		types.add(new ParameterTypeString(PARAMETER_COLUMN_SEPARATOR, "The column separator.", ";"));
		types.add(new ParameterTypeBoolean(PARAMETER_WRITE_ATTRIBUTE_NAMES, "Indicates if the attribute names should be written as first row.", true));
		return types;
	}
}
