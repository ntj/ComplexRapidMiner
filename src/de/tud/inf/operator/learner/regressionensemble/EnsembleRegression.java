package de.tud.inf.operator.learner.regressionensemble;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
//import java.util.Random;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleReader;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeValueFilter;
import com.rapidminer.example.set.AttributeValueFilterSingleCondition;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.operator.AbstractIOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.Learner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.learner.meta.AbstractMetaLearner;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeStringCategory;

import de.tud.inf.example.set.ReverseSortedExampleReader;

//OPTION gather statistics on member creation and deletion
//OPTION prediction mechanism (no writing to the example set/creating of attributes)

public class EnsembleRegression extends AbstractMetaLearner {
	// definition of parameter keys
	private static final String ENSEMBLE_STATE_FILE 			= "state file";
	private static final String ENSEMBLE_MAX_MEMBERS 			= "maximum members";
	private static final String ENSEMBLE_MIN_MEMBERS 			= "minimum members";
	private static final String ENSEMBLE_EVICTION_RATIO 		= "eviction ratio";
	private static final String ENSEMBLE_LOCAL_THRESHOLD 		= "local threshold";
	private static final String ENSEMBLE_SIMILARITY_MEASURE 	= "similarity measure";
	private static final String ENSEMBLE_DISCARD_STATE 			= "discard state on first run";
	private static final String ENSEMBLE_INITIAL_WINDOW 		= "initial window";
	
	// definition of the available similarity measures	
	protected static enum ENSEMBLE_SIMILARITY_MEASURES {
		EuclideanDistance
	};

	protected static String[] getMeasureNames() {
		List<String> l = new ArrayList<String>();
		for(Object o : ENSEMBLE_SIMILARITY_MEASURES.values()) {
			l.add(o.toString());
		}
		return l.toArray(new String[] {});
	}
	
	protected static String getDefaultMeasureNames() {
		return ENSEMBLE_SIMILARITY_MEASURES.EuclideanDistance.toString();
	}
	
	// is this the first run off apply
	protected boolean firstRun = true;
	
	//private Random randGen = new Random(0L);
	
	public EnsembleRegression(OperatorDescription description) {
		super(description);
	}
		
	public Model learn(ExampleSet exampleSet) throws OperatorException {
		EnsembleRegressionModel ensemble = null;
		//AbstractLearner learner = null; 
		Operator learner = this.getOperator(0);
		
		File state_file = getParameterAsFile(ENSEMBLE_STATE_FILE);
		
		String similarity_measure_type = getParameterAsString(ENSEMBLE_SIMILARITY_MEASURE);
		Distance distance = null;
		switch (ENSEMBLE_SIMILARITY_MEASURES.valueOf(similarity_measure_type)) {
			case EuclideanDistance:
				distance = new L2Norm();
				break;
			default:
				logError("Unknown distance function");
				break;
		}  

		int max_members = getParameterAsInt(ENSEMBLE_MAX_MEMBERS);
		int min_members = getParameterAsInt(ENSEMBLE_MIN_MEMBERS);
		int initial_window = getParameterAsInt(ENSEMBLE_INITIAL_WINDOW);
		double eviction_ratio = getParameterAsDouble(ENSEMBLE_EVICTION_RATIO);
		double local_threshold = getParameterAsDouble(ENSEMBLE_LOCAL_THRESHOLD);
		boolean discard_model = getParameterAsBoolean(ENSEMBLE_DISCARD_STATE);
		
		// get the ensemble state, i.e. the existing composite model, if it exists; if not, initialize the ensemble
		if( (discard_model == false && state_file.exists()) || (discard_model == true && firstRun == false) ) {
			// read the state file
			//log("State file read");
			InputStream in = null;
			try {
				//in = new GZIPInputStream(new FileInputStream(state_file));
				in = new FileInputStream(state_file);
				ensemble = (EnsembleRegressionModel) AbstractIOObject.read(in);
				//log("Ensemble contains " + ensemble.getNumberOfMembers() + " members.");
			} catch (IOException e) {
				throw new UserError(this, e, 303, new Object[] { state_file, e.getMessage() });
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						logError("Cannot close stream from file " + state_file);
					}
				}
			}
		} else {
			// the ensemble keeps the complete passed example set
			ensemble = new EnsembleRegressionModel(exampleSet);
			firstRun = false; // from now on: read the state file
			log("ensemble initialized");
		}
		
		
		// check whether the old and the new example set are compatible
		// fulfilled by construction for the first run; meaningful check later 
// 		for performance reasons we will just assume that the exmaple sets are compatible
//		if(exampleSetsCompatible(ensemble.getExampleSet(), exampleSet) == false) {
//			logError("New example set not compatible to example set used for training ensemble so far");
//			throw new UserError(this, -1, "Example sets not compatible");
//		}

		// ------------------------------------------------------------------------------------------
		
		// determine the parts of the example set that are new	
		List<Integer> newIds = newExampleIds(ensemble.getSeenIds(), exampleSet);
		
		if(newIds == null) {
			// no new ids --> just return, since no update is possible
			logWarning("no new Examples found; returning old state"); 
		} else {
			// update the ensemble
			for(int id : newIds) {
				ensemble = updateEnsemble(
						exampleSet, 
						ensemble, 
						learner, 
						distance,
						max_members,
						min_members,
						initial_window,
						eviction_ratio,
						local_threshold,
						id);
			}
		}

		
		// maintain the list of seen id's
		Set<Integer> seenIds = ensemble.getSeenIds(); 
		seenIds.addAll(newIds);
		ensemble.setSeenIds(seenIds);
		
		// now we can assign the new example set as reference for the next iteration of apply
		//ensemble.setExampleSet(exampleSet);
		
		// save the ensemble state
		OutputStream out = null;
		try {
			// out = new GZIPOutputStream(new FileOutputStream(state_file));
			out = new FileOutputStream(state_file);
			if(ensemble != null) {
				ensemble.write(out);
			}
			//this.getLog().log("State file updated");
		} catch (IOException e) {
			throw new UserError(this, e, 303, new Object[] { state_file, e.getMessage() });
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logError("Cannot close stream to file " + state_file);
				}
			}
		}
		return ensemble;
	}
		
	protected EnsembleRegressionModel updateEnsemble(
			ExampleSet currentExampleSet, 
			EnsembleRegressionModel ensemble, 
			Operator learnerOperator, 
			Distance distance,
			int max_members,
			int min_members,
			int initial_window,
			double eviction_ratio,
			double local_threshold,
			int mostRecentExample
	) throws OperatorException {
		Learner learner = (Learner) learnerOperator;
		
		// counter for the positive predictions
		double totalPositve = 0;
		
		// build example set for testing containing only the current new item
		Attribute idAttribute = currentExampleSet.getAttributes().getId();
		Attribute labelAttribute = currentExampleSet.getAttributes().getLabel();
		Attribute predictedLabel = createPredictedLabel(currentExampleSet, labelAttribute);
		
		Condition currentExampleCondition = new AttributeValueFilterSingleCondition(
				idAttribute, 
				AttributeValueFilterSingleCondition.EQUALS, 
				Integer.toString(mostRecentExample));
		ConditionedExampleSet currentExample = new ConditionedExampleSet(currentExampleSet, currentExampleCondition);
		
		EnsembleMember deletionCandidate = null;
		
		Iterator<EnsembleMember> iter = ensemble.iterator();
		while(iter.hasNext()) {
			EnsembleMember member = iter.next();
			
			if(member.getState() != MemberState.UNSTABLE) {
				member.getModel().performPrediction(currentExample, predictedLabel);
				
				double label = currentExample.getExample(0).getLabel();
				double prediction = currentExample.getExample(0).getDataRow().get(predictedLabel);
				double dist = distance.distance(label, prediction);
				
				//log(Double.toString(dist));
				
				if(dist < local_threshold) {
					member.incPositive();
				} else {
					member.incNegative();
				}
				
				double ratio = member.getRatio();
				
				// if the member is stable and to bad, add it to the list of deletion candidates
				if(member.getState() == MemberState.STABLE) {
					if(ratio < eviction_ratio) {
						//deletionCandidates.add(member);
						if (deletionCandidate == null) {
							deletionCandidate = member;
						} else {
							if(ratio < deletionCandidate.getRatio()) {
								deletionCandidate = member;
							}
						}
						//log("candidate added");
					}
				}
			}

			// build the new conditioned data set				
			Condition windowCondition = new AttributeValueFilter(currentExampleSet, 
					idAttribute.getName() + " >= " + member.getIntroducedAt() + " && " + idAttribute.getName() + " <= " + mostRecentExample);
			//log(windowCondition.toString());
			
			ConditionedExampleSet memberExampleSet = new ConditionedExampleSet(currentExampleSet, windowCondition);
			member.setModel((PredictionModel) learner.learn(memberExampleSet));
			
			//log(member.getModel().toString());
			
			if(member.getState() != MemberState.STABLE) {
				int attributeCount = currentExampleSet.getAttributes().allSize();
				int spezialCount = currentExampleSet.getAttributes().specialSize();
				int trainingThreshold = attributeCount - spezialCount;
				int stableThreshold = trainingThreshold;
				
				if(member.getState() == MemberState.UNSTABLE) {
					if(mostRecentExample - member.getIntroducedAt() > trainingThreshold) {
						member.setState(MemberState.TRAINING);
					}
				}
				
				if(member.getState() == MemberState.TRAINING) {
					if(mostRecentExample - member.getIntroducedAt() > stableThreshold) {
						member.setState(MemberState.STABLE);
					}
				}
			}
			
			totalPositve += member.getPositive();
		}
		
		removePredictedLabel(currentExampleSet, predictedLabel);
		
		// delete the worst member
		if(deletionCandidate != null) {
			// proceed only if there will be at least min_members be left after deletion
			if(ensemble.getNumberOfMembers() > min_members) {
				// compensate totalPositive
				totalPositve -= deletionCandidate.getPositive();
				ensemble.deleteMember(deletionCandidate);
			} 
		}		
		
		iter = ensemble.iterator();
		while(iter.hasNext()) {
			EnsembleMember member = iter.next();
			double weight = (double) member.getPositive() / (double) totalPositve;
			member.setWeight(weight);
		}
		
		// add new members if needed
		if(ensemble.getNumberOfMembers() < max_members) {
			// only add new members at random
			//if(randGen.nextBoolean()) {
			PredictionModel model  = null;
			
			if(initial_window == 1) {
				model = (PredictionModel) learner.learn(currentExample);
			} else {
				Condition initialCondition = new AttributeValueFilter(currentExampleSet, 
						idAttribute.getName() + " >= " + getLastIDFromWindow(currentExampleSet, initial_window));
				ConditionedExampleSet initialExampleSet = new ConditionedExampleSet(currentExampleSet, initialCondition);
				model = (PredictionModel) learner.learn(initialExampleSet);
			}
			
			EnsembleMember newMember = new EnsembleMember();
			newMember.setWeight(0);
			newMember.setPositive(0);
			newMember.setNegative(0);
			newMember.setIntroducedAt(mostRecentExample);		
			newMember.setState(MemberState.UNSTABLE);
			newMember.setModel(model);
			

			ensemble.addMember(newMember);
			//}
		}

		return ensemble;
	}
	
	protected static int getLastIDFromWindow(ExampleSet exampleSet, int window) {
		int windowId = 0;
		int counter = 0;
		
		ExampleReader reader = new ReverseSortedExampleReader(exampleSet);
		while(reader.hasNext()) {
			Example example = reader.next();
			windowId = (int) example.getId();
			counter++;
			
			if (counter == window) {
				break;
			} 
		}
		
		return windowId;
	}
	
	protected static Attribute createPredictedLabel(ExampleSet exampleSet, Attribute label) {
		// create and add prediction attribute
		Attribute predictedLabel = AttributeFactory.createAttribute(label, Attributes.PREDICTION_NAME);
		predictedLabel.clearTransformations();
		ExampleTable table = exampleSet.getExampleTable();
		table.addAttribute(predictedLabel);
		exampleSet.getAttributes().setPredictedLabel(predictedLabel);
		return predictedLabel;
	}
	
	protected static void removePredictedLabel(ExampleSet exampleSet, Attribute predictedLabel) {
		exampleSet.getExampleTable().removeAttribute(predictedLabel);
		exampleSet.getAttributes().remove(predictedLabel);
	}

	protected List<Integer> newExampleIds(Set<Integer> seenIds, ExampleSet exampleSet) {		
		List<Integer> newIds = new LinkedList<Integer>();
		
		ExampleReader reader = new ReverseSortedExampleReader(exampleSet);
		while(reader.hasNext()) {
			Example currExample = reader.next();
			int currId = (int) currExample.getId();
			
			if(!seenIds.contains(currId)) {
				newIds.add(currId);
			} else {
				//log(Integer.toString(currId));
				break;
			}
		}
		
		return newIds;
	}
	
	public boolean supportsCapability(LearnerCapability capability) {
		Operator innerOperator = this.getOperator(0);
		
		return ((Learner) innerOperator).supportsCapability(capability);
	}
	
	@Override
	public int getMaxNumberOfInnerOperators() {
		return 1;
	}
	
	@Override
	public int getMinNumberOfInnerOperators() {
		return 1;
	}
	
	public void checkInnerOperator() throws UserError {
		// inner operator must be a learner
		Operator innerOperator = this.getOperator(0);

		if (!(innerOperator instanceof Learner)) {
			throw new UserError(this, 127, "Inner operator is not a learner");
		}
	}
	
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		
		ParameterTypeInt max_members = new ParameterTypeInt(
				ENSEMBLE_MAX_MEMBERS, 
				"maximum number of members in the ensemble", 
				1, 
				Integer.MAX_VALUE, 
				5);
		max_members.setExpert(false);
		types.add(max_members);
		
		ParameterTypeInt min_members= new ParameterTypeInt(
				ENSEMBLE_MIN_MEMBERS, 
				"minimum number of members in the ensemble", 
				0, 
				Integer.MAX_VALUE, 
				0);
		min_members.setExpert(true);
		types.add(min_members);
		
		ParameterTypeDouble eviction_ratio = new ParameterTypeDouble(
				ENSEMBLE_EVICTION_RATIO, 
				"minimum weight that ensemble members need", 
				0, 
				Double.MAX_VALUE, 
				0.25);
		eviction_ratio.setExpert(false);
		types.add(eviction_ratio);
		
		ParameterTypeDouble local_threshold = new ParameterTypeDouble(
				ENSEMBLE_LOCAL_THRESHOLD, 
				"threshold for the prediction error", 
				0, 
				Double.MAX_VALUE, 
				5);
		local_threshold.setExpert(false);
		types.add(local_threshold);
		
		ParameterTypeInt initial_window= new ParameterTypeInt(
				ENSEMBLE_INITIAL_WINDOW, 
				"initial size of the training window", 
				1, 
				Integer.MAX_VALUE, 
				1);
		initial_window.setExpert(true);
		types.add(initial_window);
		
		ParameterTypeStringCategory similarity_measure = new ParameterTypeStringCategory(
				ENSEMBLE_SIMILARITY_MEASURE, 
				"similarity measure to use", 
				getMeasureNames(),
				getDefaultMeasureNames());
		similarity_measure.setExpert(true);
		types.add(similarity_measure);
		
		ParameterTypeBoolean discard_state = new ParameterTypeBoolean(
				ENSEMBLE_DISCARD_STATE,
				"discard the ensemble in the state file", 
				false);
		discard_state.setExpert(false);
		types.add(discard_state);
		
		ParameterTypeFile state_file = new ParameterTypeFile(
				ENSEMBLE_STATE_FILE, 
				"path to the ensemble state file", 
				"mod", 
				false);
		state_file.setExpert(false);
		types.add(state_file);
		
		return types;
	}
}
