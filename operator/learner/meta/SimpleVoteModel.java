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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JTabbedPane;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.tools.ExtendedJTabbedPane;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.SimplePredictionModel;
import com.rapidminer.tools.RandomGenerator;

/**
 * A simple vote model. For classification problems, the majority class is chosen.
 * For regression problems, the average prediction value is used. This model
 * only supports simple prediction models.
 *
 * @author Ingo Mierswa
 * @version $Id: SimpleVoteModel.java,v 1.6 2008/05/09 19:22:47 ingomierswa Exp $
 */
public class SimpleVoteModel extends SimplePredictionModel {

	private static final long serialVersionUID = 1089932073805038503L;
	
	private List<SimplePredictionModel> baseModels;
	
	public SimpleVoteModel(ExampleSet exampleSet, List<SimplePredictionModel> baseModels) {
		super(exampleSet);
		this.baseModels = baseModels;
	}
	
	public double predict(Example example) throws OperatorException {
		if (getLabel().isNominal()) {
			Map<Double, AtomicInteger> classVotes = new TreeMap<Double, AtomicInteger>();
			Iterator<SimplePredictionModel> iterator = baseModels.iterator();
			while (iterator.hasNext()) {
				double prediction = iterator.next().predict(example);
				AtomicInteger counter = classVotes.get(prediction);
				if (counter == null) {
					classVotes.put(prediction, new AtomicInteger(1));					
				} else {
					counter.incrementAndGet();
				}
			}

			Iterator<Double> votedClasses = classVotes.keySet().iterator();
			List<Double> bestClasses = new LinkedList<Double>();
			int bestClassesVotes = -1;
			while (votedClasses.hasNext()) {
				double currentClass = votedClasses.next();
				int currentVotes = classVotes.get(currentClass).intValue();
				if (currentVotes > bestClassesVotes) {
                    bestClasses.clear();
                    bestClasses.add(currentClass);
					bestClassesVotes = currentVotes;
				}
                if (currentVotes == bestClassesVotes) {
                    bestClasses.add(currentClass);
                }
				example.setConfidence(getLabel().getMapping().mapIndex((int)currentClass), ((double) currentVotes) / (double)baseModels.size());
			}
            if (bestClasses.size() == 1) {
                return bestClasses.get(0);              
            } else {
                return bestClasses.get(RandomGenerator.getGlobalRandomGenerator().nextInt(bestClasses.size()));
            }
		} else {
			double sum = 0.0d;
			Iterator<SimplePredictionModel> iterator = baseModels.iterator();
			while (iterator.hasNext()) {
				sum += iterator.next().predict(example);
			}
			return sum / baseModels.size();
		}
	}
	
	public Component getVisualizationComponent(IOContainer container) {
		JTabbedPane tabPane = new ExtendedJTabbedPane();
		int index = 1;
		for (Model model : baseModels) {
			tabPane.add("Model " + index, model.getVisualizationComponent(container));
			index++;
		}
		return tabPane;
	}
}
