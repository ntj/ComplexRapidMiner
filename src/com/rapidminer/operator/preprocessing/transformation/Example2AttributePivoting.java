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
package com.rapidminer.operator.preprocessing.transformation;

import java.util.List;
import java.util.Collections;
import java.util.Vector;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.function.AbstractAggregationFunction;
import com.rapidminer.tools.math.function.AggregationFunction;


/**
 * <p>Transforms an example set by grouping multiple examples of single groups
 * into single examples. The parameter <i>group_attribute</i> specifies an
 * attribute which identifies examples belonging to the groups. The parameter
 * <i>index_attribute</i> specifies an attribute whose values are used to
 * identify the examples inside the groups. The values of this attributes are
 * used to name the group attributes which are created during the pivoting.
 * Typically the values of such an attribute capture subgroups or dates.
 * If the source example set contains example weights, these weights may be
 * aggregated in each group to maintain the weightings among groups.</p>
 * 
 * @author Tobias Malbrecht
 * @version $Id: Example2AttributePivoting.java,v 1.2 2008/08/20 11:09:50 tobiasmalbrecht Exp $
 */
public class Example2AttributePivoting extends ExampleSetTransformationOperator {

	public static final String PARAMETER_GROUP_ATTRIBUTE = "group_attribute";

	public static final String PARAMETER_INDEX_ATTRIBUTE = "index_attribute";
	
	public static final String PARAMETER_CONSIDER_WEIGHTS = "consider_weights";
	
	public static final String PARAMETER_WEIGHT_AGGREGATION = "weight_aggregation";
	
	public Example2AttributePivoting(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet sourceExampleSet = getInput(ExampleSet.class);
		String groupAttributeName = getParameterAsString(PARAMETER_GROUP_ATTRIBUTE);
		String indexAttributeName = getParameterAsString(PARAMETER_INDEX_ATTRIBUTE);
		boolean considerWeights = getParameterAsBoolean(PARAMETER_CONSIDER_WEIGHTS);
		int weightAggregationFunctionIndex = getParameterAsInt(PARAMETER_WEIGHT_AGGREGATION);

		Attribute groupAttribute = sourceExampleSet.getAttributes().get(groupAttributeName);
		if (groupAttribute == null) {
    		throw new UserError(this, 111, groupAttributeName);
		}

		Attribute indexAttribute = sourceExampleSet.getAttributes().get(indexAttributeName);
		if (indexAttribute == null) {
    		throw new UserError(this, 111, indexAttributeName);
		}
		
		Attribute weightAttribute = sourceExampleSet.getAttributes().getWeight();
		
		SortedExampleSet exampleSet = new SortedExampleSet(sourceExampleSet, groupAttribute, SortedExampleSet.INCREASING);
		// identify static or dynamic attributes and record index values
		List<String> indexValues = new Vector<String>();
		Attribute[] attributes = exampleSet.getAttributes().createRegularAttributeArray();
		boolean[] constantAttributeValues = new boolean[attributes.length];
		for (int i = 0; i < constantAttributeValues.length; i++) {
			constantAttributeValues[i] = true;
		}
		Example lastExample = null;
		for (Example example : exampleSet) {
			if (lastExample != null) {
				if (lastExample.getValue(groupAttribute) == example.getValue(groupAttribute)) {
					for (int i = 0; i < attributes.length; i++) {
						Attribute attribute = attributes[i];
						if (Double.isNaN(lastExample.getValue(attribute)) && Double.isNaN(example.getValue(attribute))) {
							continue;
						}
						if (lastExample.getValue(attribute) != example.getValue(attribute)) {
							constantAttributeValues[i] = false;
							continue;
						}
					}
				}
			}
			String indexValue = example.getValueAsString(indexAttribute);
			if (!indexValues.contains(indexValue)) {
				indexValues.add(indexValue);
			}
			lastExample = example;
		}
		if (!indexAttribute.isNominal()) {
			Collections.sort(indexValues);
		}
		List<String> attributeNames = new Vector<String>();
		List<Attribute> newAttributes = new Vector<Attribute>();
		Attribute newWeightAttribute = null;
		if (weightAttribute != null && considerWeights) {
			newWeightAttribute = AttributeFactory.createAttribute(weightAttribute.getName(), Ontology.REAL);
			newAttributes.add(newWeightAttribute);
			attributeNames.add(newWeightAttribute.getName());
		}
		for (int i = 0; i < attributes.length; i++) {
			Attribute attribute = attributes[i];
			if (!attribute.equals(indexAttribute)) { 
				if (constantAttributeValues[i]) {
					newAttributes.add(AttributeFactory.createAttribute(attribute.getName(), attribute.getValueType()));
					attributeNames.add(attribute.getName());
				} else {
					for (String indexValue : indexValues) {
						String newAttributeName = attribute.getName() + "_" + indexValue;
						newAttributes.add(AttributeFactory.createAttribute(newAttributeName, attribute.getValueType()));
						attributeNames.add(newAttributeName);
					}
				}
			}
		}
		

		MemoryExampleTable table = new MemoryExampleTable(newAttributes);
		AggregationFunction aggregationFunction = null;
		if (newWeightAttribute != null && considerWeights) {
			try {
				aggregationFunction = AbstractAggregationFunction.createAggregationFunction(weightAggregationFunctionIndex);
			} catch (Exception e) {
				throw new UserError(this, 904, AbstractAggregationFunction.KNOWN_AGGREGATION_FUNCTION_NAMES[weightAggregationFunctionIndex], e.getMessage());
			}
		}
		lastExample = null;
		double[] data = new double[newAttributes.size()];
		for (int i = 0; i < data.length; i++) {
			data[i] = Double.NaN;
		}
		for (Example example : exampleSet) {
			if (lastExample != null) {
				if (lastExample.getValue(groupAttribute) != example.getValue(groupAttribute)) {
					if (aggregationFunction != null) {
						data[0] = aggregationFunction.getValue();
						try {
							aggregationFunction = AbstractAggregationFunction.createAggregationFunction(weightAggregationFunctionIndex);
						} catch (Exception e) {
							throw new UserError(this, 904, AbstractAggregationFunction.KNOWN_AGGREGATION_FUNCTION_NAMES[weightAggregationFunctionIndex], e.getMessage());
						}				
					}
					table.addDataRow(new DoubleArrayDataRow(data));
					data = new double[newAttributes.size()];
					for (int i = 0; i < data.length; i++) {
						data[i] = Double.NaN;
					}
				}
			}
			if (aggregationFunction != null) {
				aggregationFunction.update(example.getWeight());
			}
			for (int i = 0; i < attributes.length; i++) {
				Attribute attribute = attributes[i];
				int newIndex = -1;
				if (constantAttributeValues[i]) {
					newIndex = attributeNames.indexOf(attribute.getName());
				} else {
					String newAttributeName = attribute.getName() + "_" + example.getValueAsString(indexAttribute);
					newIndex = attributeNames.indexOf(newAttributeName);
				}
				if (newIndex != -1) {
					double value = 	example.getValue(attribute);
					if (!Double.isNaN(value)) { 
						if (attribute.isNominal()) {
							data[newIndex] = newAttributes.get(newIndex).getMapping().mapString(attribute.getMapping().mapIndex((int) value));
						} else {
							data[newIndex] = value;
						}
					}
				}
			}
			lastExample = example;
		}
		if (aggregationFunction != null) {
			data[0] = aggregationFunction.getValue();
		}
		table.addDataRow(new DoubleArrayDataRow(data));

		// create and deliver example set
		ExampleSet result = table.createExampleSet();
		if (newWeightAttribute != null) {
			result.getAttributes().setWeight(newWeightAttribute);
		}
		result.recalculateAllAttributeStatistics();
		return new IOObject[] { result };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_GROUP_ATTRIBUTE, "Attribute that groups the examples which form one example after pivoting.", false));
		types.add(new ParameterTypeString(PARAMETER_INDEX_ATTRIBUTE, "Attribute which differentiates examples inside a group.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_CONSIDER_WEIGHTS, "Determines whether weights will be kept and aggregated or ignored.", true));
		types.add(new ParameterTypeCategory(PARAMETER_WEIGHT_AGGREGATION, "Specifies how example weights are aggregated in the groups.", AbstractAggregationFunction.KNOWN_AGGREGATION_FUNCTION_NAMES, AbstractAggregationFunction.SUM));
		return types;
	}
}
