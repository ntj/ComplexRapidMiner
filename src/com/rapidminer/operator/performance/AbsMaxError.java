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
package com.rapidminer.operator.performance;

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.math.Averagable;


/**
 * Relative squared error is the total squared error made relative to what the
 * error would have been if the prediction had been the average of the absolute
 * value. As done with the root mean-squared error, the square root of the
 * relative squared error is taken to give it the same dimensions as the
 * predicted values themselves. Also, just like root mean-squared error, this
 * exaggerates the cases in which the prediction error was significantly greater
 * than the mean error.
 * 
 * @author Peter Volk
 * @version $Id: RootRelativeSquaredError.java,v 2.7 2006/04/14 07:47:17
 *          ingomierswa Exp $
 */
public class AbsMaxError extends MeasuredPerformance {

	private static final long serialVersionUID = 7781104825149866444L;

	private Attribute predictedAttribute;

	private Attribute labelAttribute;

	private double max = 0.0d;

	private double exampleCounter;

	public AbsMaxError() {
	}

	public AbsMaxError(AbsMaxError rse) {
		super(rse);
		max = rse.max ;
        this.labelAttribute = (Attribute)rse.labelAttribute.clone();
        this.predictedAttribute = (Attribute)rse.predictedAttribute.clone();
	}

	public String getName() {
		return "abs_max_error";
	}

	public String getDescription() {
		return "Absolute maximum error";
	}

	public double getExampleCount() {
		return exampleCounter;
	}

	public void startCounting(ExampleSet exampleSet, boolean useExampleWeights) throws OperatorException {
		super.startCounting(exampleSet, useExampleWeights);
		if (exampleSet.size() == 0)
			throw new UserError(null, 919, getName(), "root relative squared error can only be calculated for test sets with more than 2 examples.");
		this.predictedAttribute = exampleSet.getAttributes().getPredictedLabel();
		this.labelAttribute = exampleSet.getAttributes().getLabel();
		
		Iterator<Example> reader = exampleSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			double label = example.getValue(labelAttribute);
			double predicted = example.getValue(predictedAttribute);
			if (!Double.isNaN(label)) {
				exampleCounter++;
				if(Math.abs(label-predicted)>this.max){
					max = Math.abs(label-predicted);
				}
			}
		}
	}

	/** Calculates the error for the current example. */
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

		if(Math.abs(label-plabel)>max){
			max = Math.abs(label-plabel);
		}
		exampleCounter++;
	}

	public double getMikroAverage() {
		return max;
	}

	public double getMikroVariance() {
		return Double.NaN;
	}

	public double getFitness() {
		return (-1) * getAverage();
	}

	public void buildSingleAverage(Averagable performance) {
		AbsMaxError other = (AbsMaxError) performance;
		if(other.max > this.max){
			this.max = other.max;
		}
	}
}
