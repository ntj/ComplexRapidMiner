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
package com.rapidminer.operator.features.transformation;

import java.util.ArrayList;

import Jama.Matrix;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.AbstractModel;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.functions.kernel.functions.Kernel;
import com.rapidminer.tools.Ontology;

/**
 * The model for the Kernel-PCA.
 * 
 * @author Sebastian Land
 * @version $Id: KernelPCAModel.java,v 1.3 2008/07/13 16:39:42 ingomierswa Exp $
 */
public class KernelPCAModel extends AbstractModel {

	private static final long serialVersionUID = -6699248775014738833L;
	
	private Matrix eigenVectors;
	
	private ArrayList<double[]> exampleValues;
	
	private Kernel kernel;
	
	private ArrayList<String> attributeNames;
	
	protected KernelPCAModel(ExampleSet exampleSet) {
		super(exampleSet);
	}

	public KernelPCAModel(ExampleSet exampleSet, Matrix eigenVectors, ArrayList<double[]> exampleValues, Kernel kernel) {
		super(exampleSet);
		this.eigenVectors = eigenVectors;
		this.exampleValues = exampleValues;
		this.kernel = kernel;
	
		this.attributeNames = new ArrayList<String>();
		for (Attribute attribute: exampleSet.getAttributes()) {
			attributeNames.add(attribute.getName());
		}
	}


	public ExampleSet apply(ExampleSet exampleSet) throws OperatorException {
		Attributes attributes = exampleSet.getAttributes();
		
		checkNames(attributes);
		
		log("Adding new the derived features...");
		Attribute[] pcatts = new Attribute[exampleValues.size()];
		for (int i = 0; i < exampleValues.size(); i++) {
			pcatts[i] = AttributeFactory.createAttribute("kpc_" + (i + 1), Ontology.REAL);
			exampleSet.getExampleTable().addAttribute(pcatts[i]);
			exampleSet.getAttributes().addRegular(pcatts[i]);
		}
		log("Calculating new features");
		
		Matrix distanceValues = new Matrix(1, exampleValues.size());
		for (Example example: exampleSet) {
			int i = 0;
			for (double[] trainValue: exampleValues) {
				distanceValues.set(0, i++, kernel.calculateDistance(trainValue, getAttributeValues(example, attributes)));
			}
			Matrix resultValues = eigenVectors.times(distanceValues.transpose());
			for (int j = 0; j < exampleValues.size(); j++) {
				example.setValue(pcatts[j], resultValues.get(j, 0));
			}
		}
		
		// removing old attributes
		attributes.clearRegular();
		for(Attribute attribute: pcatts) {
			attributes.addRegular(attribute);
		}
		return exampleSet;
	}


	private void checkNames(Attributes attributes) throws UserError {
		int i = 0;
		for (Attribute attribute: attributes) {
			if (attribute.getName() != attributeNames.get(i++)) {
				throw new UserError(null, 141);
			}
		}
		
	}

	private double[] getAttributeValues(Example example, Attributes attributes) {
		double[] values = new double[attributes.size()];
		int x = 0;
		for (Attribute attribute : attributes)
			values[x++] = example.getValue(attribute);
		return values;
	}
}
