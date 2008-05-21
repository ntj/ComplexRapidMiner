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
package com.rapidminer.operator.olap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.NonSpecialAttributesExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.math.SignificanceTestResult;


/**
 * <p>This operator calculates the significance of difference for the values for 
 * all numerical attributes depending on the groups defined by all nominal attributes.
 * Please refer to the operator {@link GroupedANOVAOperator} for details of the
 * calculation.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: ANOVAMatrixOperator.java,v 1.4 2008/05/09 19:23:24 ingomierswa Exp $
 */
public class ANOVAMatrixOperator extends Operator {

	public ANOVAMatrixOperator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet inputSet = getInput(ExampleSet.class);
		ExampleSet exampleSet = new NonSpecialAttributesExampleSet(inputSet);
		
		// determine anova and grouping attributes
		List<String> nominalAttributes = new ArrayList<String>();
		List<String> numericalAttributes = new ArrayList<String>();
		Iterator<Attribute> a = exampleSet.getAttributes().allAttributes();
		while (a.hasNext()) {
			Attribute attribute = a.next();
			if (attribute.isNominal())
				nominalAttributes.add(attribute.getName());
			else
				numericalAttributes.add(attribute.getName());
		}
		
		// init "inner" operator
		Operator groupedAnovaOperator = null;
		try {
			groupedAnovaOperator = OperatorService.createOperator(GroupedANOVAOperator.class);
		} catch (OperatorCreationException e) {
			throw new UserError(this, 109, GroupedANOVAOperator.class.getName());
		}
		double significanceLevel = getParameterAsDouble(GroupedANOVAOperator.PARAMETER_SIGNIFICANCE_LEVEL);
		groupedAnovaOperator.setParameter(GroupedANOVAOperator.PARAMETER_SIGNIFICANCE_LEVEL, significanceLevel + "");
		groupedAnovaOperator.setParameter(GroupedANOVAOperator.PARAMETER_ONLY_DISTINCT, getParameterAsBoolean(GroupedANOVAOperator.PARAMETER_ONLY_DISTINCT) + "");
		
		// calculate all values
		double[][] probabilities = new double[numericalAttributes.size()][nominalAttributes.size()];
		for (int numericalCounter = 0; numericalCounter < probabilities.length; numericalCounter++) {
			String numericalAttributeName = numericalAttributes.get(numericalCounter);
			for (int nominalCounter = 0; nominalCounter < probabilities[numericalCounter].length; nominalCounter++) {
				String nominalAttributeName = nominalAttributes.get(nominalCounter);
				groupedAnovaOperator.setParameter(GroupedANOVAOperator.PARAMETER_ANOVA_ATTRIBUTE, numericalAttributeName);
				groupedAnovaOperator.setParameter(GroupedANOVAOperator.PARAMETER_GROUP_BY_ATTRIBUTE, nominalAttributeName);
				SignificanceTestResult testResult = groupedAnovaOperator.apply(new IOContainer(new IOObject[] { (ExampleSet)exampleSet.clone() })).get(SignificanceTestResult.class);
				probabilities[numericalCounter][nominalCounter] = testResult.getProbability();
			}			
		}
		
		// create and return result
		return new IOObject[] { new ANOVAMatrix(probabilities, numericalAttributes, nominalAttributes, significanceLevel) };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ANOVAMatrix.class };	
	}
	
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeDouble(GroupedANOVAOperator.PARAMETER_SIGNIFICANCE_LEVEL, "The significance level for the ANOVA calculation.", 0.0d, 1.0d, 0.05d));
        types.add(new ParameterTypeBoolean(GroupedANOVAOperator.PARAMETER_ONLY_DISTINCT, "Indicates if only rows with distinct values for the aggregation attribute should be used for the calculation of the aggregation function.", false));
        return types;
    }
}
