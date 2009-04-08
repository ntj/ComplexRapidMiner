package de.tud.inf.operator.learner.meta;

import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;

public class SlidingWindowLearnerModel extends PredictionModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2946226268493759247L;
	
	private ExampleSet trainingSet;
	
	private Model predictionModel;
	
	private double recentId;
	
	private double leastId;
	
	private int trainingSetSize;

	public double getLeastId() {
		return leastId;
	}

	public void setLeastId(double leastId) {
		this.leastId = leastId;
	}

	public int getTrainingSetSize() {
		return trainingSetSize;
	}
	
	public void setTrainingSize(int size) {
		this.trainingSetSize = size;
	}

	public ExampleSet getTraining() {
		return trainingSet;
	}

	public void setTraining(ExampleSet learning) {
		this.trainingSet = learning;
	}

	public Model getPredictionModel() {
		return predictionModel;
	}

	public void setPredictionModel(Model predictionModel) {
		this.predictionModel = predictionModel;
	}
	
	public double getRecentId() {
		return recentId;
	}

	public void setRecentId(double recentId) {
		this.recentId = recentId;
	}

	protected SlidingWindowLearnerModel(ExampleSet trainingExampleSet) {
		super(trainingExampleSet);
		
		this.trainingSet = trainingExampleSet;
		
		this.recentId = -1;
	}

	@Override
	public ExampleSet performPrediction(ExampleSet exampleSet,
			Attribute predictedLabel) throws OperatorException {
		
		return predictionModel.apply(exampleSet);
		
	}

	@Override
	public String toString() {
		
		StringBuffer buf = new StringBuffer(super.toString());
		buf.append("\n");
		buf.append("size of training set:\t");
		buf.append(this.trainingSetSize);
		buf.append("\n");
		buf.append("least recent id:\t");
		buf.append(leastId);
		buf.append("\n");
		buf.append("most recent id:\t");
		buf.append(this.recentId);
		buf.append("\n\n");
		buf.append(predictionModel.toString());
		
		return buf.toString();
	}

	
}
