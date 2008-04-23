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

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.math.Averagable;


/**
 * Normalized absolute error is the total absolute error normalized by the error
 * simply predicting the average of the actual values.
 * 
 * @author Ingo Mierswa
 * @version $Id: NormalizedAbsoluteError.java,v 2.1 2006/04/14 07:47:17
 *          ingomierswa Exp $
 */
public class NormalizedAbsoluteError extends MeasuredPerformance {

	private static final long serialVersionUID = -3899005486051589953L;

	private Attribute predictedAttribute;

	private Attribute labelAttribute;

	private double deviationSum = 0.0d;

	private double relativeSum = 0.0d;

	private double trueLabelSum = 0.0d;

	private int exampleCounter = 0;

	public NormalizedAbsoluteError() {
	}

	public NormalizedAbsoluteError(NormalizedAbsoluteError nae) {
		super(nae);
		this.deviationSum = nae.deviationSum;
		this.relativeSum = nae.relativeSum;
		this.trueLabelSum = nae.trueLabelSum;
		this.exampleCounter = nae.exampleCounter;
	}

	public String getName() {
		return "normalized_absolute_error";
	}

	public String getDescription() {
		return "The absolute error divided by the error made if the average would have been predicted.";
	}

	public int getExampleCount() {
		return exampleCounter;
	}

	public void startCounting(ExampleSet exampleSet) throws OperatorException {
		super.startCounting(exampleSet);
		if (exampleSet.size() <= 1)
			throw new UserError(null, 919, getName(), "normalized absolute error can only be calculated for test sets with more than 2 examples.");
		this.predictedAttribute = exampleSet.getAttributes().getPredictedLabel();
		this.labelAttribute = exampleSet.getAttributes().getLabel();
		this.trueLabelSum = 0.0d;
		this.deviationSum = 0.0d;
		this.relativeSum = 0.0d;
		this.exampleCounter = 0;
		Iterator<Example> reader = exampleSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			double label = example.getLabel();
			if (!Double.isNaN(label)) {
				exampleCounter++;
				trueLabelSum += label;
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
			String labelS = example.getValueAsString(labelAttribute);
			plabel = example.getConfidence(labelS);
			label = 1.0d;
		}

		double diff = Math.abs(label - plabel);
		deviationSum += diff;
		double relDiff = Math.abs(label - (trueLabelSum / exampleCounter));
		relativeSum += relDiff;
	}

	public double getMikroAverage() {
		return deviationSum / relativeSum;
	}

	public double getMikroVariance() {
		return Double.NaN;
	}

	public double getFitness() {
		return -1 * getAverage();
	}

	public void buildSingleAverage(Averagable performance) {
		NormalizedAbsoluteError other = (NormalizedAbsoluteError) performance;
		this.deviationSum += other.deviationSum;
		this.relativeSum += other.relativeSum;
	}
}
