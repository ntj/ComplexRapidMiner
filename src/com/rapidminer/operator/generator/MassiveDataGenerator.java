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

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.DoubleSparseArrayDataRow;
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
 * Generates huge amounts of data in either sparse or dense format. This
 * operator can be used to check if huge amounts of data can be handled by RapidMiner
 * for a given process setup without creating the correct format / writing
 * special purpose input operators.
 * 
 * @author Ingo Mierswa
 * @version $Id: MassiveDataGenerator.java,v 1.9 2006/03/27 13:22:00 ingomierswa
 *          Exp $
 */
public class MassiveDataGenerator extends Operator {


	/** The parameter name for &quot;The number of generated examples.&quot; */
	public static final String PARAMETER_NUMBER_EXAMPLES = "number_examples";

	/** The parameter name for &quot;The number of attributes.&quot; */
	public static final String PARAMETER_NUMBER_ATTRIBUTES = "number_attributes";

	/** The parameter name for &quot;The fraction of default attributes.&quot; */
	public static final String PARAMETER_SPARSE_FRACTION = "sparse_fraction";

	/** The parameter name for &quot;Indicates if the example should be internally represented in a sparse format.&quot; */
	public static final String PARAMETER_SPARSE_REPRESENTATION = "sparse_representation";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	public MassiveDataGenerator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {

		// init
		int numberOfExamples = getParameterAsInt(PARAMETER_NUMBER_EXAMPLES);
		int numberOfAttributes = getParameterAsInt(PARAMETER_NUMBER_ATTRIBUTES);
		double sparseFraction = getParameterAsDouble(PARAMETER_SPARSE_FRACTION);
		boolean sparseRepresentation = getParameterAsBoolean(PARAMETER_SPARSE_REPRESENTATION);

		// create table
		List<Attribute> attributes = new ArrayList<Attribute>();
		for (int m = 0; m < numberOfAttributes; m++)
			attributes.add(AttributeFactory.createAttribute("att" + (m + 1), Ontology.REAL));
		Attribute label = AttributeFactory.createAttribute("label", Ontology.NOMINAL);
		label.getMapping().mapString("positive");
		label.getMapping().mapString("negative");
		attributes.add(label);
		MemoryExampleTable table = new MemoryExampleTable(attributes);

		// create data
        RandomGenerator random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
		for (int n = 0; n < numberOfExamples; n++) {
			int counter = 0;
			if (sparseRepresentation) {
				DoubleSparseArrayDataRow dataRow = new DoubleSparseArrayDataRow(numberOfAttributes + 1);
				for (int i = 0; i < numberOfAttributes; i++) {
					double value = random.nextDouble() > sparseFraction ? 1.0d : 0.0d;
					dataRow.set(attributes.get(i), value);
					if (value == 0.0d)
						counter++;
				}
				if (counter < (sparseFraction * numberOfAttributes))
					dataRow.set(label, label.getMapping().mapString("positive"));
				else
					dataRow.set(label, label.getMapping().mapString("negative"));
				dataRow.trim();
				table.addDataRow(dataRow);
			} else {
				double[] dataRow = new double[numberOfAttributes + 1];
				for (int i = 0; i < numberOfAttributes; i++) {
					double value = random.nextDouble() > sparseFraction ? 1.0d : 0.0d;
					dataRow[i] = value;
					if (value == 0.0d)
						counter++;
				}
				if (counter < (sparseFraction * numberOfAttributes))
					dataRow[dataRow.length - 1] = label.getMapping().mapString("positive");
				else
					dataRow[dataRow.length - 1] = label.getMapping().mapString("negative");
				table.addDataRow(new DoubleArrayDataRow(dataRow));
			}
		}

		// create example set and return it
		ExampleSet result = table.createExampleSet(label);

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
		ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_EXAMPLES, "The number of generated examples.", 0, Integer.MAX_VALUE, 10000);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_NUMBER_ATTRIBUTES, "The number of attributes.", 0, Integer.MAX_VALUE, 10000);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeDouble(PARAMETER_SPARSE_FRACTION, "The fraction of default attributes.", 0.0d, 1.0d, 0.99d));
		types.add(new ParameterTypeBoolean(PARAMETER_SPARSE_REPRESENTATION, "Indicates if the example should be internally represented in a sparse format.", true));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
