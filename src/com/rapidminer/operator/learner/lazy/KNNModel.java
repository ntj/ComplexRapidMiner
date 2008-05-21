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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.operator.similarity.attributebased.ExampleBasedSimilarityMeasure;
import com.rapidminer.tools.WeightedObject;


/**
 * A simple implementation of a knn model.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: KNNModel.java,v 1.6 2008/05/09 19:23:24 ingomierswa Exp $
 * 
 */
public class KNNModel extends PredictionModel {

	private static final long serialVersionUID = -6292869962412072573L;

	private ExampleSet trainingSet;

	private Attribute weight;
	
	private int k;

	private boolean weightedVote;

	private SimilarityMeasure similarity;

	private double majorityPrediction;
	
	/**
	 * Create a knn model.
	 * 
	 * @param trainingSet the example set
	 * @param k max. number of neighbors
	 * @param weightedVote weight votes with similarity
	 * @param similarity the similarity measure
	 */
	public KNNModel(ExampleSet trainingSet, SimilarityMeasure similarity, int k, boolean weightedVote) {
		super(trainingSet);
		this.weight       = trainingSet.getAttributes().getWeight();
		this.trainingSet  = trainingSet;
		this.k            = k;
		this.weightedVote = weightedVote;
		this.similarity   = similarity;
		this.trainingSet.recalculateAttributeStatistics(trainingSet.getAttributes().getLabel());
		if (trainingSet.getAttributes().getLabel().isNominal()) {
			majorityPrediction = trainingSet.getStatistics(trainingSet.getAttributes().getLabel(), Statistics.MODE);
		} else {
			majorityPrediction = trainingSet.getStatistics(trainingSet.getAttributes().getLabel(), Statistics.AVERAGE);
		}
	}

	public ExampleSet performPrediction(ExampleSet testSet, Attribute predictedLabel) {
		for (Example e : testSet) {
			
			// determine neighbors
			String id = IdUtils.getIdFromExample(e);
			List<WeightedObject<Example>> allNeighbors = new LinkedList<WeightedObject<Example>>();
			
			for (Example trainingExample : trainingSet) {
				String trainingId = IdUtils.getIdFromExample(trainingExample);
				double similarityValue = Double.NaN;
				if (similarity instanceof ExampleBasedSimilarityMeasure) {
					similarityValue = ((ExampleBasedSimilarityMeasure) similarity).similarity(e, trainingExample);
				} else if ((id != null) && (trainingId != null))
					if (similarity.isSimilarityDefined(id, trainingId))
						similarityValue = similarity.similarity(id, trainingId);
				
				if (!Double.isNaN(similarityValue)) {
					if (similarity.isDistance())
						allNeighbors.add(new WeightedObject<Example>(trainingExample, -similarityValue));
					else
						allNeighbors.add(new WeightedObject<Example>(trainingExample, similarityValue));
				} else {
					allNeighbors.add(new WeightedObject<Example>(trainingExample, Double.NEGATIVE_INFINITY));
				}
			}
			Collections.sort(allNeighbors);
			int actualK = Math.min(k, allNeighbors.size());
			List<WeightedObject<Example>> neighbors = allNeighbors.subList(allNeighbors.size() - actualK, allNeighbors.size());
			
			// perform classification or regression
			if (getLabel().isNominal()) {
				// classification
				Map<String, Double> counter = new HashMap<String, Double>();
				double totalSum = 0.0d;
				for (WeightedObject<Example> weightedNeighbor : neighbors) {
					Example neighbor = weightedNeighbor.getObject();
					String labelValue = getLabel().getMapping().mapIndex((int)neighbor.getLabel());
					double labelSum = 0.0d;
					if (counter.get(labelValue) != null) {
						labelSum = counter.get(labelValue);
					}

					double exampleWeight = 1.0d;
					if (weight != null)
						exampleWeight = neighbor.getValue(weight);
					
					double similarityWeight = 1.0d;
					if (weightedVote) {
						if (!similarity.isDistance())
							similarityWeight = weightedNeighbor.getWeight();
						else
							similarityWeight = 1.0d - (-weightedNeighbor.getWeight() / (1.0d - weightedNeighbor.getWeight()));
					}
					double currentWeight = exampleWeight * similarityWeight; 
					labelSum += currentWeight;
					totalSum += currentWeight;
					counter.put(labelValue, labelSum);
				}

				// calculate confidences and best class
				String bestClass = null;
				double best = Double.NEGATIVE_INFINITY;
				for (String labelValue : getLabel().getMapping().getValues()) {
					Double sumObject = counter.get(labelValue);
					if (sumObject == null) {
						e.setConfidence(labelValue, 0.0d);
					} else {
						e.setConfidence(labelValue, sumObject / totalSum);
						if (sumObject > best) {
							best = sumObject;
							bestClass = labelValue;
						}
					}
				}
				
				// set crisp prediction
				if (bestClass != null) {
					e.setPredictedLabel(predictedLabel.getMapping().mapString(bestClass));
				} else {
					e.setPredictedLabel(majorityPrediction);
				}
			} else {
				// regression
				double labelSum = 0.0d;
				double totalSum = 0.0d;
				for (WeightedObject<Example> weightedNeighbor : neighbors) {
					Example neighbor = weightedNeighbor.getObject();
					
					double exampleWeight = 1.0d;
					if (weight != null)
						exampleWeight = neighbor.getValue(weight);
					
					if (!weightedVote) {
						totalSum += exampleWeight;
						labelSum += exampleWeight * neighbor.getLabel();
					} else {
						labelSum += exampleWeight * (neighbor.getLabel() * (1.0d - (-weightedNeighbor.getWeight() / (1 - weightedNeighbor.getWeight()))));
						totalSum += exampleWeight * ((1.0d - (-weightedNeighbor.getWeight() / (1.0d - weightedNeighbor.getWeight()))));
					}
				}
				if (totalSum > 0.0d)
					e.setPredictedLabel(labelSum / totalSum);
				else
					e.setPredictedLabel(majorityPrediction);
			}
		}
		return testSet;
	}
}
