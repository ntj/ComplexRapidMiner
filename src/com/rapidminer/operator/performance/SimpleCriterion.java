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
package com.rapidminer.operator.performance;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.math.Averagable;


/**
 * Simple criteria are those which error can be counted for each example and can
 * be averaged by the number of examples. Since errors should be minimized, the 
 * fitness is calculated as -1 multiplied by the the error. 
 * Subclasses might also want to implement the method
 * <code>transform(double)</code> which applies a transformation on the value
 * sum divided by the number of counted examples. This is for example usefull in
 * case of root_means_squared error. All subclasses can be used for both
 * regression and classification problems. In case of classification the
 * confidence value for the desired true label is used as prediction.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: SimpleCriterion.java,v 1.3 2007/07/13 22:52:13 ingomierswa Exp $
 */
public abstract class SimpleCriterion extends MeasuredPerformance {

	private double sum = 0.0;

	private double squaresSum = 0.0;

	private int exampleCount = 0;

	private Attribute predictedAttribute;

	private Attribute labelAttribute;

	public SimpleCriterion() {
	}

	public SimpleCriterion(SimpleCriterion sc) {
		super(sc);
		this.sum = sc.sum;
		this.squaresSum = sc.squaresSum;
		this.exampleCount = sc.exampleCount;
	}

	public int getExampleCount() {
		return exampleCount;
	}

	/**
	 * Invokes <code>countExample(double, double)</code> and counts the
	 * deviation. In case of a nominal label the confidence of the desired true
	 * label is used as prediction. For regression problems the usual predicted
	 * label is used.
	 */
	public void countExample(Example example) {
		double plabel;
		double label = example.getValue(labelAttribute);

		if (!predictedAttribute.isNominal()) {
			plabel = example.getValue(predictedAttribute);
		} else {
			String labelS = example.getNominalValue(labelAttribute);
			plabel = example.getConfidence(labelS);
			label = 1.0d;
		}

		double deviation = countExample(label, plabel);
		if (!Double.isNaN(deviation)) {
			countExample(deviation);
		} else {
			LogService.getGlobal().log("SimpleCriterion: NaN was generated!", LogService.WARNING);
		}
	}

	/** Subclasses must count the example and return the value to sum up. */
	protected abstract double countExample(double label, double predictedLabel);

	/**
	 * Simply returns the given value. Subclasses might apply a transformation
	 * on the error sum divided by the number of examples.
	 */
	protected double transform(double value) {
		return value;
	}

	protected void countExample(double deviation) {
		if (!Double.isNaN(deviation)) {
			sum += deviation;
			squaresSum += deviation * deviation;
			exampleCount++;
		}
	}

	public double getMikroAverage() {
		return transform(sum / exampleCount);
	}

	public double getMikroVariance() {
		double mean = getMikroAverage();
		double meanSquares = transform(squaresSum) / exampleCount;
		return meanSquares - mean * mean;
	}

	public void startCounting(ExampleSet eset) throws OperatorException {
		super.startCounting(eset);
		exampleCount = 0;
		sum = squaresSum = 0;
		this.predictedAttribute = eset.getAttributes().getPredictedLabel();
		this.labelAttribute = eset.getAttributes().getLabel();
	}

	public double getFitness() {
		return (-1.0d) * getAverage();
	}

    /** Returns 0.0. */
    public double getMaxFitness() {
        return 0.0d;
    }
    
	public void buildSingleAverage(Averagable performance) {
		SimpleCriterion other = (SimpleCriterion) performance;
		this.sum += other.sum;
		this.squaresSum += other.squaresSum;
		this.exampleCount += other.exampleCount;
	}
}
