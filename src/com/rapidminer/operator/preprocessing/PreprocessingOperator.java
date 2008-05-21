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
package com.rapidminer.operator.preprocessing;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;

/**
 * Superclass for all preprocessing operators. Classes which extends this class
 * must implement the method {@link #createPreprocessingModel(ExampleSet)}.
 * This method can also be returned by this operator and will be combined with
 * other models.
 * 
 * @author Ingo Mierswa
 * @version $Id: PreprocessingOperator.java,v 1.4 2006/04/12 18:04:24
 *          ingomierswa Exp $
 */
public abstract class PreprocessingOperator extends Operator {

	/** The parameter name for &quot;Indicates if the preprocessing model should also be returned&quot; */
	public static final String PARAMETER_RETURN_PREPROCESSING_MODEL = "return_preprocessing_model";
	
	/** Indicates if this operator should create a view (new example set on the view stack) instead of directly changing the data. */
	public static final String PARAMETER_CREATE_VIEW = "create_view";
	
	public PreprocessingOperator(OperatorDescription description) {
		super(description);
	}

	public abstract Model createPreprocessingModel(ExampleSet exampleSet) throws OperatorException;

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);

		Model model = createPreprocessingModel(exampleSet);
		model.setParameter(PARAMETER_CREATE_VIEW, getParameterAsBoolean(PARAMETER_CREATE_VIEW));
		exampleSet = model.apply(exampleSet);

		if (getParameterAsBoolean(PARAMETER_RETURN_PREPROCESSING_MODEL)) {
			return new IOObject[] { exampleSet, model };
		} else {
			return new IOObject[] { exampleSet };
		}
	}

	public Class[] getOutputClasses() {
        if (getParameterAsBoolean(PARAMETER_RETURN_PREPROCESSING_MODEL))
            return new Class[] { ExampleSet.class, Model.class };
        else
            return new Class[] { ExampleSet.class };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_RETURN_PREPROCESSING_MODEL, "Indicates if the preprocessing model should also be returned", false));
		types.add(new ParameterTypeBoolean(PARAMETER_CREATE_VIEW, "Create View to apply preprocessing instead of changing the data", false));
		return types;
	}
}
