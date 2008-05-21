/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2008 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.operator.learner.meta;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.Learner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.learner.PredictionModel;


/**  
 * This class uses n+1 inner learners and generates n different models
 * by using the last n learners. The predictions of these n models are
 * taken to create n new features for the example set, which is finally
 * used to serve as an input of the first inner learner.  
 * 
 * @author Ingo Mierswa, Helge Homburg
 * @version $Id: AbstractStacking.java,v 1.7 2008/05/09 19:22:47 ingomierswa Exp $
 */
public abstract class AbstractStacking extends AbstractMetaLearner {
    
	public AbstractStacking(OperatorDescription description) {
		super(description);
	}		
	
	/** Returns the model name. */
	public abstract String getModelName();
	
	/** Returns the learner which should be used for stacking. */
	public abstract Operator getStackingLearner() throws OperatorException;
	
	/** Returns the first inner operator which should be learned for base model learning. */
	public abstract int getFirstBaseModelLearnerIndex();
	
	/** Returns the last inner operator which should be learned for base model learning. */
	public abstract int getLastBaseModelLearnerIndex();
	
	/** Indicates if the old attributes should be kept for learning the stacking model. */
	public abstract boolean keepOldAttributes();
	
	public Model learn(ExampleSet exampleSet) throws OperatorException {        
        // learn base models
		List<Model> baseModels = new LinkedList<Model>();
        for (int i = getFirstBaseModelLearnerIndex(); i <= getLastBaseModelLearnerIndex(); i++) {
            Operator currentOperator = getOperator(i);
            IOContainer input = new IOContainer((ExampleSet)exampleSet.clone());
            input = currentOperator.apply(input);
            baseModels.add(input.remove(Model.class));
        }

        // create temporary example set for stacking
        ExampleSet stackingLearningSet = (ExampleSet)exampleSet.clone();
        if (!keepOldAttributes()) {
            stackingLearningSet.getAttributes().clearRegular();
        }
        
        List<Attribute> tempPredictions = new LinkedList<Attribute>();
        int i = 0;
        for (Model baseModel : baseModels) {
            exampleSet = baseModel.apply(exampleSet);
            Attribute predictedLabel = exampleSet.getAttributes().getPredictedLabel();
            // renaming attribute
            predictedLabel.setName("base_prediction" + i);
            // confidences already removed, predicted label is kept in table
            PredictionModel.removePredictedLabel(exampleSet, false, true);
            stackingLearningSet.getAttributes().addRegular(predictedLabel);
            tempPredictions.add(predictedLabel);
            i++;
        }
        
        // learn stacked model
        Model stackingModel = getStackingLearner().apply(new IOContainer(stackingLearningSet)).remove(Model.class);
        
        // remove temporary predictions from table (confidences were already removed)
        PredictionModel.removePredictedLabel(stackingLearningSet);
        for (Attribute tempPrediction : tempPredictions) {
            stackingLearningSet.getAttributes().remove(tempPrediction);
            stackingLearningSet.getExampleTable().removeAttribute(tempPrediction);
        }
        
        // create and return model
		return new StackingModel(exampleSet, getModelName(), baseModels, stackingModel, keepOldAttributes());
	}
   
	public boolean supportsCapability(LearnerCapability capability) {
		Operator learner;
		try {
			learner = getStackingLearner();
			if (learner instanceof Learner) {
				return ((Learner)learner).supportsCapability(capability);
			} else {
				return super.supportsCapability(capability);
			}
		} catch (OperatorException e) {
			return super.supportsCapability(capability);
		}
	}
}
