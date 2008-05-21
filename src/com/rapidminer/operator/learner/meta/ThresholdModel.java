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

import javax.swing.Icon;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.Tools;


/**
 * This model is created by the {@link CostBasedThresholdLearner}.
 * @author Ingo Mierswa
 * @version $Id: ThresholdModel.java,v 1.7 2008/05/09 19:22:47 ingomierswa Exp $
 */
public class ThresholdModel extends PredictionModel {

	private static final long serialVersionUID = -4224958349396815500L;

	private static final String RESULT_ICON_NAME = "cut.png";
	
	private static Icon resultIcon = null;
	
	static {
		resultIcon = SwingTools.createIcon("16/" + RESULT_ICON_NAME);
	}
	
	private double[] thresholds;

    private Model innerModel;
    
    public ThresholdModel(ExampleSet exampleSet, Model innerModel, double[] thresholds) {
        super(exampleSet);
        this.innerModel = innerModel;
        this.thresholds = thresholds;
    }

    public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
        exampleSet = innerModel.apply(exampleSet);

        for (Example example : exampleSet) {
            int predictionIndex = (int)example.getPredictedLabel();
            String className = getLabel().getMapping().mapIndex(predictionIndex);
            double confidence = example.getConfidence(className);
            if (confidence < thresholds[predictionIndex]) {
                example.setPredictedLabel(Double.NaN);
            }
        }
        
        return exampleSet;
    }

    public Component getVisualizationComponent(IOContainer container) {
        return innerModel.getVisualizationComponent(container);
    }
    
    public Icon getResultIcon() {
    	return resultIcon;
    }
    
    public String toString() {
        List<String> thresholdList = new LinkedList<String>();
        for (double d : thresholds) {
            thresholdList.add(Tools.formatIntegerIfPossible(d));
        }
        return "Thresholds: " + thresholdList + Tools.getLineSeparator() + innerModel.toString(); 
    }
}
