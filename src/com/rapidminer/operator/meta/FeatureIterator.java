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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.ValueString;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.LastInnerOperatorCondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeString;

/**
 * <p>This operator takes an input data set and applies its inner operators
 * as often as the number of features of the input data is. Inner operators
 * can access the current feature name by a macro, whose name can be
 * specified via the parameter <code>iteration_macro</code>.</p>
 * 
 * <p>The user can specify with a parameter if this loop should iterate over 
 * all features or only over features with a specific value type, i.e. only
 * over numerical or over nominal features. A regular expression can also be 
 * specified which is used as a filter, i.e. the inner operators are only
 * applied for feature names fulfilling the filter expression.</p> 
 *  
 * @author Ingo Mierswa, Tobias Malbrecht
 * @version $Id: FeatureIterator.java,v 1.5 2008/08/20 16:50:50 ingomierswa Exp $
 */
public class FeatureIterator extends OperatorChain {

	public static final String PARAMETER_FILTER = "filter";
	
	public static final String PARAMETER_INVERT_SELECTION = "invert_selection";
	
	public static final String PARAMETER_TYPE_FILTER = "type_filter";
	
	public static final String PARAMETER_ITERATION_MACRO = "iteration_macro";
	
	public static final String[] TYPE_FILTERS = new String[] {
		"none",
		"nominal",
		"numerical"
	};
	
	public static final int TYPE_FILTER_NONE      = 0;
	
	public static final int TYPE_FILTER_NOMINAL   = 1;
	
	public static final int TYPE_FILTER_NUMERICAL = 2;
	
	public static final String DEFAULT_ITERATION_MACRO_NAME = "loop_feature";	
	
	private int iteration;
	
	private String currentName = null;
	
	public FeatureIterator(OperatorDescription description) {
		super(description);
		
		addValue(new ValueDouble("iteration", "The number of the current iteration / loop.") {
			public double getDoubleValue() {
				return iteration;
			}
		});
		
		addValue(new ValueString("feature_name", "The number of the current feature.") {
			public String getStringValue() {
				return currentName;
			}
		});
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		String iterationMacroName = getParameterAsString(PARAMETER_ITERATION_MACRO);
		
		// init filters
		String filterRegExp = getParameterAsString(PARAMETER_FILTER);
		Pattern pattern = null;
		if ((filterRegExp != null) && (filterRegExp.length() > 0)) {
			pattern = Pattern.compile(filterRegExp);
		}
		int typeFilter = getParameterAsInt(PARAMETER_TYPE_FILTER);
		boolean invertSelection = getParameterAsBoolean(PARAMETER_INVERT_SELECTION);
		
		// filter and loop 
		iteration = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			if ((acceptPattern(attribute, pattern, invertSelection)) && (acceptType(attribute, typeFilter, invertSelection))) {
				String name = attribute.getName();
				getProcess().getMacroHandler().addMacro(iterationMacroName, name);
				currentName = name;
				applyInnerOperators(exampleSet);
				iteration++;
			}
		}
		getProcess().getMacroHandler().removeMacro(iterationMacroName);
		return new IOObject[] { exampleSet };
	}

	private boolean acceptPattern(Attribute attribute, Pattern pattern, boolean invertSelection) {
		if (!invertSelection) {
			if (pattern != null) {
				Matcher matcher = pattern.matcher(attribute.getName());
				return matcher.matches();
			} else {
				return true;
			}
		} else {
			if (pattern != null) {
				Matcher matcher = pattern.matcher(attribute.getName());
				return !matcher.matches();
			} else {
				return true;
			}			
		}
	}
	
	private boolean acceptType(Attribute attribute, int typeFilter, boolean invertSelection) {
		if (!invertSelection) {
			switch (typeFilter) {
			case TYPE_FILTER_NUMERICAL:
				return attribute.isNumerical();
			case TYPE_FILTER_NOMINAL:
				return attribute.isNominal();
			default:
				return true;	
			}
		} else {
			switch (typeFilter) {
			case TYPE_FILTER_NUMERICAL:
				return !attribute.isNumerical();
			case TYPE_FILTER_NOMINAL:
				return !attribute.isNominal();
			default:
				return true;	
			}			
		}
	}
	
	private void applyInnerOperators(ExampleSet exampleSet) throws OperatorException {
		IOContainer input = new IOContainer((ExampleSet)exampleSet.clone());
		for (int i = 0; i < getNumberOfOperators(); i++) {
			input = getOperator(i).apply(input);
		}
	}
	
	public InnerOperatorCondition getInnerOperatorCondition() {
		return new LastInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[0]);
	}

	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	public int getMinNumberOfInnerOperators() {
		return 1; 
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_FILTER, "A regular expression which can be used to filter the features in this loop, i.e. the inner operators are only applied to features which name fulfills the filter expression.", true));
		types.add(new ParameterTypeCategory(PARAMETER_TYPE_FILTER, "Indicates if a value type filter should be applied for this loop.", TYPE_FILTERS, TYPE_FILTER_NONE));
		types.add(new ParameterTypeBoolean(PARAMETER_INVERT_SELECTION, "Indicates if the filter settings should be inverted, i.e. the loop will run over all features not fulfilling the specified criteria.", false));
		types.add(new ParameterTypeString(PARAMETER_ITERATION_MACRO, "The name of the macro which holds the name of the current feature in each iteration.", DEFAULT_ITERATION_MACRO_NAME));
		return types;
	}
}
