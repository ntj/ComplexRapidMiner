package de.tud.inf.operator.learner.regressionensemble;

import com.rapidminer.operator.learner.PredictionModel;

public class EnsembleMember {
	/* weight of this member for consideration in the calculation of the  */ 
	private double weight;
	/* number of positive predictions */
	private int positiveTests = 0;
	/* number of negative predictions */
	private int negativeTests = 0;
	/* the temporal/serial id of the first example that was used in training this member
	 * i.e. time of first training */
	private int introducedAt;
	/* state of the member */
	private MemberState state = MemberState.UNSTABLE;
	/* the model itself*/
	private PredictionModel model;
	
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
	
	public double getRatio() {
		return ((double) positiveTests / (double) (positiveTests + negativeTests));
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
	
	public MemberState getState() {
		return state;
	}
	
	public void setState(MemberState state) {
		this.state = state;
	}
	
	public PredictionModel getModel() {
		return model;
	}
	
	public void setModel(PredictionModel model) {
		this.model = model;
	}
}
