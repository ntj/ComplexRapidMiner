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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;


/**
 * This operator takes an <code>ExampleSet</code> as input and maps the
 * values of certain attributes to other values. The operator can replace
 * nominal values (e.g. replace the value &quot;green&quot; by the value
 * &quot;green_color&quot;) as well as numerical values (e.g. replace the
 * all values &quot;3&quot; by &quot;-1&quot;). A single mapping can be
 * specified using the parameters <tt>replace_what</tt> and
 * <tt>replace_by</tt>. Multiple mappings can be specified in the parameter
 * list <tt>value_mappings</tt>.<br><br>
 * 
 * Additionally, the operator allows to define (and consider) a default
 * mapping. If <tt>add_default_mapping</tt> is set to true and <tt>default_value</tt>
 * is properly set, all values that occur in the example set but are not listed
 * in the value mappings list are replaced by the default value. This may be
 * helpful in cases where only some values should be mapped explicitly and
 * many unimportant values should be mapped to a default value (e.g. "other").<br><br>
 * 
 * <p> If the parameter <tt>consider_regular_expressions</tt> is enabled, the
 * values are replaced by the new values if the original values match the given
 * regular expressions. The value corresponding to the first matching regular
 * expression in the mappings list is taken as replacement.</p>
 * 
 * <p> This operator supports regular expressions for the attribute names,
 * i.e. the value mapping is applied on all attributes for which the name
 * fulfills the pattern defined by the name expression.</p>
 * 
 * @author Tobias Malbrecht
 * @version $Id: AttributeValueMapper.java,v 1.11 2008/09/08 19:35:53 tobiasmalbrecht Exp $
 */
public class AttributeValueMapper extends Operator {

	/** The parameter name for &quot;The specified values will be merged in all attributes specified by the given regular expression.&quot; */
	public static final String PARAMETER_ATTRIBUTES = "attributes";
	
	/** The parameter name for &quot;Filter also special attributes (label, id...)&quot; */
	public static final String PARAMETER_APPLY_TO_SPECIAL_FEATURES = "apply_to_special_features";

	/** The parameter name for &quot;The first value which should be merged.&quot; */
	public static final String PARAMETER_VALUE_MAPPINGS = "value_mappings";

	/** The parameter name for &quot;The second value which should be merged.&quot; */
	public static final String PARAMETER_OLD_VALUES = "old_values";
	
	/** The parameter name for &quot;All occurrences of this value will be replaced.&quot; */
	public static final String PARAMETER_REPLACE_WHAT = "replace_what";

	/** The parameter name for &quot;The new attribute value to use.&quot; */
	public static final String PARAMETER_REPLACE_BY = "replace_by";
	
	/** The parameter name for &quot;Enables matching based on regular expressions; original values may be specified as regular expressions.&quot */
	public static final String PARAMETER_CONSIDER_REGULAR_EXPRESSIONS = "consider_regular_expressions";
	
	/** The parameter name for &quot;If set to true, all original values which are not listed in the value mappings list are mapped to the default value.&quot; */
	public static final String PARAMETER_ADD_DEFAULT_MAPPING = "add_default_mapping";

	/** The parameter name for &quot;The default value all original values are mapped to, if add_default_mapping is set to true.&quot; */
	public static final String PARAMETER_DEFAULT_VALUE = "default_value";
	
	public AttributeValueMapper(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);

	    String attributeNameRegex = getParameterAsString(PARAMETER_ATTRIBUTES);
		Pattern pattern = null;
	    try {
	        pattern = Pattern.compile(attributeNameRegex);
	    } catch (PatternSyntaxException e) {
            throw new UserError(this, 206, attributeNameRegex, e.getMessage());
	    }

	    boolean nominal = false;
	    boolean first = true;
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		Iterator<Attribute> iterator = getParameterAsBoolean(PARAMETER_APPLY_TO_SPECIAL_FEATURES) ? exampleSet.getAttributes().allAttributes() : exampleSet.getAttributes().iterator();
		while (iterator.hasNext()) {
			Attribute attribute = iterator.next();
			Matcher matcher = pattern.matcher(attribute.getName());
			if (matcher.matches()) {
				if (first) {
					nominal = attribute.isNominal();
					first = false;
				} else {
					if (nominal != attribute.isNominal()) {
						throw new UserError(this, 126);
					}
				}
				attributes.add(attribute);
			}
			checkForStop();
		}

		boolean useValueRegex = getParameterAsBoolean(PARAMETER_CONSIDER_REGULAR_EXPRESSIONS);
		List mappingParameterList = getParameterList(PARAMETER_VALUE_MAPPINGS);
		HashMap<String, String> mappings = new HashMap<String, String>();
		HashMap<Pattern, String> patternMappings = new HashMap<Pattern, String>();
		String replaceWhat = getParameterAsString(PARAMETER_REPLACE_WHAT);
		String replaceBy = getParameterAsString(PARAMETER_REPLACE_BY);
		if (replaceWhat != null && replaceBy != null && !replaceWhat.equals("") && !replaceBy.equals("")) {
			mappings.put(replaceWhat, replaceBy);
			if (useValueRegex) {
			    try {
			        Pattern valuePattern =  Pattern.compile(replaceWhat);
			        patternMappings.put(valuePattern, replaceBy);
			    } catch (PatternSyntaxException e) {
		            throw new UserError(this, 206, replaceWhat, e.getMessage());
			    }
			}
		}
		Iterator listIterator = mappingParameterList.iterator();
		int j = 0;
		while (listIterator.hasNext()) {
			Object[] pair = (Object[]) listIterator.next();
			replaceWhat = (String) pair[1];
			replaceBy = (String) pair[0];
			mappings.put(replaceWhat,replaceBy);
			if (useValueRegex) {
			    try {
			        Pattern valuePattern =  Pattern.compile(replaceWhat);
			        patternMappings.put(valuePattern, replaceBy);
			    } catch (PatternSyntaxException e) {
		            throw new UserError(this, 206, replaceWhat, e.getMessage());
			    }
			}
			j++;
		}

		boolean defaultMappingAdded = getParameterAsBoolean(PARAMETER_ADD_DEFAULT_MAPPING); 
		String defaultValue = getParameterAsString(PARAMETER_DEFAULT_VALUE);
		if (defaultMappingAdded) {
			if (defaultValue == null || defaultValue.equals("")) {
				throw new UserError(this, 201, new Object[] { PARAMETER_ADD_DEFAULT_MAPPING, "true", PARAMETER_DEFAULT_VALUE });
			}
		}
		
		if (attributes.size() > 0) {
			if (nominal) {
				for (Attribute attribute : attributes) {
					Attribute newAttribute = AttributeFactory.createAttribute("mapped" + attribute.getName(), attribute.getValueType());
					exampleSet.getExampleTable().addAttribute(newAttribute);
					exampleSet.getAttributes().addRegular(newAttribute);
					for (Example example : exampleSet) { 
						double value = example.getValue(attribute);
						String stringValue = null;
						if (Double.isNaN(value)) {
							stringValue = "?";
						} else {
							stringValue = attribute.getMapping().mapIndex((int) value);
						}
						String mappedValue = (String) mappings.get(stringValue);
						if (useValueRegex) {
							for (java.util.Map.Entry<Pattern, String> entry : patternMappings.entrySet()) {
								Matcher matcher = entry.getKey().matcher(stringValue);
								if (matcher.matches()) {
									mappedValue = entry.getValue();
								}
							}
						}
						if (mappedValue == null) {
							if (stringValue.equals("?")) {
								example.setValue(newAttribute, Double.NaN);
							} else {
								if (defaultMappingAdded) {
									if (defaultValue.equals("?")) {
										example.setValue(newAttribute, Double.NaN);
									} else {
										example.setValue(newAttribute, defaultValue);
									}
								} else {
									example.setValue(newAttribute, newAttribute.getMapping().mapString(stringValue));
								}
							}
						} else {
							if (mappedValue.equals("?")) {
								example.setValue(newAttribute, Double.NaN);
							} else {
								example.setValue(newAttribute, newAttribute.getMapping().mapString(mappedValue));
							}
						}
						checkForStop();
					}
					AttributeRole role = exampleSet.getAttributes().getRole(attribute);
					exampleSet.getAttributes().remove(attribute);
					newAttribute.setName(attribute.getName());
					if (role.isSpecial()) {
						exampleSet.getAttributes().setSpecialAttribute(newAttribute, role.getSpecialName());
					}
				}
			} else {
				HashMap<Double, Double> numericalValueMapping = new HashMap<Double, Double>();
				for (java.util.Map.Entry<String, String> entry : mappings.entrySet()) {
					double oldValue = Double.NaN;
					double newValue = Double.NaN;
					if (!entry.getKey().equals("?")) {
						oldValue = Double.valueOf(entry.getKey());
					}
					if (!entry.getValue().equals("?")) {
						newValue = Double.valueOf(entry.getValue());
					}
					numericalValueMapping.put(oldValue, newValue);
				}
				double numericalDefaultValue = Double.NaN;
				if (defaultMappingAdded && !defaultValue.equals("?")) {
					numericalDefaultValue = Double.valueOf(defaultValue);
				}
				for (Attribute attribute : attributes) {
					Attribute newAttribute = AttributeFactory.createAttribute("mapped" + attribute.getName(), attribute.getValueType());
					exampleSet.getExampleTable().addAttribute(newAttribute);
					exampleSet.getAttributes().addRegular(newAttribute);
					for (Example example : exampleSet) { 
						double value = example.getValue(attribute);
						Double mappedValue = numericalValueMapping.get(Double.valueOf(value));
						if (mappedValue == null) {
							if (defaultMappingAdded) {
								example.setValue(newAttribute, numericalDefaultValue);
							} else {
								example.setValue(newAttribute, value);
							}
						} else {
							example.setValue(newAttribute, mappedValue);
						}
						checkForStop();
					}
	
					AttributeRole role = exampleSet.getAttributes().getRole(attribute);
					exampleSet.getAttributes().remove(attribute);
					newAttribute.setName(attribute.getName());
					if (role.isSpecial()) {
						exampleSet.getAttributes().setSpecialAttribute(newAttribute, role.getSpecialName());
					}
	
				}
			}
		}
	    
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
		types.add(new ParameterTypeString(PARAMETER_ATTRIBUTES, "The specified values will be merged in all attributes specified by the given regular expression.", false));
	    types.add(new ParameterTypeBoolean(PARAMETER_APPLY_TO_SPECIAL_FEATURES, "Filter also special attributes (label, id...)", false));
		ParameterType values = new ParameterTypeString(PARAMETER_OLD_VALUES, "The original values which should be replaced.", false);
		types.add(new ParameterTypeList(PARAMETER_VALUE_MAPPINGS, "The value mappings.", values));
		types.add(new ParameterTypeString(PARAMETER_REPLACE_WHAT, "All occurrences of this value will be replaced.", true));
		types.add(new ParameterTypeString(PARAMETER_REPLACE_BY, "The new attribute value to use.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_CONSIDER_REGULAR_EXPRESSIONS, "Enables matching based on regular expressions; original values may be specified as regular expressions.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_ADD_DEFAULT_MAPPING, "If set to true, all original values which are not listed in the value mappings list are mapped to the default value.", false));
		ParameterType type = new ParameterTypeString(PARAMETER_DEFAULT_VALUE, "The default value all original values are mapped to, if add_default_mapping is set to true.", true);
		type.registerDependencyCondition(new BooleanParameterCondition(this, PARAMETER_ADD_DEFAULT_MAPPING, true));
		types.add(type);
		return types;
	}
}
