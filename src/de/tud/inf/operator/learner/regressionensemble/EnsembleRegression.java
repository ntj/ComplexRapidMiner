package de.tud.inf.operator.learner.regressionensemble;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.set.AttributeValueFilter;
import com.rapidminer.example.set.AttributeValueFilterSingleCondition;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.operator.AbstractIOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.tools.ClassNameMapper;

public class EnsembleRegression extends AbstractLearner {
	// definition of parameter keys
	private static final String ENSEMBLE_STATE_FILE 			= "state file";
	private static final String ENSEMBLE_BASE_TYPE 				= "base type";
	private static final String ENSEMBLE_MAX_MEMBERS 			= "maximum members";
	private static final String ENSEMBLE_MIN_MEMBERS 			= "minimum members";
	private static final String ENSEMBLE_INITIAL_WINDOW 		= "initial window";
	private static final String ENSEMBLE_EVICTION_RATIO 		= "eviction ratio";
	private static final String ENSEMBLE_LOCAL_THRESHOLD 		= "local threshold";
	private static final String ENSEMBLE_SIMILARITY_MEASURE 	= "similarity measure";
	private static final String ENSEMBLE_UPDATE_MODEL 			= "update model";
	private static final String ENSEMBLE_DISCARD_STATE 			= "discard state on first run";
	private static final String ENSEMBLE_GRACE_PERIOD			= "grace period";
	
	// definition of the available base learners
	private static final String[] ENSEMBLE_BASE_TYPES = {
		"com.rapidminer.operator.learner.functions.LinearRegression",
		"com.rapidminer.operator.learner.functions.kernel.GPLearner",
		"com.rapidminer.operator.learner.functions.kernel.JMySVMLearner",
		"com.rapidminer.operator.learner.lazy.KNNLearner"
	};
	
	private static final ClassNameMapper ENSEMBLE_BASE_TYPE_MAP = 
		new ClassNameMapper(ENSEMBLE_BASE_TYPES);
	
	// definition of the available similarity measures	
	private static enum ENSEMBLE_SIMILARITY_MEASURES {
		EuclideanDistance
	};

	private static String[] getMeasureNames() {
		List<String> l = new ArrayList<String>();
		for(Object o : ENSEMBLE_SIMILARITY_MEASURES.values()) {
			l.add(o.toString());
		}
		return l.toArray(new String[] {});
	}
	
	private static String getDefaultMeasureNames() {
		return ENSEMBLE_SIMILARITY_MEASURES.EuclideanDistance.toString();
	}
	
	// member variables
	// dummy description for instantiating operators
	private final OperatorDescription dummyDescription;
	// is this the first run off apply
	private boolean firstRun = true;
	
	public EnsembleRegression(OperatorDescription description) {
		super(description);
		dummyDescription = description;
	}
		
	private boolean exampleSetsCompatible(ExampleSet setOld, ExampleSet setNew) {
		Attributes oldAttrib = setOld.getAttributes();
		Attributes newAttrib = setNew.getAttributes();
		Iterator<Attribute> oldIter = oldAttrib.iterator();
		Iterator<Attribute> newIter = newAttrib.iterator();
		
		if(oldAttrib.allSize() != newAttrib.allSize()) {
			return false;
		}
		
		while(oldIter.hasNext()) {
			Attribute oldA = oldIter.next();
			Attribute newA = newIter.next();
			
			if(! oldA.getName().equals(newA.getName())) {
				return false;
			}
		}
		
		if(!(setNew.getAttributes().getId() != null)) {
			return false;
		}
		
		if(!(setOld.size() <= setNew.size())) {
			return false;
		}
		return true;
	}
	
	private List<Integer> newExampleIds(ExampleSet setOld, ExampleSet setNew) {		
		List<Integer> newIds = null;
		
		setOld.recalculateAttributeStatistics(setOld.getAttributes().getId());
		int maxIdOld = (int) setOld.getStatistics(setOld.getAttributes().getId(), Statistics.MAXIMUM);
		setNew.recalculateAttributeStatistics(setNew.getAttributes().getId());
		int maxIdNew = (int) setNew.getStatistics(setNew.getAttributes().getId(), Statistics.MAXIMUM);
		
		// if the sets are the same we are in the first run!
		if(setOld == setNew) { 
			int minIdOld = (int) setOld.getStatistics(setOld.getAttributes().getId(), Statistics.MINIMUM);
			newIds = new ArrayList<Integer>(maxIdOld - minIdOld);
			for(int i = (minIdOld); i <= maxIdOld; ++i) {
				newIds.add(i);
			}
		} else {
			if(maxIdOld == maxIdNew) {
				return null;
			} else {
				newIds = new ArrayList<Integer>(maxIdNew - maxIdOld);
				for(int i = (maxIdOld + 1); i <= maxIdNew; ++i) {
					newIds.add(i);
				}
			}
		}
		return newIds;
	}
	
	public Model learn(ExampleSet exampleSet) throws OperatorException {
		EnsembleRegressionModel ensemble = null;
		AbstractLearner learner = null; 
		
		File state_file = getParameterAsFile(ENSEMBLE_STATE_FILE);
		String learner_type = getParameterAsString(ENSEMBLE_BASE_TYPE);
		@SuppressWarnings("unchecked")
		Class<AbstractLearner> learnerClass = (Class<AbstractLearner>) ENSEMBLE_BASE_TYPE_MAP.getClassByShortName(learner_type);
		try {
			Constructor<AbstractLearner> learnerConstructor = 
				learnerClass.getConstructor(new Class[] {OperatorDescription.class});
			learner = learnerConstructor.newInstance(dummyDescription);
		} catch (SecurityException e) {
			throw new UserError(null, 904, learner_type, e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new UserError(null, 904, learner_type, e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new UserError(null, 904, learner_type, e.getMessage());
		} catch (InstantiationException e) {
			throw new UserError(null, 904, learner_type, e.getMessage());
		} catch (IllegalAccessException e) {
			throw new UserError(null, 904, learner_type, e.getMessage());
		} catch (InvocationTargetException e) {
			throw new UserError(null, 904, learner_type, e.getMessage());
		}
		
		String similarity_measure_type = getParameterAsString(ENSEMBLE_SIMILARITY_MEASURE);
		Distance distance = null;
		switch (ENSEMBLE_SIMILARITY_MEASURES.valueOf(similarity_measure_type)) {
		case EuclideanDistance:
			distance = new MyEuclideanDistance();
			break;
		default:
			logError("Unknown distance function");
			break;
		}  

		int max_members = getParameterAsInt(ENSEMBLE_MAX_MEMBERS);
		int min_members = getParameterAsInt(ENSEMBLE_MIN_MEMBERS);
		int initial_window = getParameterAsInt(ENSEMBLE_INITIAL_WINDOW);
		int grace_period = getParameterAsInt(ENSEMBLE_GRACE_PERIOD);
		double eviction_ratio = getParameterAsDouble(ENSEMBLE_EVICTION_RATIO);
		double local_threshold = getParameterAsDouble(ENSEMBLE_LOCAL_THRESHOLD);
		boolean update_model = getParameterAsBoolean(ENSEMBLE_UPDATE_MODEL);
		boolean discard_model = getParameterAsBoolean(ENSEMBLE_DISCARD_STATE);
		
		// 
		// ------------------------------------------------------------------------------------------
		//
		
		// get the ensemble state, i.e. the existing composite model, if it exists; if not, initialize the ensemble
		// OPTION: move into own operator; add Model.class to input class list
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
		if(exampleSetsCompatible(ensemble.getExampleSet(), exampleSet) == false) {
			logError("New example set not compatible to example set used for training ensemble so far");
			throw new UserError(this, -1, "Example sets not compatible");
		}
		
		// 
		// ------------------------------------------------------------------------------------------
		//
		
		// determine the parts of the example set that are new	
		List<Integer> newIds = newExampleIds(ensemble.getExampleSet(), exampleSet);
		
		if(newIds == null) {
			// no new ids --> just return, since no updat is possible
			logWarning("no new Examples found; returning old state"); 
		} else {
			// update the ensemble
			if(update_model == true) {
				for(int id : newIds) {
					ensemble = updateEnsemble(
							exampleSet, 
							ensemble, 
							learner, 
							learner_type,
							distance,
							max_members,
							min_members,
							initial_window,
							grace_period,
							eviction_ratio,
							local_threshold,
							id);
				}
			}
		}
		
		//
		// ------------------------------------------------------------------------------------------
		//
		
		// now we can assign the new example set as reference for the next iteration of apply
		ensemble.setExampleSet(exampleSet);
		
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
	
//	int min = 10;
//	int max = 10;
//	String parameterString = "id >= " + min + " && " + " id <=" + max;
//	Condition cond = new AttributeValueFilterSingleCondition(currentExampleSet, parameterString);
	
	private static Attribute createPredictedLabel(ExampleSet exampleSet, Attribute label) {
		// create and add prediction attribute
		Attribute predictedLabel = AttributeFactory.createAttribute(label, Attributes.PREDICTION_NAME);
		predictedLabel.clearTransformations();
		ExampleTable table = exampleSet.getExampleTable();
		table.addAttribute(predictedLabel);
		exampleSet.getAttributes().setPredictedLabel(predictedLabel);
		return predictedLabel;
	}
	
	private static void removePredictedLabel(ExampleSet exampleSet, Attribute predictedLabel) {
		exampleSet.getExampleTable().removeAttribute(predictedLabel);
		exampleSet.getAttributes().remove(predictedLabel);
	}
	
	@SuppressWarnings("unused")
	private EnsembleRegressionModel updateEnsemble(
			ExampleSet currentExampleSet, 
			EnsembleRegressionModel ensemble, 
			AbstractLearner learner, 
			String base_type,
			Distance distance,
			int max_members,
			int min_members,
			int initial_window,
			int grace_period,
			double eviction_ratio,
			double local_threshold,
			int mostRecentExample
	) 
	throws OperatorException 
	{
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
		
		Condition notNewerThenCurrentExample = new AttributeValueFilterSingleCondition(
				idAttribute, 
				AttributeValueFilterSingleCondition.LEQ, 
				Integer.toString(mostRecentExample));
		
		List<EnsembleMember> deletionCandidates = new ArrayList<EnsembleMember>();
		
		Iterator<EnsembleMember> iter = ensemble.iterator();
		while(iter.hasNext()) {
			EnsembleMember member = iter.next();
			
			if(member.getState() != MemberState.UNSTABLE) {
				ExampleSet predictionResult = member.getModel().performPrediction(currentExample, predictedLabel);
				double label = currentExample.getExample(0).getDataRow().get(labelAttribute);
				double prediction = currentExample.getExample(0).getDataRow().get(predictedLabel);
				double dist = distance.distance(label, prediction);
				
				if(dist < local_threshold) {
					member.incPositive();
				} else {
					member.incNegative();
				}
				
				double ratio = (double) member.getPositive() / (double) (member.getPositive() + member.getNegative());
				//log(Integer.toString(member.getPositive()) + " " + Integer.toString(member.getNegative()));
				//log(Double.toString(ratio));
				
				// if the member is stable and to bad, add it to the list of deletion candidates
				if(member.getState() == MemberState.STABLE) {
					if(ratio < eviction_ratio) {
						deletionCandidates.add(member);
						log("candidate added");
					}
				}
			}
			
			// build the new conditioned data set				
			AttributeValueFilter windowCondition = new AttributeValueFilter(currentExampleSet, 
					"id >= " + member.getIntroducedAt() + " && id <= " + mostRecentExample);
			//log(windowCondition.toString());
			
			ConditionedExampleSet memberExampleSet = new ConditionedExampleSet(currentExampleSet, windowCondition);
			member.setModel((PredictionModel) learner.learn(memberExampleSet));
			
			//log(member.getModel().toString());
			//memberExampleSet = null;
			
			if(member.getState() != MemberState.STABLE) {
				int attributeCount = currentExampleSet.getAttributes().allSize();
				int spezialCount = currentExampleSet.getAttributes().specialSize();
				int trainingThreshold = attributeCount - spezialCount;
				int stableThreshold = trainingThreshold + grace_period;
				
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
		if(deletionCandidates.size() > 0) {
			double minRatio = Double.MAX_VALUE;
			int minMemberIndex = -1;
			
			for(int i = 0; i < deletionCandidates.size(); i++) {
				EnsembleMember member = deletionCandidates.get(i);
				double ratio = (double) member.getPositive() / (double) (member.getPositive() + member.getNegative());
				
				if(ratio < minRatio) {
					minRatio = ratio;
					minMemberIndex = i;
				}
			}
			
			// compensate totalPositive
			totalPositve -= deletionCandidates.get(minMemberIndex).getPositive();
			ensemble.deleteMember(deletionCandidates.get(minMemberIndex));
		}
		
		
		iter = ensemble.iterator();
		while(iter.hasNext()) {
			EnsembleMember member = iter.next();
			double weight = (double) member.getPositive() / (double) totalPositve;
			member.setWeight(weight);
		}
		
		// add new members if needed
		// OPTION: may be use larger window to get immediately stable members!
		if(ensemble.getNumberOfMembers() < max_members) {
			EnsembleMember newMember = new EnsembleMember();
			//newMember.setMemberId(42);
			newMember.setWeight(0);
			newMember.setPositive(0);
			newMember.setNegative(0);
			newMember.setIntroducedAt(mostRecentExample);		
			//newMember.setTrainedLast(newId);
			newMember.setState(MemberState.UNSTABLE);
			newMember.setModelType(base_type);
			newMember.setModel((PredictionModel) learner.learn(currentExampleSet));
			
			log("new " + mostRecentExample);
			log(newMember.getModel().toString());
			
			ensemble.addMember(newMember);
		}

		return ensemble;
	}

	public boolean supportsCapability(LearnerCapability capability) {
		if (capability.equals(LearnerCapability.NUMERICAL_ATTRIBUTES))
			return true;
		if (capability.equals(LearnerCapability.NUMERICAL_CLASS))
			return true;
		return false;
	}
	
	// OPTION: add parameters of the base learners
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		
		ParameterTypeStringCategory base_type = new ParameterTypeStringCategory(
				ENSEMBLE_BASE_TYPE, 
				"the type of learner to use in the ensemble", 
				ENSEMBLE_BASE_TYPE_MAP.getShortClassNames(), 
				ENSEMBLE_BASE_TYPE_MAP.getShortClassNames()[0]);
		base_type.setExpert(false);
		types.add(base_type);
		
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
		
		ParameterTypeInt grace_period = new ParameterTypeInt(
				ENSEMBLE_GRACE_PERIOD, 
				"grace period for new ensemble members", 
				0, 
				Integer.MAX_VALUE, 
				0);
		grace_period.setExpert(true);
		types.add(grace_period);
		
		ParameterTypeStringCategory similarity_measure = new ParameterTypeStringCategory(
				ENSEMBLE_SIMILARITY_MEASURE, 
				"similarity measure to use", 
				getMeasureNames(),
				getDefaultMeasureNames());
		similarity_measure.setExpert(true);
		types.add(similarity_measure);
		
		ParameterTypeBoolean update_model = new ParameterTypeBoolean(
				ENSEMBLE_UPDATE_MODEL, 
				"update the model", 
				true);
		update_model.setExpert(false);
		types.add(update_model);
		
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
	
	private interface Distance {
		public double distance(double d1, double d2);
	}
	
	private class MyEuclideanDistance implements Distance {
		public double distance(double d1, double d2) {
			return Math.sqrt((d1 - d2) * (d1 - d2));
		}
	}
}
