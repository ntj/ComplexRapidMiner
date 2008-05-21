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

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTabbedPane;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.tools.ExtendedJTabbedPane;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.Tools;


/**  
 * This class is the model build by the {@link Stacking} operator.
 * 
 * @author Ingo Mierswa, Helge Homburg
 * @version $Id: StackingModel.java,v 1.7 2008/05/09 19:22:47 ingomierswa Exp $ 
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
	
	public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
        // init
        PredictionModel.removePredictedLabel(exampleSet, true, true);
        ExampleSet stackingExampleSet = (ExampleSet)exampleSet.clone();        
        if (!useAllAttributes) {
            stackingExampleSet.getAttributes().clearRegular();
        }

        // create predictions from base models
        List<Attribute> tempPredictions = new LinkedList<Attribute>();
        int i = 0;
        for (Model baseModel : baseModels) {
            exampleSet = baseModel.apply(exampleSet);
            Attribute basePrediction = exampleSet.getAttributes().getPredictedLabel();
            // renaming attribute
            basePrediction.setName("base_prediction" + i);
            
            PredictionModel.removePredictedLabel(exampleSet, false, true);
            stackingExampleSet.getAttributes().addRegular(basePrediction);
            tempPredictions.add(basePrediction);
            i++;
        }
        
        // apply stacking model and copy prediction to original example set
        stackingExampleSet = stackingModel.apply(stackingExampleSet);
        PredictionModel.copyPredictedLabel(stackingExampleSet, exampleSet);
        
        // remove temporary predictions from table
        for (Attribute tempPrediction : tempPredictions) {
            stackingExampleSet.getAttributes().remove(tempPrediction);
            stackingExampleSet.getExampleTable().removeAttribute(tempPrediction);
        }
        
        return exampleSet;
	}
	
	public Component getVisualizationComponent(IOContainer container) {
		JTabbedPane tabPane = new ExtendedJTabbedPane();
		tabPane.add("Stacking Model", stackingModel.getVisualizationComponent(container));
		int index = 1;
		for (Model model : baseModels) {
			tabPane.add("Model " + index, model.getVisualizationComponent(container));
			index++;
		}
		return tabPane;
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
