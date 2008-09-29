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
 * Writes the weights of all attributes of an example set to a file. Therefore a
 * {@link AttributeWeights} object is needed in the input of this operator. Each
 * line holds the name of one attribute and its weight. This file can be read in
 * another process using the {@link AttributeWeightsLoader} and the
 * {@link com.rapidminer.operator.features.AttributeWeightsApplier}.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeWeightsWriter.java,v 1.10 2006/04/05 08:57:25
 *          ingomierswa Exp $
 */
public class AttributeWeightsWriter extends Operator {


	/** The parameter name for &quot;Filename for the attribute weight file.&quot; */
	public static final String PARAMETER_ATTRIBUTE_WEIGHTS_FILE = "attribute_weights_file";
	private static final Class[] INPUT_CLASSES = { AttributeWeights.class };

	private static final Class[] OUTPUT_CLASSES = { AttributeWeights.class };

	public AttributeWeightsWriter(OperatorDescription description) {
		super(description);
	}

	/** Writes the attribute set to a file. */
	public IOObject[] apply() throws OperatorException {
		File weightFile = getParameterAsFile(PARAMETER_ATTRIBUTE_WEIGHTS_FILE);
		AttributeWeights weights = getInput(AttributeWeights.class);

		try {
			weights.writeAttributeWeights(weightFile, getEncoding());
		} catch (IOException e) {
			throw new UserError(this, e, 303, new Object[] { weightFile, e.getMessage() });
		}

		return new IOObject[] { weights };
	}

	public Class<?>[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class<?>[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_ATTRIBUTE_WEIGHTS_FILE, "Filename for the attribute weight file.", "wgt", false));
		return types;
	}
}
