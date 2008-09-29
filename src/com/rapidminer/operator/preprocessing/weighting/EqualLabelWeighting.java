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
package com.rapidminer.operator.preprocessing.weighting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.tools.Ontology;

/**
 * This operator distributes example weights so that all example weights of
 * labels sum up equally.
 * 
 * @author Sebastian Land
 * @version $Id: EqualLabelWeighting.java,v 1.4 2008/07/13 16:39:42 ingomierswa Exp $
 */
public class EqualLabelWeighting extends Operator {

	private static final String PARAMETER_TOTAL_WEIGHT = "total_weight";
	public EqualLabelWeighting(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		if (exampleSet.getAttributes().getWeight() == null) {
			
			Attribute weight = AttributeFactory.createAttribute("weight", Ontology.NUMERICAL);
			exampleSet.getExampleTable().addAttribute(weight);
			exampleSet.getAttributes().addRegular(weight);
			exampleSet.getAttributes().setWeight(weight);
			
			Attribute label = exampleSet.getAttributes().getLabel();
			exampleSet.recalculateAttributeStatistics(label);
			NominalMapping labelMapping = label.getMapping();
			Map<String, Double> labelFrequencies = new HashMap<String, Double>();
			for (String labelName: labelMapping.getValues()) {
				labelFrequencies.put(labelName, exampleSet.getStatistics(label, Statistics.COUNT, labelName));			
			}
			double numberOfLabels = labelFrequencies.size();
			double perLabelWeight = getParameterAsDouble(PARAMETER_TOTAL_WEIGHT) / numberOfLabels;
			for (Example example: exampleSet) {
				double exampleWeight = perLabelWeight / labelFrequencies.get(labelMapping.mapIndex((int)example.getValue(label)));
				example.setValue(weight, exampleWeight);
			}
		}
		return new IOObject[] {exampleSet};
	}

	public Class<?>[] getInputClasses() {
		return new Class[] {ExampleSet.class};
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] {ExampleSet.class};
	}
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_TOTAL_WEIGHT, "The total weight distributed over all examples.", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1);
		type.setExpert(false);
		types.add(type);
		return types;
	}

}
