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

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.visualization.ExampleVisualizationOperator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;


/**
 * This operator adds an ID attribute to the given example set. Each example is
 * tagged with an incremental integer number. If the example set already
 * contains an id attribute, the old attribute will be removed before the new
 * one is added.
 * 
 * @author Ingo Mierswa
 * @version $Id: IdTagging.java,v 1.6 2008/05/09 19:22:54 ingomierswa Exp $
 */
public class IdTagging extends Operator {


	/** The parameter name for &quot;True if nominal ids (instead of integer ids) should be created&quot; */
	public static final String PARAMETER_CREATE_NOMINAL_IDS = "create_nominal_ids";
	private static final Class[] INPUT_CLASSES = { ExampleSet.class };

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	public IdTagging(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet eSet = getInput(ExampleSet.class);

		// only warning, removing is done by createSpecialAttribute(...)
		Attribute idAttribute = eSet.getAttributes().getId();
		if (idAttribute != null) {
			logWarning("Overwriting old id attribute!");
		}

		// create new id attribute
		boolean nominalIds = getParameterAsBoolean(PARAMETER_CREATE_NOMINAL_IDS);
		idAttribute = Tools.createSpecialAttribute(eSet, Attributes.ID_NAME, nominalIds ? Ontology.NOMINAL : Ontology.INTEGER);

		// set IDs
		int currentId = 1;
		Iterator<Example> r = eSet.iterator();
		while (r.hasNext()) {
			Example example = r.next();
			example.setValue(idAttribute, nominalIds ? idAttribute.getMapping().mapString("id_" + currentId) : currentId);
			currentId++;
			checkForStop();
		}

        // initialize example visualizer
        Operator visualizer = null;
        try {
            visualizer = OperatorService.createOperator(ExampleVisualizationOperator.class);
        } catch (OperatorCreationException e) {
            logNote("Cannot initialize example visualizer, skipping...");
        }
        if (visualizer != null)
            visualizer.apply(new IOContainer(eSet));
        
		return new IOObject[] { eSet };
	}

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeBoolean(PARAMETER_CREATE_NOMINAL_IDS, "True if nominal ids (instead of integer ids) should be created", false);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
