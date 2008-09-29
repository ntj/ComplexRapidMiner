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
package com.rapidminer.operator.meta;

import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionCreationException;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.condition.AllInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;


/**
 * <p> In each iteration step, this meta operator applies its inner operators
 * to a subset of the input example set. The subsets represent subgroups which
 * are defined by the values of the specified attributes. If an attribute
 * is specified which has 'male' or 'female' as possible values the first
 * iteration subset will consist of all males, the second of all females,
 * respectively. Please note that no attribute value combinations are
 * supported and hence only subgroups defined by exactly one attribute are
 * considered at a time.</p>
 * 
 * <p>A subset is build (and an inner operator application is executed) for each
 * possible attribute value of the specified attributes if <code>all</code> is
 * selected for the <code>values</code> parameter. If <code>above p</code> is selected,
 * a subset is only build for that values which exhibit an occurance ratio of at
 * least p. This may be helpful, if only large subgroups should be considered.</p>
 * 
 * <p> The parameter <code>filter_attribute</code> specifies, if the subgroup
 * defining attribute should be filtered from the subsets.</p>
 * 
 * <p> The parameter <code>apply_on_complete_set</code> specifies, if the inner
 * operators should be applied on the completed example set in addition to the
 * subset iterations.</p>
 * 
 * @author Tobias Malbrecht
 * @version $Id: ValueSubgroupIteration.java,v 1.4 2008/07/13 23:25:24 ingomierswa Exp $
 */
public class ValueSubgroupIteration extends OperatorChain {

	public static final String PARAMETER_ATTRIBUTES = "attributes";
	
	public static final String PARAMETER_VALUES = "values";

	public static final String[] VALUE_OPTIONS = { "all" , "above p" };
	
	public static final int VALUE_OPTION_ALL = 0;
	
	public static final int VALUE_OPTION_ABOVE_P = 1;
	
	public static final String PARAMETER_P = "p";
	
	public static final String PARAMETER_FILTER_ATTRIBUTE = "filter_attribute";
	
	public static final String PARAMETER_APPLY_ON_COMPLETE_SET = "apply_on_complete_set";
	
	public static final String PARAMETER_ITERATION_MACRO = "iteration_macro";
	
	public static final String DEFAULT_ITERATION_MACRO_NAME = "loop_value";
	
	public ValueSubgroupIteration(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		exampleSet.recalculateAllAttributeStatistics();

		IOContainer innerResult = new IOContainer(new IOObject[0]);
		
		List attributeValueOptions = getParameterList(PARAMETER_ATTRIBUTES);
		
		LinkedHashMap<Attribute, Integer> attributeValueOptionsMap = new LinkedHashMap<Attribute, Integer>();
		int[] valueOptions = new int[attributeValueOptions.size()];
		Pattern[] attributeRegexPatterns = new Pattern[attributeValueOptions.size()];

		Attribute[] attributes = new Attribute[attributeValueOptions.size()];

		Iterator iterator = attributeValueOptions.iterator();
		int j = 0;
		while (iterator.hasNext()) {
			Object[] pair = (Object[]) iterator.next();
			String regex = (String) pair[0];
		    try {
		        attributeRegexPatterns[j] = Pattern.compile(regex);
		    } catch (PatternSyntaxException e) {
	            throw new UserError(this, 206, regex, e.getMessage());
		    }
			attributes[j] = exampleSet.getAttributes().get((String) pair[0]);
			valueOptions[j] = ((Integer) pair[1]).intValue();
			j++;
		}

		for (int i = 0; i < attributeRegexPatterns.length; i++) {
			for (Attribute attribute : exampleSet.getAttributes()) {
				Matcher matcher = attributeRegexPatterns[i].matcher(attribute.getName());
				if (matcher.matches()) {
					attributeValueOptionsMap.put(attribute, valueOptions[i]);
				}
			}
		}
		
		double p = getParameterAsDouble(PARAMETER_P);
		boolean filterAttribute = getParameterAsBoolean(PARAMETER_FILTER_ATTRIBUTE);
		String iterationMacro = getParameterAsString(PARAMETER_ITERATION_MACRO);

		if (getParameterAsBoolean(PARAMETER_APPLY_ON_COMPLETE_SET)) {
			if (iterationMacro != null) {
				getProcess().getMacroHandler().addMacro(iterationMacro, "ALL");
			}
			IOContainer input = new IOContainer(new IOObject[] { exampleSet });
			for (Iterator<Operator> opIt = getOperators(); opIt.hasNext(); ) {
	            try {
	                input = opIt.next().apply(input);
	            } catch (ConcurrentModificationException e) {
	                if (isDebugMode())
	                    e.printStackTrace();
	                throw new UserError(this, 923);
	            }
			}
			for (int k = 0; k < input.size(); k++) {
				input.getElementAt(k).setSource(this.getName() + ":ALL");
			}
			innerResult = innerResult.append(input.getIOObjects());
		}
	
		for (Attribute attribute : attributeValueOptionsMap.keySet()) {
			if (!attribute.isNominal()) {
				continue;
			}
			List<String> values = null;
			switch (attributeValueOptionsMap.get(attribute)) {
			case VALUE_OPTION_ALL:
				values = attribute.getMapping().getValues();
				break;
			case VALUE_OPTION_ABOVE_P:
				values = new Vector<String>();
				for (String value : attribute.getMapping().getValues()) {
					if (exampleSet.getStatistics(attribute, Statistics.COUNT, value) / exampleSet.size() >= p) {
						values.add(value);
					}
				}
				break;
			default:
				values = attribute.getMapping().getValues();
				break;
			}
			
			for (String value : values) {
				if (exampleSet.getStatistics(attribute, Statistics.COUNT, value) > 0) {
					String className = "attribute_value_filter";
					String parameter = attribute.getName() + "=" + value;
					log("Creating condition '" + className + "' with parameter '" + parameter + "'");
					Condition condition = null;
					try {
						condition = ConditionedExampleSet.createCondition(className, exampleSet, parameter);
					} catch (ConditionCreationException e) {
						throw new UserError(this, 904, className, e.getMessage());
					}
					ExampleSet subgroupSet = new ConditionedExampleSet(exampleSet, condition, false);
					if (filterAttribute) {
						subgroupSet.getAttributes().remove(attribute);
					}
					if (iterationMacro != null) {
						getProcess().getMacroHandler().addMacro(iterationMacro, parameter.replace(' ', '_'));
					}
					
					IOContainer input = new IOContainer(new IOObject[] { subgroupSet });
					for (Iterator<Operator> opIt = getOperators(); opIt.hasNext(); ) {
			            try {
			                input = opIt.next().apply(input);
			            } catch (ConcurrentModificationException e) {
			                if (isDebugMode())
			                    e.printStackTrace();
			                throw new UserError(this, 923);
			            }
					}
					for (int k = 0; k < input.size(); k++) {
						input.getElementAt(k).setSource(this.getName() + ":" + parameter);
					}
					innerResult = innerResult.append(input.getIOObjects());
					if (filterAttribute) {
						subgroupSet.getAttributes().addRegular(attribute);
					}
				}
				inApplyLoop();
			}
		}

		if (iterationMacro != null) {
			getProcess().getMacroHandler().addMacro(iterationMacro, null);
		}
		
		return innerResult.getIOObjects();
	}

	/** All inner operators must be able to handle an example set. */
	public InnerOperatorCondition getInnerOperatorCondition() {
		return new AllInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[0]);
	}

	/** Returns the maximum number of innner operators. */
	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	/** Returns the minimum number of innner operators. */
	public int getMinNumberOfInnerOperators() {
		return 1;
	}

	public Class<?>[] getOutputClasses() {
		Iterator<Operator> i = getOperators();
		Operator current = null;
		while (i.hasNext()) {
			current = i.next();
		}
		if (current != null) {
			return current.getOutputClasses();
		} else {
			return new Class[0];
		}
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType valueOption = new ParameterTypeCategory(PARAMETER_VALUES, "Values.", VALUE_OPTIONS, 0);
		ParameterType type = new ParameterTypeList(PARAMETER_ATTRIBUTES, "The attributes.", valueOption);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_P, "Threshold of value occurance.", 0.0, 1.0, 0.2);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_FILTER_ATTRIBUTE, "Filter subgroup defining attribute.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_APPLY_ON_COMPLETE_SET, "Apply inner operators also on complete set.", false));
		types.add(new ParameterTypeString(PARAMETER_ITERATION_MACRO, "Name of macro which is set in each iteration.", DEFAULT_ITERATION_MACRO_NAME));
		return types;
	}
}
