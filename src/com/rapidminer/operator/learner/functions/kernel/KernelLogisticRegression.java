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

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.learner.functions.kernel.functions.Kernel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.optimization.ec.es.ESOptimization;


/**
 * This operator determines a logistic regression model.
 * 
 * @author Ingo Mierswa
 * @version $Id: KernelLogisticRegression.java,v 1.4 2008/05/09 19:23:01 ingomierswa Exp $
 */
public class KernelLogisticRegression extends AbstractLearner {

	/** The parameter name for &quot;The SVM kernel type&quot; */
	public static final String PARAMETER_KERNEL_TYPE = "kernel_type";

	/** The parameter name for &quot;The SVM kernel parameter gamma (RBF, anova).&quot; */
	public static final String PARAMETER_KERNEL_GAMMA = "kernel_gamma";

	/** The parameter name for &quot;The SVM kernel parameter sigma1 (Epanechnikov, Gaussian Combination, Multiquadric).&quot; */
	public static final String PARAMETER_KERNEL_SIGMA1 = "kernel_sigma1";

	/** The parameter name for &quot;The SVM kernel parameter sigma2 (Gaussian Combination).&quot; */
	public static final String PARAMETER_KERNEL_SIGMA2 = "kernel_sigma2";

	/** The parameter name for &quot;The SVM kernel parameter sigma3 (Gaussian Combination).&quot; */
	public static final String PARAMETER_KERNEL_SIGMA3 = "kernel_sigma3";

	/** The parameter name for &quot;The SVM kernel parameter degree (polynomial, anova, Epanechnikov).&quot; */
	public static final String PARAMETER_KERNEL_DEGREE = "kernel_degree";

	/** The parameter name for &quot;The SVM kernel parameter shift (polynomial, Multiquadric).&quot; */
	public static final String PARAMETER_KERNEL_SHIFT = "kernel_shift";

	/** The parameter name for &quot;The SVM kernel parameter a (neural).&quot; */
	public static final String PARAMETER_KERNEL_A = "kernel_a";

	/** The parameter name for &quot;The SVM kernel parameter b (neural).&quot; */
	public static final String PARAMETER_KERNEL_B = "kernel_b";

	/** The parameter name for &quot;The SVM complexity constant (0: calculates probably good value).&quot; */
	public static final String PARAMETER_C = "C";
	
	/** The parameter name for &quot;The type of start population initialization.&quot; */
	public static final String PARAMETER_START_POPULATION_TYPE = "start_population_type";

	/** The parameter name for &quot;Stop after this many evaluations&quot; */
	public static final String PARAMETER_MAX_GENERATIONS = "max_generations";

	/** The parameter name for &quot;Stop after this number of generations without improvement (-1: optimize until max_iterations).&quot; */
	public static final String PARAMETER_GENERATIONS_WITHOUT_IMPROVAL = "generations_without_improval";

	/** The parameter name for &quot;The population size (-1: number of examples)&quot; */
	public static final String PARAMETER_POPULATION_SIZE = "population_size";

	/** The parameter name for &quot;The fraction of the population used for tournament selection.&quot; */
	public static final String PARAMETER_TOURNAMENT_FRACTION = "tournament_fraction";

	/** The parameter name for &quot;Indicates if the best individual should survive (elititst selection).&quot; */
	public static final String PARAMETER_KEEP_BEST = "keep_best";

	/** The parameter name for &quot;The type of the mutation operator.&quot; */
	public static final String PARAMETER_MUTATION_TYPE = "mutation_type";

	/** The parameter name for &quot;The type of the selection operator.&quot; */
	public static final String PARAMETER_SELECTION_TYPE = "selection_type";

	/** The parameter name for &quot;The probability for crossovers.&quot; */
	public static final String PARAMETER_CROSSOVER_PROB = "crossover_prob";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";

	/** The parameter name for &quot;Indicates if a dialog with a convergence plot should be drawn.&quot; */
	public static final String PARAMETER_SHOW_CONVERGENCE_PLOT = "show_convergence_plot";
	
    public KernelLogisticRegression(OperatorDescription description) {
        super(description);
    }

    public Model learn(ExampleSet exampleSet) throws OperatorException {
		// kernel
        int kernelType = getParameterAsInt(PARAMETER_KERNEL_TYPE);
		Kernel kernel = Kernel.createKernel(kernelType, this);
		
        RandomGenerator random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
        KernelLogisticRegressionOptimization optimization = 
            new KernelLogisticRegressionOptimization(
                    exampleSet,
                    kernel,
                    getParameterAsDouble(PARAMETER_C),
                    getParameterAsInt(PARAMETER_START_POPULATION_TYPE), 
                    getParameterAsInt(PARAMETER_MAX_GENERATIONS), getParameterAsInt(PARAMETER_GENERATIONS_WITHOUT_IMPROVAL), 
                    getParameterAsInt(PARAMETER_POPULATION_SIZE), getParameterAsInt(PARAMETER_SELECTION_TYPE),
                    getParameterAsDouble(PARAMETER_TOURNAMENT_FRACTION), 
                    getParameterAsBoolean(PARAMETER_KEEP_BEST), getParameterAsInt(PARAMETER_MUTATION_TYPE), getParameterAsDouble(PARAMETER_CROSSOVER_PROB),
                    getParameterAsBoolean(PARAMETER_SHOW_CONVERGENCE_PLOT), 
                    random,
                    this);
        return optimization.train();
    }

    public boolean supportsCapability(LearnerCapability lc) {
        if (lc == LearnerCapability.NUMERICAL_ATTRIBUTES)
            return true;
        if (lc == LearnerCapability.BINOMINAL_CLASS)
            return true;
        if (lc == LearnerCapability.WEIGHTED_EXAMPLES)
            return true;
        return false;
    }
    
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeCategory(PARAMETER_KERNEL_TYPE, "The kernel type", Kernel.KERNEL_TYPES, Kernel.KERNEL_RADIAL);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_GAMMA, "The kernel parameter gamma (RBF, anova).", 0.0d, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_SIGMA1, "The kernel parameter sigma1 (Epanechnikov, Gaussian Combination, Multiquadric).", 0.0d, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_SIGMA2, "The kernel parameter sigma2 (Gaussian Combination).", 0.0d, Double.POSITIVE_INFINITY, 0.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_SIGMA3, "The kernel parameter sigma3 (Gaussian Combination).", 0.0d, Double.POSITIVE_INFINITY, 2.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_DEGREE, "The kernel parameter degree (polynomial, anova, Epanechnikov).", 0.0d, Double.POSITIVE_INFINITY, 3.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_SHIFT, "The kernel parameter shift (polynomial, Multiquadric).", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_A, "The kernel parameter a (neural).", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_KERNEL_B, "The kernel parameter b (neural).", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_C, "The complexity constant.", 0.00000001d, Double.POSITIVE_INFINITY, 1.0d);
        type.setExpert(false);
		types.add(type);
        types.add(new ParameterTypeCategory(PARAMETER_START_POPULATION_TYPE, "The type of start population initialization.", ESOptimization.POPULATION_INIT_TYPES, ESOptimization.INIT_TYPE_RANDOM));
        types.add(new ParameterTypeInt(PARAMETER_MAX_GENERATIONS, "Stop after this many evaluations", 1, Integer.MAX_VALUE, 10000));
        types.add(new ParameterTypeInt(PARAMETER_GENERATIONS_WITHOUT_IMPROVAL, "Stop after this number of generations without improvement (-1: optimize until max_iterations).", -1, Integer.MAX_VALUE, 30));
        types.add(new ParameterTypeInt(PARAMETER_POPULATION_SIZE, "The population size (-1: number of examples)", -1, Integer.MAX_VALUE, 1));
        types.add(new ParameterTypeDouble(PARAMETER_TOURNAMENT_FRACTION, "The fraction of the population used for tournament selection.", 0.0d, Double.POSITIVE_INFINITY, 0.75d));
        types.add(new ParameterTypeBoolean(PARAMETER_KEEP_BEST, "Indicates if the best individual should survive (elititst selection).", true));
        types.add(new ParameterTypeCategory(PARAMETER_MUTATION_TYPE, "The type of the mutation operator.", ESOptimization.MUTATION_TYPES, ESOptimization.GAUSSIAN_MUTATION));
        types.add(new ParameterTypeCategory(PARAMETER_SELECTION_TYPE, "The type of the selection operator.", ESOptimization.SELECTION_TYPES, ESOptimization.TOURNAMENT_SELECTION));
        types.add(new ParameterTypeDouble(PARAMETER_CROSSOVER_PROB, "The probability for crossovers.", 0.0d, 1.0d, 1.0d));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
        types.add(new ParameterTypeBoolean(PARAMETER_SHOW_CONVERGENCE_PLOT, "Indicates if a dialog with a convergence plot should be drawn.", false));
        return types;
    }
}
