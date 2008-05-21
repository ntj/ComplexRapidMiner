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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;


/**
 * <p>This operator transforms a given example set containing series data into a
 * new example set containing single valued examples. For this purpose, windows with
 * a specified window and step size are moved across the series and the series
 * value lying horizon values after the window end is used as label which should
 * be predicted. This operator can only be used for univariate series prediction. 
 * For the multivariate case, please use the operator
 * {@link com.rapidminer.operator.preprocessing.filter.MultivariateSeries2WindowExamples}.</p>
 * 
 * <p>
 * The series data must be given as ExampleSet. The parameter &quot;series_representation&quot;
 * defines how the series data is represented by the ExampleSet:</p>
 * <ul>
 * <li>encode_series_by_examples</li>: the series index variable (e.g. time) is encoded by the examples, 
 * i.e. there is a <em>single</em> attribute and a set of examples. Each example encodes the value for a new time point.
 * <li>encode_series_by_attributes</li>: the series index variable (e.g. time) is encoded by the attributes, 
 * i.e. there is a (set of) examples and a set of attributes. Each attribute value encodes the value for a 
 * new time point. If there is more than one example, the windowing is performed for each example independently
 * and all resulting window examples are merged into a complete example set.
 * </ul>
 * 
 * <p>Please note that the encoding as examples is usually more efficient with respect to the
 * memory usage. To ensure backward compatibility, the default representation is, however, set 
 * to time_as_attributes.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: UnivariateSeries2WindowExamples.java,v 1.3 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class UnivariateSeries2WindowExamples extends Series2WindowExamples {
    
    public UnivariateSeries2WindowExamples(OperatorDescription description) {
        super(description);
    }

    public int getNumberOfAttributes(ExampleSet exampleSet, int representation, int windowWidth) {
        return windowWidth;
    }
    
    public void performChecks(ExampleSet exampleSet, int representation, int windowWidth, int stepSize, int horizon) throws OperatorException {
        if (representation == SERIES_AS_EXAMPLES) {
            if (exampleSet.getAttributes().size() != 1) {
                throw new UserError(this, 133, Integer.valueOf(1), Integer.valueOf(exampleSet.getAttributes().size()));
            }
            if (exampleSet.size() < windowWidth + horizon) {
                // not enough examples
                throw new UserError(this, 110, "window width + horizon = " + (windowWidth + horizon));
            }
        } else {
            if (exampleSet.size() != 1) {
                logWarning("The sliding windows will be applied on each example of the example set, if you intended to perform a multivariate series2examples transformation, please use the corresponding operator");
            }
            if (exampleSet.getAttributes().size() < windowWidth + horizon) {
                // not enough attributes
                throw new UserError(this, 125, Integer.valueOf(exampleSet.getAttributes().size()), Integer.valueOf(windowWidth + horizon));
            }
        }
    }
    
    public void fillSeriesExampleTable(MemoryExampleTable table, ExampleSet exampleSet, Attribute label, int representation, int windowWidth, int stepSize, int horizon) 
        throws OperatorException {
        if (representation == SERIES_AS_EXAMPLES) {
            Attribute seriesAttribute = exampleSet.getAttributes().iterator().next();
            for (int w = 0; w < exampleSet.size() - windowWidth - horizon; w+=stepSize) {
                double[] data = new double[windowWidth + 1];
                for (int d = 0; d < data.length - 1; d++) {
                    data[d] = exampleSet.getExample(w + d).getValue(seriesAttribute);
                }
                data[data.length - 1] = exampleSet.getExample(w + windowWidth + horizon).getValue(seriesAttribute);
                table.addDataRow(new DoubleArrayDataRow(data));
                checkForStop();
            }
        } else {
        	Attribute[] attributeArray = exampleSet.getAttributes().createRegularAttributeArray();
            int lastAttribute = exampleSet.getAttributes().size() - windowWidth - horizon;
            for (Example example : exampleSet) {
                for (int w = 0; w < lastAttribute; w += stepSize) {
                    double[] data = new double[windowWidth + 1];
                    for (int d = 0; d < data.length - 1; d++) {
                        data[d] = example.getValue(attributeArray[w + d]);
                    }
                    data[data.length - 1] = example.getValue(attributeArray[w + windowWidth + horizon]);
                    table.addDataRow(new DoubleArrayDataRow(data));
                    checkForStop();
                }
            }
        }
    }
}
