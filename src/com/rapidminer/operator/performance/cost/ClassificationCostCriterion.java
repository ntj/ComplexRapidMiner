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
package com.rapidminer.operator.performance.cost;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.operator.performance.MeasuredPerformance;
import com.rapidminer.tools.math.Averagable;

/**
 * This performance Criterion works with a given cost matrix. Every
 * classification result creates costs. Costs should be minimized since
 * that the fitness is - cost.
 * 
 * @author Sebastian Land
 * @version $Id: ClassificationCostCriterion.java,v 1.3 2008/05/09 19:23:24 ingomierswa Exp $
 */
public class ClassificationCostCriterion extends MeasuredPerformance {

	private static final long serialVersionUID = -7466139591781285005L;

	private double[][] costMatrix;
	private double exampleCount; 
	private double costs;
	Attribute label;
	Attribute predictedLabel;
	
	public ClassificationCostCriterion(double[][] costMatrix, Attribute label, Attribute predictedLabel) {
		this.costMatrix = costMatrix;
		this.label = label;
		this.predictedLabel = predictedLabel;
		exampleCount = 0;
		costs = 0;
	}
	
	public String getDescription() {
		return "This Criterion delievers the misclassificationCosts";
	}

	public String getName() {
		return "Misclassifiactioncosts";
	}

	public void countExample(Example example) {
		exampleCount ++;
		costs += costMatrix[(int)example.getValue(predictedLabel)][(int)example.getValue(label)];
	}

	public double getExampleCount() {
		return exampleCount;
		}

	public double getFitness() {
		return -costs;
	}

	protected void buildSingleAverage(Averagable averagable) {
	}

	public double getMikroAverage() {
		return costs / exampleCount;
	}

	public double getMikroVariance() {
		return 0;
	}


}
