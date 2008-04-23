/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.preprocessing.discretization;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;


/**
 * An example filter that discretizes all numeric attributes in the dataset into
 * nominal attributes. This discretization is performed by equal frequency
 * binning. The number of bins is determined by a parameter, or, chooseable via
 * another parameter, by the square root of the number of examples with
 * non-missing values (calculated for every single attribute). Skips all special
 * attributes including the label.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: FrequencyDiscretizer.java,v 1.12 2006/04/14 11:42:27
 *          ingomierswa Exp $
 */
public class FrequencyDiscretizer extends Operator {


	/** The parameter name for &quot;If true, the number of bins is instead determined by the square root of the number of non-missing values.&quot; */
	public static final String PARAMETER_USE_SQRT_OF_EXAMPLES = "use_sqrt_of_examples";
    public static final String PARAMETER_NUMBER_OF_BINS = "number_of_bins";
    
	public FrequencyDiscretizer(OperatorDescription description) {
		super(description);
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = (ExampleSet)getInput(ExampleSet.class).clone();
		
		// Get and check parametervalues
		boolean useSqrt = getParameterAsBoolean(PARAMETER_USE_SQRT_OF_EXAMPLES);
		int numberOfBins = 0;
		if (!useSqrt) {
			// if not automatic sizing of bins, use parametervalue
			numberOfBins = getParameterAsInt(PARAMETER_NUMBER_OF_BINS);
			if (numberOfBins >= (exampleSet.size() - 1)) {
				throw new UserError(this, 116, PARAMETER_NUMBER_OF_BINS, "number of bins must be smaller than number of examples (here: " + exampleSet.size() + ")");
			}
		}
		
		// over all attributes
		FrequencyDiscretizerExample[] exampleAttributePairs = new FrequencyDiscretizerExample[exampleSet.size()];
		for (Attribute currentAttribute : exampleSet.getAttributes()) {
			if (!currentAttribute.isNominal()) {
				int numberOfNotMissing = 0;
				// get examples with value of current attribute and store as
				// pairs and compute the number of not missing values
				Iterator<Example> iterator = exampleSet.iterator();
				int j = 0;
				while (iterator.hasNext()) {
					Example currentExample = iterator.next();
					exampleAttributePairs[j] = new FrequencyDiscretizerExample(currentExample.getValue(currentAttribute), currentExample);
					if (!Double.isNaN(currentExample.getValue(currentAttribute))) {
						numberOfNotMissing++;
					}
					checkForStop();
					j++;
				}
				
				// sort pairs and compute number of Bins
				Arrays.sort(exampleAttributePairs);
				if (useSqrt) {
					numberOfBins = (int) Math.round(Math.sqrt(numberOfNotMissing));
				}
				
				// change attributetype of current attribute
				currentAttribute = exampleSet.getAttributes().replace(currentAttribute, AttributeFactory.changeValueType(currentAttribute, Ontology.NOMINAL));
				
				// set new nominal value
				double examplesPerBin = exampleSet.size() / (double) numberOfBins;
				double currentBinSpace = 0;
				int currentBin = 0;

				log(currentAttribute.getName() + ": start new range" + currentBin + " at " + Tools.formatNumber(exampleAttributePairs[0].getValue()));
				for (int k = 0; k < exampleAttributePairs.length; k++) {
					// change bin if full and not last
					if (currentBinSpace < 1 && currentBin < numberOfBins) {
						if (k > 0) {
							double lastValue = exampleAttributePairs[k-1].getValue();
							double thisValue = exampleAttributePairs[k].getValue();
							log(currentAttribute.getName() + ": start new range" + currentBin + " at " + Tools.formatNumber((thisValue - lastValue) / 2.0d));
						}
						currentBin++;
						currentBinSpace += examplesPerBin;
					}
					
					// set number of bin as nominal value
					Example example = exampleAttributePairs[k].getExample();
					example.setValue(currentAttribute, "range" + currentBin);
					currentBinSpace--;
					checkForStop();
				}
			}
		}
		return new IOObject[] { exampleSet };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_OF_BINS, "Defines the number of bins which should be used for each attribute.", 2, Integer.MAX_VALUE, 2);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeBoolean(PARAMETER_USE_SQRT_OF_EXAMPLES, "If true, the number of bins is instead determined by the square root of the number of non-missing values.", false);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
