package com.rapidminer.operator.learner.functions;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.SimplePredictionModel;
import com.rapidminer.tools.Tools;

public class PolynomialRegressionModel extends SimplePredictionModel {
	
	private static final long serialVersionUID = 5503523600824976254L;

	private String[] attributeNames;
	
	private double[][] coefficients;
	
	private double[][] degrees;
	
	private double offset;
	
	public PolynomialRegressionModel(ExampleSet exampleSet, double[][] coefficients, double[][] degrees, double offset) {
		super(exampleSet);
		this.attributeNames = com.rapidminer.example.Tools.getRegularAttributeNamesOrConstructions(exampleSet);
		this.coefficients = coefficients;
		this.degrees = degrees;
		this.offset = offset;
	}
	
	public double predict(Example example) throws OperatorException {
		return calculatePrediction(example, coefficients, degrees, offset);
	}
	
	public static double calculatePrediction(Example example, double[][] coefficients, double[][] degrees, double offset) {
		double prediction = 0;
		int index = 0;
		for (Attribute attribute : example.getAttributes()) {
			double value = example.getValue(attribute);
			for (int f = 0; f < coefficients.length; f++) {
				prediction += coefficients[f][index] * Math.pow(value, degrees[f][index]);
			}
			index++;
		}
		prediction += offset;
		return prediction;		
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		boolean first = true;
		int index = 0;
		for (int i = 0; i < attributeNames.length; i++) {
			for (int f = 0; f < coefficients.length; f++) {
				result.append(getCoefficientString(coefficients[f][index], first) + " * " + attributeNames[i] + " ^ " + Tools.formatNumber(degrees[f][i]) + Tools.getLineSeparator());
				first = false;
			}
			index++;
		}
		result.append(getCoefficientString(offset, first));
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
