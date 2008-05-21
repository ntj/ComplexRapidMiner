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
package com.rapidminer.operator.learner.functions.kernel.evosvm;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.learner.functions.kernel.KernelModel;
import com.rapidminer.operator.learner.functions.kernel.SupportVector;
import com.rapidminer.operator.learner.functions.kernel.functions.Kernel;


/**
 * The model for the evolutionary SVM. Basically the same as other SVM models.
 * 
 * @author Ingo Mierswa
 * @version $Id: EvoSVMModel.java,v 1.8 2008/05/09 19:23:23 ingomierswa Exp $
 */
public class EvoSVMModel extends KernelModel {

	private static final long serialVersionUID = 2848059541066828127L;

	/** The used kernel function. */
	private Kernel kernel;

	/** The list of all support vectors. */
	private List<SupportVector> supportVectors;

	/** The bias. */
	private double bias;

	/** Creates a classification model. */
	public EvoSVMModel(ExampleSet exampleSet, List<SupportVector> supportVectors, Kernel kernel, double bias) {
		super(exampleSet);
		this.supportVectors = supportVectors;
		if ((supportVectors == null) || (supportVectors.size() == 0))
			throw new IllegalArgumentException("Null or empty support vector collection: not possible to predict values!");
		this.kernel = kernel;
		this.bias = bias;
	}
    
    public boolean isClassificationModel() {
        return getLabel().isNominal();
    }
    
    public double getAlpha(int index) {
        return supportVectors.get(index).getAlpha();
    }
    
    public String getId(int index) {
        return null;
    }
    
    public double getBias() {
    	return this.bias;
    }
    
    public SupportVector getSupportVector(int index) {
    	return supportVectors.get(index);
    }
    
    public int getNumberOfSupportVectors() {
        return supportVectors.size();
    }
    
    public int getNumberOfAttributes() {
        return supportVectors.get(0).getX().length;
    }
    
    public double getAttributeValue(int exampleIndex, int attributeIndex) {
        return this.supportVectors.get(exampleIndex).getX()[attributeIndex];
    }
    
    public String getClassificationLabel(int index) {
        double y = getRegressionLabel(index);
        if (y < 0)
            return getLabel().getMapping().getNegativeString();
        else
            return getLabel().getMapping().getPositiveString();
    }
    
    public double getRegressionLabel(int index) {
    	return this.supportVectors.get(index).getY();
    }
    
    public double getFunctionValue(int index) {
        double[] values = this.supportVectors.get(index).getX();
        return (bias + kernel.getSum(supportVectors, values));
    }
    
	/** Applies the model to each example of the example set. */
	public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predLabel) {
		if (exampleSet.getAttributes().size() != getNumberOfAttributes())
			throw new RuntimeException("Cannot apply model: incompatible numbers of attributes (" + exampleSet.getAttributes().size() + " != " + getNumberOfAttributes() + ")!");
		Iterator<Example> reader = exampleSet.iterator();
		while (reader.hasNext()) {
			Example current = reader.next();
			double[] currentX = new double[exampleSet.getAttributes().size()];
			int x = 0;
			for (Attribute attribute : exampleSet.getAttributes())
				currentX[x++] = current.getValue(attribute);
			double sum = bias + kernel.getSum(supportVectors, currentX);
			if (getLabel().isNominal()) {
				int index = sum > 0 ? getLabel().getMapping().getPositiveIndex() : getLabel().getMapping().getNegativeIndex();
				current.setValue(predLabel, index);
				current.setConfidence(predLabel.getMapping().getPositiveString(), 1.0d / (1.0d + java.lang.Math.exp(-sum)));
				current.setConfidence(predLabel.getMapping().getNegativeString(), 1.0d / (1.0d + java.lang.Math.exp(sum)));
			} else {
				current.setValue(predLabel, sum);
			}
		}
		
		return exampleSet;
	}
}
