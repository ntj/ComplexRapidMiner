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
package com.rapidminer.operator.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.rapidminer.example.AttributeWeights;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;


/**
 * Reads the weights for all attributes of an example set from a file and
 * creates a new {@link AttributeWeights} IOObject. This object can be used for
 * scaling the values of an example set with help of the
 * {@link com.rapidminer.operator.features.AttributeWeightsApplier} operator.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeWeightsLoader.java,v 1.9 2006/04/05 08:57:25
 *          ingomierswa Exp $
 */
public class AttributeWeightsLoader extends Operator {


	/** The parameter name for &quot;Filename of the attribute weights file.&quot; */
	public static final String PARAMETER_ATTRIBUTE_WEIGHTS_FILE = "attribute_weights_file";
	private static final Class[] INPUT_CLASSES = {};

	private static final Class[] OUTPUT_CLASSES = { AttributeWeights.class };

	public AttributeWeightsLoader(OperatorDescription description) {
		super(description);
	}

	/** Writes the attribute set to a file. */
	public IOObject[] apply() throws OperatorException {
		File weightFile = getParameterAsFile(PARAMETER_ATTRIBUTE_WEIGHTS_FILE);
		AttributeWeights result = null;
		try {
			result = AttributeWeights.load(weightFile);
		} catch (IOException e) {
			throw new UserError(this, e, 302, new Object[] { weightFile, e.getMessage() });
		}
		return new IOObject[] { result };
	}

	public Class<?>[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class<?>[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_ATTRIBUTE_WEIGHTS_FILE, "Filename of the attribute weights file.", "wgt", false));
		return types;
	}
}
