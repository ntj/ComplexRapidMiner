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
package com.rapidminer.operator.features.weighting;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.WekaInstancesAdaptor;
import com.rapidminer.tools.WekaTools;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.AttributeEvaluator;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.UnassignedClassException;

/**
 * Performs the AttributeEvaluator of Weka with the same name to determine a
 * sort of attribute relevance. These relevance values build an instance of
 * AttributeWeights. Therefore, they can be used by other operators which make
 * use of such weights, like weight based selection or search heuristics which
 * use attribute weights to speed up the search. See the Weka javadoc for
 * further operator and parameter descriptions.
 * 
 * @author Ingo Mierswa
 * @version $Id: GenericWekaAttributeWeighting.java,v 1.10 2006/04/05 09:42:01
 *          ingomierswa Exp $
 */
public class GenericWekaAttributeWeighting extends Operator implements TechnicalInformationHandler {

	public static final String[] WEKA_ATTRIBUTE_EVALUATORS = WekaTools.getWekaClasses(AttributeEvaluator.class);

	/** The list with the weka parameters. */
	private List<ParameterType> wekaParameters = new LinkedList<ParameterType>();

	public GenericWekaAttributeWeighting(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		AttributeWeights weights = new AttributeWeights();

		AttributeEvaluator evaluator = getWekaAttributeEvaluator(getOperatorClassName(), WekaTools.getWekaParametersFromTypes(this, wekaParameters));

		log("Converting to Weka instances.");
		Instances instances = WekaTools.toWekaInstances(exampleSet, "WeightingInstances", WekaInstancesAdaptor.WEIGHTING);
		try {
			log("Building Weka attribute evaluator.");
			evaluator.buildEvaluator(instances);
        } catch (UnassignedClassException e) {
            throw new UserError(this, e, 105, new Object[] { getOperatorClassName(), e });            
		} catch (ArrayIndexOutOfBoundsException e) {
            throw new UserError(this, e, 105, new Object[] { getOperatorClassName(), e });
		} catch (Exception e) {
			throw new UserError(this, e, 905, new Object[] { getOperatorClassName(), e });
		}

		int index = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			try {
				double result = evaluator.evaluateAttribute(index++);
				weights.setWeight(attribute.getName(), result);
			} catch (Exception e) {
				logWarning("Cannot evaluate attribute '" + attribute.getName() + "', use unknown weight.");
			}
		}

		// normalize
		weights.normalize();
		
		return new IOObject[] { exampleSet, weights };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class, AttributeWeights.class };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	/**
	 * Returns the Weka attribute evaluator based on the subtype of this
	 * operator.
	 */
	private AttributeEvaluator getWekaAttributeEvaluator(String prefixName, String[] parameters) throws OperatorException {
		String actualName = prefixName.substring(WekaTools.WEKA_OPERATOR_PREFIX.length());
		String evaluatorName = null;
		for (int i = 0; i < WEKA_ATTRIBUTE_EVALUATORS.length; i++) {
			if (WEKA_ATTRIBUTE_EVALUATORS[i].endsWith(actualName)) {
				evaluatorName = WEKA_ATTRIBUTE_EVALUATORS[i];
				break;
			}
		}
		AttributeEvaluator evaluator = null;
		try {
			evaluator = (AttributeEvaluator) ASEvaluation.forName(evaluatorName, parameters);
		} catch (Exception e) {
			throw new UserError(this, e, 904, new Object[] { evaluatorName, e });
		}
		return evaluator;
	}

	public TechnicalInformation getTechnicalInformation() {
		try {
			AttributeEvaluator evaluator = getWekaAttributeEvaluator(getOperatorClassName(), null);
			if (evaluator instanceof TechnicalInformationHandler)
				return ((TechnicalInformationHandler)evaluator).getTechnicalInformation();
			else
				return null;
		} catch (OperatorException e) {
			return null;
		}
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		AttributeEvaluator evaluator = null;
		try {
			// parameters must be null, not an empty String[0] array!
			evaluator = getWekaAttributeEvaluator(getOperatorClassName(), null);
		} catch (OperatorException e) {
			throw new RuntimeException("Cannot instantiate Weka attribute evaluator " + getOperatorClassName() + ": " + e.getMessage());
		}
		wekaParameters = new LinkedList<ParameterType>();
		if ((evaluator != null) && (evaluator instanceof OptionHandler)) {
			WekaTools.addParameterTypes((OptionHandler) evaluator, types, wekaParameters, false, null);
		}
		return types;
	}
}
