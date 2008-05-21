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
package com.rapidminer.operator.visualization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;
import com.rapidminer.datatable.SimpleDataTable;
import com.rapidminer.datatable.SimpleDataTableRow;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeValue;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * This operator records almost arbitrary data. It can written to a file which
 * can be read e.g. by gnuplot. Alternatively, the collected data can be plotted
 * by the GUI. This is even possible during process runtime (i.e. online
 * plotting).<br/>
 * 
 * Parameters in the list <code>log</code> are interpreted as follows: The
 * <var>key</var> gives the name for the column name (e.g. for use in the
 * plotter). The <var>value</var> specifies where to retrieve the value from.
 * This is best explained by an example:
 * <ul>
 * <li>If the value is <code>operator.Evaluator.value.absolute</code>, the
 * ProcessLogOperator looks up the operator with the name
 * <code>Evaluator</code>. If this operator is a
 * {@link com.rapidminer.operator.performance.PerformanceEvaluator}, it has a
 * value named <var>absolute</var> which gives the absolute error of the last
 * evaluation. This value is queried by the ProcessLogOperator</li>
 * <li>If the value is <code>operator.SVMLearner.parameter.C</code>, the
 * ProcessLogOperator looks up the parameter <var>C</var> of the operator
 * named <code>SVMLearner</code>.</li>
 * </ul>
 * Each time the ProcessLogOperator is applied, all the values and parameters
 * specified by the list <var>log</var> are collected and stored in a data row.
 * When the process finishes, the operator writes the collected data rows to
 * a file (if specified). In GUI mode, 2D or 3D plots are automatically
 * generated and displayed in the result viewer. <br/> Please refer to section
 * {@rapidminer.ref sec:parameter_optimization|Advanced Processes/Parameter and performance analysis}
 * for an example application.
 * 
 * @rapidminer.todo Use IOObjects for logging as well (e.g.
 *            {@link com.rapidminer.operator.performance.PerformanceVector})
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: ProcessLogOperator.java,v 2.27 2006/03/27 13:21:58
 *          ingomierswa Exp $
 */
public class ProcessLogOperator extends Operator {


	/** The parameter name for &quot;operator.OPERATORNAME.[value|parameter].VALUE_NAME&quot; */
	public static final String PARAMETER_COLUMN_NAME = "column_name";
	public static final String PARAMETER_FILENAME = "filename";
	
	public static final String PARAMETER_LOG = "log";
	
	private static final Class[] OUTPUT_CLASSES = {};
	private static final String PARAMETER_PERSISTENT = "persistent";

	private String[] valueNames;
	
	private File outputFile;

	public ProcessLogOperator(OperatorDescription description) {
		super(description);
	}

	private double fetchValue(String name, int column) throws UndefinedParameterError {
		StringTokenizer reader = new StringTokenizer(name, ".");
		String type = reader.nextToken();
		if (type.equals("operator")) {
			String opName = reader.nextToken();
			Operator operator = getProcess().getOperator(opName);
			if (operator != null) {
				type = reader.nextToken();
				if (type.equals("value")) {
					String valueName = reader.nextToken();
					double value = operator.getValue(valueName);
					if (Double.isNaN(value)) {
						logWarning("No such value in '" + name + "'");
						return Double.NaN;
					}
					return value;
				} else if (type.equals("parameter")) {
					String parameterName = reader.nextToken();
					ParameterType parameterType = operator.getParameterType(parameterName);
					if (parameterType == null) {
						logWarning("No such parameter in '" + name + "'");
						return Double.NaN;
					} else {
						if (parameterType.isNumerical()) { // numerical
							try {
								return Double.parseDouble(operator.getParameter(parameterName).toString());
							} catch (NumberFormatException e) {
								logWarning("Cannot parse parameter value of '" + name + "'");
							}
						} else { // nominal
							String value = parameterType.toString(operator.getParameter(parameterName));
							SimpleDataTable table = (SimpleDataTable)getProcess().getDataTable(getName());
							return table.mapString(column, value);
						}
					}
				} else {
					logWarning("Unknown token '" + type + "' in '" + name + "'");
				}
			} else {
				logWarning("Unknown operator '" + opName + "' in '" + name + "'");
			}
		} else {
			logWarning("Unknown token '" + type + "' in '" + name + "'");
		}
		return Double.NaN;
	}

	public void processStarts() throws OperatorException {
		super.processStarts();
		List parameters = getParameterList(PARAMETER_LOG);
		String columnNames[] = new String[parameters.size()];
		valueNames = new String[parameters.size()];
		Iterator i = parameters.iterator();
		int j = 0;
		while (i.hasNext()) {
			Object[] parameter = (Object[]) i.next();
			columnNames[j] = (String) parameter[0];
			valueNames[j] = (String) parameter[1];
			j++;
		}
		getProcess().addDataTable(new SimpleDataTable(getName(), columnNames));
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public IOObject[] apply() throws OperatorException {
		DataTableRow row = fetchAllValues();
		if (getParameterAsBoolean(PARAMETER_PERSISTENT)) {
			writeOnline(row);
		}
		return new IOObject[] {};
	}

	private void writeOnline(DataTableRow row) throws UserError {
		DataTable table = getProcess().getDataTable(getName());
		try {
			if (outputFile == null) {
				outputFile = getParameterAsFile(PARAMETER_FILENAME);
				PrintWriter out = new PrintWriter(new FileWriter(outputFile));
				out.println("# Generated by " + getName() + "[" + getClass().getName() + "]");
				for (int j = 0; j < table.getNumberOfColumns(); j++) {
					out.print((j != 0 ? "\t" : "# ") + table.getColumnName(j));
				}
				out.println();
				out.close();
			}
		
			PrintWriter out = new PrintWriter(new FileWriter(outputFile, true));
			for (int j = 0; j < row.getNumberOfValues(); j++) {
				out.print((j != 0 ? "\t" : "") + table.getValueAsString(row, j));
			}
			out.println();
			out.close();
		} catch (IOException e) {
			throw new UserError(this, 303, outputFile.getName(), e.getMessage());
		}
	}
	
	
	private DataTableRow fetchAllValues() throws UndefinedParameterError {
		double[] row = new double[valueNames.length];
		for (int i = 0; i < valueNames.length; i++) {
			double value = fetchValue(valueNames[i], i);
			row[i] = value;
		}
		DataTableRow dataRow = new SimpleDataTableRow(row, null);
		getProcess().getDataTable(getName()).add(dataRow);
		return dataRow;
	}

	public void processFinished() throws OperatorException {
		super.processFinished();

		if (! getParameterAsBoolean(PARAMETER_PERSISTENT)) {
			File file = null;
			try {
				file = getParameterAsFile(PARAMETER_FILENAME);
			} catch (UndefinedParameterError e) {
				// tries to determine a file for output writing
				// if no file was specified --> do not write results in file
			}
			if (file != null) {
				log("Writing data to '" + file.getName() + "'");
				PrintWriter out = null;
				try {
					out = new PrintWriter(new FileWriter(file));
					getProcess().getDataTable(getName()).write(out);
				} catch (IOException e) {
					throw new UserError(this, 303, file.getName(), e.getMessage());
				} finally {
					if (out != null)
						out.close();
				}
			}
		}
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeFile(PARAMETER_FILENAME, "File to save the data to.", "log", true);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeList(PARAMETER_LOG, "List of key value pairs where the key is the column name and the value specifies the process value to log.", new ParameterTypeValue(PARAMETER_COLUMN_NAME, "operator.OPERATORNAME.[value|parameter].VALUE_NAME"));
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_PERSISTENT, "Indicates if results should be written to file immediately", false));
		return types;
	}
}
