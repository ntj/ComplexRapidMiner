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
import java.io.ObjectInputStream;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Instances;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.weka.WekaClassifier;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.tools.WekaTools;

/**
 * This operator reads in model files which were saved from the Weka toolkit. For models
 * learned within RapidMiner please use always the {@link ModelLoader} operator even it the 
 * used learner was originally a Weka learner.
 * 
 * @author Ingo Mierswa
 * @version $Id: WekaModelLoader.java,v 1.7 2008/07/07 07:06:38 ingomierswa Exp $
 */
public class WekaModelLoader extends Operator {


	/** The parameter name for &quot;Filename containing the Weka model to load.&quot; */
	public static final String PARAMETER_MODEL_FILE = "model_file";
	public WekaModelLoader(OperatorDescription description) {
		super(description);
	}

	/** Reads the model from disk and returns it. */
	public IOObject[] apply() throws OperatorException {
		File modelFile = getParameterAsFile(PARAMETER_MODEL_FILE);

		Model model = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(modelFile));
			Classifier classifier = (Classifier)in.readObject();
			
			// try to find some infos about the label
            ExampleSet trainingExampleSet = null;
			try { 
				Instances trainingInstances = (Instances)in.readObject();
				trainingExampleSet = WekaTools.toRapidMinerExampleSet(trainingInstances);
			} catch (Throwable e) {
                 // no instances were written
				log("Problem during reading label information, just try without...");
			}
			in.close();
			
			model = new WekaClassifier(trainingExampleSet, "Weka Model", classifier);
		} catch (Exception e) {
			throw new UserError(this, e, 302, new Object[] { modelFile, e.getMessage() });
		}
		return new IOObject[] { model };
	}

	public Class<?>[] getInputClasses() {
		return new Class[0];
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { Model.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_MODEL_FILE, "Filename containing the Weka model to load.", "model", false));
		return types;
	}

}
