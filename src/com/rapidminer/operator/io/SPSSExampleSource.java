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

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.SPSSDataRowReader;
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


/**
 * This operator can read spss files.
 * 
 * @rapidminer.index spss
 * @author Tobias Malbrecht
 * @version $Id: SPSSExampleSource.java,v 1.5 2008/05/09 19:22:37 ingomierswa Exp $
 */
public class SPSSExampleSource extends Operator {


	/** The parameter name for &quot;Name of the file to read the data from.&quot; */
	public static final String PARAMETER_FILENAME = "filename";

	/** The parameter name for &quot;Determines which SPSS variable properties should be used for attribute naming.&quot; */
	public static final String PARAMETER_ATTRIBUTE_NAMING_MODE = "attribute_naming_mode";

	/** The parameter name for &quot;Use SPSS value labels as values.&quot; */
	public static final String PARAMETER_USE_VALUE_LABELS = "use_value_labels";

	/** The parameter name for &quot;Recode SPSS user missings to missing values.&quot; */
	public static final String PARAMETER_RECODE_USER_MISSINGS = "recode_user_missings";

	/** The parameter name for &quot;The fraction of the data set which should be read (1 = all; only used if sample_size = -1)&quot; */
	public static final String PARAMETER_SAMPLE_RATIO = "sample_ratio";

	/** The parameter name for &quot;The exact number of samples which should be read (-1 = all; if not -1, sample_ratio will not have any effect)&quot; */
	public static final String PARAMETER_SAMPLE_SIZE = "sample_size";

	/** The parameter name for &quot;Determines, how the data is represented internally.&quot; */
	public static final String PARAMETER_DATAMANAGEMENT = "datamanagement";
    private static final Class[] INPUT_CLASSES = {};

    private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

    public SPSSExampleSource(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException {
        DataRowReader reader = null;
        File file = getParameterAsFile(PARAMETER_FILENAME);
        try {
            reader = new SPSSDataRowReader(new DataRowFactory(getParameterAsInt(PARAMETER_DATAMANAGEMENT), '.'), file, getParameterAsInt(PARAMETER_ATTRIBUTE_NAMING_MODE), getParameterAsBoolean(PARAMETER_USE_VALUE_LABELS), getParameterAsBoolean(PARAMETER_RECODE_USER_MISSINGS), getParameterAsDouble(PARAMETER_SAMPLE_RATIO), getParameterAsInt(PARAMETER_SAMPLE_SIZE));
        } catch (IOException e) {
            throw new UserError(this, 302, file, e.getMessage());
        }
        ExampleSet result = ((SPSSDataRowReader) reader).getExampleSet();
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
        types.add(new ParameterTypeFile(PARAMETER_FILENAME, "Name of the file to read the data from.", "sav", false));
        types.add(new ParameterTypeCategory(PARAMETER_ATTRIBUTE_NAMING_MODE, "Determines which SPSS variable properties should be used for attribute naming.", SPSSDataRowReader.ATTRIBUTE_NAMING_MODES, SPSSDataRowReader.USE_VAR_NAME));
        types.add(new ParameterTypeBoolean(PARAMETER_USE_VALUE_LABELS, "Use SPSS value labels as values.", false));
        types.add(new ParameterTypeBoolean(PARAMETER_RECODE_USER_MISSINGS, "Recode SPSS user missings to missing values.", true));
        types.add(new ParameterTypeDouble(PARAMETER_SAMPLE_RATIO, "The fraction of the data set which should be read (1 = all; only used if sample_size = -1)", 0.0d, 1.0d, 1.0d));
        types.add(new ParameterTypeInt(PARAMETER_SAMPLE_SIZE, "The exact number of samples which should be read (-1 = all; if not -1, sample_ratio will not have any effect)", -1, Integer.MAX_VALUE, -1));
        types.add(new ParameterTypeCategory(PARAMETER_DATAMANAGEMENT, "Determines, how the data is represented internally.", DataRowFactory.TYPE_NAMES, DataRowFactory.TYPE_DOUBLE_ARRAY));
        return types;
    }
}
