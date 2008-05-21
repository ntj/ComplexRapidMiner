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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeString;


/**
 * <p>This operator takes an <code>ExampleSet</code> as input and maps the values
 * of certain attributes to other values. For example, it can replace all
 * occurrences of the String "unknown" in a nominal Attribute by a default
 * String, for all examples in the ExampleSet.</p>
 * 
 * <p>This operator can replace nominal values (e.g. replace the value &quot;green&quot; by 
 * the value &quot;green_color&quot;) as well as numerical values (e.g. replace the all values
 * &quot;3&quot; by &quot;-1&quot;).</p>
 * 
 * <p> This operator supports regular expressions for the attribute names, i.e. the value
 * mapping is applied on all attributes for which the name fulfills the pattern defined
 * by the name expression.</p>
 * 
 * 
 * @author Timm Euler, Ingo Mierswa, Tobias Malbrecht
 * @version $Id: AttributeValueMapper.java,v 1.6 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class AttributeValueMapper extends Operator {

	/** The parameter name for &quot;Mapping of values will be applied to the attributes that match the given regular expression.&quot; */
	public static final String PARAMETER_ATTRIBUTES = "attributes";

	/** The parameter name for &quot;Filter also special attributes (label, id...)&quot; */
	public static final String PARAMETER_APPLY_TO_SPECIAL_FEATURES = "apply_to_special_features";

	/** The parameter name for &quot;All occurrences of this value will be replaced.&quot; */
	public static final String PARAMETER_REPLACE_WHAT = "replace_what";

	/** The parameter name for &quot;The new attribute value to use.&quot; */
	public static final String PARAMETER_REPLACE_BY = "replace_by";
	
	public AttributeValueMapper(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		// init
		ExampleSet inputSet = getInput(ExampleSet.class);
		
	    String regex = getParameterAsString(PARAMETER_ATTRIBUTES);
		String replaceWhat = getParameterAsString(PARAMETER_REPLACE_WHAT);
		String replaceBy = getParameterAsString(PARAMETER_REPLACE_BY);
		
		Pattern pattern = null;
	    try {
	        pattern = Pattern.compile(regex);
	    } catch (PatternSyntaxException e) {
            throw new UserError(this, 206, regex, e.getMessage());
	    }

	    Iterator<Attribute> i = inputSet.getAttributes().iterator();
        if (getParameterAsBoolean(PARAMETER_APPLY_TO_SPECIAL_FEATURES)) {
        	i = inputSet.getAttributes().allAttributes();
        }
		while (i.hasNext()) {
			Attribute attribute = i.next();
			Matcher matcher = pattern.matcher(attribute.getName());
			if (matcher.matches()) {
				replaceValue(inputSet, attribute, replaceWhat, replaceBy);
			}
			checkForStop();
		}
	    
		return new IOObject[] { inputSet };
	}

	private void replaceValue(ExampleSet inputSet, Attribute attribute, String replaceWhat, String replaceBy) throws OperatorException {
		// some checks
		if (attribute == null) {
			throw new UserError(this, 111, "unknown attribute");
		}

		// the replacement
		if (attribute.isNominal()) {
			Tools.replaceValue(inputSet, attribute, replaceWhat, replaceBy);
		} else {
			double oldValue = 0.0;
			if (replaceWhat.equals("?")) {
				oldValue = Double.NaN;
			} else {
				try {
					oldValue = Double.parseDouble(replaceWhat);
				} catch (NumberFormatException e) {
					throw new UserError(this, 207, new Object[] {replaceWhat, "replace_what", "only numbers are allowed for numerical attributes"});
				}
			}
			double newValue = 0.0d;
			if (replaceBy.equals("?")) {
				newValue = Double.NaN;
			} else {
				try {
					newValue = Double.parseDouble(replaceBy);
				} catch (NumberFormatException e) {
					throw new UserError(this, 207, new Object[] {replaceBy, "replace_by", "only numbers are allowed for numerical attributes"});
				}
			}
			Tools.replaceValue(inputSet, attribute, oldValue, newValue);
		}		
	}
	
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeString(PARAMETER_ATTRIBUTES, "Mapping of values will be applied to the attributes that match the given regular expression.", false);
		types.add(type);
	    types.add(new ParameterTypeBoolean(PARAMETER_APPLY_TO_SPECIAL_FEATURES, "Filter also special attributes (label, id...)", false));
		type = new ParameterTypeString(PARAMETER_REPLACE_WHAT, "All occurrences of this value will be replaced.", false);
		types.add(type);
		type = new ParameterTypeString(PARAMETER_REPLACE_BY, "The new attribute value to use.", false);
		types.add(type);
		return types;
	}
}
