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
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.FileDataRowReader;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.att.AttributeDataSource;
import com.rapidminer.tools.att.AttributeDataSourceCreator;
import com.rapidminer.tools.att.AttributeDataSources;
import com.rapidminer.tools.att.AttributeSet;


/**
 * <p>
 * This operator reads an example set from (a) file(s). Probably you can use the
 * default parameter values for the most file formats (including the format
 * produced by the ExampleSetWriter, CSV, ...). In fact, in many cases this operator
 * is more appropriate for CSV based file formats than the {@link CSVExampleSource} operator
 * itself.
 * </p>
 * 
 * <p>
 * In contrast to the usual ExampleSource operator this operator is able to read the
 * attribute names from the first line of the data file. However, there is one
 * restriction: the data can only be read from one file instead of multiple
 * files. If you need a fully flexible operator for data loading you should use 
 * the more powerful ExampleSource operator.
 * </p>
 * 
 * <p>
 * The column split points can be defined with regular expressions (please refer to the
 * Java API). The default split parameter &quot;,\s*|;\s*|\s+&quot; should work
 * for most file formats. This regular expression describes the following column
 * separators
 * <ul>
 * <li>the character &quot;,&quot; followed by a whitespace of arbitrary length (also no white space)</li>
 * <li>the character &quot;;&quot; followed by a whitespace of arbitrary length (also no white space)</li>
 * <li>a whitespace of arbitrary length (min. 1)</li>
 * </ul>
 * A logical XOR is defined by &quot;|&quot;. Other useful separators might be
 * &quot;\t&quot; for tabulars, &quot; &quot; for a single whitespace, and
 * &quot;\s&quot; for any whitespace.
 * </p>
 * 
 * <p>
 * Quoting is also possible with &quot;. However, using quotes slows down
 * parsing and is therefore not recommended. The user should ensure that the
 * split characters are not included in the data columns and that quotes are not
 * needed. Additionally you can specify comment characters which can be used at
 * arbitrary locations of the data lines. Unknown attribute values can be marked
 * with empty strings or a question mark.
 * </p>
 * 
 * @rapidminer.index csv
 * @author Ingo Mierswa
 * @version $Id: SimpleExampleSource.java,v 1.8 2006/04/12 18:04:24 ingomierswa
 *          Exp $
 */
public class SimpleExampleSource extends Operator {

	/** The parameter name for &quot;Name of the label attribute (if empty, the column defined by label_column will be used)&quot; */
	public static final String PARAMETER_LABEL_NAME = "label_name";

	/** The parameter name for &quot;Column number of the label attribute (only used if label_name is empty; 0 = none; negative values are counted from the last column)&quot; */
	public static final String PARAMETER_LABEL_COLUMN = "label_column";

	/** The parameter name for &quot;Name of the id attribute (if empty, the column defined by id_column will be used)&quot; */
	public static final String PARAMETER_ID_NAME = "id_name";

	/** The parameter name for &quot;Column number of the id attribute (only used if id_name is empty; 0 = none; negative values are counted from the last column)&quot; */
	public static final String PARAMETER_ID_COLUMN = "id_column";

	/** The parameter name for &quot;Name of the weight attribute (if empty, the column defined by weight_column will be used)&quot; */
	public static final String PARAMETER_WEIGHT_NAME = "weight_name";

	/** The parameter name for &quot;Column number of the weight attribute (only used if weight_name is empty; 0 = none, negative values are counted from the last column)&quot; */
	public static final String PARAMETER_WEIGHT_COLUMN = "weight_column";

	/** The parameter name for &quot;The fraction of the data set which should be read (1 = all; only used if sample_size = -1)&quot; */
	public static final String PARAMETER_SAMPLE_RATIO = "sample_ratio";

	/** The parameter name for &quot;The exact number of samples which should be read (-1 = use sample ratio; if not -1, sample_ratio will not have any effect)&quot; */
	public static final String PARAMETER_SAMPLE_SIZE = "sample_size";

	/** The parameter name for &quot;Determines, how the data is represented internally.&quot; */
	public static final String PARAMETER_DATAMANAGEMENT = "datamanagement";

	/** The parameter name for &quot;Indicates if a comment character should be used&quot; */
	public static final String PARAMETER_USE_COMMENT_CHARACTERS = "use_comment_characters";
	
	/** The parameter name for &quot;Lines beginning with these characters are ignored.&quot; */
	public static final String PARAMETER_COMMENT_CHARS = "comment_chars";

	/** The parameter name for &quot;Character that is used as decimal point.&quot; */
	public static final String PARAMETER_DECIMAL_POINT_CHARACTER = "decimal_point_character";
    
	protected static final String PARAMETER_FILENAME = "filename";
    
	protected static final String PARAMETER_READ_ATTRIBUTE_NAMES = "read_attribute_names";
    
	protected static final String PARAMETER_USE_QUOTES = "use_quotes";
    
	protected static final String PARAMETER_COLUMN_SEPARATORS = "column_separators";
	
	
	public SimpleExampleSource(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
        File file = getParameterAsFile(PARAMETER_FILENAME);
		double sampleRatio = getParameterAsDouble(PARAMETER_SAMPLE_RATIO);
        int maxLines = getParameterAsInt(PARAMETER_SAMPLE_SIZE);
		String separatorRegExpr = getParameterAsString(PARAMETER_COLUMN_SEPARATORS);
		char[] comments = null;
		if (getParameterAsBoolean(PARAMETER_USE_COMMENT_CHARACTERS)) {
			comments = getParameterAsString(PARAMETER_COMMENT_CHARS).toCharArray(); 
		}
		int dataRowType = getParameterAsInt(PARAMETER_DATAMANAGEMENT);
		boolean useQuotes = getParameterAsBoolean(PARAMETER_USE_QUOTES);
		char decimalPointCharacter = getParameterAsString(PARAMETER_DECIMAL_POINT_CHARACTER).charAt(0);

        // create attribute data sources and guess value types (performs a data scan)
		AttributeDataSourceCreator adsCreator = new AttributeDataSourceCreator();
        try {
            adsCreator.loadData(file, comments, separatorRegExpr, decimalPointCharacter, useQuotes, getParameterAsBoolean(PARAMETER_READ_ATTRIBUTE_NAMES), -1, getEncoding());
        } catch (IOException e) {
            throw new UserError(this, 302, file, e.getMessage());
        }
        List<AttributeDataSource> attributeDataSources = adsCreator.getAttributeDataSources();
        
		// set special attributes
        resetAttributeType(attributeDataSources, "label_name", "label_column", Attributes.LABEL_NAME);
        resetAttributeType(attributeDataSources, "id_name", "id_column", Attributes.ID_NAME);
        resetAttributeType(attributeDataSources, "weight_name", "weight_column", Attributes.WEIGHT_NAME);
        
        // read data
		FileDataRowReader reader = null;
		try {
			reader = new FileDataRowReader(new DataRowFactory(dataRowType, decimalPointCharacter), attributeDataSources, sampleRatio, maxLines, separatorRegExpr, comments, useQuotes, getEncoding(), RandomGenerator.getRandomGenerator(-1));
		} catch (IOException e) {
			throw new UserError(this, e, 302, new Object[] { file, e.getMessage() });
		}
        if (getParameterAsBoolean(PARAMETER_READ_ATTRIBUTE_NAMES))
            reader.skipLine();

		AttributeSet attributeSet = new AttributeSet(new AttributeDataSources(attributeDataSources, file));

        // create table and example set
		ExampleTable table = new MemoryExampleTable(attributeSet.getAllAttributes(), reader);
		ExampleSet result = table.createExampleSet(attributeSet);

		return new IOObject[] { result };
	}

    private void resetAttributeType(List<AttributeDataSource> attributeDataSources, String attributeName, String columnName, String typeName) throws OperatorException {
    	String attribute = getParameterAsString(attributeName);
    	if ((attribute == null) || (attribute.length() == 0)) {
    		int column = getParameterAsInt(columnName);
    		if (column != 0) {
    			if (column < 0)
    				column = attributeDataSources.size() + column + 1;
    			if ((column < 1) || (column >= attributeDataSources.size() + 1))
    				throw new UserError(this, 111, columnName + " = " + column);
    			column--;
    			attributeDataSources.get(column).setType(typeName);
    		}
    	} else {
    		Iterator<AttributeDataSource> i = attributeDataSources.iterator();
    		while (i.hasNext()) {
    			AttributeDataSource ads = i.next();
    			if (ads.getAttribute().getName().equals(attribute)) {
    				ads.setType(typeName);
    				break;
    			}
    		}
    	}
    }
    
	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_FILENAME, "Name of the file to read the data from.", "dat", false));
		ParameterType type = new ParameterTypeBoolean(PARAMETER_READ_ATTRIBUTE_NAMES, "Read attribute names from file (assumes the attribute names are in the first line of the file).", false);
        type.setExpert(false);
        types.add(type);
		type = new ParameterTypeString(PARAMETER_LABEL_NAME, "Name of the label attribute (if empty, the column defined by label_column will be used)", true);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_LABEL_COLUMN, "Column number of the label attribute (only used if label_name is empty; 0 = none; negative values are counted from the last column)", Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeString(PARAMETER_ID_NAME, "Name of the id attribute (if empty, the column defined by id_column will be used)", true));
        types.add(new ParameterTypeInt(PARAMETER_ID_COLUMN, "Column number of the id attribute (only used if id_name is empty; 0 = none; negative values are counted from the last column)", Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
        types.add(new ParameterTypeString(PARAMETER_WEIGHT_NAME, "Name of the weight attribute (if empty, the column defined by weight_column will be used)", true));
        types.add(new ParameterTypeInt(PARAMETER_WEIGHT_COLUMN, "Column number of the weight attribute (only used if weight_name is empty; 0 = none, negative values are counted from the last column)", Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		type = new ParameterTypeDouble(PARAMETER_SAMPLE_RATIO, "The fraction of the data set which should be read (1 = all; only used if sample_size = -1)", 0.0d, 1.0d, 1.0d);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeInt(PARAMETER_SAMPLE_SIZE, "The exact number of samples which should be read (-1 = use sample ratio; if not -1, sample_ratio will not have any effect)", -1, Integer.MAX_VALUE, -1));
		types.add(new ParameterTypeCategory(PARAMETER_DATAMANAGEMENT, "Determines, how the data is represented internally.", DataRowFactory.TYPE_NAMES, DataRowFactory.TYPE_DOUBLE_ARRAY));
		types.add(new ParameterTypeString(PARAMETER_COLUMN_SEPARATORS, "Column separators for data files (regular expression)", ",\\s*|;\\s*|\\s+"));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_COMMENT_CHARACTERS, "Indicates if qa comment character should be used.", true));
		types.add(new ParameterTypeString(PARAMETER_COMMENT_CHARS, "Lines beginning with these characters are ignored.", "#"));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_QUOTES, "Indicates if quotes should be regarded (slower!).", false));
		types.add(new ParameterTypeString(PARAMETER_DECIMAL_POINT_CHARACTER, "Character that is used as decimal point.", "."));
		return types;
	}
}
