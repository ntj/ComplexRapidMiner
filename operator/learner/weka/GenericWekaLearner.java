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
package com.rapidminer.operator.learner.weka;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.WekaInstancesAdaptor;
import com.rapidminer.tools.WekaLearnerCapabilities;
import com.rapidminer.tools.WekaTools;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.UnassignedClassException;

/**
 * Performs the Weka learning scheme with the same name. See the Weka javadoc
 * for further classifier and parameter descriptions.<br/>
 * 
 * @author Ingo Mierswa
 * @version $Id: GenericWekaLearner.java,v 1.18 2006/04/12 11:17:42 ingomierswa
 *          Exp $
 */
public class GenericWekaLearner extends AbstractLearner implements TechnicalInformationHandler {

	public static final String[] WEKA_CLASSIFIERS = WekaTools.getWekaClasses(weka.classifiers.Classifier.class, ".meta.", false);

	/** The list with the weka parameters. */
	private List<ParameterType> wekaParameters = new LinkedList<ParameterType>();

	public GenericWekaLearner(OperatorDescription description) {
		super(description);
	}

	public Model learn(ExampleSet exampleSet) throws OperatorException {
		Classifier classifier = getWekaClassifier(WekaTools.getWekaParametersFromTypes(this, wekaParameters));
		log("Converting to Weka instances.");
		Instances instances = WekaTools.toWekaInstances(exampleSet, "LearningInstances", WekaInstancesAdaptor.LEARNING);
		try {
			log("Building Weka classifier.");
			classifier.buildClassifier(instances);
        } catch (UnassignedClassException e) {
            throw new UserError(this, e, 105, new Object[] { getOperatorClassName(), e });            
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new UserError(this, e, 105, new Object[] { getOperatorClassName(), e });
		} catch (Exception e) {
			throw new UserError(this, e, 905, new Object[] { getOperatorClassName(), e.getMessage() });
		}
		return new WekaClassifier(exampleSet, getOperatorClassName(), classifier);
	}

	/**
	 * Returns the Weka classifier based on the subtype of this operator.
	 * Parameters must be either the complete set of parameters or null (not an
	 * empty array).
	 */
	private Classifier getWekaClassifier(String[] parameters) throws OperatorException {
		String classifierName = getWekaClassPath();
		Classifier classifier = null;
		try {
			classifier = Classifier.forName(classifierName, parameters);
		} catch (Exception e) {
			throw new UserError(this, e, 904, new Object[] { classifierName, e });
		}
		return classifier;
	}

	public TechnicalInformation getTechnicalInformation() {
		try {
			Classifier classifier = getWekaClassifier(null);
			if (classifier instanceof TechnicalInformationHandler)
				return ((TechnicalInformationHandler)classifier).getTechnicalInformation();
			else
				return null;
		} catch (OperatorException e) {
			return null;
		}
	}
	
	/**
	 * This method is used by the {@link GenericWekaLearner} to specify the
	 * learners name.
	 */
	public String getWekaClassPath() {
		String prefixName = getOperatorClassName();
		String actualName = prefixName.substring(WekaTools.WEKA_OPERATOR_PREFIX.length());
		for (int i = 0; i < WEKA_CLASSIFIERS.length; i++) {
			if (WEKA_CLASSIFIERS[i].endsWith(actualName)) {
				return WEKA_CLASSIFIERS[i];
			}
		}
		return null;
	}

	/**
	 * This method is used by the {@link GenericWekaMetaLearner} to specify the
	 * learners parameters.
	 */
	public List getWekaParameterList() {
		return wekaParameters;
	}

	/** Returns true. */
	public boolean onlyWarnForNonSufficientCapabilities() {
		return true;
	}
	
	public boolean supportsCapability(LearnerCapability capability) {
		Classifier classifier;
		try {
			classifier = getWekaClassifier(WekaTools.getWekaParametersFromTypes(this, wekaParameters));
		} catch (OperatorException e) {
			return true;
		}
		if (classifier != null) {
			try {
				return WekaLearnerCapabilities.supportsCapability(classifier, capability);
			} catch (Throwable t) {
				return true;
			}
		}
		return true;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		Classifier classifier = null;
		try {
			// parameters must be null, not an empty String[0] array!
			classifier = getWekaClassifier(null);
		} catch (OperatorException e) {
			throw new RuntimeException("Cannot instantiate Weka classifier " + getOperatorClassName() + ": " + e.getMessage());
		}
		wekaParameters = new LinkedList<ParameterType>();
		if (classifier != null) {
			WekaTools.addParameterTypes(classifier, types, wekaParameters, false, null);
		}
		return types;
	}
}
