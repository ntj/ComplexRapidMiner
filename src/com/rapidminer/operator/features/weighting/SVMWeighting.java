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
package com.rapidminer.operator.features.weighting;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.functions.kernel.AbstractMySVMLearner;
import com.rapidminer.operator.learner.functions.kernel.JMySVMLearner;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;


/**
 * Uses the coefficients of the normal vector of a linear SVM as feature weights.
 * In contrast to most of the SVM based operators available in RapidMiner, this one works
 * for multiple classes, too.
 * 
 * @author Ingo Mierswa
 * @version $Id: SVMWeighting.java,v 1.5 2007/07/10 18:02:02 ingomierswa Exp $
 *
 */
public class SVMWeighting extends Operator {

    public SVMWeighting(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException {
        ExampleSet exampleSet = getInput(ExampleSet.class);
  
        // checks
        Attribute label = exampleSet.getAttributes().getLabel();
        if (label == null) {
            throw new UserError(this, 105);
        }
        
        // create and init SVM operator
        Operator svmOperator = null;
        try {
             svmOperator = OperatorService.createOperator(JMySVMLearner.class);
        } catch (OperatorCreationException e) {
            throw new UserError(this, 904, "inner pca operator", e.getMessage());
        }
        svmOperator.setParameter(AbstractMySVMLearner.PARAMETER_KERNEL_TYPE, AbstractMySVMLearner.KERNEL_DOT + "");
        svmOperator.setParameter(AbstractMySVMLearner.PARAMETER_C, getParameterAsDouble(AbstractMySVMLearner.PARAMETER_C) + "");
        svmOperator.setParameter(AbstractMySVMLearner.PARAMETER_CALCULATE_WEIGHTS, "true");
        
        // calculate weights
        AttributeWeights result = null;
       
        // regression or binomnial case
        if ((!label.isNominal()) || label.getMapping().size() == 2) {
        	result = calculateAttributeWeights(svmOperator, exampleSet);
        } else { 
        	// polynominal case
        	exampleSet.recalculateAttributeStatistics(label);
        	int totalClassSizeSum = 0;
        	int[] classFrequencies = new int[label.getMapping().size()];
        	int counter = 0;
        	List<AttributeWeights> allWeights = new LinkedList<AttributeWeights>();
        	for (String value : label.getMapping().getValues()) {
        		int frequency = (int)exampleSet.getStatistics(label, Statistics.COUNT, value);
        		classFrequencies[counter++] = frequency;
        		totalClassSizeSum += frequency;
        		
        		// create temp label (one vs. all)
        		Attribute tempLabel = AttributeFactory.createAttribute("temp_label", Ontology.BINOMINAL);
        		int positiveIndex = tempLabel.getMapping().mapString("positive");
        		int negativeIndex = tempLabel.getMapping().mapString("negative");
        		exampleSet.getExampleTable().addAttribute(tempLabel);
        		exampleSet.getAttributes().addRegular(tempLabel);
        		int currentLabelIndex = label.getMapping().mapString(value);
        		for (Example e : exampleSet) {
        			int oldLabelValue = (int)e.getValue(label);
        			if (oldLabelValue == currentLabelIndex) {
        				e.setValue(tempLabel, positiveIndex);
        			} else {
        				e.setValue(tempLabel, negativeIndex);
        			}
        		}
        		exampleSet.getAttributes().remove(tempLabel);
        		exampleSet.getAttributes().setLabel(tempLabel);
        		
        		// actual calculating weights for this class
        		AttributeWeights currentWeights = calculateAttributeWeights(svmOperator, exampleSet);
        		allWeights.add(currentWeights);
        		
        		// clean up
        		exampleSet.getAttributes().setLabel(label);
        		exampleSet.getExampleTable().removeAttribute(tempLabel);
        	}
        	
        	// build the weighted average for all weights
        	result = new AttributeWeights();
        	Iterator<String> nameIterator = allWeights.get(0).getAttributeNames().iterator();
        	while (nameIterator.hasNext()) {
        		String attributeName = nameIterator.next();
        		double currentWeightSum = 0.0d;
        		counter = 0;
        		for (AttributeWeights weights : allWeights) {
        			double weight = weights.getWeight(attributeName);
        			currentWeightSum += Math.abs(weight) * classFrequencies[counter++];
        		}
        		result.setWeight(attributeName, currentWeightSum / totalClassSizeSum);
        	}
        }
        
	    // normalization
        result.normalize();
        
    	result.setSource(this.getName());
        return new IOObject[] { exampleSet, result };
    }

    private AttributeWeights calculateAttributeWeights(Operator svmOperator, ExampleSet exampleSet) throws OperatorException {
    	IOContainer ioContainer = new IOContainer(exampleSet);
    	ioContainer = svmOperator.apply(ioContainer);
    	AttributeWeights result = ioContainer.remove(AttributeWeights.class);
    	return result;
    }
    
    public Class[] getInputClasses() {
        return new Class[] { ExampleSet.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] { ExampleSet.class, AttributeWeights.class };
    }

    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeDouble(AbstractMySVMLearner.PARAMETER_C, "The SVM complexity weighting factor.", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d)); 
        return types;
    }
}
