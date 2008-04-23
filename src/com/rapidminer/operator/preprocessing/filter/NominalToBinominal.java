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
package com.rapidminer.operator.preprocessing.filter;

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
import com.rapidminer.example.table.DoubleSparseArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.Ontology;


/**
 * This operator maps the values of all nominal values to binary attributes.
 * 
 * @author Buelent Moeller, Ingo Mierswa, Julien Nioche
 * @version $Id: NominalToBinominal.java,v 1.1 2007/06/22 15:31:44 ingomierswa Exp $
 */
public class NominalToBinominal extends Operator {


	/** The parameter name for &quot;Indicates if numerical attributes should be created instead of boolean attributes.&quot; */
	public static final String PARAMETER_CREATE_NUMERICAL_ATTRIBUTES = "create_numerical_attributes";
	public static final int TYPE_BOOLEAN = 0;

	public static final int TYPE_NUMERICAL = 1;

	public NominalToBinominal(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		int type = getParameterAsBoolean(PARAMETER_CREATE_NUMERICAL_ATTRIBUTES) ? TYPE_NUMERICAL : TYPE_BOOLEAN;

		// generate boolean attributes
		List<Attribute> attributes = new ArrayList<Attribute>();
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (attribute.isNominal() && (attribute.getMapping().getValues().size() > 2)) {
				handleNominalAttribute(attribute, attributes, type);
			} else {
				attributes.add((Attribute) attribute.clone()); // clone is
																// necessary
																// since the
																// creation of
																// the new
																// example table
																// provides a
																// new index
			}
		}

		// copy special attributes
		Map<Attribute, String> specialAttributeMap = new HashMap<Attribute, String>();
		Iterator<AttributeRole> i = exampleSet.getAttributes().specialAttributes();
		while (i.hasNext()) {
			AttributeRole role = i.next();
			String specialName = role.getSpecialName();
			Attribute special = role.getAttribute();
			Attribute newSpecial = (Attribute) special.clone(); // necessary
																// since the
																// creation of
																// the new
																// example table
																// provides a
																// new index
			attributes.add(newSpecial);
			specialAttributeMap.put(newSpecial, specialName);
		}

		// transform data
		MemoryExampleTable table = new MemoryExampleTable(attributes);
		Iterator<Example> reader = exampleSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			int index = 0;
			DataRow dataRow = new DoubleSparseArrayDataRow(example.getAttributes().size());
			for (Attribute attribute : exampleSet.getAttributes()) {
				if (attribute.isNominal() && (attribute.getMapping().getValues().size() > 2)) {
					// find index of attribute which should have value "true"
					// the others are 0.0 or "false" per default (--> sparse
					// data rows)
					int trueAttributeIndex = (int) example.getValue(attribute);
					Attribute currentNewAttribute = attributes.get(index + trueAttributeIndex);
					if (type == TYPE_NUMERICAL) {
						dataRow.set(currentNewAttribute, 1.0);
					} else if (type == TYPE_BOOLEAN) {
						dataRow.set(currentNewAttribute, currentNewAttribute.getMapping().mapString("true"));
					} else {} // this cannot happen
					index += attribute.getMapping().getValues().size();
				} else {
					// numerical or BiNominal attributes
					dataRow.set(attributes.get(index++), example.getValue(attribute));
				}
			}

			// special attributes
			Iterator<AttributeRole> s = exampleSet.getAttributes().specialAttributes();
			while (s.hasNext()) {
				Attribute special = s.next().getAttribute();
				dataRow.set(attributes.get(index++), example.getValue(special));
			}
			// trims the data row to the needed size
			dataRow.trim();

			table.addDataRow(dataRow);
			checkForStop();
		}

		// create example set from example table
		ExampleSet resultSet = table.createExampleSet(specialAttributeMap);
		return new IOObject[] { resultSet };
	}

	public void handleNominalAttribute(Attribute attribute, List<Attribute> attributeList, int type) {
		Iterator<String> i = attribute.getMapping().getValues().iterator();
		while (i.hasNext()) {
			String nominalValue = i.next();
			Attribute newAttribute = null;
			if (type == TYPE_BOOLEAN) {
				newAttribute = AttributeFactory.createAttribute(attribute.getName() + "_" + nominalValue, Ontology.BINOMINAL, Ontology.SINGLE_VALUE);
				if (newAttribute.getMapping().mapString("false") != 0)
					logWarning("'false' was not mapped to the first attribute value index!");
				newAttribute.getMapping().mapString("true");
			} else if (type == TYPE_NUMERICAL) {
				newAttribute = AttributeFactory.createAttribute(attribute.getName() + "_" + nominalValue, Ontology.NUMERICAL, Ontology.SINGLE_VALUE);
			} else {} // cannot happen
			attributeList.add(newAttribute);
		}
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_CREATE_NUMERICAL_ATTRIBUTES, "Indicates if numerical attributes should be created instead of boolean attributes.", false));
		return types;
	}
}
