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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
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
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;


/**
 * Absolute sampling operator. This operator takes a random sample with the
 * given size. For example, if the sample size is set to 50, the result will
 * have exactly 50 examples randomly drawn from the complete data set. Please
 * note that this operator does not sample during a data scan but jumps to the
 * rows. It should therefore only be used in case of memory data management and
 * not, for example, for database or file management.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbsoluteSampling.java,v 1.5 2008/05/09 19:23:16 ingomierswa Exp $
 */
public class AbsoluteSampling extends Operator {

	/** The parameter name for &quot;The number of examples which should be sampled&quot; */
	public static final String PARAMETER_SAMPLE_SIZE = "sample_size";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	public AbsoluteSampling(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		int size = getParameterAsInt(PARAMETER_SAMPLE_SIZE);

		if (size > exampleSet.size()) 
			throw new UserError(this, 110, size);
		
		// fill new table
		List<Integer> indices = new ArrayList<Integer>(exampleSet.size());
		for (int i = 0; i < exampleSet.size(); i++)
			indices.add(i);
		RandomGenerator random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
		List<DataRow> dataList = new LinkedList<DataRow>();
		for (int i = 0; i < size; i++) {
			int index = indices.remove(random.nextInt(indices.size()));
			dataList.add(exampleSet.getExample(index).getDataRow());
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

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_SAMPLE_SIZE, "The number of examples which should be sampled", 1, Integer.MAX_VALUE, 100);
		type.setExpert(false);
		types.add(type);
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
