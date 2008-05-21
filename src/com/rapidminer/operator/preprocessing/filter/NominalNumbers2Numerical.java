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

import java.util.Iterator;
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
import com.rapidminer.operator.preprocessing.GuessValueTypes;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;

/**
 * <p>This operator transforms nominal attributes into numerical ones. In contrast to
 * the NominalToNumeric operator, this operator directly parses numbers from
 * the wrongly as nominal values encoded values. Please note that this operator
 * will first check the stored nominal mappings for all attributes. If (old) mappings
 * are still stored which actually are nominal (without the corresponding data being part of 
 * the example set), the attribute will not be converted. Please use the operator
 * {@link GuessValueTypes} in these cases.</p>
 * 
 * @author Regina Fritsch, Ingo Mierswa
 * @version $Id: NominalNumbers2Numerical.java,v 1.8 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class NominalNumbers2Numerical extends Operator {

	/** The parameter name for &quot;Character that is used as decimal point.&quot; */
	public static final String PARAMETER_DECIMAL_POINT_CHARACTER = "decimal_point_character";
	
	public NominalNumbers2Numerical(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		char decimalPointCharacter = getParameterAsString(PARAMETER_DECIMAL_POINT_CHARACTER).charAt(0);
		
		List<Attribute> newAttributes = new LinkedList<Attribute>();
		// using iterator for avoiding "concurrent modification"
		Iterator<Attribute> a = exampleSet.getAttributes().iterator();
		while (a.hasNext()) {
			Attribute attribute = a.next();
			if (attribute.isNominal()) {
				boolean isNumericalNominal = true;
				try {
					for(String value : attribute.getMapping().getValues()) {
						String checkValue = value.replace(decimalPointCharacter, '.');
						Double.parseDouble(checkValue);
					}
				} catch (Exception e){
					isNumericalNominal = false;
				}
				
				if (isNumericalNominal) {
					// new attribute
					Attribute newAttribute = AttributeFactory.createAttribute(Ontology.NUMERICAL);
					exampleSet.getExampleTable().addAttribute(newAttribute);
					newAttributes.add(newAttribute);
		
					// copy data
					for (Example e : exampleSet) {
						double oldValue = e.getValue(attribute);
						if (!Double.isNaN(oldValue)) {
							String value = e.getValueAsString(attribute);
							String replaceValue = value.replace(decimalPointCharacter, '.');
							e.setValue(newAttribute, Double.parseDouble(replaceValue));							
						} else {
							e.setValue(newAttribute, Double.NaN);
						}
					}
		
					// delete attribute and rename the new attribute
					exampleSet.getExampleTable().removeAttribute(attribute);
					a.remove();
					newAttribute.setName(attribute.getName());
				}
			}
		}

		for (Attribute attribute : newAttributes) {
			exampleSet.getAttributes().addRegular(attribute);
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
