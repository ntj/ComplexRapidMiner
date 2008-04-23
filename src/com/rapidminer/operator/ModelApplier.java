/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;


/**
 * This operator applies a {@link Model} to an {@link ExampleSet}. All
 * parameters of the training process should be stored within the model.
 * However, this operator is able to take any parameters for the rare case that
 * the model can use some parameters during application. Models can be read from
 * a file by using a {@link com.rapidminer.operator.io.ModelLoader}.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ModelApplier.java,v 1.2 2007/06/15 16:58:40 ingomierswa Exp $
 */
public class ModelApplier extends Operator {


	/** The parameter name for &quot;value&quot; */
	public static final String PARAMETER_KEY = "key";
	public static final String PARAMETER_APPLICATION_PARAMETERS = "application_parameters";
	
	public ModelApplier(OperatorDescription description) {
		super(description);
	}

	/**
	 * Applies the operator and labels the {@link ExampleSet}. The example set
	 * in the input is not consumed.
	 */
	public IOObject[] apply() throws OperatorException {
		ExampleSet inputExampleSet = getInput(ExampleSet.class);
		Model model = getInput(Model.class);

		log("Set parameters for " + model.getClass().getName());
		List modelParameters = getParameterList(PARAMETER_APPLICATION_PARAMETERS);
		Iterator i = modelParameters.iterator();
		while (i.hasNext()) {
			Object[] parameter = (Object[]) i.next();
			model.setParameter((String) parameter[0], (String) parameter[1]);
		}

		log("Applying " + model.getClass().getName());
		try {
			model.apply(inputExampleSet);
		} catch (UserError e) {
			if (e.getOperator() == null)
				e.setOperator(this);
			throw e;
		}
		return new IOObject[] { inputExampleSet };
	}

	/** Indicates that the consumption of Models can be user defined. */
	public InputDescription getInputDescription(Class cls) {
		if (Model.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	public Class[] getInputClasses() {
		return new Class[] { Model.class, ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}	
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeList(PARAMETER_APPLICATION_PARAMETERS, "Model parameters for application (usually not needed).", new ParameterTypeString(PARAMETER_KEY, "value")));
		return types;
	}
}
