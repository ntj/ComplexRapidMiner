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

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * <p>This operator transforms a given example set containing series data into a
 * new example set containing single valued examples. For this purpose, windows with
 * a specified window and step size are moved across the series and the attribute
 * value lying horizon values after the window end is used as label which should
 * be predicted. In contrast to the {@link Series2WindowExamples} operator, this operator
 * can also handle multivariate series data. In order to specify the dimension which should
 * be predicted, one must use the parameter &quot;label_dimension&quot; (counting starts at
 * 0). If you want to predict all dimensions of your multivariate series you 
 * must setup several process definitions with different label dimensions, one for each dimension.</p>
 * 
 * <p>
 * The series data must be given as ExampleSet. The parameter &quot;series_representation&quot;
 * defines how the series data is represented by the ExampleSet:</p>
 * <ul>
 * <li>encode_series_by_examples</li>: the series index variable (e.g. time) is encoded by the 
 * examples, i.e. there is a set of attributes (one for each dimension of the multivariate 
 * series) and a set of examples. Each example encodes the value vector for a new time point, 
 * each attribute value represents another dimension of the multivariate series.
 * <li>encode_series_by_attributes</li>: the series index variable (e.g. time) is encoded by 
 * the attributes, i.e. there is a set of examples (one for each dimension of the multivariate 
 * series) and a set of attributes. The set of attribute values for all examples encodes the 
 * value vector for a new time point, each example represents another dimension of the 
 * multivariate series.
 * </ul>
 * 
 * <p>Please note that the encoding as examples is usually more efficient with respect to the
 * memory usage.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: Series2WindowExamples.java,v 1.3 2006/04/05 08:57:27
 *          ingomierswa Exp $
 */
public class MultivariateSeries2WindowExamples extends Series2WindowExamples {
    

	/** The parameter name for &quot;The dimension which should be used for creating the label values (counting starts with 0).&quot; */
	public static final String PARAMETER_LABEL_DIMENSION = "label_dimension";
    public MultivariateSeries2WindowExamples(OperatorDescription description) {
        super(description);
    }

    public int getNumberOfAttributes(ExampleSet exampleSet, int representation, int windowWidth) {
        if (representation == SERIES_AS_EXAMPLES) {
            return windowWidth * exampleSet.getAttributes().size();
        } else {
            return windowWidth * exampleSet.size();
        }
    }
    
    public void performChecks(ExampleSet exampleSet, int representation, int windowWidth, int stepSize, int horizon) throws OperatorException {
        if (representation == SERIES_AS_EXAMPLES) {
            if (exampleSet.size() < windowWidth + horizon) {
                // not enough examples
                throw new UserError(this, 110, "window width + horizon = " + (windowWidth + horizon));
            }
        } else {
            if (exampleSet.getAttributes().size() < windowWidth + horizon) {
                // not enough attributes
                throw new UserError(this, 125, exampleSet.getAttributes().size(), (windowWidth + horizon));
            }
        }
    }
    
    public void fillSeriesExampleTable(MemoryExampleTable table, ExampleSet exampleSet, Attribute label, int representation, int windowWidth, int stepSize, int horizon) throws OperatorException {
        int labelDimension = getParameterAsInt(PARAMETER_LABEL_DIMENSION);
        if (representation == SERIES_AS_EXAMPLES) {
        	for (int w = 0; w < exampleSet.size() - windowWidth - horizon; w+=stepSize) { 
        		double[] data = new double[windowWidth * exampleSet.getAttributes().size() + 1]; 
        		int a = 0; 
        		for (Attribute currentAttribute : exampleSet.getAttributes()) { 
        			for (int d = 0; d < windowWidth; d++) { 
        				data[a * windowWidth + d] = exampleSet.getExample(w + d).getValue(currentAttribute); 
        			} 
        			if (a == labelDimension) 
        				data[data.length - 1] = exampleSet.getExample(w + windowWidth + 
        						horizon).getValue(currentAttribute); 
        			a++; 
        		} 
        		table.addDataRow(new DoubleArrayDataRow(data)); 
        		checkForStop(); 
        	} 
        } else {
            int lastAttribute = exampleSet.getAttributes().size() - windowWidth - horizon;
            Attribute[] attributeArray = new Attribute[exampleSet.getAttributes().size()];
            int index = 0;
            for (Attribute attribute : exampleSet.getAttributes()) {
            	attributeArray[index++] = attribute;
            }
            for (int w = 0; w < lastAttribute; w += stepSize) {
                double[] data = new double[windowWidth * exampleSet.size() + 1];
                int counter = 0;
                Iterator<Example> reader = exampleSet.iterator();
                while (reader.hasNext()) {
                    Example example = reader.next();
                    for (int d = 0; d < windowWidth; d++) {
                        data[counter * windowWidth + d] = example.getValue(attributeArray[w + d]);
                    }
                    if (counter == labelDimension) {
                        data[data.length - 1] = example.getValue(attributeArray[w + windowWidth + horizon]);
                    }
                    counter++;
                }
                table.addDataRow(new DoubleArrayDataRow(data));
                checkForStop();
            }
        }
    }
    
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeInt(PARAMETER_LABEL_DIMENSION, "The dimension which should be used for creating the label values (counting starts with 0).", 0, Integer.MAX_VALUE, false);
        type.setExpert(false);
        types.add(type);
        return types;
    }
}
