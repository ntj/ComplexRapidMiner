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
package com.rapidminer.operator.features.transformation;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.AbstractModel;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;

import Jama.Matrix;

/**
 * This is the transformation model of the <code>GHA</code> The number of
 * components is initially specified by the <code>GHA</code>. Additionally
 * the user can specify a lower number in the <code>ModelApplier</code>. You
 * can add two prediction parameter:
 * <ul>
 * <li><b>number_of_components</b> <i>integer</i> Specify a lower number of
 * components
 * <li><b>keep_attributes</b> <i>true|false</i> If true, the original
 * features are not removed.
 * </ul>
 * 
 * @author Daniel Hakenjos, Ingo Mierswa
 * @version $Id: GHAModel.java,v 1.5 2008/05/09 19:22:51 ingomierswa Exp $
 * @see GHA
 */
public class GHAModel extends AbstractModel implements ComponentWeightsCreatable {

	private static final long serialVersionUID = -5204076842779376622L;

	private int numberOfAttributes;

	private int numberOfComponents;

	private double[] means;

	private String[] attributeNames;
    
    private List<WeightVector> weightVectors;

	private boolean keepAttributes = false;

	public GHAModel(ExampleSet eSet, double[] eigenvalues, double[][] weights, double[] mean) {
		super(eSet);
		keepAttributes = false;
		numberOfAttributes = weights[0].length;
		numberOfComponents = weights.length;
        this.means = mean;
        attributeNames = new String[numberOfAttributes];
        int i = 0;
        for (Attribute attribute : eSet.getAttributes()) {
            attributeNames[i++] = attribute.getName();
        }
        
        this.weightVectors = new ArrayList<WeightVector>(eigenvalues.length);
        
        for (i = 0; i < eigenvalues.length; i++) {
            double[] currentVector = new double[eSet.getAttributes().size()];
            for (int j = 0; j < currentVector.length; j++) {
                currentVector[j] = weights[j][i];
            }
            this.weightVectors.add(new WeightVector(currentVector, eigenvalues[i]));
        }
        
        Collections.sort(this.weightVectors);
	}

	public double[] getMean() {
		return means;
	}

    public double[] getWeights(int index) {
        return this.weightVectors.get(index).getWeights();
    }
    
    public double getEigenvalue(int index) {
        return this.weightVectors.get(index).getEigenvalue();
    }
    
    public double getNumberOfComponents() {
		return numberOfComponents;
	}

	public ExampleSet apply(ExampleSet exampleSet) throws OperatorException {
		exampleSet.recalculateAllAttributeStatistics();

		if (numberOfAttributes != exampleSet.getAttributes().size()) {
			throw new UserError(null, 133, numberOfAttributes, exampleSet.getAttributes().size());
		}

		log("Number of components: " + numberOfComponents);
        
		// 1) prepare data
		double[][] data = new double[exampleSet.size()][exampleSet.getAttributes().size()];
		boolean haslabel = (exampleSet.getAttributes().getLabel() != null);
		boolean haspredlabel = (exampleSet.getAttributes().getPredictedLabel() != null);

		double[] labelvalues = new double[0], predvalues = new double[0];
		if (haslabel) {
			labelvalues = new double[exampleSet.size()];
		}
		if (haspredlabel) {
			predvalues = new double[exampleSet.size()];
		}

		Iterator<Example> reader = exampleSet.iterator();
		Example example;

		for (int sample = 0; sample < exampleSet.size(); sample++) {
			example = reader.next();
			int d = 0;
			for (Attribute attribute : exampleSet.getAttributes()) {
				data[sample][d] = example.getValue(attribute) - means[d];
				if (haslabel) {
					labelvalues[sample] = example.getValue(example.getAttributes().getLabel());
				}
				if (haspredlabel) {
					predvalues[sample] = example.getValue(example.getAttributes().getPredictedLabel());
				}
				d++;
			}

		}
		Matrix dataMatrix = new Matrix(data);

		// 2) Derive the new DataSet
		double[][] values = new double[this.weightVectors.size()][attributeNames.length];
		int counter = 0;
		for (WeightVector wv : this.weightVectors) {
		    values[counter++] = wv.getWeights();
		}
		Matrix W = new Matrix(values);
		Matrix finaldataMatrix = dataMatrix.times(W.transpose());

		double[][] finaldata = finaldataMatrix.getArray();

		if (!keepAttributes) {
			exampleSet.getAttributes().clearRegular();
		}

		log("Adding new the derived features...");
		Attribute[] principalComponentAttributes = new Attribute[numberOfComponents];
		for (int i = 0; i < numberOfComponents; i++) {
			principalComponentAttributes[i] = AttributeFactory.createAttribute("pc_" + (i + 1), Ontology.REAL);
			exampleSet.getExampleTable().addAttribute(principalComponentAttributes[i]);
			exampleSet.getAttributes().addRegular(principalComponentAttributes[i]);
		}

		reader = exampleSet.iterator();
		for (int sample = 0; sample < exampleSet.size(); sample++) {
			example = reader.next();
			for (int d = 0; d < numberOfComponents; d++) {
				example.setValue(principalComponentAttributes[d], finaldata[sample][d]);
			}
		}
        
        return exampleSet;
	}

	public void setParameter(String name, Object object) throws OperatorException {
		if (name.equals("number_of_components")) {
			String value = (String) object;

			try {
				this.numberOfComponents = Math.min(numberOfComponents, Integer.parseInt(value));
			} catch (NumberFormatException error) {
				super.setParameter(name, value);
			}

		} else if (name.equals("keep_attributes")) {
			String value = (String) object;
			keepAttributes = false;
			if (value.equals("true")) {
				keepAttributes = true;
			}
		} else {
			super.setParameter(name, object);
		}
	}
    
    public AttributeWeights getWeightsOfComponent(int component) throws OperatorException {
        if (component < 1) {
            component = 1;
        }
        if (component > attributeNames.length) {
            logWarning("Creating weights of component " + attributeNames.length + "!");
            component = attributeNames.length;
        }
        AttributeWeights weights = new AttributeWeights();
        for (int i = 0; i < attributeNames.length; i++) {
            weights.setWeight(attributeNames[i], weightVectors.get(component - 1).getWeights()[i]);
        }

        return weights;
    }

    public Component getVisualizationComponent(IOContainer container) {
        double[] cumulativeVariance = new double[numberOfComponents];
        double cumulative = 0.0d;
        int i = 0;
        for (WeightVector wv : weightVectors) {
            cumulative += wv.getEigenvalue();
            cumulativeVariance[i++] = cumulative;
        }
        return (new EigenvectorModelVisualization(getName(), this.attributeNames, cumulativeVariance,
                weightVectors, false, numberOfComponents, 1.0d)).getVisualizationComponent(container);
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer(Tools.getLineSeparator() + "GHA Components:" + Tools.getLineSeparator());
        for (int i = 0; i < weightVectors.size(); i++) {
            result.append("PC " + (i+1) + ": ");
            for (int j = 0; j < attributeNames.length; j++) {
                double value = weightVectors.get(j).getWeights()[i];
                if (value > 0)
                    result.append(" + ");
                else
                    result.append(" - ");
                result.append(Tools.formatNumber(Math.abs(value)) + " * " + attributeNames[j]);
            }
            result.append(Tools.getLineSeparator());
        }
        return result.toString();
    }
}
