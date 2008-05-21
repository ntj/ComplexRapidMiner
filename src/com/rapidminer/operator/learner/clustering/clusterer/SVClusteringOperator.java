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
package com.rapidminer.operator.learner.clustering.clusterer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.Kernel;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelDot;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelNeural;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelPolynomial;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelRadial;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;

/**
 * An implementation of Support Vector Clustering based on {@rapidminer.cite BenHur/etal/2001a}.
 * 
 * @author Stefan Rueping, Ingo Mierswa, Michael Wurst
 * @version $Id: SVClusteringOperator.java,v 1.8 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class SVClusteringOperator extends AbstractDensityBasedClusterer {

	/** The parameter name for &quot;The SVM kernel type&quot; */
	public static final String PARAMETER_KERNEL_TYPE = "kernel_type";

	/** The parameter name for &quot;The SVM kernel parameter gamma (radial).&quot; */
	public static final String PARAMETER_KERNEL_GAMMA = "kernel_gamma";

	/** The parameter name for &quot;The SVM kernel parameter degree (polynomial).&quot; */
	public static final String PARAMETER_KERNEL_DEGREE = "kernel_degree";

	/** The parameter name for &quot;The SVM kernel parameter a (neural).&quot; */
	public static final String PARAMETER_KERNEL_A = "kernel_a";

	/** The parameter name for &quot;The SVM kernel parameter b (neural).&quot; */
	public static final String PARAMETER_KERNEL_B = "kernel_b";

	/** The parameter name for &quot;Size of the cache for kernel evaluations im MB &quot; */
	public static final String PARAMETER_KERNEL_CACHE = "kernel_cache";

	/** The parameter name for &quot;Precision on the KKT conditions&quot; */
	public static final String PARAMETER_CONVERGENCE_EPSILON = "convergence_epsilon";

	/** The parameter name for &quot;Stop after this many iterations&quot; */
	public static final String PARAMETER_MAX_ITERATIONS = "max_iterations";

	/** The parameter name for &quot;The fraction of allowed outliers.&quot; */
	public static final String PARAMETER_P = "p";

	/** The parameter name for &quot;Use this radius instead of the calculated one (-1 for calculated radius).&quot; */
	public static final String PARAMETER_R = "r";

	/** The parameter name for &quot;The number of virtual sample points to check for neighborship.&quot; */
	public static final String PARAMETER_NUMBER_SAMPLE_POINTS = "number_sample_points";
	/** The kernels which can be used from RapidMiner for the mySVM / myKLR. */
	private static final String[] KERNEL_TYPES = {
			"dot", "radial", "polynomial", "neural"
	};

	/** Indicates a linear kernel. */
	public static final int KERNEL_DOT = 0;

	/** Indicates a rbf kernel. */
	public static final int KERNEL_RADIAL = 1;

	/** Indicates a polynomial kernel. */
	public static final int KERNEL_POLYNOMIAL = 2;

	/** Indicates a neural net kernel. */
	public static final int KERNEL_NEURAL = 3;

	private SVClustering model;

	public SVClusteringOperator(OperatorDescription description) {
		super(description);
	}

	public ClusterModel createClusterModel(ExampleSet es) throws OperatorException {
		es.remapIds();
		
		// kernel
		int kernelType = getParameterAsInt(PARAMETER_KERNEL_TYPE);
		int cacheSize = getParameterAsInt(PARAMETER_KERNEL_CACHE);
		Kernel kernel = createKernel(kernelType);
		if (kernelType == KERNEL_RADIAL)
			((KernelRadial) kernel).setGamma(getParameterAsDouble(PARAMETER_KERNEL_GAMMA));
		else if (kernelType == KERNEL_POLYNOMIAL)
			((KernelPolynomial) kernel).setDegree(getParameterAsInt(PARAMETER_KERNEL_DEGREE));
		else if (kernelType == KERNEL_NEURAL)
			((KernelNeural) kernel).setParameters(getParameterAsDouble(PARAMETER_KERNEL_A), getParameterAsDouble(PARAMETER_KERNEL_B));
		SVCExampleSet svmExamples = new SVCExampleSet(es, false);
		kernel.init(svmExamples, cacheSize);
		model = new SVClustering(this, kernel, svmExamples);
		model.train();
		
		ClusterModel result = doClustering(es);
		return result;
	}

	protected List<String> getNeighbours(ExampleSet es, String id) throws UndefinedParameterError {		
		List ids = getIds();
		List<String> neighbors = new LinkedList<String>();
		Example example = getExample(es, id);
		double paramR = getParameterAsDouble(PARAMETER_R);
		double maxRadius = paramR < 0 ? model.getR() : paramR;
		int numSamplePoints = getParameterAsInt(PARAMETER_NUMBER_SAMPLE_POINTS);
		Iterator it = ids.iterator();
		while (it.hasNext()) {
			String neighborId = (String) it.next();
			if ((!id.equals(neighborId)) && (getAssignment(neighborId) == UNASSIGNED)) {
				Example neighbor = getExample(es, neighborId);
				double[] directions = new double[example.getAttributes().size()];
				int x = 0;
				for (Attribute attribute : example.getAttributes()) {
					directions[x++] = neighbor.getValue(attribute) - example.getValue(attribute);
				}
				boolean addAsNeighbor = true;
				for (int i = 0; i < numSamplePoints; i++) {
					if (addAsNeighbor) {
						double[] virtualExample = new double[directions.length];
						x = 0;
						for (Attribute attribute : example.getAttributes()) {
							virtualExample[x] = example.getValue(attribute) + (i + 1) * directions[x] / (numSamplePoints + 1);
							x++;
						}
						com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample svmExample = new com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample(virtualExample);
						double currentRadius = model.predict(svmExample);
						if (currentRadius > maxRadius) {
							addAsNeighbor = false;
							break;
						}
					} else {
						break;
					}
				}
				if (addAsNeighbor)
					neighbors.add(neighborId);
			}
		}
		return neighbors;
	}

	/**
	 * Creates a new kernel of the given type. The kernel type has to be one out of KERNEL_DOT, KERNEL_RADIAL, KERNEL_POLYNOMIAL, or KERNEL_NEURAL.
	 */
	public static Kernel createKernel(int kernelType) {
		switch (kernelType) {
			case KERNEL_RADIAL:
				return new KernelRadial();
			case KERNEL_POLYNOMIAL:
				return new KernelPolynomial();
			case KERNEL_NEURAL:
				return new KernelNeural();
			default:
				return new KernelDot();
		}
	}

	private Example getExample(ExampleSet es, String id) {
		return IdUtils.getExampleFromId(es, id);
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeCategory(PARAMETER_KERNEL_TYPE, "The SVM kernel type", KERNEL_TYPES, KERNEL_RADIAL);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_GAMMA, "The SVM kernel parameter gamma (radial).", 0.0d, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_KERNEL_DEGREE, "The SVM kernel parameter degree (polynomial).", 0, Integer.MAX_VALUE, 2);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_A, "The SVM kernel parameter a (neural).", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_B, "The SVM kernel parameter b (neural).", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeInt(PARAMETER_KERNEL_CACHE, "Size of the cache for kernel evaluations im MB ", 0, Integer.MAX_VALUE, 200));
		type = new ParameterTypeDouble(PARAMETER_CONVERGENCE_EPSILON, "Precision on the KKT conditions", 0.0d, Double.POSITIVE_INFINITY, 1e-3);
		types.add(type);
		types.add(new ParameterTypeInt(PARAMETER_MAX_ITERATIONS, "Stop after this many iterations", 1, Integer.MAX_VALUE, 100000));
		type = new ParameterTypeDouble(PARAMETER_P, "The fraction of allowed outliers.", 0, 1, 0.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_R, "Use this radius instead of the calculated one (-1 for calculated radius).", -1.0d,
				Double.POSITIVE_INFINITY, -1.0d);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeInt(PARAMETER_NUMBER_SAMPLE_POINTS, "The number of virtual sample points to check for neighborship.", 1,
				Integer.MAX_VALUE, 20));
		return types;
	}
}
