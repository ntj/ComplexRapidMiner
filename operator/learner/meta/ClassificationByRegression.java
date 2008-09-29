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

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.tools.Ontology;


/**
 * For a classified dataset (with possibly more than two classes) builds a
 * classifier using a regression method which is specified by the inner
 * operator. For each class {@rapidminer.math i} a regression model is trained after
 * setting the label to {@rapidminer.math +1} if the label equals {@rapidminer.math i} and
 * to {@rapidminer.math -1} if it is not. Then the regression models are combined into
 * a classification model. In order to determine the prediction for an unlabeled
 * example, all models are applied and the class belonging to the regression
 * model which predicts the greatest value is chosen.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ClassificationByRegression.java,v 1.15 2006/03/21 15:35:48
 *          ingomierswa Exp $
 */
public class ClassificationByRegression extends AbstractMetaLearner {

	private int numberOfClasses;

	public ClassificationByRegression(OperatorDescription description) {
		super(description);
	}

	/**
	 * ClassificationByRegression supports all types of labels, so it would
	 * return true for all class check otherwise, it gives a call to the
	 * super.supportsCapability(...) method to check which attributes it
	 * supports.
	 */
	public boolean supportsCapability(LearnerCapability lc) {
		if (lc == LearnerCapability.POLYNOMINAL_CLASS || lc == LearnerCapability.BINOMINAL_CLASS || lc == LearnerCapability.NUMERICAL_CLASS) {
			return true;
        } else {
			return super.supportsCapability(lc);
        }
	}

	public Model learn(ExampleSet inputSet) throws OperatorException {
		Attribute classLabel = inputSet.getAttributes().getLabel();
		numberOfClasses = classLabel.getMapping().getValues().size();
		Model[] models = new Model[numberOfClasses];

		ExampleSet eSet = (ExampleSet) inputSet.clone();
		Attribute tempLabel = AttributeFactory.createAttribute("temp_regression_label", Ontology.REAL);
		eSet.getExampleTable().addAttribute(tempLabel);
		eSet.getAttributes().setLabel(tempLabel);

		for (int i = 0; i < numberOfClasses; i++) {
			// 1. Set regression labels
			Iterator<Example> r = eSet.iterator();
			while (r.hasNext()) {
				Example e = r.next();
				if (e.getValue(classLabel) == i) {
					e.setValue(tempLabel, +1.0);
				} else {
					e.setValue(tempLabel, -1.0);
				}
			}
			// 2. Apply learner
			models[i] = applyInnerLearner(eSet);
            inApplyLoop();
		}

		return new MultiModelByRegression(inputSet, models);
	}
}
