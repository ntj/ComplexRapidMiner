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
package com.rapidminer.operator.features.transformation;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;


/**
 * Projects the examples onto the hyperplane using AttributeWeights as the
 * normal. Additionally the user can specify a bias of the hyperplane.
 * 
 * @author Daniel Hakenjos, Ingo Mierswa
 * @version $Id: HyperplaneProjection.java,v 1.1 2006/04/14 13:07:13 ingomierswa
 *          Exp $
 */
public class HyperplaneProjection extends Operator {


	/** The parameter name for &quot;The bias of the hyperplane&quot; */
	public static final String PARAMETER_BIAS = "bias";
	private static final Class[] INPUT_CLASSES = new Class[] { ExampleSet.class, AttributeWeights.class };

	private static final Class[] OUTPUT_CLASSES = new Class[] { ExampleSet.class };

	private int numberOfSamples, numberOfAttributes;

	private double[][] samples;

	private double[] weights;

	private double bias;

	public HyperplaneProjection(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {

		ExampleSet exampleSet = this.getInput(ExampleSet.class);
		AttributeWeights attributeWeights = this.getInput(AttributeWeights.class);

		this.bias = getParameterAsDouble(PARAMETER_BIAS);

		this.numberOfSamples = exampleSet.size();
		this.numberOfAttributes = exampleSet.getAttributes().size();

		// Create Samples
		this.samples = new double[numberOfSamples][numberOfAttributes];

		weights = new double[numberOfAttributes];
		int w = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			weights[w++] = attributeWeights.getWeight(attribute.getName());
		}

		Iterator<Example> reader = exampleSet.iterator();
		Example example;
		for (int sample = 0; sample < numberOfSamples; sample++) {
			example = reader.next();
			
			int i = 0;
			for (Attribute attribute : exampleSet.getAttributes()) {
				samples[sample][i++] = example.getValue(attribute);
			}
		}

		calculateHyperplaneSamples();

		reader = exampleSet.iterator();
		for (int sample = 0; sample < exampleSet.size(); sample++) {
			example = reader.next();
			int d = 0;
			for (Attribute attribute : exampleSet.getAttributes()) {
				example.setValue(attribute, samples[sample][d++]);
			}

		}
		exampleSet.recalculateAllAttributeStatistics();

		return new IOObject[] { exampleSet };
	}

	private void calculateHyperplaneSamples() {
		double ww = 0.0d;
		for (int i = 0; i < weights.length; i++) {
			ww += weights[i] * weights[i];
		}

		double wx;
		double t;
		// double planepoint;

		for (int s = 0; s < numberOfSamples; s++) {
			wx = 0.0d;
			for (int i = 0; i < this.numberOfAttributes; i++) {
				wx += weights[i] * samples[s][i];
			}
			t = ((-1.0d) * bias - wx) / ww;

			for (int i = 0; i < this.numberOfAttributes; i++) {
				samples[s][i] = samples[s][i] + t * weights[i];
			}
		}

	}

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> list = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_BIAS, "The bias of the hyperplane", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d);
		list.add(type);
		return list;
	}
}
