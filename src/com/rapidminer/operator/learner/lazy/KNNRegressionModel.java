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
package com.rapidminer.operator.learner.lazy;

import java.util.ArrayList;
import java.util.Collection;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.Tupel;
import com.rapidminer.tools.math.container.GeometricDataCollection;


/**
 * An implementation of a knn model used for regression
 * 
 * @author Sebastian Land
 * @version $Id: KNNRegressionModel.java,v 1.1 2008/06/02 11:23:15 stiefelolm Exp $
 * 
 */
public class KNNRegressionModel extends PredictionModel {

	private static final long serialVersionUID = -6292869962412072573L;

	private int k;

	private GeometricDataCollection<Double> samples;
	
	private ArrayList<String> sampleAttributeNames;

	private boolean weightByDistance;
		
	public KNNRegressionModel(ExampleSet trainingSet, GeometricDataCollection<Double> samples, int k, boolean weightByDistance) {
		super(trainingSet);
		this.k = k;
		this.samples = samples;
		this.weightByDistance = weightByDistance;
		
		// finding training attributes
		Attributes attributes = trainingSet.getAttributes();
		sampleAttributeNames = new ArrayList<String>(attributes.size());
		for (Attribute attribute: attributes) {
			sampleAttributeNames.add(attribute.getName());
		}
	}

	public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
		// building attribute order from trainingset
		ArrayList<Attribute> sampleAttributes = new ArrayList<Attribute>(sampleAttributeNames.size());
		Attributes attributes = exampleSet.getAttributes();
		for (String attributeName: sampleAttributeNames) {
			sampleAttributes.add(attributes.get(attributeName));
		}
		
		double[] values = new double[sampleAttributes.size()];
		for (Example example: exampleSet) {
			// reading values
			int i = 0;
			for(Attribute attribute: sampleAttributes) {
				values[i] = example.getValue(attribute);
				i++;
			}

			double result = 0;
			if (!weightByDistance) {
				// finding next k neighbours
				Collection<Double> neighbourLabels = samples.getNearestValues(k, values);
	
				// building mean
				for (double label: neighbourLabels) {
					result += label;
				}
				result /= k;
			} else {
				// finding next k neighbours and their distances
				Collection<Tupel<Double, Double>> neighbourTupels = samples.getNearestValueDistances(k, values);
				// finding total distance
				double totalDistance = 0;
				for (Tupel<Double, Double> tupel: neighbourTupels) {
					totalDistance += tupel.getFirst();
				}
				
				double totalSimilarity = 0;
				for (Tupel<Double, Double> tupel: neighbourTupels) {
					totalSimilarity += 1d - tupel.getFirst() / totalDistance;
				}
				
				// building weighted mean
				for (Tupel<Double, Double> tupel: neighbourTupels) {
					result += tupel.getSecond() * (1d - tupel.getFirst() / totalDistance) / totalSimilarity;
				}
			}
			// setting prediction 
			example.setValue(predictedLabel, result);
		}
		
		
		return exampleSet;
	}

}
