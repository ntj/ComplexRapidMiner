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
package com.rapidminer.operator.learner.functions.kernel;

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;


/** 
 *  A model learned by the GPLearner. 
 *  
 *  @author Piotr Kasprzak, Ingo Mierswa
 *  @version $Id: GPModel.java,v 1.5 2007/07/14 12:31:38 ingomierswa Exp $
 */
public class GPModel extends KernelModel {
	
	private static final long serialVersionUID = 6094706651995436944L;
	
	private com.rapidminer.operator.learner.functions.kernel.gaussianprocess.Model model = null;
	
	public GPModel(ExampleSet exampleSet, com.rapidminer.operator.learner.functions.kernel.gaussianprocess.Model model) {
		super(exampleSet);	
		this.model = model;		
	}
    
    public boolean isClassificationModel() {
        return getLabel().isNominal();
    }
    
	public double getBias() {
		return 0;
	}

	public SupportVector getSupportVector(int index) {
		return null;
	}
	
    public double getAlpha(int index) {
        return Double.NaN;
    }
    
    public String getId(int index) {
        return null;
    }
    
    public int getNumberOfSupportVectors() {
        return this.model.getNumberOfBasisVectors();
    }
    
    public int getNumberOfAttributes() {
        return this.model.getInputDim();
    }
    
    public double getAttributeValue(int exampleIndex, int attributeIndex) {
        return this.model.getBasisVectorValue(exampleIndex, attributeIndex);
    }
    
    public String getClassificationLabel(int index) {
        return "?";
    }
    
    public double getRegressionLabel(int index) {
        return Double.NaN;
    }
    
    public double getFunctionValue(int index) {
        return model.applyToVector(this.model.getBasisVector(index));
    }
        
    public void performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
    	Iterator<Example> i = exampleSet.iterator();
    	while (i.hasNext()) {
    	    Example e = i.next();
    	    double functionValue = model.applyToVector(RVMModel.makeInputVector(e));
    	    if (getLabel().isNominal()) {
    	        if (functionValue > 0)
    	            e.setValue(predictedLabel, getLabel().getMapping().getPositiveIndex());
    	        else
    	            e.setValue(predictedLabel, getLabel().getMapping().getNegativeIndex());
    	        // set confidence to numerical prediction, such that can be scaled later
    	        e.setConfidence(predictedLabel.getMapping().getPositiveString(), 1.0d / (1.0d + java.lang.Math.exp(-functionValue)));
    	        e.setConfidence(predictedLabel.getMapping().getNegativeString(), 1.0d / (1.0d + java.lang.Math.exp(functionValue)));
    	    } else {
    	        e.setValue(predictedLabel, functionValue);
    	    }
    	}
    }
}
 
