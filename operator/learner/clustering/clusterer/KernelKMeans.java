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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.Cluster;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.Kernel;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelNeural;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelPolynomial;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelRadial;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.tools.ClassNameMapper;
import com.rapidminer.tools.RandomGenerator;


/**
 * Simple implementation of kernel k-means {@rapidminer.cite Dhillon/etal/2004a}.
 * 
 * @rapidminer.reference Dhillon/etal/2004a
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: KernelKMeans.java,v 1.9 2008/09/12 10:31:42 tobiasmalbrecht Exp $
 */
public class KernelKMeans extends AbstractKMethod {

	/** The parameter name for &quot;scale the examples before applying clustering&quot; */
	public static final String PARAMETER_SCALE = "scale";

	/** The parameter name for &quot;the size of the kernel cache (currently not supported)&quot; */
	public static final String PARAMETER_CACHE_SIZE_MB = "cache_size_mb";

	/** The parameter name for &quot;similarity measure to apply&quot; */
	public static final String PARAMETER_KERNEL_TYPE = "kernel_type";

	/** The parameter name for &quot;The SVM kernel parameter gamma (radial).&quot; */
	public static final String PARAMETER_KERNEL_GAMMA = "kernel_gamma";

	/** The parameter name for &quot;The SVM kernel parameter degree (polynomial).&quot; */
	public static final String PARAMETER_KERNEL_DEGREE = "kernel_degree";

	/** The parameter name for &quot;The SVM kernel parameter a (neural).&quot; */
	public static final String PARAMETER_KERNEL_A = "kernel_a";

	/** The parameter name for &quot;The SVM kernel parameter b (neural).&quot; */
	public static final String PARAMETER_KERNEL_B = "kernel_b";
	
	private double[] g = null;

	private Kernel kernel;

	private Map<String, com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample> index;

	public static final String[] DEFAULT_KERNEL_CLASSES = {
			"com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelDot", 
			"com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelRadial",			
			"com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelPolynomial"
	};

	private ClassNameMapper KERNEL_CLASS_MAP;

	public KernelKMeans(OperatorDescription description) {
		super(description);
	}

	protected void initKMethod(List<String> ids, int k) {
		g = null;
	}

	public ClusterModel createClusterModel(ExampleSet es) throws OperatorException {
		int maxK = getParameterAsInt(PARAMETER_K); 
		int maxOptimizationSteps = getParameterAsInt(PARAMETER_MAX_OPTIMIZATION_STEPS); 
		int maxRuns = getParameterAsInt(PARAMETER_MAX_RUNS); 
		
		Tools.onlyNumericalAttributes(es, "Kernel KMeans");

		kernel = null;
		String kernelClassName = getParameterAsString(PARAMETER_KERNEL_TYPE);
		kernel = (Kernel) KERNEL_CLASS_MAP.getInstantiation(kernelClassName);
		if (kernel instanceof KernelRadial)
			((KernelRadial) kernel).setGamma(getParameterAsDouble(PARAMETER_KERNEL_GAMMA));
		else if (kernel instanceof KernelPolynomial)
			((KernelPolynomial) kernel).setDegree(getParameterAsInt(PARAMETER_KERNEL_DEGREE));
		else if (kernel instanceof KernelNeural)
			((KernelNeural) kernel).setParameters(getParameterAsDouble(PARAMETER_KERNEL_A), getParameterAsDouble(PARAMETER_KERNEL_B));
		com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples svmEs = new com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples(es, null, getParameterAsBoolean(PARAMETER_SCALE));
		kernel.init(svmEs, getParameterAsInt(PARAMETER_CACHE_SIZE_MB));
		index = new HashMap<String, com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample>();
		for (int i = 0; i < svmEs.count_examples(); i++) {
			String id = svmEs.getId(i);
			com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample ex = svmEs.get_example(i);
			index.put(id, new com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample(ex));
		}
		
		FlatClusterModel result = kmethod(es, maxK, maxOptimizationSteps, maxRuns);

		return result;
	}

	protected int bestIndex(String id, FlatCrispClusterModel cmNew, FlatCrispClusterModel cm) {
		// If the system is not initialized yet (the first run) return random
		// cluster assigments
		if (g == null)
			return RandomGenerator.getGlobalRandomGenerator().nextInt(cmNew.getNumberOfClusters());
		int best = -1;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < cm.getNumberOfClusters(); i++) {
			double v = 0;
			Cluster cl = cm.getClusterAt(i);
			Iterator it = cl.getObjects();
			int count = 0;
			while (it.hasNext()) {
				String id2 = (String) it.next();
				double kernelValue = calculateK(id, id2);
				v = v + kernelValue;
				count++;
			}
			v = g[i] - 2 * (v / count);
			log("Membership " + id + " to cluster " + i + " is " + v);
			if (v < min) {
				min = v;
				best = i;
			}
		}
		return best;
	}

	private void recalculateG(FlatClusterModel cm) {
		for (int i = 0; i < cm.getNumberOfClusters(); i++) {
			double v = 0;
			Cluster cl = cm.getClusterAt(i);
			Iterator it = cl.getObjects();
			int count = 0;
			while (it.hasNext()) {
				String id1 = (String) it.next();
				Iterator it2 = cl.getObjects();
				while (it2.hasNext()) {
					String id2 = (String) it2.next();
					v = v + calculateK(id1, id2);
					count++;
				}
			}
			if (count > 0)
				g[i] = v / count;
			else
				g[i] = 0.0;
		}
	}

	private double calculateK(String id1, String id2) {
		com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample ex1 = index.get(id1);
		com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample ex2 = index.get(id2);
		double v = kernel.calculate_K(ex1, ex2);
		return v;
	}

	protected void recalculateCentroids(FlatCrispClusterModel cl) {
		if (g == null)
			g = new double[cl.getNumberOfClusters()];
		recalculateG(cl);
	}

	protected double evaluateClusterModel(FlatCrispClusterModel cl) {
		double v = 0.0;
		for (int i = 0; i < g.length; i++)
			v = v + g[i];
		return v;
	}

	public List<ParameterType> getParameterTypes() {
		KERNEL_CLASS_MAP = new ClassNameMapper(DEFAULT_KERNEL_CLASSES);
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_SCALE, "Indicates if the examples are scaled before clustering is applied.", true));
		types.add(new ParameterTypeInt(PARAMETER_CACHE_SIZE_MB, "The size of the kernel cache.", 0, Integer.MAX_VALUE, 50));
		ParameterType type = new ParameterTypeStringCategory(PARAMETER_KERNEL_TYPE, "The kernel type, i.e. the similarity measure which should be applied.", KERNEL_CLASS_MAP.getShortClassNames());
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
		return types;
	}
}
