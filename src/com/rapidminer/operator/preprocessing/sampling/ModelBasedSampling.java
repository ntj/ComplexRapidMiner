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
import com.rapidminer.example.Statistics;
import com.rapidminer.example.Tools;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.ListDataRowReader;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.meta.WeightedPerformanceMeasures;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;


/**
 * Sampling based on a learned model.
 * 
 * @see com.rapidminer.operator.learner.meta.BayesianBoosting
 * @author Martin Scholz, Ingo Mierswa
 * @version $Id: ModelBasedSampling.java,v 1.14 2006/04/05 08:57:27 ingomierswa
 *          Exp $
 */
public class ModelBasedSampling extends Operator {


	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	public ModelBasedSampling(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);

		Attribute weightAttr = exampleSet.getAttributes().getWeight();
		if (weightAttr == null) {
			weightAttr = Tools.createWeightAttribute(exampleSet);
			Iterator<Example> reader = exampleSet.iterator();
			while (reader.hasNext()) {
				reader.next().setValue(weightAttr, 1.0d);
			}
		}

		WeightedPerformanceMeasures wp = new WeightedPerformanceMeasures(exampleSet);
		WeightedPerformanceMeasures.reweightExamples(exampleSet, wp.getContingencyMatrix(), true);

		// recalc weight att statistics
		exampleSet.recalculateAttributeStatistics(exampleSet.getAttributes().getWeight());

		// fill new table
		Attribute[] allAttributes = exampleSet.getExampleTable().getAttributes();
		List<DataRow> dataList = new LinkedList<DataRow>();
		Iterator<Example> reader = exampleSet.iterator();
		double maxWeight = exampleSet.getStatistics(exampleSet.getAttributes().getWeight(), Statistics.MAXIMUM);
		while (reader.hasNext()) {
			Example example = reader.next();
			if (RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED)).nextDouble() > example.getValue(weightAttr) / maxWeight) {
				example.setValue(weightAttr, 1.0d);
				double[] values = new double[allAttributes.length];
				for (int i = 0; i < values.length; i++)
					values[i] = example.getValue(allAttributes[i]);
				dataList.add(new DoubleArrayDataRow(values));
			}
			checkForStop();
		}

		List<Attribute> attributes = Arrays.asList(allAttributes);
		ExampleTable exampleTable = new MemoryExampleTable(attributes, new ListDataRowReader(dataList.iterator()));

		// regular attributes.
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
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
        return types;
    }
}
