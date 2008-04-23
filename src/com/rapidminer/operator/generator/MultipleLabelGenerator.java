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
package com.rapidminer.operator.generator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.ListDataRowReader;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;


/**
 * Generates a random example set for testing purposes with more than one label.
 * 
 * @author Ingo Mierswa
 * @version $Id: MultipleLabelGenerator.java,v 1.10 2006/03/27 13:22:00
 *          ingomierswa Exp $
 */
public class MultipleLabelGenerator extends Operator {


	/** The parameter name for &quot;The number of generated examples.&quot; */
	public static final String PARAMETER_NUMBER_EXAMPLES = "number_examples";

	/** The parameter name for &quot;Defines if multiple labels for regression tasks should be generated.&quot; */
	public static final String PARAMETER_REGRESSION = "regression";

	/** The parameter name for &quot;The minimum value for the attributes.&quot; */
	public static final String PARAMETER_ATTRIBUTES_LOWER_BOUND = "attributes_lower_bound";

	/** The parameter name for &quot;The maximum value for the attributes.&quot; */
	public static final String PARAMETER_ATTRIBUTES_UPPER_BOUND = "attributes_upper_bound";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	private static final int NUMBER_OF_ATTRIBUTES = 5;

	public MultipleLabelGenerator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {

		// init
		int numberOfExamples = getParameterAsInt(PARAMETER_NUMBER_EXAMPLES);
		double lower = getParameterAsDouble(PARAMETER_ATTRIBUTES_LOWER_BOUND);
		double upper = getParameterAsDouble(PARAMETER_ATTRIBUTES_UPPER_BOUND);

		// create table
		List<Attribute> attributes = new LinkedList<Attribute>();
		for (int m = 0; m < NUMBER_OF_ATTRIBUTES; m++)
			attributes.add(AttributeFactory.createAttribute("att" + (m + 1), Ontology.REAL));

		// generate labels
		int type = Ontology.NOMINAL;
		if (getParameterAsBoolean(PARAMETER_REGRESSION)) {
			type = Ontology.REAL;
		}
		Attribute label1 = AttributeFactory.createAttribute("label1", type);
		attributes.add(label1);
		Attribute label2 = AttributeFactory.createAttribute("label2", type);
		attributes.add(label2);
		Attribute label3 = AttributeFactory.createAttribute("label3", type);
		attributes.add(label3);

		if (!getParameterAsBoolean(PARAMETER_REGRESSION)) {
			label1.getMapping().mapString("positive");
			label1.getMapping().mapString("negative");
			label2.getMapping().mapString("positive");
			label2.getMapping().mapString("negative");
			label3.getMapping().mapString("positive");
			label3.getMapping().mapString("negative");
		}

		MemoryExampleTable table = new MemoryExampleTable(attributes);

		// create data
        RandomGenerator random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
		List<DataRow> data = new LinkedList<DataRow>();
		for (int n = 0; n < numberOfExamples; n++) {
			double[] features = new double[NUMBER_OF_ATTRIBUTES];
			for (int i = 0; i < features.length; i++)
				features[i] = random.nextDoubleInRange(lower, upper);

			double[] example = new double[NUMBER_OF_ATTRIBUTES + 3];
			System.arraycopy(features, 0, example, 0, features.length);
			if (getParameterAsBoolean(PARAMETER_REGRESSION)) {
				example[example.length - 3] = example[0] + example[1] + example[2];
				example[example.length - 2] = 2 * example[0] + example[3];
				example[example.length - 1] = example[3] * example[3];
			} else {
				example[example.length - 3] = example[0] + example[1] + example[2] > 0 ? label1.getMapping().mapString("positive") : label1.getMapping().mapString("negative");
				example[example.length - 2] = 2 * example[0] + example[3] > 0 ? label1.getMapping().mapString("positive") : label1.getMapping().mapString("negative");
				example[example.length - 1] = example[3] * example[3] - example[2] * example[2] > 0 ? label1.getMapping().mapString("positive") : label1.getMapping().mapString("negative");
			}
			data.add(new DoubleArrayDataRow(example));
		}

		// fill table with data
		table.readExamples(new ListDataRowReader(data.iterator()));

		// create example set and return it
		Map<Attribute, String> specialMap = new HashMap<Attribute, String>();
		specialMap.put(label1, "label1");
		specialMap.put(label2, "label2");
		specialMap.put(label3, "label3");
		ExampleSet result = table.createExampleSet(specialMap);

		return new IOObject[] { result };
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_EXAMPLES, "The number of generated examples.", 1, Integer.MAX_VALUE, 100);
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeBoolean(PARAMETER_REGRESSION, "Defines if multiple labels for regression tasks should be generated.", false);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeDouble(PARAMETER_ATTRIBUTES_LOWER_BOUND, "The minimum value for the attributes.", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, -10));
		types.add(new ParameterTypeDouble(PARAMETER_ATTRIBUTES_UPPER_BOUND, "The maximum value for the attributes.", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 10));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
