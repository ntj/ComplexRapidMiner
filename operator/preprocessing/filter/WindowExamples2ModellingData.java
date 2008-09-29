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
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;

/**
 * <p>
 * This operator performs several transformations which could be performed
 * by basic RapidMiner operators but lead to complex operator chains. Therefore,
 * this operator can be used as a shortcut.
 * </p>
 * 
 * <p>
 * The basic idea is to apply this operator on a windowed data set like those
 * create be the (Multivariate)Series2WindowExamples operators. Please note that
 * only series data sets where the series was represented by the examples (rows)
 * are supported by this operator due to the naming conventions of the 
 * resulting attributes.
 * </p>
 * 
 * <p>
 * This operator performs three basic tasks. First, it removed all attributes lying 
 * between the time point zero (attribute name ending &quot;-0&quot;) and the time
 * point before horizon values. Second, it transforms the corresponding time point zero
 * of the specified label stem to the actual label. Last, it re-represents all values 
 * relative to the last known time value for each original dimension including the 
 * label value.
 * </p>
 * 
 * @author Ingo Mierswa
 * @version $Id: WindowExamples2ModellingData.java,v 1.1 2008/09/08 18:53:49 ingomierswa Exp $
 */
public class WindowExamples2ModellingData extends Operator {

	public static final String PARAMETER_LABEL_NAME_STEM = "label_name_stem";
	
	public static final String PARAMETER_HORIZON = "horizon";
	
	public static final String PARAMETER_RELATIVE_TRANSFORMATION = "relative_transformation";
	
	
	public WindowExamples2ModellingData(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		String labelName = getParameterAsString(PARAMETER_LABEL_NAME_STEM);
		int horizon = getParameterAsInt(PARAMETER_HORIZON);
		
		// TODO: check if appropriate label is there
		// TODO: check if window width is large enough
		
		// collect base names and attributes to remove, find label
		Attribute labelAttribute = null;
		List<String> baseNames = new LinkedList<String>();
		List<Attribute> toRemove = new LinkedList<Attribute>();
		int windowWidth = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (attribute.getName().endsWith("-0")) {
				String baseName = attribute.getName().substring(0, attribute.getName().lastIndexOf("-")); 
				baseNames.add(baseName);
				if (attribute.getName().startsWith(labelName)) {
					labelAttribute = attribute;
				}
			}
			
			int index = Integer.valueOf(attribute.getName().substring(attribute.getName().lastIndexOf("-") + 1));
			windowWidth = Math.max(windowWidth, index);
			
			if ((index >= 0) && (index < horizon)) {
				toRemove.add(attribute);
			}
		}
		
		// remove horizon attributes
		for (Attribute attribute : toRemove) {
			exampleSet.getAttributes().remove(attribute);
		}
		
		// set label
		exampleSet.getAttributes().setLabel(labelAttribute);
		
		// transform all values relative to last known label attribute value and create base value column
		if (getParameterAsBoolean(PARAMETER_RELATIVE_TRANSFORMATION)) {
			if (labelAttribute.isNumerical()) {
				Attribute baseValueAttribute = AttributeFactory.createAttribute("base_value", Ontology.REAL);
				exampleSet.getExampleTable().addAttribute(baseValueAttribute);
				exampleSet.getAttributes().setSpecialAttribute(baseValueAttribute, "base_value");

				for (Example example : exampleSet) {
					// handle label
					String lastKnownLabelName = labelName + "-" + horizon;
					Attribute lastKnownLabelAttribute = exampleSet.getAttributes().get(lastKnownLabelName);
					double baseLabelValue = example.getValue(lastKnownLabelAttribute);
					example.setValue(baseValueAttribute, baseLabelValue);

					for (String baseName : baseNames) {
						String lastKnownBaseName = baseName + "-" + horizon;
						Attribute lastKnownBaseAttribute = exampleSet.getAttributes().get(lastKnownBaseName);
						double baseAttributeValue = example.getValue(lastKnownBaseAttribute);

						for (int w = horizon; w <= windowWidth; w++) {
							String currentName = baseName + "-" + w;
							Attribute currentAttribute = exampleSet.getAttributes().get(currentName);
							double currentValue = example.getValue(currentAttribute);

							example.setValue(currentAttribute, currentValue - baseAttributeValue);
						}
					}

					example.setValue(labelAttribute, example.getValue(labelAttribute) - baseLabelValue);
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
		types.add(new ParameterTypeString(PARAMETER_LABEL_NAME_STEM, "The name stem of the label attribute.", false));
		types.add(new ParameterTypeInt(PARAMETER_HORIZON, "The horizon for the prediction.", 1, Integer.MAX_VALUE, 1));
		types.add(new ParameterTypeBoolean(PARAMETER_RELATIVE_TRANSFORMATION, "Indicates if a relative transformation of value should be performed", true));
		return types;
	}
}
