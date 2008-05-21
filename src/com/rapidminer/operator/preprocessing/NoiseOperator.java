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
package com.rapidminer.operator.preprocessing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;


/**
 * This operator adds random attributes and white noise to the data. New random
 * attributes are simply filled with random data which is not correlated to the
 * label at all. Additionally, this operator might add noise to the label
 * attribute or to the regular attributes. In case of a numerical label the
 * given <code>label_noise</code> is the percentage of the label range which
 * defines the standard deviation of normal distributed noise which is added to
 * the label attribute. For nominal labels the parameter
 * <code>label_noise</code> defines the probability to randomly change the
 * nominal label value. In case of adding noise to regular attributes the
 * parameter <code>default_attribute_noise</code> simply defines the standard
 * deviation of normal distributed noise without using the attribute value
 * range. Using the parameter list it is possible to set different noise levels
 * for different attributes. However, it is not possible to add noise to nominal
 * attributes.
 * 
 * @author Ingo Mierswa
 * @version $Id: NoiseOperator.java,v 1.5 2008/05/09 19:22:54 ingomierswa Exp $
 */
public class NoiseOperator extends Operator {


	/** The parameter name for &quot;Adds this number of random attributes.&quot; */
	public static final String PARAMETER_RANDOM_ATTRIBUTES = "random_attributes";

	/** The parameter name for &quot;Add this percentage of a numerical label range as a normal distributed noise or probability for a nominal label change.&quot; */
	public static final String PARAMETER_LABEL_NOISE = "label_noise";

	/** The parameter name for &quot;The standard deviation of the default attribute noise.&quot; */
	public static final String PARAMETER_DEFAULT_ATTRIBUTE_NOISE = "default_attribute_noise";

	/** The parameter name for &quot;List of noises for each attributes.&quot; */
	public static final String PARAMETER_NOISE = "noise";

	/** The parameter name for &quot;Offset added to the values of each random attribute&quot; */
	public static final String PARAMETER_OFFSET = "offset";

	/** The parameter name for &quot;Linear factor multiplicated with the values of each random attribute&quot; */
	public static final String PARAMETER_LINEAR_FACTOR = "linear_factor";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	private static final Class[] INPUT_CLASSES = { ExampleSet.class };

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public NoiseOperator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		exampleSet.recalculateAllAttributeStatistics();

        RandomGenerator random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
		// read noise values from list
		Map<String, Double> noiseMap = new HashMap<String, Double>();
		List noises = getParameterList(PARAMETER_NOISE);
		Iterator i = noises.iterator();
		while (i.hasNext()) {
			Object[] pair = (Object[]) i.next();
			noiseMap.put((String) pair[0], (Double) pair[1]);
		}

		// add noise to existing attributes
		double defaultAttributeNoise = getParameterAsDouble(PARAMETER_DEFAULT_ATTRIBUTE_NOISE);
		double labelNoise = getParameterAsDouble(PARAMETER_LABEL_NOISE);
		Iterator<Example> reader = exampleSet.iterator();
		Attribute label = exampleSet.getAttributes().getLabel();
		while (reader.hasNext()) {
			Example example = reader.next();
			// attribute noise
			for (Attribute attribute : exampleSet.getAttributes()) {
				if (!attribute.isNominal()) {
					Double noiseObject = noiseMap.get(attribute.getName());
					double noise = noiseObject == null ? defaultAttributeNoise : noiseObject.doubleValue();
					double noiseValue = random.nextGaussian() * noise;
					example.setValue(attribute, example.getValue(attribute) + noiseValue);
				}
			}
			// label noise
			if (label != null) {
				if (!label.isNominal()) {
				    double min = exampleSet.getStatistics(label, Statistics.MINIMUM);
                    double max = exampleSet.getStatistics(label, Statistics.MAXIMUM);
					double labelRange = Math.abs(max - min);
					double noiseValue = random.nextGaussian() * labelNoise * labelRange;
					example.setValue(label, example.getValue(label) + noiseValue);
				} else if (label.isNominal() && (label.getMapping().size() >= 2)) {
					if (random.nextDouble() < labelNoise) {
						int oldValue = (int) example.getValue(label);
						int newValue = oldValue;
						while (newValue == oldValue) {
							newValue = random.nextInt(label.getMapping().size());
						}
						example.setValue(label, newValue);
					}
				}
			}
			checkForStop();
		}

		// add new noise attributes
		int numberOfNewAttributes = getParameterAsInt(PARAMETER_RANDOM_ATTRIBUTES);
		double offset = getParameterAsDouble(PARAMETER_OFFSET);
		double linearFactor = getParameterAsDouble(PARAMETER_LINEAR_FACTOR);

		List<Attribute> newAttributes = new LinkedList<Attribute>();
		for (int j = 0; j < numberOfNewAttributes; j++) {
			Attribute newAttribute = AttributeFactory.createAttribute(AttributeFactory.createName("random"), Ontology.REAL); 
			newAttributes.add(newAttribute);
			exampleSet.getExampleTable().addAttribute(newAttribute);
			exampleSet.getAttributes().addRegular(newAttribute);
		}
		
		reader = exampleSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			i = newAttributes.iterator();
			while (i.hasNext()) {
				example.setValue((Attribute) i.next(), offset + linearFactor * random.nextGaussian());
			}
			checkForStop();
		}

		return new IOObject[] { exampleSet };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_RANDOM_ATTRIBUTES, "Adds this number of random attributes.", 0, Integer.MAX_VALUE, 0);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_LABEL_NOISE, "Add this percentage of a numerical label range as a normal distributed noise or probability for a nominal label change.", 0.0d, Double.POSITIVE_INFINITY, 0.05d);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeDouble(PARAMETER_DEFAULT_ATTRIBUTE_NOISE, "The standard deviation of the default attribute noise.", 0.0d, Double.POSITIVE_INFINITY, 0.0d));
		types.add(new ParameterTypeList(PARAMETER_NOISE, "List of noises for each attributes.", new ParameterTypeDouble(PARAMETER_NOISE, "Names of attributes and noises to use.", 0.0d, Double.POSITIVE_INFINITY, 0.05d)));
		type = new ParameterTypeDouble(PARAMETER_OFFSET, "Offset added to the values of each random attribute", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_LINEAR_FACTOR, "Linear factor multiplicated with the values of each random attribute", 0.0d, Double.POSITIVE_INFINITY, 1.0d);
		types.add(type);
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
		return types;
	}

}
