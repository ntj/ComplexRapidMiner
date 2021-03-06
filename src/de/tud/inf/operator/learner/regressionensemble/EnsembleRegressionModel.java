package de.tud.inf.operator.learner.regressionensemble;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeValueFilterSingleCondition;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.Tools;

@SuppressWarnings("unused")
public class EnsembleRegressionModel extends PredictionModel implements Iterable<EnsembleMember> {
	private static final long serialVersionUID = 4075168877323509822L;
	
	private List<EnsembleMember> members;
	
	private Set<Integer> seenIds;

	//private ExampleSet exampleSet;
	
	protected EnsembleRegressionModel(ExampleSet exampleSet) {
		this(exampleSet, null);
	}

	protected EnsembleRegressionModel(ExampleSet exampleSet, EnsembleMember[] initMembers) {
		super(exampleSet);
		members = new LinkedList<EnsembleMember>();
		if(initMembers != null) {
			for (EnsembleMember member : initMembers) {
				members.add(member);
			}
		}
		//this.exampleSet = exampleSet;
		this.seenIds = new HashSet<Integer>();
	}
	
	public Set<Integer> getSeenIds() {
		return seenIds;
	}

	public void setSeenIds(Set<Integer> seenIds) {
		this.seenIds = seenIds;
	}
	
	public boolean addMember(EnsembleMember member) {
		return members.add(member);
	}
	
	public void deleteMember(int index) {
		members.remove(index);
	}
	
	public void deleteMember(EnsembleMember member) {
		members.remove(member);
	}
	
	public int getNumberOfMembers() {
		return members.size();
	}
	
	public EnsembleMember getMember(int index) {
		return members.get(index);
	}

	/**
	 * assign labels to the examples in the passed example set
	 */
	@Override
	public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
//		Attribute idAttribute = exampleSet.getAttributes().getId();
//		if(idAttribute == null) {
//			throw new OperatorException("id-Attribute missing!");
//		}
		
		HashMap<Integer, Double> predictions = new HashMap<Integer, Double>(exampleSet.size());
		
		// initialize
		Iterator<Example> initIter = exampleSet.iterator();
		while(initIter.hasNext()) {
			Example currentExample = initIter.next();
			int currentId = (int) currentExample.getId();
			predictions.put(currentId, 0.0);
		}
		
		for(EnsembleMember member : members) {
			// skip unstable members
			if(member.getState() == MemberState.UNSTABLE) {
				continue;
			}
			
			// let the member model write it's prediction into the example set
			member.getModel().performPrediction(exampleSet, predictedLabel);
			
			// gather the predictions
			Iterator<Example> iter = exampleSet.iterator();
			while(iter.hasNext()) {
				Example currentExample = iter.next();
				int currentId = (int) currentExample.getId();
				double weight = member.getWeight();
				
				double memberprediction = currentExample.getNumericalValue(predictedLabel); 
				double predictionSoFar = predictions.get(currentId);
				
				predictions.put(currentId, predictionSoFar + memberprediction * weight);
			}
		}

		return exampleSet;
	}
	
//	/**
//	 * assign labels to the examples in the passed example set
//	 */
//	@Override
//	public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
//		Attribute idAttribute = exampleSet.getAttributes().getId();
//		
//		if(idAttribute == null) {
//			throw new OperatorException("id-Attribute missing!");
//		}
//		
//		Iterator<Example> iter = exampleSet.iterator();
//		while(iter.hasNext()) {
//			Example currentExample = iter.next();
//			int currentId = (int) currentExample.getNumericalValue(idAttribute);
//			double predictionForExample = 0;
//			
//			Condition currentExampleCondition = new AttributeValueFilterSingleCondition(
//				idAttribute, 
//				AttributeValueFilterSingleCondition.EQUALS, 
//				Integer.toString(currentId));
//			
//			ConditionedExampleSet currentExampleSet = new ConditionedExampleSet(exampleSet, currentExampleCondition);
//			
//			log(Integer.toString(currentExampleSet.size()));
//			
//			for(EnsembleMember member : members) {
//				// skip unstable members
//				if(member.getState() == MemberState.UNSTABLE) {
//					continue;
//				}
//				
//				// let the member model write it's prediction into the example set
//				member.getModel().performPrediction(currentExampleSet, predictedLabel);
//				
//				// the condition should assure that only on example qualifies
//				double memberprediction = currentExampleSet.getExample(0).getNumericalValue(predictedLabel); 
//				
//				predictionForExample += memberprediction * member.getWeight();				
//			}
//			
//			//currentExampleSet.getExample(0).setValue(predictedLabel, predictionForExample);
//		}
//
//		return exampleSet;
//	}
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		for (EnsembleMember member : members) {
			PredictionModel model = member.getModel();			
			result.append(model.getName());
			result.append(Tools.getLineSeparator());
			result.append(model.toString());
			result.append(Tools.getLineSeparator());
		}

		return result.toString();
	}

	public Iterator<EnsembleMember> iterator() {
		return members.iterator();
	}
	
//	public ExampleSet getExampleSet() {
//	return exampleSet;
//}
//
//public void setExampleSet(ExampleSet exampleSet) {
//	this.exampleSet = exampleSet;
//}
}
