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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.ListDataRowReader;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.Tools;


/**
 * Generates a random example set for testing purposes. Uses a subclass of
 * {@link TargetFunction} to create the examples from the attribute values.
 * Possible target functions are: random, sum (of all attributes), polynomial
 * (of the first three attributes, degree 3), non linear, sinus, sinus frequency
 * (like sinus, but with frequencies in the argument), random classification,
 * sum classification (like sum, but positive for positive sum and negative for
 * negative sum), interaction classification (positive for negative x or
 * positive y and negative z), sinus classification (positive for positive sinus
 * values).
 * 
 * @author Ingo Mierswa
 * @version $Id: ExampleSetGenerator.java,v 1.28 2006/04/14 07:47:17 ingomierswa
 *          Exp $
 */
public class ExampleSetGenerator extends Operator {

	/** The parameter name for &quot;Specifies the target function of this example set&quot; */
	public static final String PARAMETER_TARGET_FUNCTION = "target_function";

	/** The parameter name for &quot;The number of generated examples.&quot; */
	public static final String PARAMETER_NUMBER_EXAMPLES = "number_examples";

	/** The parameter name for &quot;The number of attributes.&quot; */
	public static final String PARAMETER_NUMBER_OF_ATTRIBUTES = "number_of_attributes";

	/** The parameter name for &quot;The minimum value for the attributes.&quot; */
	public static final String PARAMETER_ATTRIBUTES_LOWER_BOUND = "attributes_lower_bound";

	/** The parameter name for &quot;The maximum value for the attributes.&quot; */
	public static final String PARAMETER_ATTRIBUTES_UPPER_BOUND = "attributes_upper_bound";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";

	/** The parameter name for &quot;Determines, how the data is represented internally.&quot; */
	public static final String PARAMETER_DATAMANAGEMENT = "datamanagement";
	
	private static final String[] KNOWN_FUNCTION_NAMES = new String[] { 
		"random", // regression
	    "sum", 
	    "polynomial", 
	    "non linear", 
	    "one variable non linear", 
	    "complicated function", 
	    "complicated function2", 
	    "simple sinus", 
	    "sinus", 
	    "simple superposition", 
	    "sinus frequency",
	    "sinus with trend",
	    "sinc", 
	    "triangular function", 
	    "square pulse function", 
	    "random classification", // classification
		"one third classification", 
		"sum classification",
		"quadratic classification",
		"simple non linear classification", 
		"interaction classification", 
		"simple polynomial classification", 
		"polynomial classification", 
		"checkerboard classification", 
		"random dots classification", 
        "global and local models classification",
		"sinus classification",
		"multi classification",
		"two gaussians classification",
		"transactions dataset", // transactions
		"grid function", // clusters
		"three ring clusters", 
		"spiral cluster", 
		"single gaussian cluster", 
		"gaussian mixture clusters",
		"driller oscillation timeseries" // timeseries
		
	};

	private static final Class[] KNOWN_FUNCTION_IMPLEMENTATIONS = new Class[] {
		RandomFunction.class, // regression
		SumFunction.class,
		PolynomialFunction.class, 
		NonLinearFunction.class, 
		OneVariableNonLinearFunction.class, 
		ComplicatedFunction.class, 
		ComplicatedFunction2.class,
		SimpleSinusFunction.class,
		SinusFunction.class, 
		SimpleSuperpositionFunction.class,
		SinusFrequencyFunction.class,
		SinusWithTrendFunction.class,
		SincFunction.class,
		TriangularFunction.class,
		SquarePulseFunction.class,
		RandomClassificationFunction.class, // classification
		OneThirdClassification.class,
		SumClassificationFunction.class,
		QuadraticClassificationFunction.class,
		SimpleNonLinearClassificationFunction.class,
		InteractionClassificationFunction.class,
		SimplePolynomialClassificationFunction.class,
		PolynomialClassificationFunction.class,
		CheckerboardClassificationFunction.class,
		RandomDotsClassificationFunction.class,
        GlobalAndLocalPatternsFunction.class,
		SinusClassificationFunction.class,
		MultiClassificationFunction.class,
		TwoGaussiansClassificationFunction.class,
		TransactionDatasetFunction.class, // transactions
		GridFunction.class, // clusters
		RingClusteringFunction.class, 
		SpiralClusteringFunction.class,
		GaussianFunction.class,
		GaussianMixtureFunction.class,
		DrillerOscillationFunction.class // timeseries
	};

	private static final Class[] INPUT_CLASSES = new Class[0];

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	public ExampleSetGenerator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {

		// init
		int numberOfExamples = getParameterAsInt(PARAMETER_NUMBER_EXAMPLES);
		int numberOfAttributes = getParameterAsInt(PARAMETER_NUMBER_OF_ATTRIBUTES);
		double lower = getParameterAsDouble(PARAMETER_ATTRIBUTES_LOWER_BOUND);
		double upper = getParameterAsDouble(PARAMETER_ATTRIBUTES_UPPER_BOUND);
		String functionName = getParameterAsString(PARAMETER_TARGET_FUNCTION);
		if (functionName == null)
			throw new UserError(this, 205, "target_function");

		TargetFunction function = null;
		try {
			function = getFunctionForName(functionName);
		} catch (Exception e) {
			throw new UserError(this, e, 904, new Object[] { functionName, e });
		}
		function.setLowerArgumentBound(lower);
		function.setUpperArgumentBound(upper);
		function.setTotalNumberOfExamples(numberOfExamples);
		function.setTotalNumberOfAttributes(numberOfAttributes);

		// create table
		List<Attribute> attributes = new ArrayList<Attribute>();
		for (int m = 0; m < numberOfAttributes; m++)
			attributes.add(AttributeFactory.createAttribute("att" + (m + 1), Ontology.REAL));
		Attribute label = function.getLabel();
		if (label != null)
			attributes.add(label);
		MemoryExampleTable table = new MemoryExampleTable(attributes);

		// create data
        RandomGenerator random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
		List<DataRow> data = new LinkedList<DataRow>();
        DataRowFactory factory = new DataRowFactory(getParameterAsInt(PARAMETER_DATAMANAGEMENT), '.');
		try {
			function.init(random);
			for (int n = 0; n < numberOfExamples; n++) {
				double[] features = function.createArguments(numberOfAttributes, random);
				double[] example = features;
				if (label != null) {
					example = new double[numberOfAttributes + 1];
					System.arraycopy(features, 0, example, 0, features.length);
					example[example.length - 1] = function.calculate(features);
				}
                DataRow row = factory.create(example.length);
                for (int i = 0; i < example.length; i++)
                    row.set(attributes.get(i), example[i]);
                row.trim();
				data.add(row);
			}
		} catch (TargetFunction.FunctionException e) {
			throw new UserError(this, 918, e.getFunctionName(), e.getMessage());
		}

		// fill table with data
		table.readExamples(new ListDataRowReader(data.iterator()));

		// create example set and return it
		ExampleSet result = table.createExampleSet(label);

		return new IOObject[] { result };
	}

	// ================================================================================

	public static TargetFunction getFunctionForName(String functionName) 
	    throws IllegalAccessException, InstantiationException, ClassNotFoundException {
		for (int i = 0; i < KNOWN_FUNCTION_NAMES.length; i++) {
			if (KNOWN_FUNCTION_NAMES[i].equals(functionName))
				return (TargetFunction)KNOWN_FUNCTION_IMPLEMENTATIONS[i].newInstance();
		}
		Class clazz = Tools.classForName(functionName);
		return (TargetFunction) clazz.newInstance();
	}

	// ================================================================================

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeStringCategory(PARAMETER_TARGET_FUNCTION, "Specifies the target function of this example set", KNOWN_FUNCTION_NAMES);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_NUMBER_EXAMPLES, "The number of generated examples.", 1, Integer.MAX_VALUE, 100);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_NUMBER_OF_ATTRIBUTES, "The number of attributes.", 1, Integer.MAX_VALUE, 5);
		type.setExpert(false);
		types.add(type);

		types.add(new ParameterTypeDouble(PARAMETER_ATTRIBUTES_LOWER_BOUND, "The minimum value for the attributes.", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, -10));
		types.add(new ParameterTypeDouble(PARAMETER_ATTRIBUTES_UPPER_BOUND, "The maximum value for the attributes.", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 10));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
        types.add(new ParameterTypeCategory(PARAMETER_DATAMANAGEMENT, "Determines, how the data is represented internally.", DataRowFactory.TYPE_NAMES, DataRowFactory.TYPE_DOUBLE_ARRAY));
		return types;
	}

}
