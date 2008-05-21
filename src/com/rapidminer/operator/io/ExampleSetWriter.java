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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleFormatter;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.FormatterException;
import com.rapidminer.example.table.NumericalAttribute;
import com.rapidminer.example.table.SparseFormatDataRowReader;
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


/**
 * Writes values of all examples in an {@link ExampleSet} to a file. Dense,
 * sparse, and user defined formats (specified by the parameter 'format') can be
 * used. Attribute description files may be generated for dense and sparse
 * format as well. These formats can be read using the {@link ExampleSource} and
 * {@link SparseFormatExampleSource} operators.
 * 
 * <dl>
 * <dt>dense:</dt>
 * <dd> Each line of the generated data file is of the form<br/> <center>
 * 
 * <pre>
 * regular attributes &lt;special attributes&gt;
 * </pre>
 * 
 * </center> For example, each line could have the form <center>
 * 
 * <pre>
 * value1 value2 ... valueN &lt;id&gt; &lt;label&gt; &lt;prediction&gt; ... &lt;confidences&gt;
 * </pre>
 * 
 * </center> Values in parenthesis are optional and are only printed if they are
 * available. The confidences are only given for nominal predictions. Other
 * special attributes might be the example weight or the cluster number. </dd>
 * <dt>sparse:</dt>
 * <dd>Only non 0 values are written to the file, prefixed by a column index.
 * See the description of {@link SparseFormatExampleSource} for details. </dd>
 * <dt>special:</dt>
 * <dd>Using the parameter 'special_format', the user can specify the exact
 * format. The $ sign has a special meaning and introduces a command (the
 * following character) Additional arguments to this command may be supplied
 * enclosing it in square brackets.
 * <dl>
 * <dt>$a:</dt>
 * <dd> All attributes separated by the default separator</dd>
 * <dt>$a[separator]:</dt>
 * <dd> All attributes separated by separator</dd>
 * <dt>$s[separator][indexSeparator]:</dt>
 * <dd> Sparse format. For all non zero attributes the following strings are
 * concatenated: the column index, the value of indexSeparator, the attribute
 * value. Attributes are separated by separator.</dd>
 * <dt>$v[name]:</dt>
 * <dd> The value of the attribute with the given name (both regular and special
 * attributes)</dd>
 * <dt>$k[index]:</dt>
 * <dd> The value of the attribute with the given index</dd>
 * <dt>$l:</dt>
 * <dd> The label</dd>
 * <dt>$p:</dt>
 * <dd> The predicted label</dd>
 * <dt>$d:</dt>
 * <dd> All prediction confidences for all classes in the form conf(class)=value</dd>
 * <dt>$d[class]:</dt>
 * <dd> The prediction confidence for the defined class as a simple number</dd>
 * <dt>$i:</dt>
 * <dd> The id</dd>
 * <dt>$w:</dt>
 * <dd> The weight</dd>
 * <dt>$b:</dt>
 * <dd> The batch number</dd>
 * <dt>$n:</dt>
 * <dd> The newline character</dd>
 * <dt>$t:</dt>
 * <dd> The tabulator character</dd>
 * <dt>$$:</dt>
 * <dd> The dollar sign</dd>
 * <dt>$[:</dt>
 * <dd> The '[' character</dd>
 * <dt>$]:</dt>
 * <dd> The ']' character</dd>
 * </dl>
 * Make sure the format string ends with $n if you want examples to be separated
 * by newlines!</dd>
 * </dl>
 * 
 * @see com.rapidminer.example.ExampleSet
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: ExampleSetWriter.java,v 1.15 2006/03/27 13:22:00 ingomierswa
 *          Exp $
 */
public class ExampleSetWriter extends Operator {


	/** The parameter name for &quot;File to save the example set to.&quot; */
	public static final String PARAMETER_EXAMPLE_SET_FILE = "example_set_file";

	/** The parameter name for &quot;File to save the attribute descriptions to.&quot; */
	public static final String PARAMETER_ATTRIBUTE_DESCRIPTION_FILE = "attribute_description_file";

	/** The parameter name for &quot;Format to use for output.&quot; */
	public static final String PARAMETER_FORMAT = "format";

	/** The parameter name for &quot;Format string to use for output.&quot; */
	public static final String PARAMETER_SPECIAL_FORMAT = "special_format";

	/** The parameter name for &quot;The number of fraction digits in the output file (-1: all possible digits).&quot; */
	public static final String PARAMETER_FRACTION_DIGITS = "fraction_digits";

	/** The parameter name for &quot;Indicates if nominal values containing whitespace characters should be quoted with double quotes.&quot; */
	public static final String PARAMETER_QUOTE_WHITESPACE = "quote_whitespace";

	/** The parameter name for &quot;Indicates if the data file content should be zipped.&quot; */
	public static final String PARAMETER_ZIPPED = "zipped";

	/** The parameter name for &quot;Indicates if the data should be appended to an possible existing data file. Otherwise the existing file will be overwritten.&quot; */
	public static final String PARAMETER_APPEND = "append";
	private static String[] formatNames;

	private static final int DENSE_FORMAT = 0;

	static {
		formatNames = new String[SparseFormatDataRowReader.FORMAT_NAMES.length + 2];
		formatNames[0] = "dense";
		for (int i = 0; i < SparseFormatDataRowReader.FORMAT_NAMES.length; i++) {
			formatNames[i + 1] = "sparse_" + SparseFormatDataRowReader.FORMAT_NAMES[i];
		}
		formatNames[formatNames.length - 1] = "special_format";
	}

	public ExampleSetWriter(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet eSet = getInput(ExampleSet.class);
		boolean zipped = getParameterAsBoolean(PARAMETER_ZIPPED);
		File dataFile = getParameterAsFile(PARAMETER_EXAMPLE_SET_FILE);
		if (zipped) {
			dataFile = new File(dataFile.getAbsolutePath() + ".gz");
		}
		File attFile = getParameterAsFile(PARAMETER_ATTRIBUTE_DESCRIPTION_FILE);
		boolean quoteWhitespace = getParameterAsBoolean(PARAMETER_QUOTE_WHITESPACE);
		boolean append = getParameterAsBoolean(PARAMETER_APPEND);
		int fractionDigits = getParameterAsInt(PARAMETER_FRACTION_DIGITS);
		if (fractionDigits < 0)
			fractionDigits = NumericalAttribute.UNLIMITED_NUMBER_OF_DIGITS;
		
		Charset encoding = getEncoding();
		
		try {
			// write example set
			int format = getParameterAsInt(PARAMETER_FORMAT);
			log("Writing example set in format '" + formatNames[format] + "'.");
			if (format == DENSE_FORMAT) { // dense
			    eSet.writeDataFile(dataFile, fractionDigits, quoteWhitespace, zipped, append, encoding);
                if (attFile != null) {
                    eSet.writeAttributeFile(attFile, dataFile, getEncoding());
                }
			} else if (format == formatNames.length - 1) { // special format
				if (attFile != null)
					logError("special_format used. Ignoring attribute description file.");
				writeSpecialFormat(eSet, dataFile, fractionDigits, quoteWhitespace, zipped, append, encoding);
			} else { // sparse
			    eSet.writeSparseDataFile(dataFile, format - 1, fractionDigits, quoteWhitespace, zipped, append, encoding);
                if (attFile != null)
                    eSet.writeSparseAttributeFile(attFile, dataFile, format - 1, encoding);
			}
		} catch (IOException e) {
			throw new UserError(this, e, 303, new Object[] { dataFile + " / " + attFile, e.getMessage() });
		}

		return new IOObject[] { eSet };
	}

	private void writeSpecialFormat(ExampleSet exampleSet, File dataFile, int fractionDigits, boolean quoteWhitespace, boolean zipped, boolean append, Charset encoding) throws OperatorException {
		String format = getParameterAsString(PARAMETER_SPECIAL_FORMAT);
		if (format == null)
			throw new UserError(this, 201, new Object[] { "special_format", "format", "special_format" });
		ExampleFormatter formatter;
		try {
			formatter = ExampleFormatter.compile(format, exampleSet, fractionDigits, quoteWhitespace);
		} catch (FormatterException e) {
			throw new UserError(this, 901, format, e.getMessage());
		}
		
		OutputStream out = null;
		PrintWriter writer = null;
		try {
			if (zipped) {
				out = new GZIPOutputStream(new FileOutputStream(dataFile, append));
			} else {
				out = new FileOutputStream(dataFile, append);
			}

			writer = new PrintWriter(new OutputStreamWriter(out, encoding));
			Iterator<Example> reader = exampleSet.iterator();
			while (reader.hasNext())
				writer.println(formatter.format(reader.next()));
		} catch (IOException e) {
			throw new UserError(this, 303, dataFile, e.getMessage());
		} finally {
			if (writer != null) {
				writer.close();		
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logError("Cannot close stream to file " + dataFile);
				}
			}
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
		types.add(new ParameterTypeFile(PARAMETER_EXAMPLE_SET_FILE, "File to save the example set to.", "dat", false));
		types.add(new ParameterTypeFile(PARAMETER_ATTRIBUTE_DESCRIPTION_FILE, "File to save the attribute descriptions to.", "aml", true));
		types.add(new ParameterTypeCategory(PARAMETER_FORMAT, "Format to use for output.", formatNames, 0));
		types.add(new ParameterTypeString(PARAMETER_SPECIAL_FORMAT, "Format string to use for output.", true));
        types.add(new ParameterTypeInt(PARAMETER_FRACTION_DIGITS, "The number of fraction digits in the output file (-1: all possible digits).", -1, Integer.MAX_VALUE, -1));
        types.add(new ParameterTypeBoolean(PARAMETER_QUOTE_WHITESPACE, "Indicates if nominal values containing whitespace characters should be quoted with double quotes.", true));
        types.add(new ParameterTypeBoolean(PARAMETER_ZIPPED, "Indicates if the data file content should be zipped.", false));
        types.add(new ParameterTypeBoolean(PARAMETER_APPEND, "Indicates if the data should be appended to an possible existing data file. Otherwise the existing file will be overwritten.", false));
		return types;
	}
}
