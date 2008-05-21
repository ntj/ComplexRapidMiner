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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.AbstractModel;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.som.KohonenNet;

/**
 * The model for the SOM dimensionality reduction.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: SOMDimensionalityReductionModel.java,v 1.2 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class SOMDimensionalityReductionModel extends AbstractModel {

	private static final long serialVersionUID = 7249399167412746295L;

	private KohonenNet net;
	
	private int dimensions;
	
	protected SOMDimensionalityReductionModel(ExampleSet exampleSet, KohonenNet net, int dimensions) {
		super(exampleSet);
		this.net = net;
		this.dimensions = dimensions;
	}

	public ExampleSet apply(ExampleSet exampleSet) throws OperatorException {
		// creating new ExampleTable
		List<Attribute> attributes = new LinkedList<Attribute>();
		for (int i = 0; i < dimensions; i++) {
			attributes.add(AttributeFactory.createAttribute("SOM_" + i, Ontology.NUMERICAL));
		}
		
		// copy special attributes
		Iterator<AttributeRole> s = exampleSet.getAttributes().specialAttributes();
		Map<Attribute, String> newSpecialAttributes = new HashMap<Attribute, String>();
		while (s.hasNext()) {
			AttributeRole role = s.next();
			Attribute specialAttribute = role.getAttribute();
			Attribute newAttribute = (Attribute) specialAttribute.clone();
			attributes.add(newAttribute);
			newSpecialAttributes.put(newAttribute, role.getSpecialName());
		}
		
		MemoryExampleTable newDataTable = new MemoryExampleTable(attributes);
		Iterator<Example> iterator = exampleSet.iterator();
		
		// applying Example on net
		while (iterator.hasNext()) {
			Example currentExample = iterator.next();
			int[] coords = net.apply(SOMDimensionalityReduction.getDoubleArrayFromExample(currentExample));
			double[] exampleData = new double[attributes.size()];
			for (int i = 0; i < dimensions; i++) {
				exampleData[i] = coords[i];
			}
			s = exampleSet.getAttributes().specialAttributes();
			int i = dimensions;
			while (s.hasNext()) {
				exampleData[i++] = currentExample.getValue(s.next().getAttribute());
			}
			DataRow newRow = new DoubleArrayDataRow(exampleData);
			newDataTable.addDataRow(newRow);
		}
		return newDataTable.createExampleSet(newSpecialAttributes);
	}
	
	public String getName() {
		return "SOM Dimensionality Reduction Model";
	}
	
	public String toString() {
		return "Transforms the input data into a new data set with " + dimensions + " dimensions.";
	}
}
