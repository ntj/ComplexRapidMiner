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

import java.util.ArrayList;
import java.util.List;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.functions.kernel.functions.Kernel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;

/**
 * This operator performs a kernel-based principal components analysis (PCA).
 * Hence, the result will be the set of data points in a non-linearly 
 * transformed space. Please note that in contrast to the usual linear PCA
 * the kernel variant does also works for large numbers of attributes but 
 * will become slow for large number of examples.
 * 
 * @author Sebastian Land
 * @version $Id: KernelPCA.java,v 1.3 2008/07/13 16:39:42 ingomierswa Exp $
 */
public class KernelPCA extends Operator {

	/** The parameter name for &quot;The kernel type&quot; */
	public static final String PARAMETER_KERNEL_TYPE = "kernel_type";

	/** The parameter name for &quot;The kernel parameter gamma (RBF, anova).&quot; */
	public static final String PARAMETER_KERNEL_GAMMA = "kernel_gamma";

	/** The parameter name for &quot;The kernel parameter sigma1 (Epanechnikov, Gaussian Combination, Multiquadric).&quot; */
	public static final String PARAMETER_KERNEL_SIGMA1 = "kernel_sigma1";

	/** The parameter name for &quot;The kernel parameter sigma2 (Gaussian Combination).&quot; */
	public static final String PARAMETER_KERNEL_SIGMA2 = "kernel_sigma2";

	/** The parameter name for &quot;The kernel parameter sigma3 (Gaussian Combination).&quot; */
	public static final String PARAMETER_KERNEL_SIGMA3 = "kernel_sigma3";

	/** The parameter name for &quot;The kernel parameter degree (polynomial, anova, Epanechnikov).&quot; */
	public static final String PARAMETER_KERNEL_DEGREE = "kernel_degree";

	/** The parameter name for &quot;The kernel parameter shift (polynomial, Multiquadric).&quot; */
	public static final String PARAMETER_KERNEL_SHIFT = "kernel_shift";

	/** The parameter name for &quot;The kernel parameter a (neural).&quot; */
	public static final String PARAMETER_KERNEL_A = "kernel_a";

	/** The parameter name for &quot;The kernel parameter b (neural).&quot; */
	public static final String PARAMETER_KERNEL_B = "kernel_b";

	/** The parameter name for &quot;The width of the regression tube loss function of the regression SVM&quot; */
	public static final String PARAMETER_EPSILON = "epsilon";

	
	public KernelPCA(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		// needs to check if data has been normalized
		
		// only use numeric attributes
		
		ExampleSet exampleSet = getInput(ExampleSet.class);
		Attributes attributes = exampleSet.getAttributes();
		int numberOfExamples = exampleSet.size();

		// kernel
        int kernelType = getParameterAsInt(PARAMETER_KERNEL_TYPE);
		Kernel kernel = Kernel.createKernel(kernelType, this);

		// filling kernelmatrix and copying exampleValues by the way
		Matrix kernelMatrix = new Matrix(numberOfExamples, numberOfExamples);
		ArrayList<double[]> exampleValues = new ArrayList<double[]>();
		int i = 0;
		for(Example columnExample: exampleSet) {
			int j = 0;
			double[] columnValues = getAttributeValues(columnExample, attributes);
			exampleValues.add(columnValues);
			for (Example rowExample: exampleSet) {
				kernelMatrix.set(i, j, kernel.calculateDistance(columnValues, getAttributeValues(rowExample, attributes)));
				j++;
			}
			i++;
		}
		
		// calculating eigenVectors
		EigenvalueDecomposition eig = kernelMatrix.eig();
		Model model = new KernelPCAModel(exampleSet, eig.getV(), exampleValues, kernel);
		return new IOObject[] {exampleSet, model};
	}
	private double[] getAttributeValues(Example example, Attributes attributes) {
		double[] values = new double[attributes.size()];
		int x = 0;
		for (Attribute attribute : attributes)
			values[x++] = example.getValue(attribute);
		return values;
	}
	public Class<?>[] getInputClasses() {
		return new Class[] {ExampleSet.class};
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] {ExampleSet.class, Model.class};
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeCategory(PARAMETER_KERNEL_TYPE, "The SVM kernel type", Kernel.KERNEL_TYPES, Kernel.KERNEL_RADIAL);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_GAMMA, "The SVM kernel parameter gamma (RBF, anova).", 0.0d, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_SIGMA1, "The SVM kernel parameter sigma1 (Epanechnikov, Gaussian Combination, Multiquadric).", 0.0d, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_SIGMA2, "The SVM kernel parameter sigma2 (Gaussian Combination).", 0.0d, Double.POSITIVE_INFINITY, 0.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_SIGMA3, "The SVM kernel parameter sigma3 (Gaussian Combination).", 0.0d, Double.POSITIVE_INFINITY, 2.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_DEGREE, "The SVM kernel parameter degree (polynomial, anova, Epanechnikov).", 0.0d, Double.POSITIVE_INFINITY, 3.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_SHIFT, "The SVM kernel parameter shift (polynomial, Multiquadric).", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_A, "The SVM kernel parameter a (neural).", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_B, "The SVM kernel parameter b (neural).", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
