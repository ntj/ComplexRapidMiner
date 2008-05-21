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
package com.rapidminer.operator.generator;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;


/**
 * Generates a random example set for testing purposes. All attributes have only
 * (random) nominal values and a classification label.
 * 
 * @author Ingo Mierswa
 * @version $Id: NominalExampleSetGenerator.java,v 1.9 2006/04/05 08:57:24
 *          ingomierswa Exp $
 */
public class NominalExampleSetGenerator extends Operator {


	/** The parameter name for &quot;The number of generated examples.&quot; */
	public static final String PARAMETER_NUMBER_EXAMPLES = "number_examples";

	/** The parameter name for &quot;The number of attributes.&quot; */
	public static final String PARAMETER_NUMBER_OF_ATTRIBUTES = "number_of_attributes";

	/** The parameter name for &quot;The number of nominal values for each attribute.&quot; */
	public static final String PARAMETER_NUMBER_OF_VALUES = "number_of_values";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	private static final Class[] INPUT_CLASSES = new Class[0];

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	public NominalExampleSetGenerator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {

		// init
		int numberOfExamples = getParameterAsInt(PARAMETER_NUMBER_EXAMPLES);
		int numberOfAttributes = getParameterAsInt(PARAMETER_NUMBER_OF_ATTRIBUTES);
		int numberOfValues = getParameterAsInt(PARAMETER_NUMBER_OF_VALUES);

		if (numberOfValues < 2) {
			logWarning("Less than 2 different values used, change to '2'.");
			numberOfValues = 2;
		}
		
		// create table
		List<Attribute> attributes = new LinkedList<Attribute>();
		for (int m = 0; m < numberOfAttributes; m++) {
			Attribute current = AttributeFactory.createAttribute("att" + (m + 1), Ontology.NOMINAL);
			for (int v = 0; v < numberOfValues; v++)
				current.getMapping().mapString("value" + v);
			attributes.add(current);
		}
		Attribute label = AttributeFactory.createAttribute("label", Ontology.NOMINAL);
		label.getMapping().mapString("negative");
		label.getMapping().mapString("positive");
		attributes.add(label);

		MemoryExampleTable table = new MemoryExampleTable(attributes);

		// create data
        RandomGenerator random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
		for (int n = 0; n < numberOfExamples; n++) {
			double[] features = new double[numberOfAttributes];
			for (int a = 0; a < features.length; a++)
				features[a] = random.nextIntInRange(0, numberOfValues - 1);
			double[] example = features;
			if (label != null) {
				example = new double[numberOfAttributes + 1];
				System.arraycopy(features, 0, example, 0, features.length);
				if (features.length >= 2) {
					example[example.length - 1] = ((features[0] == 0) || features[1] == 0) ? label.getMapping().mapString("positive") : label.getMapping().mapString("negative");
				} else if (features.length == 1) {
					example[example.length - 1] = (features[0] == 0) ? label.getMapping().mapString("positive") : label.getMapping().mapString("negative");
				} else {
					example[example.length - 1] = label.getMapping().mapString("positive");
				}
			}
			table.addDataRow(new DoubleArrayDataRow(example));
		}

		// create example set and return it
		return new IOObject[] { table.createExampleSet(label) };
	}

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_EXAMPLES, "The number of generated examples.", 1, Integer.MAX_VALUE, 100);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_NUMBER_OF_ATTRIBUTES, "The number of attributes.", 0, Integer.MAX_VALUE, 5);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_NUMBER_OF_VALUES, "The number of nominal values for each attribute.", 0, Integer.MAX_VALUE, 5);
		type.setExpert(false);
		types.add(type);
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
