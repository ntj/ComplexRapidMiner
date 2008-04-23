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
package com.rapidminer.operator.learner.lazy;

import java.util.HashMap;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.learner.meta.Vote;


/**
 * Average model simply calculates the average of the attributes as prediction.
 * For classification problems the mode of all attribute values is returned.
 * This model is mainly used in meta learning schemes (like {@link Vote}.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeBasedVotingModel.java,v 1.2 2007/07/13 22:52:13 ingomierswa Exp $
 */
public class AttributeBasedVotingModel extends PredictionModel {

	private static final long serialVersionUID = -8814468417883548971L;

	private double majorityVote;
	
	public AttributeBasedVotingModel(ExampleSet exampleSet, double majorityVote) {
		super(exampleSet);
		this.majorityVote = majorityVote;
	}

	public void performPrediction(ExampleSet exampleSet, Attribute predictedLabelAttribute) throws OperatorException {
		for (Example example : exampleSet) {
			if (predictedLabelAttribute.isNominal()) {
				// classification
				Map<String, Double> counter = new HashMap<String, Double>();
				for (Attribute attribute : example.getAttributes()) {
					if (!attribute.isNominal())
						throw new UserError(null, 103, "nominal voting");
					
					String labelValue = attribute.getMapping().mapIndex((int)example.getValue(attribute));
					double labelSum = 0.0d;
					if (counter.get(labelValue) != null) {
						labelSum = counter.get(labelValue);
					}
					labelSum += 1.0d;
					counter.put(labelValue, labelSum);
				}

				// calculate confidences and best class
				String bestClass = null;
				double best = Double.NEGATIVE_INFINITY;
				for (String labelValue : getLabel().getMapping().getValues()) {
					Double sumObject = counter.get(labelValue);
					if (sumObject == null) {
						example.setConfidence(labelValue, 0.0d);
					} else {
						example.setConfidence(labelValue, sumObject / exampleSet.getAttributes().size());
						if (sumObject > best) {
							best = sumObject;
							bestClass = labelValue;
						}
					}
				}
				
				// set crisp prediction
				if (bestClass != null) {
					example.setPredictedLabel(predictedLabelAttribute.getMapping().mapString(bestClass));
				} else {
					example.setPredictedLabel(majorityVote);
				}
			} else {
				// regression
				double average = 0.0d;
				for (Attribute attribute : example.getAttributes()) {
					average += example.getValue(attribute);
				}
				average /= example.getAttributes().size();
				example.setValue(predictedLabelAttribute, average);
			}
		}
	}
}
