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
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.FileDataRowReader;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.gui.wizards.ExampleSourceConfigurationWizardCreator;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeAttributeFile;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeConfiguration;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.att.AttributeDataSource;
import com.rapidminer.tools.att.AttributeDataSources;
import com.rapidminer.tools.att.AttributeSet;


/**
 * <p>
 * This operator reads an example set from (a) file(s). Probably you can use the
 * default parameter values for the most file formats (including the format
 * produced by the ExampleSetWriter, CSV, ...). Please refer to section
 * {@rapidminer.ref sec:inputfiles|First steps/File formats} for details on the
 * attribute description file set by the parameter <var>attributes</var> used
 * to specify attribute types.
 * </p>
 * 
 * <p>
 * This operator supports the reading of data from multiple source files. Each
 * attribute (including special attributes like labels, weights, ...) might be
 * read from another file. Please note that only the minimum number of lines of
 * all files will be read, i.e. if one of the data source files has less lines
 * than the others, only this number of examples will be read.
 * </p>
 * 
 * <p>
 * The split points can be defined with regular expressions (please refer to the
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
 * Quoting is also possible with &quot;. However, since using quotes might slow down
 * the parsing it is therefore recommended to ensure that the
 * split characters are not included in the data columns and that quotes are not
 * needed.
 * </p>
 * 
 * <p>
 * Additionally you can specify comment characters which can be used at
 * arbitrary locations of the data lines. Any content after the comment character
 * will be ignored. Unknown attribute values can be marked with empty strings 
 * (if this is possible for your column separators) or by a question mark (recommended).
 * </p>
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: ExampleSource.java,v 1.12 2008/05/09 19:22:37 ingomierswa Exp $
 */
public class ExampleSource extends Operator {

	/** The parameter name for &quot;Filename for the xml attribute description file. This file also contains the names of the files to read the data from.&quot; */
	public static final String PARAMETER_ATTRIBUTES = "attributes";

	/** The parameter name for &quot;The fraction of the data set which should be read (1 = all; only used if sample_size = -1)&quot; */
	public static final String PARAMETER_SAMPLE_RATIO = "sample_ratio";

	/** The parameter name for &quot;The exact number of samples which should be read (-1 = use sample ratio; if not -1, sample_ratio will not have any effect)&quot; */
	public static final String PARAMETER_SAMPLE_SIZE = "sample_size";

	/** The parameter name for &quot;Determines, how the data is represented internally.&quot; */
	public static final String PARAMETER_DATAMANAGEMENT = "datamanagement";

	/** The parameter name for &quot;Column separators for data files (regular expression)&quot; */
	public static final String PARAMETER_COLUMN_SEPARATORS = "column_separators";

	/** The parameter name for &quot;Indicates if a comment character should be used&quot; */
	public static final String PARAMETER_USE_COMMENT_CHARACTERS = "use_comment_characters";
	
	/** The parameter name for &quot;Lines beginning with these characters are ignored.&quot; */
	public static final String PARAMETER_COMMENT_CHARS = "comment_chars";

	/** The parameter name for &quot;Character that is used as decimal point.&quot; */
	public static final String PARAMETER_DECIMAL_POINT_CHARACTER = "decimal_point_character";

	/** The parameter name for &quot;Indicates if quotes should be regarded (slower!).&quot; */
	public static final String PARAMETER_USE_QUOTES = "use_quotes";

	/** The parameter name for &quot;Indicates if the loaded data should be permutated.&quot; */
	public static final String PARAMETER_PERMUTATE = "permutate";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (only for permutation, -1: use global).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
    
	
	private static final Class[] INPUT_CLASSES = {};

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	public ExampleSource(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		AttributeDataSources attributeDataSources = null;
		FileDataRowReader reader = null;

		File attributeFile = getParameterAsFile(PARAMETER_ATTRIBUTES);
		try {
			attributeDataSources = AttributeDataSource.createAttributeDataSources(attributeFile, true, this);
			char[] commentCharacters = null;
			if (getParameterAsBoolean(PARAMETER_USE_COMMENT_CHARACTERS)) {
				commentCharacters = getParameterAsString(PARAMETER_COMMENT_CHARS).toCharArray(); 
			}
			reader = new FileDataRowReader(new DataRowFactory(getParameterAsInt(PARAMETER_DATAMANAGEMENT), getParameterAsString(PARAMETER_DECIMAL_POINT_CHARACTER).charAt(0)), attributeDataSources.getDataSources(), getParameterAsDouble(PARAMETER_SAMPLE_RATIO), getParameterAsInt(PARAMETER_SAMPLE_SIZE), getParameterAsString(PARAMETER_COLUMN_SEPARATORS), commentCharacters, getParameterAsBoolean(PARAMETER_USE_QUOTES), getEncoding(), RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED)));
		} catch (IOException e) {
			throw new UserError(this, e, 302, new Object[] { attributeFile, e.getMessage() });
		} catch (com.rapidminer.tools.XMLException e) {
			throw new UserError(this, e, 401, e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new UserError(this, e, 401, e.toString());
		} catch (SAXException e) {
			throw new UserError(this, e, 401, e.toString());
		}

		AttributeSet attributeSet = new AttributeSet(attributeDataSources);

		ExampleTable table = new MemoryExampleTable(attributeSet.getAllAttributes(), reader, getParameterAsBoolean(PARAMETER_PERMUTATE));
		ExampleSet result = table.createExampleSet(attributeSet);
		if (result.size() == 0) {
			throw new UserError(this, 117);
		}
		return new IOObject[] { result };
	}

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}
    
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeConfiguration(ExampleSourceConfigurationWizardCreator.class, this);
        type.setExpert(false);
        types.add(type);
		types.add(new ParameterTypeAttributeFile(PARAMETER_ATTRIBUTES, "Filename for the xml attribute description file. This file also contains the names of the files to read the data from.", false));
		type = new ParameterTypeDouble(PARAMETER_SAMPLE_RATIO, "The fraction of the data set which should be read (1 = all; only used if sample_size = -1)", 0.0d, 1.0d, 1.0d);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeInt(PARAMETER_SAMPLE_SIZE, "The exact number of samples which should be read (-1 = use sample ratio; if not -1, sample_ratio will not have any effect)", -1, Integer.MAX_VALUE, -1));
		types.add(new ParameterTypeCategory(PARAMETER_DATAMANAGEMENT, "Determines, how the data is represented internally.", DataRowFactory.TYPE_NAMES, DataRowFactory.TYPE_DOUBLE_ARRAY));
		types.add(new ParameterTypeString(PARAMETER_COLUMN_SEPARATORS, "Column separators for data files (regular expression)", ",\\s*|;\\s*|\\s+"));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_COMMENT_CHARACTERS, "Indicates if qa comment character should be used.", true));
		types.add(new ParameterTypeString(PARAMETER_COMMENT_CHARS, "Any content in a line after one of these characters will be ignored.", "#"));
		types.add(new ParameterTypeString(PARAMETER_DECIMAL_POINT_CHARACTER, "Character that is used as decimal point.", "."));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_QUOTES, "Indicates if quotes should be regarded.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_PERMUTATE, "Indicates if the loaded data should be permutated.", false));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (only for permutation, -1: use global).", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
