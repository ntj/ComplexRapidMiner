package de.tud.inf.operator.learner.functions.weka.gaussianprocess;

import java.util.List;

import org.freehep.graphicsio.swf.DoInitAction;
import org.math.plot.utils.Array;

import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.WeightedInstancesHandler;
import weka.core.Capabilities.Capability;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.unsupervised.attribute.Standardize;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.WekaInstancesAdaptor;
import com.rapidminer.tools.WekaLearnerCapabilities;
import com.rapidminer.tools.WekaTools;

public class GaussianProcess extends AbstractLearner implements CapabilitiesHandler {

	/*
	 * Parameter Constants
	 */

	/* indicates if classifier is run in debug mode */
	public static final String PARAMETER_D = "Debug_Mode";

	/* Level of Gaussian Noise */
	public static final String PARAMETER_L = "L";

	/* indicates wether to normalize, standardize or none */
	public static final String PARAMETER_N = "N";

	/* The Kernel to use */
	public static final String PARAMETER_K = "Kernel";

	/*
	 * Filter Constants
	 */
	/** normalizes the data */
	public static final int FILTER_NORMALIZE = 0;
	/** standardizes the data */
	public static final int FILTER_STANDARDIZE = 1;
	/** no filter */
	public static final int FILTER_NONE = 2;

	/*
	 * Private Fields
	 */

	/** The filter used to make attributes numeric. */
	private NominalToBinary m_NominalToBinary;

	/** The filter used to standardize/normalize all values. */
	private Filter m_Filter = null;

	/** Whether to normalize/standardize/neither */
	private int m_filterType = FILTER_NORMALIZE;

	/** The filter used to get rid of missing values. */
	private ReplaceMissingValues m_Missing;

	/**
	 * Turn off all checks and conversions? Turning them off assumes that data
	 * is purely numeric, doesn't contain any missing values, and has a numeric
	 * class.
	 */
	private boolean m_checksTurnedOff = false;

	/** Gaussian Noise Value. */
	private double m_delta = 1.0;

	/** The class index from the training data */
	private int m_classIndex = -1;

	/**
	 * The parameters of the linear transforamtion realized by the filter on the
	 * class attribute
	 */
	private double m_Alin;
	private double m_Blin;

	/** Kernel to use **/
	private Kernel m_kernel = null;

	/** The number of training instances */
	//private int m_NumTrain = 0;

	/** The training data. */
	private double m_avg_target;

	/** The covariance matrix. */
	private weka.core.matrix.Matrix m_C;

	/** The vector of target values. */
	private weka.core.matrix.Matrix m_t;

	/** whether the kernel is a linear one */
	private boolean m_KernelIsLinear = false;
	
	private GaussianProcessesModel model;

	/*
	 * Reference to the weka Gaussian Processes classifier
	 */
	private Classifier gaussianProcesses = new GaussianProcesses();

	public GaussianProcess(OperatorDescription description) {
		super(description);
		
		// gaussianProcesses = new GaussianProcesses();

		m_kernel = new RBFKernel();
		//this.setKernel(new RBFKernel());
		((RBFKernel) m_kernel).setGamma(1.0);
	}

	public Model learn(ExampleSet exampleSet) throws OperatorException {

		/*The Model to return*/
		model = new GaussianProcessesModel(exampleSet);
		
		setOptions();
		
		/*
		 * transform example set to weka instances
		 */
		Instances insts = WekaTools.toWekaInstances(exampleSet, "LearningInstances", WekaInstancesAdaptor.LEARNING);

		try {
			
		/* check the set of training instances */
		if (!m_checksTurnedOff) {
			// can classifier handle the data?
				getCapabilities().testWithFail(insts);
			
			// remove instances with missing class
			insts = new Instances(insts);
			insts.deleteWithMissingClass();
		}

		if (!m_checksTurnedOff) {
			//m_Missing = new ReplaceMissingValues();
			this.setMissing(new ReplaceMissingValues());
			
			m_Missing.setInputFormat(insts);
			insts = Filter.useFilter(insts, m_Missing);
		} else {
			//m_Missing = null;
			this.setMissing(null);
		}

		if (getCapabilities().handles(Capability.NUMERIC_ATTRIBUTES)) {
			boolean onlyNumeric = true;
			if (!m_checksTurnedOff) {
				for (int i = 0; i < insts.numAttributes(); i++) {
					if (i != insts.classIndex()) {
						if (!insts.attribute(i).isNumeric()) {
							onlyNumeric = false;
							break;
						}
					}
				}
			}

			if (!onlyNumeric) {
				//m_NominalToBinary = new NominalToBinary();
				this.setModelToBinary(new NominalToBinary());
				m_NominalToBinary.setInputFormat(insts);
				insts = Filter.useFilter(insts, m_NominalToBinary);
			} else {
				//m_NominalToBinary = null;
				this.setModelToBinary(null);
			}
		} else {
			
			this.setModelToBinary(null);
		}

		m_classIndex = insts.classIndex();
		if (m_filterType == FILTER_STANDARDIZE) {
			//m_Filter = new Standardize();
			this.setFilter(new Standardize());
			
			// ((Standardize)m_Filter).setIgnoreClass(true);
			m_Filter.setInputFormat(insts);
			insts = Filter.useFilter(insts, m_Filter);
		} else if (m_filterType == FILTER_NORMALIZE) {
			//m_Filter = new Normalize();
			this.setFilter(new Normalize());
			// ((Normalize)m_Filter).setIgnoreClass(true);
			m_Filter.setInputFormat(insts);
			insts = Filter.useFilter(insts, m_Filter);
		} else {
			//m_Filter = null;
			this.setFilter(null);
		}

		//m_NumTrain = insts.numInstances();
		model.setNumberOfInstances(insts.numInstances());

		// determine which linear transformation has been
		// applied to the class by the filter
		if (m_Filter != null) {
			Instance witness = (Instance) insts.instance(0).copy();
			witness.setValue(m_classIndex, 0);
			m_Filter.input(witness);
			m_Filter.batchFinished();
			Instance res = m_Filter.output();
			m_Blin = res.value(m_classIndex);
			witness.setValue(m_classIndex, 1);
			m_Filter.input(witness);
			m_Filter.batchFinished();
			res = m_Filter.output();
			m_Alin = res.value(m_classIndex) - m_Blin;
		} else {
			m_Alin = 1.0;
			m_Blin = 0.0;
		}

		// Initialize kernel
		m_kernel.buildKernel(insts);
		m_KernelIsLinear = (m_kernel instanceof PolyKernel)
				&& (((PolyKernel) m_kernel).getExponent() == 1.0);

		// Build Inverted Covariance Matrix

		m_C = new weka.core.matrix.Matrix(insts.numInstances(), insts
				.numInstances());
		
		
		double kv;
		double sum = 0.0;

		for (int i = 0; i < insts.numInstances(); i++) {
			sum += insts.instance(i).classValue();
			for (int j = 0; j < i; j++) {
				kv = m_kernel.eval(i, j, insts.instance(i));
				m_C.set(i, j, kv);
				m_C.set(j, i, kv);
			}
			kv = m_kernel.eval(i, i, insts.instance(i));
			m_C.set(i, i, kv + (m_delta * m_delta));
		}

		m_avg_target = sum / insts.numInstances();
		model.setAverageTarget(m_avg_target);

		// weka.core.matrix.CholeskyDecomposition cd = new
		// weka.core.matrix.CholeskyDecomposition(m_C);

		// if (!cd.isSPD())
		// throw new Exception("No semi-positive-definite kernel?!?");

		weka.core.matrix.LUDecomposition lu = new weka.core.matrix.LUDecomposition(
				m_C);
		if (!lu.isNonsingular())
			throw new Exception("Singular Matrix?!?");

		weka.core.matrix.Matrix iMat = weka.core.matrix.Matrix.identity(insts
				.numInstances(), insts.numInstances());

		m_C = lu.solve(iMat);

		m_t = new weka.core.matrix.Matrix(insts.numInstances(), 1);

		for (int i = 0; i < insts.numInstances(); i++)
			m_t.set(i, 0, insts.instance(i).classValue() - m_avg_target);

		m_t = m_C.times(m_t);
		model.setVectorOfTargetValues(m_t);
		
		model.setCovarianceMatrix(m_C);
		
		} catch (Exception e) {
			throw new OperatorException(e.getMessage(),e.getCause());
		}

		return model;
	}

	
	public boolean supportsCapability(LearnerCapability capability) {

		//return WekaLearnerCapabilities.supportsCapability(gaussianProcesses,
			//	capability);
		
		Capabilities wekaCapabilities = this.getCapabilities();
		
		if (capability == LearnerCapability.POLYNOMINAL_ATTRIBUTES) {
			return wekaCapabilities.handles(Capabilities.Capability.NOMINAL_ATTRIBUTES);
		} else if (capability == LearnerCapability.BINOMINAL_ATTRIBUTES) {
			return wekaCapabilities.handles(Capabilities.Capability.BINARY_ATTRIBUTES);
		} else if (capability == LearnerCapability.NUMERICAL_ATTRIBUTES) {
			return wekaCapabilities.handles(Capabilities.Capability.NUMERIC_ATTRIBUTES);
		} else if (capability == LearnerCapability.POLYNOMINAL_CLASS) {
			return wekaCapabilities.handles(Capabilities.Capability.NOMINAL_CLASS);
		} else if (capability == LearnerCapability.BINOMINAL_CLASS) {
			return wekaCapabilities.handles(Capabilities.Capability.BINARY_CLASS);
		} else if (capability == LearnerCapability.NUMERICAL_CLASS) {
			return wekaCapabilities.handles(Capabilities.Capability.NUMERIC_CLASS);
		}
		
		return false;
	}

	@Override
	public List<ParameterType> getParameterTypes() {

		List<ParameterType> types = super.getParameterTypes();

		// add GP specific Options
		types
				.add(new ParameterTypeBoolean(
						PARAMETER_D,
						"If set, classifier is run in debug mode and may output additional info to the console",
						false));
		
		types.add(new ParameterTypeDouble(PARAMETER_L,
				"Level of Gaussian Noise", Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, 1.0));
		
		types.add(new ParameterTypeCategory(PARAMETER_N,
				"Whether to normalize, standardize or none", new String[] {
						"normalize", "standardize", "none" }, 0));
		
		types
				.add(new ParameterTypeString(PARAMETER_K, "The Kernel to use",
						"weka.classifiers.functions.supportVector.RBFKernel -C 250007 -G 1.0"));

		return types;
	}

	private void setOptions() throws OperatorException {

		/*
		 * Sets the options from the given parameters
		 */

		/* create the kernel */

		String kernelParameter = getParameterAsString(PARAMETER_K);

		if (kernelParameter.length() > 0) {

			String[] kernel = kernelParameter.split(" ");

			String[] options = null;

			if (kernel.length > 1) {

				options = new String[kernel.length - 1];

				for (int i = 1; i < kernel.length; i++) {

					options[i - 1] = kernel[i];
				}
			}

			try {
//				m_kernel = Kernel.forName(kernel[0], (options == null ? null
//						: options));
				
				this.setKernel(Kernel.forName(kernel[0],
						(options == null ? null : options)));
				
			} catch (Exception e) {
				throw new UserError(this, 207, new Object[] { kernelParameter,
						PARAMETER_K, "Please insert another kernel" });
			}
		}
		/* set debug mode */
		m_kernel.setDebug(getParameterAsBoolean(PARAMETER_D));

		/* set filter type */
		m_filterType = getParameterAsInt(PARAMETER_N);

		/* set gaussian noise value */
		m_delta = getParameterAsDouble(PARAMETER_L);
	}
	
	public Capabilities getCapabilities() {
	    Capabilities result = getKernel().getCapabilities();
	    result.setOwner(this);

	    // attribute
	    result.enableAllAttributeDependencies();
	    // with NominalToBinary we can also handle nominal attributes, but only
	    // if the kernel can handle numeric attributes
	    if (result.handles(Capability.NUMERIC_ATTRIBUTES))
	      result.enable(Capability.NOMINAL_ATTRIBUTES);
	    result.enable(Capability.MISSING_VALUES);
	    
	    // class
	    result.disableAllClasses();
	    result.disableAllClassDependencies();
	    result.enable(Capability.NUMERIC_CLASS);
	    result.enable(Capability.DATE_CLASS);
	    result.enable(Capability.MISSING_CLASS_VALUES);
	    
	    return result;
	  }
	
	public Kernel getKernel() {
		
		return m_kernel;
	}
	
	/*
	 * Private set Methods 
	 */
	
	private void setModelToBinary(NominalToBinary nominalToBinary) {
		this.m_NominalToBinary = nominalToBinary;
		
		model.setNominalToBinary(nominalToBinary);
	}
	
	private void setFilter(Filter filter) {
		
		this.m_Filter = filter;
		
		model.setFilter(filter);
	}
	
	private void setMissing(ReplaceMissingValues replaceMissing) {
		
		this.m_Missing = replaceMissing;
		
		model.setMissing(replaceMissing);
	}
	
	private void setKernel(Kernel kernel) {
		
		this.m_kernel = kernel;
		
		model.setKernel(kernel);
	}
}
