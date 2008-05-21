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
import com.rapidminer.operator.ContainerModel;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.features.transformation.ComponentWeightsCreatable;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * For models creating components like <code>PCA</code>, <code>GHA</code>
 * and <code>FastICA</code> you can create the <code>AttributeWeights</code>
 * from a component.
 * 
 * @author Daniel Hakenjos, Ingo Mierswa
 * @version $Id: ComponentWeights.java,v 1.5 2008/05/09 19:23:22 ingomierswa Exp $
 */
public class ComponentWeights extends Operator {

	/** The parameter name for &quot;Activates the normalization of all weights.&quot; */
	public static final String PARAMETER_NORMALIZE_WEIGHTS = "normalize_weights";
	
	/** The parameter name for &quot;Create the weights of this component.&quot; */
	public static final String PARAMETER_COMPONENT_NUMBER = "component_number";
    
	private static final Class[] INPUT_CLASSES = new Class[] { Model.class };

	private static final Class[] OUTPUT_CLASSES = new Class[] { Model.class, AttributeWeights.class };

	public ComponentWeights(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		Model model = getInput(Model.class);
		if (model instanceof ContainerModel) {
			while (model instanceof ContainerModel) {
				model = ((ContainerModel) model).getModel(0);
			}
		} else {
            if (!(model instanceof ComponentWeightsCreatable))
                return new IOObject[] { model };
		}
		if (!(model instanceof ComponentWeightsCreatable)) {
			throw new OperatorException(getName() + ": needs an input model wich implements the ComponentWeightsCreatable interface:" + model.getClass().getName());
		}

		int component = getParameterAsInt(PARAMETER_COMPONENT_NUMBER);
		AttributeWeights weights = ((ComponentWeightsCreatable) model).getWeightsOfComponent(component);

		// normalize
		if (getParameterAsBoolean(PARAMETER_NORMALIZE_WEIGHTS)) {
			weights.normalize();
		}
		
		return new IOObject[] { model, weights };
	}

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> list = super.getParameterTypes();
		list.add(new ParameterTypeBoolean(PARAMETER_NORMALIZE_WEIGHTS, "Activates the normalization of all weights.", false));
		list.add(new ParameterTypeInt(PARAMETER_COMPONENT_NUMBER, "Create the weights of this component.", 1, Integer.MAX_VALUE, 1));
		return list;
	}

}
