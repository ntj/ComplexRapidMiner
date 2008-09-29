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
package com.rapidminer.operator.preprocessing.filter.attributes;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionCreationException;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.parameter.conditions.EqualStringCondition;

/**
 * <p>This operator filters the attributes of an exampleSet. Therefore, different conditions may be selected as 
 * parameter and only attributes fulfilling this condition are kept. The rest will be removed from the exampleSet
 * The conditions may be inverted.
 * The conditions are tested over all attributes and for every attribute over all examples. For example the
 * numeric_value_filter with the parameter string &quot;&gt; 6&quot; will keep all nominal attributes and all numeric attributes
 * having a value of greater 6 in every example. A combination of conditions is possible: &quot;&gt; 6 ANDAND &lt; 11&quot; or &quot;&lt;= 5 || &lt; 0&quot;.
 * But ANDAND and || must not be mixed. Please note that ANDAND has to be replaced by two ampers ands.</p>
 
 * <p>The attribute_name_filter keeps all attributes which names match the given regular expression.
 * The nominal_value_filter keeps all numeric attribute and all nominal attributes containing at least one of specified 
 * nominal values. &quot;rainy ANDAND cloudy&quot; would keep all attributes containing at least one time &quot;rainy&quot; and one time &quot;cloudy&quot;.
 * &quot;rainy || sunny&quot; would keep all attributes containing at least one time &quot;rainy&quot; or one time &quot;sunny&quot;. ANDAND and || are not
 * allowed to be mixed. And again, ANDAND has to be replaced by two ampers ands.</p>
 *  
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: AttributeFilter.java,v 1.8 2008/07/19 16:31:17 ingomierswa Exp $
 */
public class AttributeFilter extends Operator {
	
	/** The parameter name for &quot;Implementation of the condition.&quot; */
	public static final String PARAMETER_CONDITION_NAME = "condition_class";

	/** The parameter name for &quot;Parameter string for the condition, e.g. 'attribute=value' for the AttributeValueFilter.&quot; */
	public static final String PARAMETER_PARAMETER_STRING = "parameter_string";

	/** The parameter name for &quot;Indicates if only examples should be accepted which would normally filtered.&quot; */
	public static final String PARAMETER_INVERT_FILTER = "invert_filter";
	
	
	public static final String[] CONDITION_NAMES = new String[] {
        "no_missing_values", 
        "numeric_value_filter",
        "attribute_name_filter",
        "is_nominal",
        "is_numerical",
        "is_date"
	};
	
	private static final String[] CONDITION_IMPLEMENTATIONS = { 
		NoMissingValuesAttributeFilter.class.getName(),
		NumericValueAttributeFilter.class.getName(),
		NameAttributeFilter.class.getName(),
		NominalAttributeFilter.class.getName(),
		NumericalAttributeFilter.class.getName(),
		DateAttributeFilter.class.getName()
	};
	
	public static final int CONDITION_NO_MISSING_VALUES     = 0;
	
	public static final int CONDITION_NUMERIC_VALUE_FILTER  = 1;
	
	public static final int CONDITION_ATTRIBUTE_NAME_FILTER = 2;
	
	public static final int CONDITION_IS_NOMINAL            = 3;
	
	public static final int CONDITION_IS_NUMERICAL          = 4;
	
	public static final int CONDITION_IS_DATE               = 5;
	
	
	public AttributeFilter(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		Attributes attributes = exampleSet.getAttributes();
		
		try {
			AttributeFilterCondition condition = createCondition(getParameterAsString(PARAMETER_CONDITION_NAME));
			
			// init and removing attributes not needed to checked per example
			String parameterString = getParameterAsString(PARAMETER_PARAMETER_STRING);
			boolean invert = getParameterAsBoolean(PARAMETER_INVERT_FILTER);
			List<Attribute> remainingAttributes = new LinkedList<Attribute>();
			Iterator<Attribute> iterator = attributes.iterator();
			while (iterator.hasNext()) {
				Attribute attribute = iterator.next();
				if (condition.beforeScanCheck(attribute, parameterString, invert)) {
					iterator.remove();
				} else {
					remainingAttributes.add(attribute);
				}
			}
			
			// now checking for every example
			if (condition.isNeedingScan()) {
				condition.initScanCheck();
				Iterator<Attribute> r = remainingAttributes.iterator();
				while (r.hasNext()) {
					Attribute attribute = r.next();
					boolean remove = false;
					for (Example example: exampleSet) {
						if (condition.check(attribute, example)) {
							remove = true;
							break;
						}
					}
					if (remove) {
						exampleSet.getAttributes().remove(attribute);
					}
				}
			}
		} catch (ConditionCreationException e) {
			throw new UserError(this, 904, getParameterAsString(PARAMETER_CONDITION_NAME), e.getMessage());
		}
		
		return new IOObject[] {exampleSet};
	}
	
	
	/**
	 * Checks if the given name is the short name of a known condition and
	 * creates it. If the name is not known, this method creates a new instance
	 * of className which must be an implementation of {@link Condition} by
	 * calling its two argument constructor passing it the example set and the
	 * parameter string
	 */
	public static AttributeFilterCondition createCondition(String name) throws ConditionCreationException {
		String className = name;
		for (int i = 0; i < CONDITION_NAMES.length; i++) {
			if (CONDITION_NAMES[i].equals(name)) {
				className = CONDITION_IMPLEMENTATIONS[i];
				break;
			}
		}
		try {
			Class<?> clazz = com.rapidminer.tools.Tools.classForName(className);
			return (AttributeFilterCondition) clazz.newInstance();
		} catch (ClassNotFoundException e) {
			throw new ConditionCreationException("Cannot find class '" + className + "'. Check your classpath.");
		} catch (IllegalAccessException e) {
			throw new ConditionCreationException("'" + className + "' cannot access two argument constructor " + className + "(ExampleSet, String)!");
		} catch (InstantiationException e) {
			throw new ConditionCreationException(className + ": cannot create condition (" + e.getMessage() + ").");
		} catch (Throwable e) {
			throw new ConditionCreationException(className + ": cannot invoke condition (" + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()) + ").");
		}
	}	
	
	public Class<?>[] getInputClasses() {
		return  new Class[] { ExampleSet.class };
	}

	public Class<?>[] getOutputClasses() {
		return  new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeStringCategory(PARAMETER_CONDITION_NAME, "Implementation of the condition.", CONDITION_NAMES, CONDITION_NAMES[0]);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeString(PARAMETER_PARAMETER_STRING, "Parameter string for the condition, e.g. '>= 5' for the numerical value filter.", true);
		type.registerDependencyCondition(new EqualStringCondition(this, PARAMETER_CONDITION_NAME, true, CONDITION_NAMES[CONDITION_NUMERIC_VALUE_FILTER], CONDITION_NAMES[CONDITION_ATTRIBUTE_NAME_FILTER]));		
		type.setExpert(false);
		types.add(type);
        types.add(new ParameterTypeBoolean(PARAMETER_INVERT_FILTER, "Indicates if only attributes should be accepted which would normally filtered.", false));
		return types;
	}
}
