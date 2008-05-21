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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.ListDataRowReader;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.AbstractModel;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;

/**
 * The model for the generic dimensionality reducer.
 * 
 * @author Ingo Mierswa
 * @version $Id: DimensionalityReducerModel.java,v 1.2 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class DimensionalityReducerModel extends AbstractModel {

	private static final long serialVersionUID = 1036161585615738268L;

	private int dimensions;
	
	private double[][] p;
	
	protected DimensionalityReducerModel(ExampleSet exampleSet, double[][] p, int dimensions) {
		super(exampleSet);
		this.p = p;
		this.dimensions = dimensions;
	}

	public ExampleSet apply(ExampleSet es) throws OperatorException {
		List<Attribute> attributes = new ArrayList<Attribute>();
		Map<Attribute, String> specialAttributes = new HashMap<Attribute, String>();
		for (int i = 0; i < dimensions; i++) {
			Attribute att = AttributeFactory.createAttribute("d" + i, Ontology.REAL);
			attributes.add(att);
		}

		Iterator<AttributeRole> s = es.getAttributes().specialAttributes();
		while (s.hasNext()) {
			AttributeRole role = s.next();
			Attribute att = (Attribute) role.getAttribute().clone();
			specialAttributes.put(att, role.getSpecialName());
			attributes.add(att);
		}
		MemoryExampleTable et = new MemoryExampleTable(attributes);

		// Apply the measures and build the instances
		List<DataRow> dataRows = new ArrayList<DataRow>();

		// Apply the measures and build the instances
		int i = 0;
		for (Example oldExample : es) {
			DataRow dr = new DoubleArrayDataRow(new double[dimensions + specialAttributes.size()]);
			for (int j = 0; j < dimensions; j++)
				dr.set(attributes.get(j), p[i][j]);

			Iterator<Attribute> sa = specialAttributes.keySet().iterator();

            while (sa.hasNext()) {
                Attribute att = sa.next();
                dr.set(att, oldExample.getValue(es.getAttributes().getSpecial(att.getName())));
            }
            
			dataRows.add(dr);
			i++;
		}

		et.readExamples(new ListDataRowReader(dataRows.iterator()));
		ExampleSet examples = et.createExampleSet(specialAttributes);
		return examples;
	}

	public String getName() {
		return "Dimensionality Reduction";
	}
	
	public String toString() {
		return "This model reduces the number of dimensions to " + dimensions + ".";
	}
}
