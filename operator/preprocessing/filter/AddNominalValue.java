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
package com.rapidminer.operator.preprocessing.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.BinominalAttribute;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;


/**
 * Adds a value to a nominal attribute definition.
 * 
 * @author Peter B. Volk, Ingo Mierswa
 * @version $Id: AddNominalValue.java,v 1.6 2008/07/07 07:06:40 ingomierswa Exp $
 */
public class AddNominalValue extends Operator {


	/** The parameter name for &quot;The name of the nominal attribute to which values should be added.&quot; */
	public static final String PARAMETER_ATTRIBUTE_NAME = "attribute_name";

	/** The parameter name for &quot;The value which should be added.&quot; */
	public static final String PARAMETER_NEW_VALUE = "new_value";
	public AddNominalValue(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		Attribute attribute = exampleSet.getAttributes().get(getParameterAsString(PARAMETER_ATTRIBUTE_NAME));
		
		// some checks
		if (attribute == null) {
			throw new UserError(this, 111, getParameterAsString(PARAMETER_ATTRIBUTE_NAME));
		}
		if (!attribute.isNominal()) {
			throw new UserError(this, 119, new Object[] { attribute.getName(), this.getName() });
		}

		// replace binominal attribute if already two values are mapped
		Map<Attribute, Attribute> replacementMap = new HashMap<Attribute, Attribute>();
		ExampleTable table = exampleSet.getExampleTable();
		if (attribute instanceof BinominalAttribute) {
			if (attribute.getMapping().size() == 2) {
				Attribute newAttribute = AttributeFactory.createAttribute(attribute.getName(), Ontology.NOMINAL);
				replacementMap.put(attribute, newAttribute);
				table.addAttribute(newAttribute);
				exampleSet.getAttributes().addRegular(newAttribute);
			}
		}
		
		Set<Attribute> originalAttributes = replacementMap.keySet();

		// copying mapping
		for (Attribute originalAttribute: originalAttributes) {
			NominalMapping originalMapping = originalAttribute.getMapping();
			NominalMapping newMapping = replacementMap.get(originalAttribute).getMapping();
			for (int i = 0; i < originalMapping.size(); i++)
				newMapping.mapString(originalMapping.mapIndex(i));
		}
		
		// copying data
		for (Example example: exampleSet) {
			for (Attribute originalAttribute: originalAttributes) {
				example.setValue(replacementMap.get(originalAttribute), example.getValue(originalAttribute));
			}
		}
		
		// removing old attributes
		for (Attribute originalAttribute: originalAttributes) {
			exampleSet.getAttributes().remove(originalAttribute);
		}
		
		
		String newValue = getParameterAsString(PARAMETER_NEW_VALUE);
		attribute.getMapping().mapString(newValue);

		return new IOObject[] { exampleSet };
	}
		
	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_ATTRIBUTE_NAME, "The name of the nominal attribute to which values should be added.", false));
		types.add(new ParameterTypeString(PARAMETER_NEW_VALUE, "The value which should be added.", false));
		return types;
	}
}
