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
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.tools.WekaInstancesAdaptor;
import com.rapidminer.tools.WekaTools;

import weka.attributeSelection.PrincipalComponents;
import weka.core.Instances;

/**
 * Builds the principal components of the given data. The user can specify the
 * amount of variance to cover in the original data when retaining the best
 * number of principal components. This operator makes use of the Weka
 * implementation <code>PrincipalComponent</code>.
 * 
 * @author Ingo Mierswa
 * @version $Id: PrincipalComponentsTransformation.java,v 1.1 2006/04/14
 *          13:07:13 ingomierswa Exp $
 */
public class PrincipalComponentsTransformation extends Operator {


	/** The parameter name for &quot;The minimum variance to cover in the original data to determine the number of principal components.&quot; */
	public static final String PARAMETER_MIN_VARIANCE_COVERAGE = "min_variance_coverage";
	public PrincipalComponentsTransformation(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);

		PrincipalComponents transformation = new PrincipalComponents();
		transformation.setNormalize(false); // if the user wants to normalize
											// the data he has to apply the
											// filter before
		transformation.setVarianceCovered(getParameterAsDouble(PARAMETER_MIN_VARIANCE_COVERAGE));

		log(getName() + ": Converting to Weka instances.");
		Instances instances = WekaTools.toWekaInstances(exampleSet, "PCAInstances", WekaInstancesAdaptor.LEARNING);
		try {
		    log(getName() + ": Building principal components.");
			transformation.buildEvaluator(instances);
		} catch (Exception e) {
			throw new UserError(this, e, 905, new Object[] { "PrincipalComponents", e });
		}

		ExampleSet result = null;
		try {
			Instances transformed = transformation.transformedData(instances);
			result = WekaTools.toRapidMinerExampleSet(transformed, "pc");
		} catch (Exception e) {
			throw new UserError(this, 905, "Principal Components Transformation", "Cannot convert to principal components (" + e.getMessage() + ")");
		}
		return new IOObject[] { result };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_MIN_VARIANCE_COVERAGE, "The minimum variance to cover in the original data to determine the number of principal components.", 0.0, 1.0, 0.95);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
