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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;

/**
 * This operator creates new attributes from nominal attributes where the new attributes contain only of
 * substrings of the original values. Please note that the counting starts with 1 and that the first 
 * and the last character will be included in the resulting substring. For example, the value is 
 * &quot;RapidMiner&quot; and the first index is set to 6 and the last index is set to 9 the result will be
 * &quot;Mine&quot;. If the last index is larger than the length of the word, the resulting substrings 
 * will end with the last character.
 *  
 * @author Ingo Mierswa
 * @version $Id: AttributeValueSubstring.java,v 1.2 2008/07/07 07:06:40 ingomierswa Exp $
 */
public class AttributeValueSubstring extends Operator {

	public static final String PARAMETER_ATTRIBUTES = "attributes";

	public static final String PARAMETER_APPLY_TO_SPECIAL_FEATURES = "apply_to_special_features";

	public static final String PARAMETER_FIRST = "first";

	public static final String PARAMETER_LAST = "last";
	
	public AttributeValueSubstring(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
	    String regex   = getParameterAsString(PARAMETER_ATTRIBUTES);
	    int firstIndex = getParameterAsInt(PARAMETER_FIRST);
	    int lastIndex  = getParameterAsInt(PARAMETER_LAST);
		
		Pattern pattern = null;
	    try {
	        pattern = Pattern.compile(regex);
	    } catch (PatternSyntaxException e) {
            throw new UserError(this, 206, regex, e.getMessage());
	    }

	    Iterator<Attribute> i = exampleSet.getAttributes().iterator();
        if (getParameterAsBoolean(PARAMETER_APPLY_TO_SPECIAL_FEATURES)) {
        	i = exampleSet.getAttributes().allAttributes();
        }
        
        List<Attribute> matchingAttributes = new LinkedList<Attribute>();
		while (i.hasNext()) {
			Attribute attribute = i.next();
			Matcher matcher = pattern.matcher(attribute.getName());
			if (matcher.matches()) {
				if (attribute.isNominal()) {
					matchingAttributes.add(attribute);
				} else {
					logWarning("Cannot create substring for non-nominal attribute '" + attribute.getName() + "', skipping...");
				}
			}
			checkForStop();
		}
		
		for (Attribute attribute : matchingAttributes) {
			Attribute newAttribute = createSubstringAttribute(exampleSet, attribute, firstIndex, lastIndex);
			AttributeRole role = exampleSet.getAttributes().getRole(attribute);
			exampleSet.getAttributes().remove(attribute);
			if (role.isSpecial()) {
				String specialName = role.getSpecialName();
				exampleSet.getAttributes().setSpecialAttribute(newAttribute, specialName);
			}
		}	
		
		return new IOObject[] { exampleSet };
	}
	
	private Attribute createSubstringAttribute(ExampleSet exampleSet, Attribute originalAttribute, int firstIndex, int lastIndex) {
		Attribute newAttribute = AttributeFactory.createAttribute(originalAttribute.getName(), Ontology.NOMINAL);
		exampleSet.getExampleTable().addAttribute(newAttribute);
		exampleSet.getAttributes().addRegular(newAttribute);
		for (Example example : exampleSet) {
			String value = example.getNominalValue(originalAttribute);
			int actualFirst = firstIndex - 1;
			int actualLast  = lastIndex;

			if (lastIndex > value.length()) {
				actualLast = value.length();
			}
			
			if (lastIndex <= firstIndex) {
				example.setValue(newAttribute, Double.NaN);	
			} else {
				String newValue = value.substring(actualFirst, actualLast);
				if (newValue.length() == 0) {
					example.setValue(newAttribute, Double.NaN);
				} else {
					example.setValue(newAttribute, newValue);
				}
			}
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
		ParameterType type = new ParameterTypeString(PARAMETER_ATTRIBUTES, "Substring creation of values will be applied to the attributes that match the given regular expression.", false);
		types.add(type);
	    types.add(new ParameterTypeBoolean(PARAMETER_APPLY_TO_SPECIAL_FEATURES, "Filter also special attributes (label, id...)", false));
		type = new ParameterTypeInt(PARAMETER_FIRST, "The index of the first character of the substring which should be kept (counting starts with 1, 0: start with beginning of value).", 1, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_LAST, "The index of the last character of the substring which should be kept (counting starts with 1, 0: end with end of value).", 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
