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
package com.rapidminer.operator.preprocessing.sampling;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.ListDataRowReader;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.RandomGenerator;


/**
 * Simple sampling operator. This operator performs a random sampling of a given
 * fraction. For example, if the input example set contains 5000 examples and
 * the sample ratio is set to 0.1, the result will have approximately 500
 * examples.
 * 
 * @author Ingo Mierswa
 * @version $Id: SimpleSampling.java,v 1.4 2008/05/09 19:23:16 ingomierswa Exp $
 */
public class SimpleSampling extends Operator {


	/** The parameter name for &quot;The fraction of examples which should be sampled&quot; */
	public static final String PARAMETER_SAMPLE_RATIO = "sample_ratio";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	private double fraction = 0.1d;

	public SimpleSampling(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		this.fraction = getParameterAsDouble(PARAMETER_SAMPLE_RATIO);

		// fill new table
		List<DataRow> dataList = new LinkedList<DataRow>();
		Iterator<Example> reader = exampleSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			if (accept(example)) {
				dataList.add(example.getDataRow());
			}
			checkForStop();
		}

		List<Attribute> attributes = Arrays.asList(exampleSet.getExampleTable().getAttributes());
		ExampleTable exampleTable = new MemoryExampleTable(attributes, new ListDataRowReader(dataList.iterator()));

		// regular attributes
		List<Attribute> regularAttributes = new LinkedList<Attribute>();
		for (Attribute attribute : exampleSet.getAttributes()) {
			regularAttributes.add(attribute);
		}

		// special attributes
		ExampleSet result = new SimpleExampleSet(exampleTable, regularAttributes);
		Iterator<AttributeRole> special = exampleSet.getAttributes().specialAttributes();
		while (special.hasNext()) {
			AttributeRole role = special.next();
			result.getAttributes().setSpecialAttribute(role.getAttribute(), role.getSpecialName());
		}

		return new IOObject[] { result };
	}

	protected boolean accept(Example example) throws UndefinedParameterError {
		return RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED)).nextDouble() < fraction;
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_SAMPLE_RATIO, "The fraction of examples which should be sampled", 0.0d, 1.0d, 0.1d);
		type.setExpert(false);
		types.add(type);
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
