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
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.DoubleSparseArrayDataRow;
import com.rapidminer.example.table.ListDataRowReader;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.Ontology;


/**
 * This operator maps all non numeric attributes to real valued attributes.
 * Nothing is done for numeric attributes, binary attributes are mapped to 0 and
 * 1.
 * 
 * For nominal attributes one of the following calculations will be done:
 * <ul>
 * <li>Dichotomization, i.e. one new attribute for each value of the nominal
 * attribute. The new attribute which corresponds to the actual nominal value
 * gets value 1 and all other attributes gets value 0.</li>
 * <li>Alternatively the values of nominal attributes can be seen as equally
 * ranked, therefore the nominal attribute will simply be turned into a real
 * valued attribute, the old values results in equidistant real values.</li>
 * </ul>
 * 
 * At this moment the same applies for ordinal attributes, in a future release
 * more appropriate values based on the ranking between the ordinal values may
 * be included.
 * 
 * @rapidminer.todo ordinal attributes are mapped in accordance to their ranks
 * @rapidminer.todo non numeric value series ?
 * @rapidminer.todo Remove duplicated code / refactor value mappings. Shevek.
 * 
 * @author Ingo Mierswa
 * @version $Id: NominalToNumeric.java,v 1.15 2006/04/05 08:57:27
 *          ingomierswa Exp $
 */
public class NominalToNumeric extends Operator {


	/** The parameter name for &quot;Uses one new attribute for each possible value of nominal attributes (new example table increasing used memory)&quot; */
	public static final String PARAMETER_DICHOTOMIZATION = "dichotomization";
	public NominalToNumeric(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet eSet = getInput(ExampleSet.class);

		ExampleSet result = null;
		List<String> nomAtts = new LinkedList<String>();

		if (getParameterAsBoolean(PARAMETER_DICHOTOMIZATION)) {
			List<Attribute> attributes = new LinkedList<Attribute>();

			// regular attributes
			for (Attribute attribute : eSet.getAttributes()) {
				if (attribute.isNominal()) { // nominal
					handleNominalAttribute(attribute, attributes);
					nomAtts.add(attribute.getName());
				} else { // numeric
					Attribute newAttribute = AttributeFactory.createAttribute(attribute.getName(), Ontology.REAL);
					attributes.add(newAttribute);
				}
			}

			// add all special attributes
			Map<Attribute, String> specialMap = new HashMap<Attribute, String>();
			Iterator<AttributeRole> i = eSet.getAttributes().specialAttributes();
			while (i.hasNext()) {
				AttributeRole role = i.next();
				Attribute attribute = (Attribute) role.getAttribute().clone();
				attributes.add(attribute);
				specialMap.put(attribute, role.getSpecialName());
			}

			// create new example table and fill with data
			MemoryExampleTable exampleTable = new MemoryExampleTable(attributes);

			List<DataRow> dataRows = new LinkedList<DataRow>();
			Iterator<Example> exampleReader = eSet.iterator();
			while (exampleReader.hasNext()) {
				Example example = exampleReader.next();
				dataRows.add(createDataRow(example, eSet, specialMap, attributes.size(), attributes, nomAtts));
				checkForStop();
			}

			DataRowReader reader = new ListDataRowReader(dataRows.iterator());
			exampleTable.readExamples(reader);

			// create a new example set with the correct special attributes
			result = exampleTable.createExampleSet(specialMap);
		} else {
			// simply set values types on real for non-dichotomization
			for (Attribute attribute : eSet.getAttributes()) {
				eSet.getAttributes().replace(attribute, AttributeFactory.changeValueType(attribute, Ontology.REAL));
			}
			result = eSet;
		}
		return new IOObject[] { result };
	}

	private void handleNominalAttribute(Attribute attribute, List<Attribute> attributes) {
		if ((Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), Ontology.BINOMINAL)) ||
		   ((Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), Ontology.NOMINAL)) &&
		    (attribute.getMapping().size() == 2))) {
			Attribute newAttribute = AttributeFactory.createAttribute(attribute.getName(), Ontology.REAL);
			attributes.add(newAttribute);
		} else { // all other nominal values which are not one of the above
					// (including ordered attributes for now)...
			for (String value : attribute.getMapping().getValues()) {
				Attribute newAttribute = AttributeFactory.createAttribute(attribute.getName() + "_" + value, Ontology.REAL);
				attributes.add(newAttribute);
			}
		}
	}

	/**
	 * Only used in dichotomization case. Therefore, this method creates sparse
	 * array data rows.
	 */
	private DataRow createDataRow(Example example, ExampleSet eSet, Map<Attribute, String> specialAttributes, int size, List allAttributes, List nomAtts) {
		DataRow result = new DoubleSparseArrayDataRow();
		// regular data
		int currentResultIndex = 0;
		for (Attribute oldAttribute : eSet.getAttributes()) {
			if (oldAttribute.isNominal() && nomAtts.contains(oldAttribute.getName())) {
				if ((Ontology.ATTRIBUTE_VALUE_TYPE.isA(oldAttribute.getValueType(), Ontology.BINOMINAL)) || 
					((Ontology.ATTRIBUTE_VALUE_TYPE.isA(oldAttribute.getValueType(), Ontology.NOMINAL)) &&
					 (oldAttribute.getMapping().size() == 2))) {
					Attribute newAttribute = (Attribute) allAttributes.get(currentResultIndex++);
					result.set(newAttribute, example.getValue(oldAttribute));
				} else { // other nominal and ordered
					int index = (int) example.getValue(oldAttribute);
					Attribute newAttribute = (Attribute) allAttributes.get(currentResultIndex + index);
					result.set(newAttribute, 1.0d);
					currentResultIndex += oldAttribute.getMapping().getValues().size();
				}
			} else {
				Attribute newAttribute = (Attribute) allAttributes.get(currentResultIndex++);
				result.set(newAttribute, example.getValue(oldAttribute));
			}
		}

		// special data
		Iterator<AttributeRole> i = eSet.getAttributes().specialAttributes();
		while (i.hasNext()) {
			Attribute oldAttribute = i.next().getAttribute();
			Attribute newAttribute = (Attribute) allAttributes.get(currentResultIndex++);
			result.set(newAttribute, example.getValue(oldAttribute));
		}
		return result;
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_DICHOTOMIZATION, "Uses one new attribute for each possible value of nominal attributes (new example table increasing used memory)", false));
		return types;
	}
}
