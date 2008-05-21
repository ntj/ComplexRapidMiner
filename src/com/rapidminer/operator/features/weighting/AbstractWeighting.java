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
package com.rapidminer.operator.features.weighting;

import java.util.List;

import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;

/**
 * This is an abstract superclass for RapidMiner weighting operators. New weighting 
 * schemes should extend this class to support the same normalization parameter as 
 * other weighting operators.
 * 
 * @author Helge Homburg
 * @version $Id: AbstractWeighting.java,v 1.3 2008/05/09 19:23:22 ingomierswa Exp $
 */
public abstract class AbstractWeighting extends Operator{

	/** The parameter name for &quot;Activates the normalization of all weights.&quot; */
	public static final String PARAMETER_NORMALIZE_WEIGHTS = "normalize_weights";
	
	public AbstractWeighting(OperatorDescription description) {
		super(description);
	}
	
	public abstract AttributeWeights calculateWeights(ExampleSet exampleSet) throws OperatorException;
	
	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		AttributeWeights weights = calculateWeights(exampleSet);
		if (getParameterAsBoolean(PARAMETER_NORMALIZE_WEIGHTS)) {
			weights.normalize();
		}
		return new IOObject[] {exampleSet, weights};
	}
	
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class, AttributeWeights.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> list = super.getParameterTypes();
		list.add(new ParameterTypeBoolean(PARAMETER_NORMALIZE_WEIGHTS, "Activates the normalization of all weights.", true));
		return list;
	}
}
