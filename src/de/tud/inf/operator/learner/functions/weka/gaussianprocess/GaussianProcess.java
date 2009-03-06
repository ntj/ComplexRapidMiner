package de.tud.inf.operator.learner.functions.weka.gaussianprocess;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.set.NoMissingLabelsCondition;
import com.rapidminer.example.set.ReplaceMissingExampleSet;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;

import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;

import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.Kernel;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelCauchy;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelEpanechnikov;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelGaussianCombination;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelLaplace;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelMultiquadric;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelPoly;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelRadial;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.KernelSigmoid;

import com.rapidminer.operator.preprocessing.PreprocessingOperator;
import com.rapidminer.operator.preprocessing.normalization.Normalization;

import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;

import com.rapidminer.tools.OperatorService;


public class GaussianProcess extends AbstractLearner {

	/*
	 * Parameter Constants
	 */

	/** indicates if classifier is run in debug mode */
	public static final String PARAMETER_D = "Debug_Mode";

	/** Level of Gaussian Noise */
	public static final String PARAMETER_L = "L";

	/** indicates wether to normalize, standardize or none */
	public static final String PARAMETER_N = "N";

	/* The Kernel to use */
	public static final String PARAMETER_K = "Kernel";

	/** The parameter name for &quot;The kind of kernel.&quot; */
	public static final String PARAMETER_KERNEL_TYPE = "kernel_type";

	/**
	 * The parameter name for &quot;The lengthscale r for rbf kernel functions
	 * (exp{-1.0 * r^-2 * ||x - bla||}).&quot;
	 */
	public static final String PARAMETER_KERNEL_LENGTHSCALE = "kernel_lengthscale";

	/** The parameter name for &quot;The degree used in the poly kernel.&quot; */
	public static final String PARAMETER_KERNEL_DEGREE = "kernel_degree";

	/** The parameter name for &quot;The bias used in the poly kernel.&quot; */
	public static final String PARAMETER_KERNEL_BIAS = "kernel_bias";

	/**
	 * The parameter name for &quot;The SVM kernel parameter sigma1
	 * (Epanechnikov, Gaussian Combination, Multiquadric).&quot;
	 */
	public static final String PARAMETER_KERNEL_SIGMA1 = "kernel_sigma1";

	/**
	 * The parameter name for &quot;The SVM kernel parameter sigma2 (Gaussian
	 * Combination).&quot;
	 */
	public static final String PARAMETER_KERNEL_SIGMA2 = "kernel_sigma2";

	/**
	 * The parameter name for &quot;The SVM kernel parameter sigma3 (Gaussian
	 * Combination).&quot;
	 */
	public static final String PARAMETER_KERNEL_SIGMA3 = "kernel_sigma3";

	/**
	 * The parameter name for &quot;The SVM kernel parameter shift (polynomial,
	 * Multiquadric).&quot;
	 */
	public static final String PARAMETER_KERNEL_SHIFT = "kernel_shift";

	/** The parameter name for &quot;The SVM kernel parameter a (neural).&quot; */
	public static final String PARAMETER_KERNEL_A = "kernel_a";

	/** The parameter name for &quot;The SVM kernel parameter b (neural).&quot; */
	public static final String PARAMETER_KERNEL_B = "kernel_b";

	public static final String[] KERNEL_TYPES = { "rbf", "cauchy", "laplace",
			"poly", "sigmoid", "Epanechnikov", "gaussian combination",
			"multiquadric" };

	/** The parameter name for nominal transformation */
	public static final String PARAMETER_NOMINAL_TRANSFORMATION = "nominal transformation";

	public static final String[] TRANSFORMATION_TYPES = { "Nominal to Binary",
			"Nominal To Numeric" };

	public static final int TRANSFORMATION_TO_BINARY = 0;

	public static final int TRANSFORMATION_TO_NUMERIC = 1;

	public static final String OPERATOR_NOMINAL2BINOMINAL = "Nominal2Binominal";

	public static final String OPERATOR_NOMINAL2NUMERIC = "Nominal2Numeric";

	/*
	 * Filter Constants
	 */
	/** normalizes the data */
	public static final int FILTER_NORMALIZE = 0;
	/** standardizes the data */
	public static final int FILTER_STANDARDIZE = 1;
	/** no filter */
	public static final int FILTER_NONE = 2;

	/** Whether to normalize/standardize/neither */
	private int filterType = FILTER_NORMALIZE;

	/**
	 * Turn off all checks and conversions? Turning them off assumes that data
	 * is purely numeric, doesn't contain any missing values, and has a numeric
	 * class.
	 */
	private boolean checksTurnedOff = false;

	/** Gaussian Noise Value. */
	private double delta = 1.0;

	/** The kernel to use */
	private Kernel kernel;

	/** The training data. */
	private double avgTarget;

	/** The covariance matrix. */
	private Jama.Matrix covarianceMatrix;

	/** The vector of target values. */
	private Jama.Matrix targetVector;

	/*
	 * private Class Attributes
	 */

	private Normalization normalize;

	public GaussianProcess(OperatorDescription description) {
		super(description);

	}

	@Override
	public Model learn(ExampleSet exampleSet) throws OperatorException {

		double[][] inputVectors;

		Model normalizationModel = null;

		Model nominalTransformingModel = null;

		setOptions();

		try {

			/* check the set of training instances */
			if (!checksTurnedOff) {

				exampleSet = new ConditionedExampleSet(exampleSet,
						new NoMissingLabelsCondition(exampleSet, null));
			}

			if (!checksTurnedOff) {
				
				/* replace missing values */
				exampleSet = new ReplaceMissingExampleSet(exampleSet);
			}

			/* checks if nominal attributes exist */

			boolean onlyNumeric = true;

			if (!checksTurnedOff) {

				for (Attribute attr : exampleSet.getAttributes()) {

					if (attr.isNominal()) {

						onlyNumeric = false;

						break;
					}
				}
			}

			if (!onlyNumeric) {

				/* Transforms all nominal attributes */
				Operator nominalTransformation;

				switch (getParameterAsInt(PARAMETER_NOMINAL_TRANSFORMATION)) {

				case TRANSFORMATION_TO_BINARY:
					nominalTransformation = OperatorService
							.createOperator(OPERATOR_NOMINAL2BINOMINAL);

					break;

				case TRANSFORMATION_TO_NUMERIC:
					nominalTransformation = OperatorService
							.createOperator(OPERATOR_NOMINAL2NUMERIC);
					break;

				default:
					nominalTransformation = OperatorService
							.createOperator(OPERATOR_NOMINAL2BINOMINAL);
				}

				nominalTransformation.setParameter(
						PreprocessingOperator.PARAMETER_CREATE_VIEW,
						Boolean.FALSE.toString());
				nominalTransformation
						.setParameter(
								PreprocessingOperator.PARAMETER_RETURN_PREPROCESSING_MODEL,
								Boolean.TRUE.toString());

				IOContainer result = nominalTransformation
						.apply(new IOContainer(new IOObject[] { exampleSet }));

				nominalTransformingModel = result.get(Model.class);

				exampleSet = result.get(ExampleSet.class);
			}

			/*
			 * normalize or standardize the examples in the exanple set
			 */
			if (!(filterType == FILTER_NONE)) {

				normalize = OperatorService.createOperator(Normalization.class);
				normalize.setParameter(Normalization.PARAMETER_CREATE_VIEW,
						Boolean.TRUE.toString());
				normalize.setParameter(
						Normalization.PARAMETER_RETURN_PREPROCESSING_MODEL,
						Boolean.TRUE.toString());

				if (filterType != FILTER_STANDARDIZE) {

					normalize.setParameter(Normalization.PARAMETER_Z_TRANSFORM,
							"false");
				}

				IOContainer container = normalize.apply(new IOContainer(
						new IOObject[] { exampleSet }));

				normalizationModel = container.get(Model.class);

				exampleSet = normalizationModel.apply(exampleSet);
			}

			// Build Inverted Covariance Matrix

			covarianceMatrix = new Jama.Matrix(exampleSet.size(), exampleSet
					.size());

			double sum = 0.0;
			double kv;

			inputVectors = buildInputVector(exampleSet);

			int k = 0;
			for (Example ex : exampleSet) {

				sum += ex.getLabel();

				for (int j = 0; j < k; j++) {

					kv = kernel.eval(inputVectors[k], inputVectors[j]);

					covarianceMatrix.set(k, j, kv);

					covarianceMatrix.set(j, k, kv);
				}

				kv = kernel.eval(inputVectors[k], inputVectors[k]);

				covarianceMatrix.set(k, k, kv + (delta * delta));

				k++;
			}

			avgTarget = sum / exampleSet.size();

			Jama.LUDecomposition luRM = new Jama.LUDecomposition(
					covarianceMatrix);

			if (!luRM.isNonsingular())
				throw new OperatorException("Singular Matrix?!?");

			Jama.Matrix iMatRM = Jama.Matrix.identity(exampleSet.size(),
					exampleSet.size());

			covarianceMatrix = luRM.solve(iMatRM);

			targetVector = new Jama.Matrix(exampleSet.size(), 1);

			{
				int i = 0;
				for (Example ex : exampleSet) {

					targetVector.set(i, 0, ex.getLabel() - avgTarget);
					i++;
				}
			}

			targetVector = covarianceMatrix.times(targetVector);

		} catch (Exception e) {
			throw new OperatorException(e.getMessage(), e.getCause());
		}

		return new GaussianProcessesModel(exampleSet, inputVectors, kernel,
				normalizationModel, avgTarget, covarianceMatrix, targetVector,
				nominalTransformingModel);
	}

	@Override
	public boolean supportsCapability(LearnerCapability capability) {

		if (capability == LearnerCapability.POLYNOMINAL_ATTRIBUTES
				|| capability == LearnerCapability.BINOMINAL_ATTRIBUTES
				|| capability == LearnerCapability.NUMERICAL_ATTRIBUTES
				|| capability == LearnerCapability.NUMERICAL_CLASS)
			return true;

		return false;
	}

	@Override
	public List<ParameterType> getParameterTypes() {

		List<ParameterType> types = super.getParameterTypes();

		ParameterType type;

		type = new ParameterTypeCategory(PARAMETER_NOMINAL_TRANSFORMATION,
				"The way nominal attributes should be transformed",
				TRANSFORMATION_TYPES, 0);
		type.setExpert(false);
		types.add(type);

		types.add(new ParameterTypeDouble(PARAMETER_L,
				"Level of Gaussian Noise", Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, 1.0));

		types.add(new ParameterTypeCategory(PARAMETER_N,
				"Whether to normalize, standardize or none", new String[] {
						"normalize", "standardize", "none" }, 0));

		type = new ParameterTypeCategory(PARAMETER_KERNEL_TYPE,
				"The kind of kernel.", KERNEL_TYPES, 0);
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeDouble(
				PARAMETER_KERNEL_LENGTHSCALE,
				"The lengthscale r for rbf kernel functions (exp{-1.0 * r^-2 * ||x - y||}).",
				0, Double.POSITIVE_INFINITY, 1.0);
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeDouble(PARAMETER_KERNEL_DEGREE,
				"The degree used in the poly kernel.", 0.0d,
				Double.POSITIVE_INFINITY, 2.0d);
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeDouble(PARAMETER_KERNEL_BIAS,
				"The bias used in the poly kernel.", 0,
				Double.POSITIVE_INFINITY, 1.0);
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeDouble(
				PARAMETER_KERNEL_SIGMA1,
				"The SVM kernel parameter sigma1 (Epanechnikov, Gaussian Combination, Multiquadric).",
				0.0d, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeDouble(PARAMETER_KERNEL_SIGMA2,
				"The SVM kernel parameter sigma2 (Gaussian Combination).",
				0.0d, Double.POSITIVE_INFINITY, 0.0d);
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeDouble(PARAMETER_KERNEL_SIGMA3,
				"The SVM kernel parameter sigma3 (Gaussian Combination).",
				0.0d, Double.POSITIVE_INFINITY, 2.0d);
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeDouble(PARAMETER_KERNEL_SHIFT,
				"The SVM kernel parameter shift (polynomial, Multiquadric).",
				Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeDouble(PARAMETER_KERNEL_A,
				"The SVM kernel parameter a (neural).",
				Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeDouble(PARAMETER_KERNEL_B,
				"The SVM kernel parameter b (neural).",
				Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d);
		type.setExpert(false);
		types.add(type);

		return types;
	}

	private void setOptions() throws OperatorException {

		/* set filter type */
		filterType = getParameterAsInt(PARAMETER_N);

		/* set gaussian noise value */
		delta = getParameterAsDouble(PARAMETER_L);

		/** create the Kernel **/
		this.kernel = createKernel();
	}

	public com.rapidminer.operator.learner.functions.kernel.rvm.kernel.Kernel createKernel()
			throws OperatorException {

		Kernel kernel = null;

		double lengthScale = getParameterAsDouble(PARAMETER_KERNEL_LENGTHSCALE);
		double bias = getParameterAsDouble(PARAMETER_KERNEL_BIAS);
		double degree = getParameterAsDouble(PARAMETER_KERNEL_DEGREE);
		double a = getParameterAsDouble(PARAMETER_KERNEL_A);
		double b = getParameterAsDouble(PARAMETER_KERNEL_B);
		double sigma1 = getParameterAsDouble(PARAMETER_KERNEL_SIGMA1);
		double sigma2 = getParameterAsDouble(PARAMETER_KERNEL_SIGMA2);
		double sigma3 = getParameterAsDouble(PARAMETER_KERNEL_SIGMA3);
		double shift = getParameterAsDouble(PARAMETER_KERNEL_SHIFT);

		switch (getParameterAsInt(PARAMETER_KERNEL_TYPE)) {
		case 0:
			kernel = new KernelRadial(lengthScale);
			break;
		case 1:
			kernel = new KernelCauchy(lengthScale);
			break;
		case 2:
			kernel = new KernelLaplace(lengthScale);
			break;
		case 3:
			kernel = new KernelPoly(lengthScale, bias, degree);
			break;
		case 4:
			kernel = new KernelSigmoid(a, b);
			break;
		case 5:
			kernel = new KernelEpanechnikov(sigma1, degree);
			break;
		case 6:
			kernel = new KernelGaussianCombination(sigma1, sigma2, sigma3);
			break;
		case 7:
			kernel = new KernelMultiquadric(sigma1, shift);
			break;
		default:
			kernel = new KernelRadial(lengthScale);
		}

		return kernel;
	}

	private double[][] buildInputVector(ExampleSet e) {

		double vector[][] = new double[e.size()][e.getAttributes().size()];

		int i = 0;

		for (Example ex : e) {

			int j = 0;

			for (Attribute att : e.getAttributes()) {

				vector[i][j] = ex.getValue(att);
				j++;
			}

			i++;
		}
		return vector;
	}
}
