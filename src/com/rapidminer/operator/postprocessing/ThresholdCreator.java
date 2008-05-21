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
package com.rapidminer.operator.postprocessing;

import java.util.List;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeString;


/**
 * This operator creates a user defined threshold for crisp classifying based on
 * prediction confidences.
 * 
 * @author Ingo Mierswa
 * @version $Id: ThresholdCreator.java,v 1.4 2008/05/09 19:23:27 ingomierswa Exp $
 */
public class ThresholdCreator extends Operator {


	/** The parameter name for &quot;The confidence threshold to determine if the prediction should be positive.&quot; */
	public static final String PARAMETER_THRESHOLD = "threshold";

	/** The parameter name for &quot;The class which should be considered as the first one (confidence 0).&quot; */
	public static final String PARAMETER_FIRST_CLASS = "first_class";

	/** The parameter name for &quot;The class which should be considered as the second one (confidence 1).&quot; */
	public static final String PARAMETER_SECOND_CLASS = "second_class";
	public ThresholdCreator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		double threshold = getParameterAsDouble(PARAMETER_THRESHOLD);
		String negativeClass = getParameterAsString(PARAMETER_FIRST_CLASS);
		String positiveClass = getParameterAsString(PARAMETER_SECOND_CLASS);
		return new IOObject[] { new Threshold(threshold, negativeClass, positiveClass) };
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return new Class[] { Threshold.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> list = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_THRESHOLD, "The confidence threshold to determine if the prediction should be positive.", 0, 1.0d, 0.5d);
		type.setExpert(false);
		list.add(type);
		list.add(new ParameterTypeString(PARAMETER_FIRST_CLASS, "The class which should be considered as the first one (confidence 0).", false));
		list.add(new ParameterTypeString(PARAMETER_SECOND_CLASS, "The class which should be considered as the second one (confidence 1).", false));
		return list;
	}
}
