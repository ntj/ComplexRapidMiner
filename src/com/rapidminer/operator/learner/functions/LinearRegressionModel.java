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
package com.rapidminer.operator.learner.functions;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.SimplePredictionModel;
import com.rapidminer.tools.Tools;


/**
 * The model for linear regression.
 * 
 * @author Ingo Mierswa
 * @version $Id: LinearRegressionModel.java,v 1.5 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class LinearRegressionModel extends SimplePredictionModel {

	private static final long serialVersionUID = 8381268071090932037L;

	private String[] attributeNames;
	
	private boolean[] selectedAttributes;
	
	private double[] coefficients;
	
	public LinearRegressionModel(ExampleSet exampleSet, boolean[] selectedAttributes, double[] coefficients) {
		super(exampleSet);
		this.attributeNames = com.rapidminer.example.Tools.getRegularAttributeNames(exampleSet);
		this.selectedAttributes = selectedAttributes;
		this.coefficients = coefficients;
	}
	
	public double predict(Example example) throws OperatorException {
		double prediction = 0;
		int index = 0;
		int attributeCounter = 0;
		for (Attribute attribute : example.getAttributes()) {
			if (selectedAttributes[attributeCounter]) {
				prediction += coefficients[index] * example.getValue(attribute);
				index++;
			}
			attributeCounter++;
		}
		prediction += coefficients[index];
		return prediction;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		boolean first = true;
		int index = 0;
		for (int i = 0; i < selectedAttributes.length; i++) {
			if (selectedAttributes[i]) {
				result.append(getCoefficientString(coefficients[index], first) + " * " + attributeNames[i] + Tools.getLineSeparator());
				index++;
				first = false;
			}
		}
		result.append(getCoefficientString(coefficients[coefficients.length - 1], first));
		return result.toString();
	}
	
	private String getCoefficientString(double coefficient, boolean first) {
		if (!first) {
			if (coefficient >= 0)
				return "+ " + Tools.formatNumber(Math.abs(coefficient));
			else
				return "- " + Tools.formatNumber(Math.abs(coefficient));
		} else {
			if (coefficient >= 0)
				return "  " + Tools.formatNumber(Math.abs(coefficient));
			else
				return "- " + Tools.formatNumber(Math.abs(coefficient));
		}
	}
}
