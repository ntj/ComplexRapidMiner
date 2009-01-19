package de.tud.inf.operator.learner.regressionensemble;

import com.rapidminer.operator.learner.PredictionModel;

public class EnsembleMember {
	/* id of the member */
	//private int memberId;
	/* weight of this member for consideration in the calculation of the  */ 
	private double weight;
	/* number of positive predictions */
	private int positiveTests = 0;
	/* number of negative predictions */
	private int negativeTests = 0;
	/* the temporal/serial id of the first example that was used in training this member
	 * i.e. time of first training */
	private int introducedAt;
	/* the temporal/serial id of the last example that was used in training this member
	 * i.e. time of last training */
	//private int trainedLast;
	/* type of the model */
	private String modelType;
	/* state of the member */
	private MemberState state = MemberState.UNSTABLE;
	/* the model itself*/
	private PredictionModel model;
	
	// unclear whether required or not
	/* prediction for the last training sample */
	//private double currentPrediction;
	/* error of the prediction for the last training sample (distance(prediction, sample)) */
	//private double currentError;

//	public int getMemberId() {
//		return memberId;
//	}
//	
//	public void setMemberId(int memberId) {
//		this.memberId = memberId;
//	}
	
	public double getWeight() {
		return weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public void incPositive() {
		positiveTests++;
	}
	
	public int getPositive() {
		return positiveTests;
	}
	
	public void setPositive(int positiveTests) {
		this.positiveTests = positiveTests;
	}
	
	public void incNegative() {
		negativeTests++;
	}
	
	public int getNegative() {
		return negativeTests;
	}
	
	public void setNegative(int negativeTests) {
		this.negativeTests = negativeTests;
	}
	
	public int getIntroducedAt() {
		return introducedAt;
	}
	
	public void setIntroducedAt(int introducedAt) {
		this.introducedAt = introducedAt;
	}
	
//	public int getTrainedLast() {
//		return trainedLast;
//	}
//	
//	public void setTrainedLast(int trainedLast) {
//		this.trainedLast = trainedLast;
//	}
	
	public MemberState getState() {
		return state;
	}
	
	public void setState(MemberState state) {
		this.state = state;
	}
	
	public String getModelType() {
		return modelType;
	}
	
	public void setModelType(String modelType) {
		this.modelType = modelType;
	}
	
	public PredictionModel getModel() {
		return model;
	}
	
	public void setModel(PredictionModel model) {
		this.model = model;
	}
}
