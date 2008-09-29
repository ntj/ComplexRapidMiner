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
package com.rapidminer.operator.learner.functions.kernel;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.learner.functions.kernel.rvm.ClassificationProblem;
import com.rapidminer.operator.learner.functions.kernel.rvm.ConstructiveRegression;
import com.rapidminer.operator.learner.functions.kernel.rvm.Parameter;
import com.rapidminer.operator.learner.functions.kernel.rvm.RVMClassification;
import com.rapidminer.operator.learner.functions.kernel.rvm.RVMRegression;
import com.rapidminer.operator.learner.functions.kernel.rvm.RegressionProblem;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelBasisFunction;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelCauchy;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelEpanechnikov;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelGaussianCombination;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelLaplace;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelMultiquadric;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelPoly;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelRadial;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelSigmoid;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;


/** 
 * 	Relevance Vector Machine (RVM) Learner. The RVM is a probabilistic method 
 *  both for classification and regression. The implementation of the relevance 
 *  vector machine is based on the original algorithm described by Tipping/2001.
 *  The fast version of the marginal likelihood maximization (Tipping/Faul/2003) 
 *  is also available if the parameter &quot;rvm_type&quot; is set to 
 *  &quot;Constructive-Regression-RVM&quot;.
 *  
 *  @rapidminer.reference Tipping/2001a
 *  @rapidminer.reference Tipping/Faul/2003a
 *  
 *  @author Piotr Kasprzak, Ingo Mierswa
 *  @version $Id: RVMLearner.java,v 1.9 2008/05/09 19:23:01 ingomierswa Exp $
 *  @rapidminer.index RVM
 */
public class RVMLearner extends AbstractLearner {


	/** The parameter name for &quot;Regression RVM&quot; */
	public static final String PARAMETER_RVM_TYPE = "rvm_type";

	/** The parameter name for &quot;The type of the kernel functions.&quot; */
	public static final String PARAMETER_KERNEL_TYPE = "kernel_type";

	/** The parameter name for &quot;The maximum number of iterations used.&quot; */
	public static final String PARAMETER_MAX_ITERATION = "max_iteration";

	/** The parameter name for &quot;Abort iteration if largest log alpha change is smaller than this&quot; */
	public static final String PARAMETER_MIN_DELTA_LOG_ALPHA = "min_delta_log_alpha";

	/** The parameter name for &quot;Prune basis function if its alpha is bigger than this&quot; */
	public static final String PARAMETER_ALPHA_MAX = "alpha_max";

	/** The parameter name for &quot;The lengthscale used in all kernels.&quot; */
	public static final String PARAMETER_KERNEL_LENGTHSCALE = "kernel_lengthscale";

	/** The parameter name for &quot;The degree used in the poly kernel.&quot; */
	public static final String PARAMETER_KERNEL_DEGREE = "kernel_degree";

	/** The parameter name for &quot;The bias used in the poly kernel.&quot; */
	public static final String PARAMETER_KERNEL_BIAS = "kernel_bias";

	/** The parameter name for &quot;The SVM kernel parameter sigma1 (Epanechnikov, Gaussian Combination, Multiquadric).&quot; */
	public static final String PARAMETER_KERNEL_SIGMA1 = "kernel_sigma1";

	/** The parameter name for &quot;The SVM kernel parameter sigma2 (Gaussian Combination).&quot; */
	public static final String PARAMETER_KERNEL_SIGMA2 = "kernel_sigma2";

	/** The parameter name for &quot;The SVM kernel parameter sigma3 (Gaussian Combination).&quot; */
	public static final String PARAMETER_KERNEL_SIGMA3 = "kernel_sigma3";

	/** The parameter name for &quot;The SVM kernel parameter shift (polynomial, Multiquadric).&quot; */
	public static final String PARAMETER_KERNEL_SHIFT = "kernel_shift";

	/** The parameter name for &quot;The SVM kernel parameter a (neural).&quot; */
	public static final String PARAMETER_KERNEL_A = "kernel_a";

	/** The parameter name for &quot;The SVM kernel parameter b (neural).&quot; */
	public static final String PARAMETER_KERNEL_B = "kernel_b";
	public static final String[] RVM_TYPES    = { "Regression-RVM", "Classification-RVM", "Constructive-Regression-RVM" };
    public static final String[] KERNEL_TYPES = { "rbf", "cauchy", "laplace", "poly", "sigmoid", "Epanechnikov", "gaussian combination", "multiquadric" };    
    
    public RVMLearner(OperatorDescription description) {
    	super(description);
    }
    
	public boolean supportsCapability(LearnerCapability lc) {
		if (lc == com.rapidminer.operator.learner.LearnerCapability.NUMERICAL_ATTRIBUTES)
			return true;
		
		if (lc == com.rapidminer.operator.learner.LearnerCapability.BINOMINAL_CLASS)
			return true;
		
		if (lc == com.rapidminer.operator.learner.LearnerCapability.NUMERICAL_CLASS)
			return true;
		
		return false;
	}
    
    public Model learn(ExampleSet exampleSet) throws OperatorException {
    	
    	log("Creating RVM.");

    	Parameter parameter			= new Parameter();
    	
    	int numExamples				= exampleSet.size();
    	int numBases			    = numExamples + 1;
    	
    	/** Get user defined control parameters from RapidMiner */

    	parameter.min_delta_log_alpha	= getParameterAsDouble(PARAMETER_MIN_DELTA_LOG_ALPHA);  // Abort iteration if largest log alpha change is smaller than this
    	parameter.alpha_max			= getParameterAsDouble(PARAMETER_ALPHA_MAX);				// Prune basis function if its alpha is bigger than this
    	parameter.maxIterations		= getParameterAsInt(PARAMETER_MAX_ITERATION);				// Maximum number of iterations
    	    	
    	/** Transfer input / target vectors into array form */
    	
    	log("=> Creating input / output vectors.");
    	
    	double[][] x				= new double[numExamples][exampleSet.getAttributes().size()];
    	double[][] t				= new double[numExamples][1];
    	Iterator<Example> reader		= exampleSet.iterator();
    	int k = 0;
    	while (reader.hasNext()) {
    		double[] targetVector	= new double[1];
    		Example e 				= reader.next();
    		targetVector[0]			= e.getLabel();
    		x[k]					= RVMModel.makeInputVector(e);    		
    		t[k]					= targetVector;
    		k++;
    	}
    	
    	/** Init hyperparameters with more or less sensible values (shouldn't be too important) */
    	
    	Attribute label			= exampleSet.getAttributes().getLabel();

    	parameter.initAlpha	= Math.pow((1.0 / numExamples), 2);
//    	parameter.initSigma	= Math.sqrt(label.getVariance()) * 0.1;
    	parameter.initSigma	= 0.1;
    	
    	/** Create kernel functions */
    
    	log("Creating kernel basis functions [" + KERNEL_TYPES[getParameterAsInt(PARAMETER_KERNEL_TYPE)] + "].");
    	
    	KernelBasisFunction[] kernels	= createKernels(x, numBases);						
    	
   		/** Create RVM and learn the model */

   		String RVMType = RVM_TYPES[getParameterAsInt(PARAMETER_RVM_TYPE)];
   		com.rapidminer.operator.learner.functions.kernel.rvm.Model model = null;
   		
   		if (label.isNominal()) {

            if (label.getMapping().size() != 2)
                throw new UserError(this, 114, getName(), label.getName());
            
   			/** Classification problem */
   			
   			int[] c = new int[numExamples];
   			for (k = 0; k < numExamples; k++) {
   				c[k] = (int)t[k][0];
   			}
   			
   			ClassificationProblem problem	= new ClassificationProblem(x, c, kernels);
   			
   			if (RVMType.equals("Classification-RVM")) {
   				RVMClassification RVM		= new RVMClassification(problem, parameter);
                try {
                    model						= RVM.learn();
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new UserError(this, 924);
                }
   			} else {
   			    throw new UserError(this, 207, new Object[] { RVMType, "rvm_type", "only Classification-RVM can be used for the given two class classification problem" });
   			}
   		} else {
   			
   			/** Regression problem */
   			
   	   		RegressionProblem problem		= new RegressionProblem(x, t, kernels);

   	   		if (RVMType.equals("Regression-RVM")) {
   				RVMRegression RVM			= new RVMRegression(problem, parameter);
   				model						= RVM.learn();
   			} else if (RVMType.equals("Constructive-Regression-RVM")) {
   				ConstructiveRegression RVM	= new ConstructiveRegression(problem, parameter);
   				model						= RVM.learn();
   			} else {
                throw new UserError(this, 207, new Object[] { RVMType, "rvm_type", "only one of the regression types can be used for the given regression problem" });
            }

   		}

    	return new RVMModel(exampleSet, model);
    }    
    
    /**
     * Create the appropriate kernel functions depending on the ui settings.
     */
    
    public KernelBasisFunction[] createKernels(double[][] x, int numKernels) throws OperatorException {

    	KernelBasisFunction[] kernels	= new KernelBasisFunction[numKernels];
    	KernelBasisFunction kernel		= null;
    	double[] input;
    	
    	double lengthScale	= getParameterAsDouble(PARAMETER_KERNEL_LENGTHSCALE);
    	double bias			= getParameterAsDouble(PARAMETER_KERNEL_BIAS);
    	double degree	    = getParameterAsDouble(PARAMETER_KERNEL_DEGREE);
    	double a            = getParameterAsDouble(PARAMETER_KERNEL_A);
    	double b            = getParameterAsDouble(PARAMETER_KERNEL_B);
    	double sigma1       = getParameterAsDouble(PARAMETER_KERNEL_SIGMA1);
    	double sigma2       = getParameterAsDouble(PARAMETER_KERNEL_SIGMA2);
    	double sigma3       = getParameterAsDouble(PARAMETER_KERNEL_SIGMA3);
    	double shift        = getParameterAsDouble(PARAMETER_KERNEL_SHIFT);
    	
   		for (int j = 0; j < numKernels - 1; j++) {
   			
   			input = x[j];
   			
   			switch (getParameterAsInt(PARAMETER_KERNEL_TYPE)) {
				case 0:
					kernel = new KernelBasisFunction(new KernelRadial(lengthScale), input);
					break;
				case 1:
					kernel = new KernelBasisFunction(new KernelCauchy(lengthScale), input);
					break;
				case 2:
					kernel = new KernelBasisFunction(new KernelLaplace(lengthScale), input);
					break;
				case 3:
					kernel = new KernelBasisFunction(new KernelPoly(lengthScale, bias, degree), input);
					break;
				case 4:
					kernel = new KernelBasisFunction(new KernelSigmoid(a, b), input);
					break;
				case 5:
					kernel = new KernelBasisFunction(new KernelEpanechnikov(sigma1, degree), input);
					break;
				case 6:
					kernel = new KernelBasisFunction(new KernelGaussianCombination(sigma1, sigma2, sigma3), input);
					break;
				case 7:
					kernel = new KernelBasisFunction(new KernelMultiquadric(sigma1, shift), input);
					break;
				default:
					kernel = new KernelBasisFunction(new KernelRadial(lengthScale), input);
			}
   			
   			kernels[j + 1]	= kernel;
    	}

   		return kernels;
    }
    
    public List<ParameterType> getParameterTypes() {
    	
    	List<ParameterType> types = super.getParameterTypes();
    	ParameterType type;
    	
    	type = new ParameterTypeCategory(PARAMETER_RVM_TYPE, "Regression RVM", RVM_TYPES, 0);
    	type.setExpert(false);
    	types.add(type);
    	
    	type = new ParameterTypeCategory(PARAMETER_KERNEL_TYPE, "The type of the kernel functions.", KERNEL_TYPES, 0);
    	type.setExpert(false);
    	types.add(type);
  
    	type = new ParameterTypeInt(PARAMETER_MAX_ITERATION, "The maximum number of iterations used.", 1, Integer.MAX_VALUE, 100);
    	types.add(type);
    	
    	type = new ParameterTypeDouble(PARAMETER_MIN_DELTA_LOG_ALPHA, "Abort iteration if largest log alpha change is smaller than this", 0, Double.POSITIVE_INFINITY, 1e-3);
    	types.add(type);
    	
    	type = new ParameterTypeDouble(PARAMETER_ALPHA_MAX, "Prune basis function if its alpha is bigger than this", 0, Double.POSITIVE_INFINITY, 1e12);
    	types.add(type);
    	
    	type = new ParameterTypeDouble(PARAMETER_KERNEL_LENGTHSCALE, "The lengthscale used in all kernels.", 0, Double.POSITIVE_INFINITY, 3.0);
    	type.setExpert(false);
    	types.add(type);

    	type = new ParameterTypeDouble(PARAMETER_KERNEL_DEGREE, "The degree used in the poly kernel.", 0.0d, Double.POSITIVE_INFINITY, 2.0d);
    	type.setExpert(false);
    	types.add(type);

    	type = new ParameterTypeDouble(PARAMETER_KERNEL_BIAS, "The bias used in the poly kernel.", 0, Double.POSITIVE_INFINITY, 1.0);
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
