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
package com.rapidminer.operator.features.transformation;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.preprocessing.PreprocessingOperator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;

/**
 * Abstract class representing some common functionality of dimensionality reduction methods. 
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: DimensionalityReducer.java,v 1.6 2008/05/09 19:22:52 ingomierswa Exp $
 */
public abstract class DimensionalityReducer extends Operator {

	/** The parameter name for &quot;the number of dimensions in the result representation&quot; */
	public static final String PARAMETER_DIMENSIONS = "dimensions";
	
	public DimensionalityReducer(OperatorDescription description) {
		super(description);
	}

	/**
	 * Perform the actual dimensionality reduction.
	 */
	protected abstract double[][] dimensionalityReduction(ExampleSet es, int dimensions);

	public IOObject[] apply() throws OperatorException {
		ExampleSet es = getInput(ExampleSet.class);
		int dimensions = getParameterAsInt(PARAMETER_DIMENSIONS);

		Tools.onlyNumericalAttributes(es, "dimensionality reduction");
		Tools.isNonEmpty(es);
		Tools.checkAndCreateIds(es);

		double[][] p = dimensionalityReduction(es, dimensions);

		DimensionalityReducerModel model = new DimensionalityReducerModel(es, p, dimensions);
		ExampleSet result = model.apply(es);
		
		if (getParameterAsBoolean(PreprocessingOperator.PARAMETER_RETURN_PREPROCESSING_MODEL)) {
			return new IOObject[] { result, model };
		} else {
			return new IOObject[] { result };
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
		types.add(new ParameterTypeBoolean(PreprocessingOperator.PARAMETER_RETURN_PREPROCESSING_MODEL, "Indicates if the preprocessing model should also be returned", false));
		ParameterType type = new ParameterTypeInt(PARAMETER_DIMENSIONS, "the number of dimensions in the result representation", 1, Integer.MAX_VALUE, 2);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
