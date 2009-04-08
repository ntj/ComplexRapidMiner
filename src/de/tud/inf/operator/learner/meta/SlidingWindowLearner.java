package de.tud.inf.operator.learner.meta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeValueFilter;
import com.rapidminer.example.set.AttributeValueFilterSingleCondition;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.operator.AbstractIOObject;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.Learner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.learner.functions.LinearRegression;
import com.rapidminer.operator.learner.meta.AbstractMetaLearner;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInt;

public class SlidingWindowLearner extends AbstractMetaLearner {
	
	public static final String ENSAMBLE_WINDOW_SIZE = "window size";
	public static final String ENSAMBLE_STATE_FILE = "state file";
	public static final String ENSABMLE_OVERRIDE = "override";
	
	private boolean firstRun = true;

	@Override
	public Model learn(ExampleSet exampleSet) throws OperatorException {
		
		boolean override = this.getParameterAsBoolean(ENSABMLE_OVERRIDE);
		int windowSize = this.getParameterAsInt(ENSAMBLE_WINDOW_SIZE);
		File stateFile = this.getParameterAsFile(ENSAMBLE_STATE_FILE);
		
		ExampleSet updatedSet = null;
		
		SlidingWindowLearnerModel learningModel = null;
		
		/*check process setup*/
		checkInnerOperator();
			
			if((!override && stateFile.exists()) || (override && !firstRun)) {
				
				// load the model from the state file
				learningModel = loadModel(stateFile);
			} else {
				
				/* create a new Model*/
				learningModel = new SlidingWindowLearnerModel(exampleSet);
				firstRun = false;
			}
	
			if(exampleSetsCompatible(learningModel.getTraining(), exampleSet) == false) {
				logError("New example set not compatible to example set used for training ensemble so far");
				throw new OperatorException("Example sets not compatible");
			}
			
			/* new Examples available? */
			Condition newIds = new AttributeValueFilterSingleCondition(
					exampleSet.getAttributes().getId(),
					AttributeValueFilterSingleCondition.GREATER, String
							.valueOf(learningModel.getRecentId()));
			
			ConditionedExampleSet newIDSet = new ConditionedExampleSet(exampleSet,newIds);
			
			if(newIDSet.size() > 0) {
				
				/* neue Examples vorhanden */
				updatedSet = exampleSet;
			}
		
		
		/* update the Model*/
		if(updatedSet != null) {
			
			/*sort according to the id*/
			updatedSet = new SortedExampleSet(updatedSet,updatedSet.getAttributes().getId(),SortedExampleSet.DECREASING);
			
			learningModel.setRecentId(updatedSet.getExample(0).getId());
			
			learningModel.setLeastId((updatedSet.size() > windowSize ? updatedSet.getExample(windowSize - 1).getId() : updatedSet.getExample(updatedSet.size() - 1).getId()));
			
			/* train the new model*/
			
			Condition windowCondition = new AttributeValueFilter(updatedSet, 
					"id >= " + learningModel.getLeastId() + " && id <= " + learningModel.getRecentId());
			
			ConditionedExampleSet trainingSet = new ConditionedExampleSet(updatedSet,windowCondition);
			
			learningModel.setTraining(exampleSet);
			
			learningModel.setTrainingSize(trainingSet.size());
			
			Model newModel = getOperator(0).apply(new IOContainer(new IOObject[] {trainingSet})).get(Model.class);
			
			learningModel.setPredictionModel(newModel);
			
			/*save the state of the model*/
			saveModel(learningModel, stateFile);
		}
		
		return learningModel;
	}

	public SlidingWindowLearner(OperatorDescription description) throws UserError {
		
		super(description);
	}

	@Override
	public int getMaxNumberOfInnerOperators() {
		
		return 1;
	}
	
	public void checkInnerOperator() throws UserError {
		
		// inner Operator must be a Learner and has to support numerical labels
		Operator innerOperator = this.getOperator(0);

		if (!(innerOperator instanceof Learner)
				|| (innerOperator instanceof Learner && !((Learner) innerOperator)
						.supportsCapability(LearnerCapability.NUMERICAL_CLASS)))

			throw new UserError(this, 127, "Base Learner is not supported");
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		
		ParameterType type;
		
		List<ParameterType> types = super.getParameterTypes();
		
		type = new ParameterTypeInt(ENSAMBLE_WINDOW_SIZE,"The number of Examples to consider",0,Integer.MAX_VALUE,5);
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeFile(ENSAMBLE_STATE_FILE,"path to the ensemble state file","mod",false);
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeBoolean(ENSABMLE_OVERRIDE,"Indicates if the state file should be overridden on the first run",false);
		type.setExpert(false);
		types.add(type);
		
		return types;
	}
	
	public SlidingWindowLearnerModel loadModel(File file) throws UserError {
		
		SlidingWindowLearnerModel loadedModel = null;
			
		InputStream in = null;
		
		try {
			
			in = new FileInputStream(file);
			
			loadedModel = (SlidingWindowLearnerModel) AbstractIOObject.read(in);
			
		} catch (FileNotFoundException e) {
			
			throw new UserError(this,301,file.getPath());
			
		} catch (IOException e) {
			
			throw new UserError(this,302,file.getAbsolutePath(),e.getCause());
		} finally {
			
			if(in != null) {
				
				try {
					in.close();
				} catch (IOException e) {
					logError("Cannot close stream from file " + file);
				}
			}
		}
		
		return loadedModel;
	}
	
	public void saveModel(Model model, File file) throws UserError {
		
		OutputStream out = null;
		
		try {
			
			out = new FileOutputStream(file);
			
			if(model != null)
				model.write(out);
			
		} catch (FileNotFoundException e) {
			
			throw new UserError(this,301,file.getPath());
		} catch (IOException e) {
			
			throw new UserError(this,303,file,e.getCause());
		} finally {
			
			if(out != null)
				try {
					out.close();
				} catch (IOException e) {
					logError("Cannot close stream to file " + file);
				}
		}
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

}
