/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.learner.meta;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.Tools;


/**  
 * This class is the model build by the {@link Stacking} operator.
 * 
 * @author Ingo Mierswa, Helge Homburg
 * @version $Id: StackingModel.java,v 1.3 2007/07/13 22:52:12 ingomierswa Exp $ 
 */
public class StackingModel extends PredictionModel {
	
	private static final long serialVersionUID = -3978054415189320147L;

	private String modelName;
	
	private List<Model> baseModels;
	
    private Model stackingModel;
    
    private boolean useAllAttributes;
    
	public StackingModel(ExampleSet exampleSet, String modelName, List<Model> baseModels, Model stackingModel, boolean useAllAttributes) {
		super(exampleSet);
		this.modelName = modelName;
		this.baseModels = baseModels;
        this.stackingModel = stackingModel;
        this.useAllAttributes = useAllAttributes;
	}
	
	public String getName() {
		return this.modelName;
	}
	
	public void performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
        // init
        PredictionModel.removePredictedLabel(exampleSet, true, true);
        ExampleSet stackingExampleSet = (ExampleSet)exampleSet.clone();        
        if (!useAllAttributes) {
            stackingExampleSet.getAttributes().clearRegular();
        }

        // create predictions from base models
        List<Attribute> tempPredictions = new LinkedList<Attribute>();
        for (Model baseModel : baseModels) {
            baseModel.apply(exampleSet);
            Attribute basePrediction = exampleSet.getAttributes().getPredictedLabel();
            PredictionModel.removePredictedLabel(exampleSet, false, true);
            stackingExampleSet.getAttributes().addRegular(basePrediction);
            tempPredictions.add(basePrediction);
        }
        
        // apply stacking model and copy prediction to original example set
        stackingModel.apply(stackingExampleSet);
        PredictionModel.copyPredictedLabel(stackingExampleSet, exampleSet);
        
        // remove temporary predictions from table
        for (Attribute tempPrediction : tempPredictions) {
            stackingExampleSet.getAttributes().remove(tempPrediction);
            stackingExampleSet.getExampleTable().removeAttribute(tempPrediction);
        }
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer(super.toString() + Tools.getLineSeparators(2));
        result.append(this.modelName + ":");
        result.append(Tools.getLineSeparator() + stackingModel.toString() + Tools.getLineSeparators(2));
        
        result.append("Base Models:");
		for (Model baseModel : baseModels)
			result.append(Tools.getLineSeparator() + baseModel.toString());
		return result.toString();
	}
}
