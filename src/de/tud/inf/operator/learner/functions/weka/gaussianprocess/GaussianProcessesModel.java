package de.tud.inf.operator.learner.functions.weka.gaussianprocess;

import java.util.Iterator;

import weka.classifiers.functions.supportVector.Kernel;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.WekaInstancesAdaptor;
import com.rapidminer.tools.WekaTools;

public class GaussianProcessesModel extends PredictionModel{

	private static final long serialVersionUID = -4980181227681934936L;
	
	/*
	 * private Fields
	 */
	
	/** The number of training instances */
	private int m_NumTrain = 0;
	
	/** The filter used to standardize/normalize all values. */
	private Filter m_Filter = null;
	
	/** The filter used to make attributes numeric. */
	private NominalToBinary m_NominalToBinary;
	
	/** The filter used to get rid of missing values. */
	private ReplaceMissingValues m_Missing;
	
	/** The training data. */
	private double m_avg_target;
	
	/** The vector of target values. */
	private weka.core.matrix.Matrix m_t;
	
	/** The covariance matrix. */
	private weka.core.matrix.Matrix m_C;
	
	/**
	 * Turn off all checks and conversions? Turning them off assumes that data
	 * is purely numeric, doesn't contain any missing values, and has a numeric
	 * class.
	 */
	private boolean m_checksTurnedOff = false;
	
	/** Kernel to use **/
	private Kernel m_kernel = null;

	protected GaussianProcessesModel(ExampleSet trainingExampleSet) {
		super(trainingExampleSet);
		
		m_NumTrain = trainingExampleSet.size();
	}
	
	@Override
	public ExampleSet performPrediction(ExampleSet exampleSet,
			Attribute predictedLabel) throws OperatorException {
		
		
		
		Instances instances = WekaTools.toWekaInstances(exampleSet, "ApplierInstances", WekaInstancesAdaptor.PREDICTING);
		
		int i = 0;
		Iterator<Example> r = exampleSet.iterator();
		while (r.hasNext()) {
			Example e = r.next();
			Instance instance = instances.instance(i++);
			try {
				applyModelForInstance(instance, e, predictedLabel);
			} catch (Exception e1) {
				
				throw new OperatorException(e1.getMessage(),e1.getCause());
			}
		}
		
		return exampleSet;
	}

	public double classifyInstance(Instance inst) throws Exception{
		
		/** K Vector */
		weka.core.matrix.Matrix k = new weka.core.matrix.Matrix(m_NumTrain, 1);

			// Filter instance
			if (!m_checksTurnedOff) {
				m_Missing.input(inst);
				m_Missing.batchFinished();
				inst = m_Missing.output();
			}

			if (m_NominalToBinary != null) {
				m_NominalToBinary.input(inst);
				m_NominalToBinary.batchFinished();
				inst = m_NominalToBinary.output();
			}

			if (m_Filter != null) {
				m_Filter.input(inst);
				m_Filter.batchFinished();
				inst = m_Filter.output();
			}

			
			// Build K vector
			for (int i = 0; i < m_NumTrain; i++)
				k.set(i, 0, m_kernel.eval(-1, i, inst));

		double result = k.transpose().times(m_t).get(0, 0) + m_avg_target;

		return result;
	}
	
	public void applyModelForInstance(Instance instance, Example e, Attribute predictedLabelAttribute) throws Exception {
		
		double predictedLabel = Double.NaN;
		
		try {
			
			double wekaPrediction = classifyInstance(instance);
			
			if (predictedLabelAttribute.isNominal()) {
				
				double confidences[] = distributionForInstance(instance);
				
				for (int i = 0; i < confidences.length; i++) {
					
					String classification = instance.classAttribute().value(i);
					
					e.setConfidence(classification, confidences[i]);
				}
				
				String classification = instance.classAttribute().value((int) wekaPrediction);
				predictedLabel = predictedLabelAttribute.getMapping().mapString(classification);
				
			} else {
				
				predictedLabel = classifyInstance(instance);
			}
		} catch (Exception exc) {
			
			logError("Exception occured while classifying example:" + exc.getMessage() + " [" + exc.getClass() + "]");
			
			throw new Exception(exc);
		}
		
		e.setValue(predictedLabelAttribute, predictedLabel);
	}
	
	public double[] distributionForInstance(Instance instance) throws Exception {

	    double[] dist = new double[instance.numClasses()];
		switch (instance.classAttribute().type()) {
		case weka.core.Attribute.NOMINAL:
			double classification = classifyInstance(instance);
			if (Instance.isMissingValue(classification)) {
				return dist;
			} else {
				dist[(int) classification] = 1.0;
			}
			return dist;
		case weka.core.Attribute.NUMERIC:
			dist[0] = classifyInstance(instance);
			return dist;
		default:
			return dist;
		}
	  }    
	
	public void setNumberOfInstances(int numOfInst) {
		
		this.m_NumTrain = numOfInst;
	}
	
	public void setNominalToBinary(NominalToBinary nominalToBinary) {
		
		this.m_NominalToBinary = nominalToBinary;
	}
	
	public void setFilter(Filter filter) {
		
		this.m_Filter = filter;
	}
	
	public void setMissing(ReplaceMissingValues replaceMissing) {
		
		this.m_Missing = replaceMissing;
	}
	
	public void setAverageTarget(double averageTarget) {
		
		this.m_avg_target = averageTarget;
	}
	
	public void setVectorOfTargetValues(weka.core.matrix.Matrix vectorOfTarget) {
		
		this.m_t = vectorOfTarget;
	}
	
	public void setKernel(Kernel kernel) {
		
		this.m_kernel = kernel;
	}
	
	public void setCovarianceMatrix(weka.core.matrix.Matrix covarianceMatrix) {
		
		this.m_C = covarianceMatrix;
	}
	
	public String toString() {

	    StringBuffer text = new StringBuffer();

		if (m_t == null)
			return "Gaussian Processes: No model built yet.";

		try {

			text.append("Gaussian Processes\n\n");
			text.append("Kernel used:\n  " + m_kernel.toString() + "\n\n");

			text.append("Average Target Value : " + m_avg_target + "\n");

			text.append("Inverted Covariance Matrix:\n");
			double min = m_C.get(0, 0);
			double max = m_C.get(0, 0);
			for (int i = 0; i < m_NumTrain; i++)
				for (int j = 0; j < m_NumTrain; j++) {
					if (m_C.get(i, j) < min)
						min = m_C.get(i, j);
					else if (m_C.get(i, j) > max)
						max = m_C.get(i, j);
				}
			text.append("    Lowest Value = " + min + "\n");
			text.append("    Highest Value = " + max + "\n");
			text.append("Inverted Covariance Matrix * Target-value Vector:\n");
			min = m_t.get(0, 0);
			max = m_t.get(0, 0);
			for (int i = 0; i < m_NumTrain; i++) {
				if (m_t.get(i, 0) < min)
					min = m_t.get(i, 0);
				else if (m_t.get(i, 0) > max)
					max = m_t.get(i, 0);
			}
			text.append("    Lowest Value = " + min + "\n");
			text.append("    Highest Value = " + max + "\n \n");

		} catch (Exception e) {
			return "Can't print the classifier.";
		}

		return text.toString();
	  }
} 
