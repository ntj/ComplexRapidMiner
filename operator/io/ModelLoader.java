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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.rapidminer.operator.AbstractIOObject;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;


/**
 * Reads a {@link com.rapidminer.operator.Model} from a file that was generated
 * by an operator like {@link com.rapidminer.operator.learner.Learner} in a
 * previous process. Once a model is generated, it can be applied several
 * times to newly acquired data using a model loader, an {@link ExampleSource},
 * and a {@link com.rapidminer.operator.ModelApplier}.
 * 
 * @see com.rapidminer.operator.Model
 * @see com.rapidminer.operator.ModelApplier
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: ModelLoader.java,v 1.6 2008/07/07 07:06:38 ingomierswa Exp $
 */
public class ModelLoader extends Operator {


	/** The parameter name for &quot;Filename containing the model to load.&quot; */
	public static final String PARAMETER_MODEL_FILE = "model_file";
	private static final Class[] INPUT_CLASSES = {};

	private static final Class[] OUTPUT_CLASSES = { Model.class };

	public ModelLoader(OperatorDescription description) {
		super(description);
	}

	/** Reads the model from disk and returns it. */
	public IOObject[] apply() throws OperatorException {
		File modelFile = getParameterAsFile(PARAMETER_MODEL_FILE);

		Model model = null;
		try {
            // try if the model was written as a serializable model
			ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(modelFile));
			model = (Model)objectIn.readObject();
			objectIn.close();
		} catch (Exception e) {
            // if not serialized, then try the usual model serialization (xml)
			InputStream in = null;
	        try {
	            in = new GZIPInputStream(new FileInputStream(modelFile));
	        } catch (IOException e1) {
	            try {
	                // maybe already uncompressed?
	                in = new FileInputStream(modelFile);
	            } catch (IOException e2) {
	                throw new UserError(this, e, 302, new Object[] { modelFile, e2.getMessage() });
	            }
	        }
	        
			try {
				model = (Model)AbstractIOObject.read(in);
	            in.close();
			} catch (IOException e3) {
				throw new UserError(this, e, 302, new Object[] { modelFile, e3.getMessage() });
			}
		}
			
		return new IOObject[] { model };
	}

	public Class<?>[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class<?>[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_MODEL_FILE, "Filename containing the model to load.", "mod", false));
		return types;
	}

}
