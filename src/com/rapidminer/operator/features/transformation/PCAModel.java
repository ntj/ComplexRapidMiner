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
import com.rapidminer.example.Statistics;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.AbstractModel;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;

import Jama.Matrix;

/**
 * This is the transformation model of the principal components analysis. The
 * number of components is initially specified by the <code>PCA</code>.
 * Additionally you can specify the number of components in the
 * <code>ModelApplier</code>. You can add two prediction parameter:
 * <ul>
 * <li><b>variance_threshold</b> <i>double</i> Specify a new threshold for
 * the cumulative variance of the principal components.
 * <li><b>number_of_components</b> <i>integer</i> Specify a lower number of
 * components
 * <li><b>keep_attributes</b> <i>true|false</i> If true, the original
 * features are not removed.
 * </ul>
 * 
 * @author Daniel Hakenjos, Ingo Mierswa
 * @version $Id: PCAModel.java,v 1.7 2008/05/09 19:22:51 ingomierswa Exp $
 * @see PCA
 */
public class PCAModel extends AbstractModel implements ComponentWeightsCreatable {
    
	private static final long serialVersionUID = 5424591594470376525L;

	private List<Eigenvector> eigenVectors;
    
	private double[] means;

	private String[] attributeNames;

	private boolean manualNumber;

	private int numberOfComponents = -1;

	private double varianceThreshold;

	// -----------------------------------

	private double[] variances;
	
	private double[] cumulativeVariance;

	private boolean keepAttributes = false;

	public PCAModel(ExampleSet eSet, double[] eigenvalues, double[][] eigenvectors) {
		super(eSet);
        
		this.keepAttributes = false;
		this.attributeNames = new String[eSet.getAttributes().size()];
		this.means = new double[eSet.getAttributes().size()];
		int counter = 0;
		for (Attribute attribute : eSet.getAttributes()) {
			attributeNames[counter] = attribute.getName();
			means[counter] = eSet.getStatistics(attribute, Statistics.AVERAGE);
			counter++;
		}
        this.eigenVectors = new ArrayList<Eigenvector>(eigenvalues.length);
        for (int i = 0; i < eigenvalues.length; i++) {
            double[] currentEigenVector = new double[eSet.getAttributes().size()];
            for (int j = 0; j < currentEigenVector.length; j++) {
                currentEigenVector[j] = eigenvectors[j][i];
            }
            this.eigenVectors.add(new Eigenvector(currentEigenVector, eigenvalues[i]));
        }

		// order the eigenvectors by the eigenvalues        
        Collections.sort(this.eigenVectors);
        
		calculateCumulativeVariance();
	}

	public String[] getAttributeNames() {
		return attributeNames;
	}

	public double[] getMeans() {
		return means;
	}
	
	public double getMean(int index) {
		return means[index];
	}

	public double getVariance(int index) {
		return this.variances[index];
	}
	
	public double getCumulativeVariance(int index) {
		return this.cumulativeVariance[index];
	}
	
    public double getEigenvalue(int index) {
        return this.eigenVectors.get(index).getEigenvalue();
    }

    public double[] getEigenvector(int index) {
        return this.eigenVectors.get(index).getEigenvector();
    }
    
	public double getVarianceThreshold() {
		return this.varianceThreshold;
	}

	public int getMaximumNumberOfComponents() {
		return attributeNames.length;
	}
	
	public int getNumberOfComponents() {
		return numberOfComponents;
	}

	public void setVarianceThreshold(double threshold) {
		this.manualNumber = false;
		this.varianceThreshold = threshold;
		this.numberOfComponents = -1;
	}

	public void setNumberOfComponents(int numberOfComponents) {
		this.varianceThreshold = 0.95;
		this.manualNumber = true;
		this.numberOfComponents = numberOfComponents;
	}

	public ExampleSet apply(ExampleSet exampleSet) throws OperatorException {
		exampleSet.recalculateAllAttributeStatistics();

		if (attributeNames.length != exampleSet.getAttributes().size()) {
			throw new UserError(null, 133, numberOfComponents, exampleSet.getAttributes().size());
		}

		// 1) prepare data
		double[][] data = new double[exampleSet.size()][exampleSet.getAttributes().size()];
		Iterator<Example> reader = exampleSet.iterator();
		for (int sample = 0; sample < exampleSet.size(); sample++) {
			Example example = reader.next();
			int d = 0;
			for (Attribute attribute : example.getAttributes()) {
				data[sample][d] = example.getValue(attribute) - means[d];
				d++;
			}
		}

		// 2) Derive the new DataSet
        Matrix dataMatrix = new Matrix(data);
        double[][] values = new double[this.eigenVectors.size()][attributeNames.length];
        int counter = 0;
        for (Eigenvector ev : this.eigenVectors) {
            values[counter++] = ev.getEigenvector();
        }
		Matrix eigenvectorMatrix = new Matrix(values).transpose();
		Matrix finaldataMatrix = dataMatrix.times(eigenvectorMatrix);

		int components = -1;
		if (manualNumber) {
			components = numberOfComponents;
		} else {
			if (varianceThreshold == 0.0d) {
				components = -1;
			} else {
				components = 0;
				while (cumulativeVariance[components] < varianceThreshold) {
					components++;
				}
				components++;
				if (components == eigenVectors.size()) {
					components--;
				}
			}
		}

		if (components == -1) {
			// keep all components
			components = exampleSet.getAttributes().size();
		}

		log("Number of components: " + components);
		finaldataMatrix = new Matrix(finaldataMatrix.getArray(), exampleSet.size(), components);
		double[][] finaldata = finaldataMatrix.getArray();

		if (!keepAttributes) {
			exampleSet.getAttributes().clearRegular();
		}

		log("Adding new the derived features...");
		Attribute[] pcatts = new Attribute[components];
		for (int i = 0; i < components; i++) {
			pcatts[i] = AttributeFactory.createAttribute("pc_" + (i + 1), Ontology.REAL);
			exampleSet.getExampleTable().addAttribute(pcatts[i]);
			exampleSet.getAttributes().addRegular(pcatts[i]);
		}

		reader = exampleSet.iterator();
		for (int sample = 0; sample < exampleSet.size(); sample++) {
			Example example = reader.next();
			for (int d = 0; d < components; d++) {
				example.setValue(pcatts[d], finaldata[sample][d]);
			}

		}
        
        return exampleSet;
	}

	/** Calculates the cumulative variance. */
	private void calculateCumulativeVariance() {
		double sumvariance = 0.0d;
        for (Eigenvector ev : this.eigenVectors) {
			sumvariance += ev.getEigenvalue();
		}
		this.variances = new double[this.eigenVectors.size()];
		this.cumulativeVariance = new double[variances.length];
		double cumulative = 0.0d;
        int counter = 0;
        for (Eigenvector ev : this.eigenVectors) {
			double proportion = ev.getEigenvalue() / sumvariance;
			this.variances[counter] = proportion;
			cumulative += proportion;
			this.cumulativeVariance[counter] = cumulative;
            counter++;
		}
	}

	public void setParameter(String name, Object object) throws OperatorException {
		if (name.equals("variance_threshold")) {
			String value = (String) object;

			try {
				this.setVarianceThreshold(Double.parseDouble(value));
			} catch (NumberFormatException error) {
				super.setParameter(name, value);
			}

		} else if (name.equals("number_of_components")) {
			String value = (String) object;

			try {
				this.setNumberOfComponents(Integer.parseInt(value));
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
            weights.setWeight(attributeNames[i], eigenVectors.get(component - 1).getEigenvector()[i]);
		}

		return weights;
	}
    
    public Component getVisualizationComponent(IOContainer container) {
        return (new EigenvectorModelVisualization(getName(), this.attributeNames, cumulativeVariance,
                eigenVectors, manualNumber, this.eigenVectors.size(), varianceThreshold)).getVisualizationComponent(container);
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer(Tools.getLineSeparator() + "Principal Components:" + Tools.getLineSeparator());
        if (manualNumber) {
            result.append("Number of Components: " + numberOfComponents + Tools.getLineSeparator());
        } else {
            result.append("Variance Threshold: " + varianceThreshold + Tools.getLineSeparator());
        }
        for (int i = 0; i < eigenVectors.size(); i++) {
            result.append("PC " + (i+1) + ": ");
            for (int j = 0; j < attributeNames.length; j++) {
                double value = eigenVectors.get(j).getEigenvector()[i];
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
