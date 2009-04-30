package de.tud.inf.operator.learner.functions.weka.gaussianprocess;

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import com.rapidminer.example.set.HeaderExampleSet;
import com.rapidminer.example.set.ReplaceMissingExampleSet;
import com.rapidminer.example.table.NominalMapping;

import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;

import com.rapidminer.operator.learner.PredictionModel;

import com.rapidminer.operator.learner.functions.kernel.RVMModel;
import com.rapidminer.operator.learner.functions.kernel.rvm.kernel.Kernel;


public class GaussianProcessesModel extends PredictionModel{

	private static final long serialVersionUID = -4980181227681934936L;
	
	/*
	 * private Fields
	 */
	
	/** The number of training instances */
	private int m_NumTrain = 0;
	
	/** The training data. */
	private double m_avg_target;
	
	private Jama.Matrix covarianceMatrix;
	
	private Jama.Matrix targetVector;
	
	private Model normalizationModel;
	
	private double[][] trainingVectors;
	
	private Model nominalTransformationModel;
	
	private ExampleSet originalHeader;
	
	/**
	 * Turn off all checks and conversions? Turning them off assumes that data
	 * is purely numeric, doesn't contain any missing values, and has a numeric
	 * class.
	 */
	private boolean m_checksTurnedOff = false;
	
	private Kernel kernel;

	protected GaussianProcessesModel(ExampleSet trainingExampleSet) {
		super(trainingExampleSet);
		
		m_NumTrain = trainingExampleSet.size();
		
	}
	
	public GaussianProcessesModel(
			ExampleSet trainingExampleSet,
			double[][] trainingVectors,
			Kernel kernel,
			Model normalization, double avgTargetValues,
			Jama.Matrix covarianceMatrix, Jama.Matrix targetVector,
			Model nominalToBinominal,
			ExampleSet originalExampleSet) {

		super(trainingExampleSet);

		this.trainingVectors = trainingVectors;

		m_NumTrain = trainingExampleSet.size();

		this.kernel = kernel;

		this.normalizationModel = normalization;

		this.m_avg_target = avgTargetValues;

		this.covarianceMatrix = covarianceMatrix;

		this.targetVector = targetVector;
		
		this.nominalTransformationModel = nominalToBinominal;
		
		this.originalHeader = originalExampleSet;
		
	}
	
	@Override
	public ExampleSet performPrediction(ExampleSet exampleSet,
			Attribute predictedLabel) throws OperatorException {
		
		ExampleSet predictionExampleSet = (ExampleSet)exampleSet.clone();
		
		//checkCompatibility(predictionExampleSet);
		
		checkMapping(exampleSet);
		
		if(!m_checksTurnedOff)
			exampleSet = new ReplaceMissingExampleSet(exampleSet);
			
		if(nominalTransformationModel != null)
			exampleSet = nominalTransformationModel.apply(exampleSet);
		
		if(normalizationModel != null)
			exampleSet = normalizationModel.apply(exampleSet);
		
		checkExampleSetCompatibility(exampleSet);
		
		Iterator<Example> r = exampleSet.iterator();
		while (r.hasNext()) {
			Example e = r.next();

			try {
				applyModelForExample(e, predictedLabel);
				
			} catch (Exception e1) {
				
				throw new OperatorException("Couldn't apply ExampleSet to Model",e1.getCause());
			}
		}
		
		//return predictionExampleSet;
		return exampleSet;
	}

	/*
	 * adds a mapping from headerExampleSet if its not in the prediction example Set
	 */
	private void checkMapping(ExampleSet exampleSet) {
		
		//ExampleSet header = this.getTrainingHeader();
		
		for(Attribute h : originalHeader.getAttributes()) {
			
			if(h.isNominal()) {
				
				Attribute exampleAttr = exampleSet.getAttributes().get(h.getName());
				
				if(exampleAttr != null) {
					
					NominalMapping headerMapping = h.getMapping();
					NominalMapping predictionMapping = exampleAttr.getMapping();
					
					for(String nominalValues : headerMapping.getValues()) {
						
						if(!predictionMapping.getValues().contains(nominalValues)) {
							
							predictionMapping.mapString(nominalValues);
						}
					}
				}
			}
		}
	}

	public double classifyInstance(Example ex) throws Exception{
		
		/** K Vector */
		Jama.Matrix kRm = new Jama.Matrix(m_NumTrain,1);
			
			double[] trainingVector = RVMModel.makeInputVector(ex);
			
			// Build K Vector
			for(int i = 0; i< m_NumTrain; i++) {
				
				kRm.set(i, 0, kernel.eval(trainingVector, trainingVectors[i]));
			}
			
		double resultRm = kRm.transpose().times(targetVector).get(0, 0) + m_avg_target;

		return resultRm;
	}
	
	public void applyModelForExample(Example e, Attribute predictedLabelAttribute) throws Exception {
		
		double predictedLabel = Double.NaN;
		
		try {
				
				predictedLabel = classifyInstance(e);
				
		} catch (Exception exc) {
			
			logError("Exception occured while classifying example:" + exc.getMessage() + " [" + exc.getClass() + "]");
			
			throw new Exception(exc);
		}
		
		e.setValue(predictedLabelAttribute, predictedLabel);
	}
	
	
	public String toString() {

	    StringBuffer text = new StringBuffer();

		if (targetVector == null)
			return "Gaussian Processes: No model built yet.";

		try {

			text.append("Gaussian Processes\n\n");
			text.append("Kernel used:\n  " + kernel.toString() + "\n\n");

			text.append("Average Target Value : " + m_avg_target + "\n");

			text.append("Inverted Covariance Matrix:\n");
			double min = covarianceMatrix.get(0, 0);
			double max = covarianceMatrix.get(0, 0);
			for (int i = 0; i < m_NumTrain; i++)
				for (int j = 0; j < m_NumTrain; j++) {
					if (covarianceMatrix.get(i, j) < min)
						min = covarianceMatrix.get(i, j);
					else if (covarianceMatrix.get(i, j) > max)
						max = covarianceMatrix.get(i, j);
				}
			text.append("    Lowest Value = " + min + "\n");
			text.append("    Highest Value = " + max + "\n");
			text.append("Inverted Covariance Matrix * Target-value Vector:\n");
			min = targetVector.get(0, 0);
			max = targetVector.get(0, 0);
			for (int i = 0; i < m_NumTrain; i++) {
				if (targetVector.get(i, 0) < min)
					min = targetVector.get(i, 0);
				else if (targetVector.get(i, 0) > max)
					max = targetVector.get(i, 0);
			}
			text.append("    Lowest Value = " + min + "\n");
			text.append("    Highest Value = " + max + "\n \n");

		} catch (Exception e) {
			return "Can't print the classifier.";
		}

		return text.toString();
	  }

	@Override
	protected void checkCompatibility(ExampleSet exampleSet)
			throws OperatorException {
		
		/*
		 * has to be empty because PredictionModel checks the ExampleSet before! transformation
		 */
	}
	
	private void checkExampleSetCompatibility( ExampleSet exampleSet) throws OperatorException{
		
		ExampleSet header = this.getTrainingHeader();

		if (header.getAttributes().size() != exampleSet.getAttributes().size()) {

			logWarning("Training and prediction ExampleSet differ in size");

			if (exampleSet.getAttributes().size() > header.getAttributes()
					.size()) {

				logNote("Try a projection to the attributes used in training");

				Iterator<Attribute> predictionAttributes = exampleSet
						.getAttributes().iterator();
				
				Attribute nextPrediction;
				
				while(predictionAttributes.hasNext()) {
					
					nextPrediction = predictionAttributes.next();
					
					if(!header.getAttributes().contains(nextPrediction))
						predictionAttributes.remove();
				}
			}
		}

		/*
		 * double check size to see if a possible projection has helped
		 */

		if (header.getAttributes().size() != exampleSet.getAttributes().size()) {

			/*
			 * stop prediction and throw an error
			 */
			throw new UserError(null, 925, "Different numbers of attributes");
		}
		
		//TODO: check if the order of the attributes is messed up
	}
} 
