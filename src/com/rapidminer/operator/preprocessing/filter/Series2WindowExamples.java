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
package com.rapidminer.operator.preprocessing.filter;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;


/**
 * <p>This is the superclass for all series to example transformation operators based on windowing.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: Series2WindowExamples.java,v 1.3 2006/04/05 08:57:27
 *          ingomierswa Exp $
 */
public abstract class Series2WindowExamples extends Operator {

	/** The parameter name for &quot;This parameter defines how the series values will be represented.&quot; */
	public static final String PARAMETER_SERIES_REPRESENTATION = "series_representation";

	/** The parameter name for &quot;The prediction horizon, i.e. the distance between the last window value and the value to predict.&quot; */
	public static final String PARAMETER_HORIZON = "horizon";

	/** The parameter name for &quot;The width of the used windows.&quot; */
	public static final String PARAMETER_WINDOW_SIZE = "window_size";

	/** The parameter name for &quot;The step size of the used windows, i.e. the distance between the first values&quot; */
	public static final String PARAMETER_STEP_SIZE = "step_size";

	/** The parameter name for &quot;Indicates if the result example set should use single attributes instead of series attributes.&quot; */
	public static final String PARAMETER_CREATE_SINGLE_ATTRIBUTES = "create_single_attributes";
	
    public static final String[] SERIES_REPRESENTATIONS = {
        "encode_series_by_examples", "encode_series_by_attributes"
    };
    
    public static final int SERIES_AS_EXAMPLES   = 0;
    
    public static final int SERIES_AS_ATTRIBUTES = 1;
    
	public Series2WindowExamples(OperatorDescription description) {
		super(description);
	}

    public abstract void performChecks(ExampleSet exampleSet, int representation, int width, int stepSize, int horizon) throws OperatorException;
    
    public abstract void fillSeriesExampleTable(MemoryExampleTable table, ExampleSet exampleSet, Attribute label, int representation, int width, int stepSize, int horizon) throws OperatorException;
    
    public abstract int getNumberOfAttributes(ExampleSet exampleSet, int representation, int windowWidth);
    
	public IOObject[] apply() throws OperatorException {
		// init and sanity checks
		ExampleSet exampleSet = getInput(ExampleSet.class);
        int representation = getParameterAsInt(PARAMETER_SERIES_REPRESENTATION);
		int horizon = getParameterAsInt(PARAMETER_HORIZON) - 1;
		int windowWidth = getParameterAsInt(PARAMETER_WINDOW_SIZE);
		int stepSize = getParameterAsInt(PARAMETER_STEP_SIZE);

		performChecks(exampleSet, representation, windowWidth, stepSize, horizon);

		// determine value type
		int valueType = -1;
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (valueType < 0) {
				valueType = attribute.getValueType();
			} else {
				if (attribute.getValueType() != valueType)
					throw new UserError(this, 126); // not all attributes have the same value type
			}
		}

		// create attributes
        int numberOfResultingAttributes = getNumberOfAttributes(exampleSet, representation, windowWidth);
		List<Attribute> attributes = new ArrayList<Attribute>(numberOfResultingAttributes + 1);
		if (getParameterAsBoolean(PARAMETER_CREATE_SINGLE_ATTRIBUTES)) {
			for (int i = 0; i < numberOfResultingAttributes; i++)
				attributes.add(AttributeFactory.createAttribute("series" + i, valueType));
		} else {
			attributes.add(AttributeFactory.createAttribute("series0", valueType, Ontology.VALUE_SERIES_START));
			for (int i = 1; i < numberOfResultingAttributes - 1; i++)
				attributes.add(AttributeFactory.createAttribute("series" + i, valueType, Ontology.VALUE_SERIES));
			attributes.add(AttributeFactory.createAttribute("series" + (numberOfResultingAttributes - 1), valueType, Ontology.VALUE_SERIES_END));
		}
		Attribute label = AttributeFactory.createAttribute("label", valueType);
		attributes.add(label);

		// create example table
		MemoryExampleTable table = new MemoryExampleTable(attributes);
		fillSeriesExampleTable(table, exampleSet, label, representation, windowWidth, stepSize, horizon);

		// create example set and return result
		ExampleSet result = table.createExampleSet(label);
		return new IOObject[] { result };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeCategory(PARAMETER_SERIES_REPRESENTATION, "This parameter defines how the series values will be represented.",
                                                       SERIES_REPRESENTATIONS, SERIES_AS_ATTRIBUTES);
        type.setExpert(false);
        types.add(type);
		type = new ParameterTypeInt(PARAMETER_HORIZON, "The prediction horizon, i.e. the distance between the last window value and the value to predict.", 1, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_WINDOW_SIZE, "The width of the used windows.", 1, Integer.MAX_VALUE, 100);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeInt(PARAMETER_STEP_SIZE, "The step size of the used windows, i.e. the distance between the first values", 1, Integer.MAX_VALUE, 1));
		types.add(new ParameterTypeBoolean(PARAMETER_CREATE_SINGLE_ATTRIBUTES, "Indicates if the result example set should use single attributes instead of series attributes.", true));
		return types;
	}
}
