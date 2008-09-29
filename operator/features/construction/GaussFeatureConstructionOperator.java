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
package com.rapidminer.operator.features.construction;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;

/**
 * Creates a gaussian function based on a given attribute and a specified mean and standard deviation sigma.
 * 
 * @author Ingo Mierswa
 * @version $Id: GaussFeatureConstructionOperator.java,v 1.1 2008/09/04 17:54:08 ingomierswa Exp $
 */
public class GaussFeatureConstructionOperator extends Operator {

	public static final String PARAMETER_ATTRIBUTE_NAME = "attribute_name";
	
	public static final String PARAMETER_MEAN = "mean";
	
	public static final String PARAMETER_SIGMA = "sigma";
	
	
	public GaussFeatureConstructionOperator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		String attributeName = getParameterAsString(PARAMETER_ATTRIBUTE_NAME);
		double mean = getParameterAsDouble(PARAMETER_MEAN);
		double sigma = getParameterAsDouble(PARAMETER_SIGMA);
		List<Attribute> newAttributes = new LinkedList<Attribute>();
		
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (attribute.isNumerical()) {
				if (attribute.getName().matches(attributeName)) {
					newAttributes.add(createAttribute(exampleSet, attribute, mean, sigma));
				}
			}
		}
		
		for (Attribute attribute : newAttributes) {
			exampleSet.getAttributes().addRegular(attribute);
		}
		
		return new IOObject[] { exampleSet };
	}
	
	private Attribute createAttribute(ExampleSet exampleSet, Attribute base, double mean, double sigma) {
		Attribute newAttribute = AttributeFactory.createAttribute("gauss(" + base.getName() + ", " + mean + ", " + sigma + ")", Ontology.REAL);
		exampleSet.getExampleTable().addAttribute(newAttribute);
		
		for (Example example : exampleSet) {
			double value = example.getValue(base);
			double gaussValue = Math.exp((-1) * ((value - mean) * (value - mean)) / (sigma * sigma));
			example.setValue(newAttribute, gaussValue);
		}
		
		return newAttribute;
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_ATTRIBUTE_NAME, "Indicates on which attribute(s) the gaussian construction should be applied (regular expression possible)", false));
		types.add(new ParameterTypeDouble(PARAMETER_MEAN, "The mean value for the gaussian function.", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d));
		types.add(new ParameterTypeDouble(PARAMETER_SIGMA, "The sigma value for the gaussian function.", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d));
		return types;
	}
}
