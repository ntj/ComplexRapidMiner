package de.tud.inf.operator.learner.regressionensemble;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleReader;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeValueFilter;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.set.SortedExampleReader;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.ExampleTable;
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

import de.tud.inf.support.TreeMultiMap;

public class BatchEnsembleRegression extends AbstractMetaLearner {
	private static final String ENSEMBLE_MAX_MEMBERS 		= "maximum members";
	private static final String ENSEMBLE_LOCAL_THRESHOLD 	= "local threshold";
	private static final String ENSEMBLE_SIMILARITY_MEASURE = "similarity measure";
	private static final String ENSEMBLE_SLIDING_TEST		= "test on training window";
	private static final String ENSEMBLE_STATE_FILE 		= "state file";
	private static final String ENSEMBLE_FULL_MATERIALIZED	= "materialize full";
	private static final String ENSEMBLE_PENALTY_WEIGHT		= "penalty weight";
	private static final String ENSEMBLE_SAMPLING_INTERVAL	= "sampling interval";

	public BatchEnsembleRegression(OperatorDescription description) {
		super(description);
	}

	public Model learn(ExampleSet exampleSet) throws OperatorException {
		EnsembleRegressionModel ensemble = null;
		Operator learner = this.getOperator(0);
		
		File state_file = getParameterAsFile(ENSEMBLE_STATE_FILE);
		
		int max_members = getParameterAsInt(ENSEMBLE_MAX_MEMBERS);
		int sampling_interval = getParameterAsInt(ENSEMBLE_SAMPLING_INTERVAL);
		double local_threshold = getParameterAsDouble(ENSEMBLE_LOCAL_THRESHOLD);
		double penalty_weight = getParameterAsDouble(ENSEMBLE_PENALTY_WEIGHT);
		boolean sliding_test = getParameterAsBoolean(ENSEMBLE_SLIDING_TEST);
		boolean materialize_full = getParameterAsBoolean(ENSEMBLE_FULL_MATERIALIZED);
		
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

		if(materialize_full == true) {
			ensemble = createEnsembleTotalMaterialized(exampleSet, learner, distance, max_members, local_threshold, penalty_weight, sliding_test, sampling_interval); 
		} else {
			ensemble = createEnsembleScanMaterialized(exampleSet, learner, distance, max_members, local_threshold, penalty_weight, sliding_test, sampling_interval);
		}
		
		log("Back in main");
		// save the ensemble
		OutputStream out = null;
		try {
			out = new FileOutputStream(state_file);
			if(ensemble != null) {
				ensemble.write(out);
			}
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
		log("Write done");
		return ensemble;
	}
	
	private EnsembleRegressionModel createEnsembleTotalMaterialized(
			ExampleSet exampleSet, 
			Operator learnerOperator, 
			Distance distance,
			int max_members,
			double local_threshold,
			double penalty_weight,
			boolean sliding_test,
			int sampling_interval
	) throws OperatorException {
		EnsembleRegressionModel ensemble = new EnsembleRegressionModel(exampleSet);	
		HashMap<Integer, EnsembleMember> candidates = new HashMap<Integer, EnsembleMember>();
		//EnsembleMember[] candidates = new EnsembleMember[exampleSet.size()];
		TreeMultiMap<Double, Integer> ratios = new TreeMultiMap<Double, Integer>();

		Attribute idAttribute = exampleSet.getAttributes().getId();
		Attribute labelAttribute = exampleSet.getAttributes().getLabel();
		Attribute predictedLabel = createPredictedLabel(exampleSet, labelAttribute);
		
		Learner learner = (Learner) learnerOperator;
		int first = 1;
		int last = exampleSet.size();
		int size = last - first + 1;
		
		// iterate over all possible windows
		// OPTION use a sampling schema to select only some windows
		// FIXME extract collection of all values of the id attribute
		
		ArrayList<Integer> ids = getAllIds(exampleSet);
		int maxId = ids.get(ids.size() - 1);
		
		for(int round = last; round >= first; round = round - sampling_interval) {
			int currentId = ids.get(round - 1);
			
			//String windowConditionExpression = idAttribute.getName() + " >= " + round + " && " + idAttribute.getName() + " <= " + last;
			String windowConditionExpression = idAttribute.getName() + " >= " + currentId + " && " + idAttribute.getName() + " <= " + maxId;
			
			Condition windowCondition = new AttributeValueFilter(exampleSet, windowConditionExpression);
			//log(windowCondition.toString());

			ConditionedExampleSet window = new ConditionedExampleSet(exampleSet, windowCondition);
			//log(Integer.toString(window.size()));
			
			PredictionModel model = (PredictionModel) learner.learn(window);
			EnsembleMember member = new EnsembleMember();
			int positives = 0;
			int negatives = 0;
			
			// testing
			if (sliding_test == true) {
				// sliding test -> test on all values used in training!
				// iterate over all examples in the window and test [e.g. batch predict]; result will be written to predictedLabel
				model.performPrediction(window, predictedLabel);
				
				// test on window! --> iterate for classification
				Iterator<Example> iter = window.iterator();
				while (iter.hasNext()) {
					Example example = iter.next();
					double label = example.getLabel();
					double prediction = example.getNumericalValue(predictedLabel);
					double dist = distance.distance(label, prediction);
					
					if(dist < local_threshold) {
						positives++;
					} else {
						negatives++;
					}
				}
			} else {
				// test on test set...
			}
			
			//log(Double.toString(dist));
			member.setPositive(positives);
			member.setNegative(negatives);
			member.setIntroducedAt(currentId);		

			//OPTION correct state init
			member.setState(MemberState.STABLE);
			member.setModel(model);
			
			//candidates[round - 1] = member;
			candidates.put(round-1, member);
			double ratio = member.getRatio();
			double size_penalty = ((double) ((size + 1) - round)) / ((double)size);   
			double key = penalty_weight * size_penalty + (1 - penalty_weight) * ratio;
			
			// put into map of ratios; use corrected index for the member
			ratios.put(key, (round-1));
		}
		
		// select members 
		Entry<Double, ArrayList<Integer>> currEntry = ratios.lastEntry();
		ArrayList<Integer> currList = currEntry.getValue();
		double currKey = currEntry.getKey();
		
		EnsembleMember selected_member = null;
		int ensemble_positives = 0;
		int selected_members = 0;
		int currIndex = 0;
		int memberIndex;
		
		while(selected_members < max_members) {
			if(currList != null) {
				// the index of the member is also round number when added
				memberIndex = currList.get(currIndex);
				// retrieve the member
				//selected_member = candidates[memberIndex];
				selected_member = candidates.get(memberIndex);
				// maintain number sum of positives in ensemble
				ensemble_positives += selected_member.getPositive();
				// add the member to the ensemble
				ensemble.addMember(selected_member);
				selected_members++;
				
				// book keeping
				if(currIndex < (currList.size() - 1)) {
					currIndex++;
				} else {
					currEntry = ratios.lowerEntry(currKey);
					if(currEntry != null) {
						currList = currEntry.getValue();
						currKey = currEntry.getKey();
						currIndex = 0;
					} else {
						break;
					}
				}
			} else {
				break;
			}
		}
		log(ratios.toString());
		
		ensemble.setSeenIds(new HashSet<Integer>(ids));
		
		for (EnsembleMember mem : ensemble) {
			mem.setWeight(mem.getPositive() / ensemble_positives);
			log(Double.toString(mem.getRatio()));
			log(Double.toString(mem.getIntroducedAt()));
		}

		// clean up
		removePredictedLabel(exampleSet, predictedLabel);
		
		return ensemble;
	}
	
	//TODO then exchange the test protocol!
	private EnsembleRegressionModel createEnsembleScanMaterialized(
			ExampleSet exampleSet, 
			Operator learnerOperator, 
			Distance distance,
			int max_members,
			double local_threshold,
			double penalty_weight,
			boolean sliding_test,
			int sampling_interval
	) throws OperatorException {
		EnsembleRegressionModel ensemble = new EnsembleRegressionModel(exampleSet);
		// candidate set; is maintained while iterating through the windows!
		HashMap<Integer, EnsembleMember> candidates = new HashMap<Integer, EnsembleMember>();
		// the ratios for maintaing the candidates!
		TreeMultiMap<Double, Integer> ratios = new TreeMultiMap<Double, Integer>();
		
		Attribute idAttribute = exampleSet.getAttributes().getId();
		Attribute labelAttribute = exampleSet.getAttributes().getLabel();
		Attribute predictedLabel = createPredictedLabel(exampleSet, labelAttribute);
		
		Learner learner = (Learner) learnerOperator;
		int first = 1;
		int last = exampleSet.size();
		int size = last - first + 1;
		
		// iterate over all possible windows
		// OPTION use a sampling schema to select only some windows
		ArrayList<Integer> ids = getAllIds(exampleSet);
		int maxId = ids.get(ids.size() - 1);

		for(int round = last; round >= first; round = round - sampling_interval) {
			int currentId = ids.get(round - 1);
			
			//String windowConditionExpression = idAttribute.getName() + " >= " + round + " && " + idAttribute.getName() + " <= " + last;
			String windowConditionExpression = idAttribute.getName() + " >= " + currentId + " && " + idAttribute.getName() + " <= " + maxId;
			
			Condition windowCondition = new AttributeValueFilter(exampleSet, windowConditionExpression);
			//log(windowCondition.toString());

			ConditionedExampleSet window = new ConditionedExampleSet(exampleSet, windowCondition);
			//log(Integer.toString(window.size()));
			
			PredictionModel model = (PredictionModel) learner.learn(window);
			EnsembleMember member = new EnsembleMember();
			int positives = 0;
			int negatives = 0;
			
			// testing
			if (sliding_test == true) {
				// sliding test -> test on all values used in training!
				// iterate over all examples in the window and test [e.g. batch predict]; result will be written to predictedLabel
				model.performPrediction(window, predictedLabel);
				
				// test on window! --> iterate for classification
				Iterator<Example> iter = window.iterator();
				while (iter.hasNext()) {
					Example example = iter.next();
					double label = example.getLabel();
					double prediction = example.getNumericalValue(predictedLabel);
					double dist = distance.distance(label, prediction);
					
					if(dist < local_threshold) {
						positives++;
					} else {
						negatives++;
					}
				}
			} else {
				// test on test set...
			}
			
			//log(Double.toString(dist));
			member.setPositive(positives);
			member.setNegative(negatives);
			member.setIntroducedAt(currentId);		

			//OPTION correct state init
			member.setState(MemberState.STABLE);
			member.setModel(model);
			
			//candidates[round - 1] = member;
			double ratio = member.getRatio();
			double size_penalty = ((double) ((size + 1) - round)) / ((double)size);   
			double currentPenalizedRatio = penalty_weight * size_penalty + (1 - penalty_weight) * ratio;
			
			if(candidates.size() < max_members) {
				// can safely use round as "index" here
				candidates.put(round, member);
				ratios.put(currentPenalizedRatio, round);
				//ensemble_positives += member.getPositive();
			} else {
				// check weather there is a lower ranking member to replace; always replace in the lowest Key
				double leastKey = ratios.firstKey();
				
				if(leastKey < currentPenalizedRatio) {
					// there is at least one entry
					int leastMemberIndex = ratios.get(leastKey).get(0);
					
					// maintain ensemble_positices 
					//ensemble_positives -=  candidates.get(leastMemberIndex).getPositive();
					
					// remove it
					ratios.remove(leastKey, leastMemberIndex);
					candidates.remove(leastMemberIndex);
					
					// now put the new member
					candidates.put(round, member);
					ratios.put(currentPenalizedRatio, round);
					//ensemble_positives += member.getPositive();
				}
			}
		}
		
		int ensemble_positives = 0;
		
		for (Iterator<EnsembleMember> iterator = candidates.values().iterator(); iterator.hasNext();) {
			EnsembleMember member = iterator.next();
			ensemble_positives += member.getPositive();
			ensemble.addMember(member);
		}
		
		ensemble.setSeenIds(new HashSet<Integer>(ids));

		for (EnsembleMember mem : ensemble) {
			mem.setWeight(mem.getPositive() / ensemble_positives);
			log(Double.toString(mem.getRatio()));
			log(Double.toString(mem.getIntroducedAt()));
		}

		// clean up
		removePredictedLabel(exampleSet, predictedLabel);
		
		return ensemble;
	}
	
	protected ArrayList<Integer> getAllIds(ExampleSet exampleSet) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		
		ExampleReader reader = new SortedExampleReader(exampleSet);
		
		while(reader.hasNext()) {
			Example currExample = reader.next();
			ids.add((int) currExample.getId());
		}
		
		return ids;
	}
	
//	protected Set<Integer> buildSeenIds(List<Integer> ids, int firstIndex) {
//		Set<Integer> seenIds = new HashSet<Integer>();
//		
//		for(int i = firstIndex; i < ids.size(); i++) {
//			seenIds.add(ids.get(i));
//		}
//		
//		return seenIds;
//	}
	
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
	
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		
		ParameterTypeFile state_file = new ParameterTypeFile(
				ENSEMBLE_STATE_FILE, 
				"path to the ensemble state file", 
				"mod", 
				false);
		state_file.setExpert(false);
		types.add(state_file);
		
		ParameterTypeStringCategory similarity_measure = new ParameterTypeStringCategory(
				ENSEMBLE_SIMILARITY_MEASURE, 
				"similarity measure to use", 
				getMeasureNames(),
				getDefaultMeasureNames());
		similarity_measure.setExpert(true);
		types.add(similarity_measure);
		
		ParameterTypeInt max_members = new ParameterTypeInt(
				ENSEMBLE_MAX_MEMBERS, 
				"maximum number of members to select into ensemble", 
				1, 
				Integer.MAX_VALUE, 
				5);
		max_members.setExpert(false);
		types.add(max_members);
		
		ParameterTypeDouble local_threshold = new ParameterTypeDouble(
				ENSEMBLE_LOCAL_THRESHOLD, 
				"local prediction error threshold", 
				0, 
				Double.MAX_VALUE, 
				5);
		local_threshold.setExpert(false);
		types.add(local_threshold);
		
		ParameterTypeBoolean sliding_test = new ParameterTypeBoolean(
				ENSEMBLE_SLIDING_TEST,
				"test member candidates on training set",
				true
				);
		sliding_test.setExpert(false);
		types.add(sliding_test);
		
		ParameterTypeBoolean materialize_full = new ParameterTypeBoolean(
				ENSEMBLE_FULL_MATERIALIZED,
				"materialize all possible members",
				true
				);
		materialize_full.setExpert(false);
		types.add(materialize_full);
		
		ParameterTypeDouble penalty_weight = new ParameterTypeDouble(
				ENSEMBLE_PENALTY_WEIGHT,
				"weight of the window legth penalty term",
				0.0,
				1.0,
				0.0
				);
		penalty_weight.setExpert(false);
		types.add(penalty_weight);
		
		ParameterTypeInt sampling_interval = new ParameterTypeInt(
				ENSEMBLE_SAMPLING_INTERVAL,
				"interval between successively tested windows",
				1,
				Integer.MAX_VALUE,
				1
				);
		sampling_interval.setExpert(true);
		types.add(sampling_interval);
		
		return types;
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
}
