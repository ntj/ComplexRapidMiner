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
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;


/**
 * This operator generates TF-IDF values from the input data. The input example
 * set must contain either simple counts, which will be normalized during
 * calculation of the term frequency TF, or it already contains the calculated
 * term frequency values (in this case no normalization will be done).
 * 
 * @author Ingo Mierswa
 * @version $Id: TFIDFFilter.java,v 1.5 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class TFIDFFilter extends Operator {

	/** The parameter name for &quot;Indicates if term frequency values should be generated (must be done if input data is given as simple occurence counts).&quot; */
	public static final String PARAMETER_CALCULATE_TERM_FREQUENCIES = "calculate_term_frequencies";
	
	private static final Class[] INPUT_CLASSES = { ExampleSet.class };

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public TFIDFFilter(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);

		if (exampleSet.size() < 1)
			throw new UserError(this, 110, new Object[] { "1" });
		if (exampleSet.getAttributes().size() == 0)
			throw new UserError(this, 106, new Object[0]);
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (attribute.isNominal())
				throw new UserError(this, 104, new Object[] { getName(), attribute.getName() });
		}

		// init
		double[] termFrequencySum = new double[exampleSet.size()];
		int[] documentFrequencies = new int[exampleSet.getAttributes().size()];

		// calculate frequencies
		int exampleCounter = 0;
		Iterator<Example> reader = exampleSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			int i = 0;
			for (Attribute attribute : exampleSet.getAttributes()) {
				double value = example.getValue(attribute);
				termFrequencySum[exampleCounter] += value;
				if (value > 0)
					documentFrequencies[i]++;
				i++;
			}
			exampleCounter++;
			checkForStop();
		}

		// calculate IDF values
		double[] inverseDocumentFrequencies = new double[documentFrequencies.length];
		for (int i = 0; i < exampleSet.getAttributes().size(); i++)
			inverseDocumentFrequencies[i] = Math.log((double) exampleSet.size() / (double) documentFrequencies[i]);

		// set values
		boolean calculateTermFrequencies = getParameterAsBoolean(PARAMETER_CALCULATE_TERM_FREQUENCIES);
		exampleCounter = 0;
		reader = exampleSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			int i = 0;
			for (Attribute attribute : exampleSet.getAttributes()) { 
				double value = example.getValue(attribute);
				if (termFrequencySum[exampleCounter] == 0.0d) {
					example.setValue(attribute, 0.0d);
				} else {
					double tf = value;
					if (calculateTermFrequencies)
						tf /= termFrequencySum[exampleCounter];
					double idf = inverseDocumentFrequencies[i];
					example.setValue(attribute, (tf * idf));
				}
				i++;
			}
			exampleCounter++;
			checkForStop();
		}
		return new IOObject[] { exampleSet };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeBoolean(PARAMETER_CALCULATE_TERM_FREQUENCIES, "Indicates if term frequency values should be generated (must be done if input data is given as simple occurence counts).", true);
		type.setExpert(false);
		types.add(type);
		return types;
	}

}
