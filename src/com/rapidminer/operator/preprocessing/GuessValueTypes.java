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
package com.rapidminer.operator.preprocessing;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.preprocessing.filter.NominalNumbers2Numerical;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;

/**
 * This operator can be used to (re-)guess the value types of all attributes. This
 * might be useful after some preprocessing transformations and &quot;purifying&quot;
 * some of the columns, especially if columns which were nominal before can be handled
 * as numerical columns. With this operator, the value types of all attributes do not
 * have to be transformed manually with operators like {@link NominalNumbers2Numerical}.
 *
 * @author Ingo Mierswa
 * @version $Id: GuessValueTypes.java,v 1.4 2008/05/09 19:22:54 ingomierswa Exp $
 */
public class GuessValueTypes extends Operator {

	/** The parameter name for &quot;Character that is used as decimal point.&quot; */
	public static final String PARAMETER_DECIMAL_POINT_CHARACTER = "decimal_point_character";
	
	public GuessValueTypes(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		// init
		char decimalPointCharacter = getParameterAsString(PARAMETER_DECIMAL_POINT_CHARACTER).charAt(0);
		
		int[] valueTypes = new int[exampleSet.getAttributes().allSize()];
		Iterator<Attribute> a = exampleSet.getAttributes().allAttributes();
		int index = 0;
		while (a.hasNext()) {
			Attribute attribute = a.next();
			valueTypes[index++] = attribute.getValueType();
		}
		
		// guessing
		boolean[] checked = new boolean[valueTypes.length];
		int checkedCounter = 0;
		for (Example example : exampleSet) {
			a = exampleSet.getAttributes().allAttributes();
			index = 0;
			while (a.hasNext()) {
				Attribute attribute = a.next();
				if (!checked[index]) {
					try {
						String valueString = example.getValueAsString(attribute);
						valueString = valueString.replace(decimalPointCharacter, '.');
						double value = Double.parseDouble(valueString);
						if (Tools.isEqual(Math.round(value), value)) {
							valueTypes[index] = Ontology.INTEGER;
						} else {
							valueTypes[index] = Ontology.REAL;
						}
					} catch (NumberFormatException e) {
						valueTypes[index] = Ontology.NOMINAL;
						checked[index] = true;
						checkedCounter++;
					}
				}
				index++;
			}
			if (checkedCounter >= checked.length) {
				break;
			}
		}
		
		
		// new attributes
		List<AttributeRole> newAttributes = new LinkedList<AttributeRole>();

		Iterator<AttributeRole> r = exampleSet.getAttributes().allAttributeRoles();
		index = 0;
		while (r.hasNext()) {
			AttributeRole role = r.next();
			Attribute attribute = role.getAttribute();

			Attribute newAttribute = AttributeFactory.createAttribute(valueTypes[index]);
			exampleSet.getExampleTable().addAttribute(newAttribute);
			AttributeRole newRole = new AttributeRole(newAttribute);
			newRole.setSpecial(role.getSpecialName());
			newAttributes.add(newRole);

			// copy data
			for (Example e : exampleSet) {
				double oldValue = e.getValue(attribute);
				if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueTypes[index], Ontology.NUMERICAL)) {
					if (!Double.isNaN(oldValue)) {
						String valueString = e.getValueAsString(attribute);
						valueString = valueString.replace(decimalPointCharacter, '.');
						e.setValue(newAttribute, Double.parseDouble(valueString));							
					} else {
						e.setValue(newAttribute, Double.NaN);
					}
				} else {
					if (!Double.isNaN(oldValue)) {
						String value = e.getValueAsString(attribute);
						e.setValue(newAttribute, newAttribute.getMapping().mapString(value));							
					} else {
						e.setValue(newAttribute, Double.NaN);
					}
				}
			}

			// delete attribute and rename the new attribute (due to deletion and data scans: no more memory used :-)
			exampleSet.getExampleTable().removeAttribute(attribute);
			r.remove();
			newAttribute.setName(attribute.getName());
			
			index++;
		}
		
		for (AttributeRole role : newAttributes) {
			if (role.isSpecial()) {
				exampleSet.getAttributes().setSpecialAttribute(role.getAttribute(), role.getSpecialName());
			} else {
				exampleSet.getAttributes().addRegular(role.getAttribute());
			}
		}
		
		return new IOObject[] { exampleSet };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_DECIMAL_POINT_CHARACTER, "Character that is used as decimal point.", "."));
		return types;
	}
}
