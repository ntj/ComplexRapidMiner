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

import java.util.Calendar;
import java.util.List;

import com.rapidminer.example.Attribute;
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
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;


/**
 * This operator changes a date attribute into a numerical one. It either
 * extracts numbers from the dates, e.g. the minute or hour from the actual
 * time the date captures or the quarter, year, etc. the date belongs to.
 * Alternatively, the operator allows to extract the number of milliseconds,
 * seconds, etc. from the epoch (01/01/1970 00:00:00 GMT) through the given dates.
 * 
 * @author Tobias Malbrecht
 * @version $Id: Date2Numerical.java,v 1.3 2008/08/12 12:49:17 tobiasmalbrecht Exp $
 */
public class Date2Numerical extends Operator {

	public static final String PARAMETER_ATTRIBUTE_NAME = "attribute_name";
	
	public static final String PARAMETER_RELATIVE_TO = "relative_to";
	
	public static final String[] RELATIVE_TO_MODES = { "epoch" , "superior unit" };
	
	public static final String PARAMETER_TIME_UNIT = "time_unit";
	
	public static final String PARAMETER_KEEP_OLD_ATTRIBUTE = "keep_old_attribute";
	
	public static final String[] TIME_UNITS = { "milliseconds", "seconds", "minutes", "hours", "day" , "week" , "month", "quarter", "year" };
	
	public Date2Numerical(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		String attributeName = getParameterAsString(PARAMETER_ATTRIBUTE_NAME);
		int timeUnit = getParameterAsInt(PARAMETER_TIME_UNIT);
		boolean relativeToEpoch = getParameterAsInt(PARAMETER_RELATIVE_TO) == 0;
		Attribute dateAttribute = exampleSet.getAttributes().get(attributeName);
		if (dateAttribute == null) {
			throw new UserError(this, 111, attributeName);
		}
		
		Attribute newAttribute = AttributeFactory.createAttribute(attributeName, Ontology.NUMERICAL);
		exampleSet.getExampleTable().addAttribute(newAttribute);
		exampleSet.getAttributes().addRegular(newAttribute);

		Calendar calendar = Calendar.getInstance();
		for (Example example : exampleSet) {
			example.setValue(newAttribute, Double.NaN);
			if (!Double.isNaN(example.getValue(dateAttribute))) {
				calendar.setTimeInMillis((long) example.getValue(dateAttribute));
				double timeValue = Double.NaN;

				if (relativeToEpoch) {
					switch (timeUnit) {
					case 0:
						timeValue = (long) calendar.getTimeInMillis();
						break;
					case 1:
						timeValue = (long) (calendar.getTimeInMillis() / 1000);
						break;
					case 2:
						timeValue = (long) (calendar.getTimeInMillis() / 60000);
						break;
					case 3:
						timeValue = (long) (calendar.getTimeInMillis() / 3600000);
						break;
					case 4:
						timeValue = (long) (calendar.getTimeInMillis() / 86400000);
						break;
					case 5:
						timeValue = (long) (calendar.getTimeInMillis() / 604800000L);
						break;
					case 6:
						timeValue = (long) (calendar.get(Calendar.MONTH) + 1) + (calendar.get(Calendar.YEAR) - 1970) * 12;
						break;
					case 7:
						timeValue = (long) ((long) (calendar.get(Calendar.MONTH) / 3 + 1) + (calendar.get(Calendar.YEAR) - 1970) * 4);
						break;
					case 8:
						timeValue = (long) (calendar.get(Calendar.YEAR) - 1970);
						break;
					default:
						break;
					}
				} else {
					switch (timeUnit) {
					case 0:
						timeValue = (long) calendar.get(Calendar.MILLISECOND);
						break;
					case 1:
						timeValue = (long) calendar.get(Calendar.SECOND);
						break;
					case 2:
						timeValue = (long) calendar.get(Calendar.MINUTE);
						break;
					case 3:
						timeValue = (long) calendar.get(Calendar.HOUR_OF_DAY);
						break;
					case 4:
						timeValue = (long) calendar.get(Calendar.DAY_OF_MONTH);
						break;
					case 5:
						timeValue = (long) calendar.get(Calendar.WEEK_OF_YEAR);
						break;
					case 6:
						timeValue = (long) (calendar.get(Calendar.MONTH) + 1);
						break;
					case 7:
						timeValue = (long) (calendar.get(Calendar.MONTH) / 3 + 1);
						break;
					case 8:
						timeValue = (long) calendar.get(Calendar.YEAR);
						break;
					default:
						break;
					}
				}
				example.setValue(newAttribute, timeValue);
			}
		}
		
		if (!getParameterAsBoolean(PARAMETER_KEEP_OLD_ATTRIBUTE)) {
			exampleSet.getAttributes().remove(dateAttribute);
		} else {
			newAttribute.setName(attributeName + "_" + TIME_UNITS[timeUnit]);
		}
		
		return new IOObject[]{exampleSet};
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_ATTRIBUTE_NAME, "The attribute which should be parsed.", false));
		types.add(new ParameterTypeCategory(PARAMETER_TIME_UNIT, "The unit in which the time is measured.", TIME_UNITS, 0));
		types.add(new ParameterTypeCategory(PARAMETER_RELATIVE_TO, "The date the actual date is set relative to.", RELATIVE_TO_MODES, 0));
		types.add(new ParameterTypeBoolean(PARAMETER_KEEP_OLD_ATTRIBUTE, "Indicates if the original date attribute should be kept.", false));
		return types;
	}
}
