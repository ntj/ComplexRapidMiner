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
package com.rapidminer.operator.learner.functions.kernel.functions;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.learner.functions.kernel.SupportVector;
import com.rapidminer.operator.learner.functions.kernel.evosvm.EvoSVM;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * Returns the distance of two examples. The method {@link #init(ExampleSet)}
 * must be invoked before the correct distances can be returned. Please note
 * that subclasses must provide an empty constructor to allow kernel creation
 * via reflection (for reading kernels from disk).
 * 
 * @author Ingo Mierswa
 * @version $Id: Kernel.java,v 1.2 2008/05/09 19:22:56 ingomierswa Exp $
 */
public abstract class Kernel implements Serializable {

	/** The kernels which can be used for the EvoSVM. */
	public static final String[] KERNEL_TYPES = { 
		"dot", "radial", "polynomial", "sigmoid", "anova", "epanechnikov", "gausian_combination", "multiquadric" 
	};

	/** Indicates a linear kernel. */
	public static final int KERNEL_DOT = 0;

	/** Indicates a rbf kernel. */
	public static final int KERNEL_RADIAL = 1;

	/** Indicates a polynomial kernel. */
	public static final int KERNEL_POLYNOMIAL = 2;

	/** Indicates a sigmoid kernel. */
	public static final int KERNEL_SIGMOID = 3;
	
	/** Indicates an anova kernel. */
	public static final int KERNEL_ANOVA = 4;

	/** Indicates a Epanechnikov kernel. */
	public static final int KERNEL_EPANECHNIKOV = 5;

	/** Indicates a Gaussian combination kernel. */
	public static final int KERNEL_GAUSSIAN_COMBINATION = 6;
	
	/** Indicates a multiquadric kernel. */
	public static final int KERNEL_MULTIQUADRIC = 7;
	
	/** The complete distance matrix for this kernel and a given example set. */
	private KernelCache cache;
	
	private ExampleSet exampleSet;
	

	/**
	 * Must return one out of KERNEL_DOT, KERNEL_RADIAL, KERNEL_POLYNOMIAL,
	 * KERNEL_SIGMOID, KERNEL_ANOVA, KERNEL_EPANECHNIKOV, KERNEL_GAUSSIAN_COMBINATION,
	 * or KERNEL_MULTIQUADRIC.
	 */
	public abstract int getType();

	/** Subclasses must implement this method. */
	public abstract double calculateDistance(double[] x1, double[] x2);
	
	/**
	 * Calculates all distances and store them in a matrix to speed up
	 * optimization.
	 */
	public void init(ExampleSet exampleSet) {
		this.exampleSet = exampleSet;
		int exampleSetSize = exampleSet.size();
		if (exampleSetSize < 8000) {
			this.cache = new FullCache(exampleSet, this);	
		} else {
			this.cache = new MapBasedCache(exampleSetSize);
		}
	}

	/** Returns the distance between the examples with the given indices. */
	public double getDistance(int x1, int x2) {
		double result = cache.get(x1, x2);
		if (Double.isNaN(result)) {
			result = calculateDistance(getAttributeValues(x1), getAttributeValues(x2));
			cache.store(x1, x2, result);
		}
		return result;
	}

	public double[] getAttributeValues(int i) {
		Example example = this.exampleSet.getExample(i);
		double[] values = new double[this.exampleSet.getAttributes().size()];
		int x = 0;
		for (Attribute attribute : exampleSet.getAttributes())
			values[x++] = example.getValue(attribute);
		return values;
	}
	
	/** Calculates the inner product of the given vectors. */
	public double innerProduct(double[] x1, double[] x2) {
		double result = 0.0d;
		for (int i = 0; i < x1.length; i++) {
			result += x1[i] * x2[i];
		}
		return result;
	}

	/** Calculates the L2-norm, i.e. ||x-y||^2. */
	public double norm2(double[] x1, double[] x2) {
		double result = 0;
		for (int i = 0; i < x1.length; i++) {
			double factor = x1[i] - x2[i];
			result += factor * factor;
		}
		return result;
	}

	/** Calculates w*x from the given support vectors using this kernel function. */
	public double getSum(Collection supportVectors, double[] currentX) {
		double sum = 0.0d;
		Iterator i = supportVectors.iterator();
		while (i.hasNext()) {
			SupportVector sv = (SupportVector) i.next();
			sum += sv.getY() * sv.getAlpha() * calculateDistance(sv.getX(), currentX);
		}
		return sum;
	}

	public static Kernel createKernel(int kernelType) {
		try {
			return createKernel(kernelType, null);
		} catch (UndefinedParameterError e) {
			return null;
		} // cannot happen
	}
	
	public static Kernel createKernel(int kernelType, Operator operator) throws UndefinedParameterError {
		if (kernelType == KERNEL_DOT) {
			return new DotKernel();
		} else if (kernelType == KERNEL_RADIAL) {
			RBFKernel kernel = new RBFKernel();
			if (operator != null)
				kernel.setGamma(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_GAMMA)); 
			return kernel;
		} else if (kernelType == KERNEL_POLYNOMIAL) {
			PolynomialKernel kernel = new PolynomialKernel();
			if (operator != null)
				kernel.setPolynomialParameters(
						operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_DEGREE),  
						operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_SHIFT)); 
			return kernel;
		} else if (kernelType == KERNEL_SIGMOID) {
			SigmoidKernel kernel = new SigmoidKernel();
			if (operator != null)
				kernel.setSigmoidParameters(
						operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_A),  
						operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_B)); 
			return kernel;
		} else if (kernelType == KERNEL_ANOVA) {
			AnovaKernel kernel = new AnovaKernel();
			if (operator != null) {
				kernel.setGamma(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_GAMMA)); 
				kernel.setDegree(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_DEGREE)); 
			}
			return kernel;
		} else if (kernelType == KERNEL_EPANECHNIKOV) {
			EpanechnikovKernel kernel = new EpanechnikovKernel();
			if (operator != null) {
				kernel.setSigma(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_SIGMA1)); 
				kernel.setDegree(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_DEGREE)); 
			}
			return kernel;
		} else if (kernelType == KERNEL_GAUSSIAN_COMBINATION) {
			GaussianCombinationKernel kernel = new GaussianCombinationKernel();
			if (operator != null) {
				kernel.setSigma1(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_SIGMA1)); 
				kernel.setSigma2(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_SIGMA2)); 
				kernel.setSigma3(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_SIGMA3)); 
			}
			return kernel;
		} else if (kernelType == KERNEL_MULTIQUADRIC) {
			MultiquadricKernel kernel = new MultiquadricKernel();
			if (operator != null) {
				kernel.setSigma(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_SIGMA1)); 
				kernel.setShift(operator.getParameterAsDouble(EvoSVM.PARAMETER_KERNEL_SHIFT)); 
			}
			return kernel;
		} else {
			return null;
		}
	}
}
