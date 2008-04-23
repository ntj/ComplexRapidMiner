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

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.math.Averagable;


/**
 * Returns the average value of the prediction. This criterion can be used to
 * detect whether a learning scheme predicts nonsense, e.g. always make the same
 * error. This criterion is not suitable for evaluating the performance and
 * should never be used as main criterion. The {@link #getFitness()} method
 * always returns 0.
 * 
 * @author Ingo Mierswa
 * @version $Id: PredictionAverage.java,v 2.18 2006/03/21 15:35:51 ingomierswa
 *          Exp $
 */
public class PredictionAverage extends MeasuredPerformance {

	private static final long serialVersionUID = -5316112625406102611L;

	private double sum;

	private double squaredSum;

	private int count;

	public PredictionAverage() {
	}

	public PredictionAverage(PredictionAverage pa) {
		super(pa);
		this.sum = pa.sum;
		this.squaredSum = pa.squaredSum;
		this.count = pa.count;
	}

	public int getExampleCount() {
		return count;
	}

	public void countExample(Example example) {
		count++;
		double v = example.getLabel();
		if (!Double.isNaN(v)) {
			sum += v;
			squaredSum += v * v;
		}
	}

	public double getMikroAverage() {
		return sum / count;
	}

	public double getMikroVariance() {
		double avg = getMikroAverage();
		return (squaredSum / count) - avg * avg;
	}

	public void startCounting(ExampleSet set) {
		count = 0;
		sum = 0.0;
	}

	public String getName() {
		return "prediction_average";
	}

	/** Returns 0. */
	public double getFitness() {
		return 0.0;
	}

	public void buildSingleAverage(Averagable performance) {
		PredictionAverage other = (PredictionAverage) performance;
		this.sum += other.sum;
		this.squaredSum += other.squaredSum;
		this.count += other.count;
	}

	public String getDescription() {
		return "This is not a real performance measure, but merely the average of the predicted labels.";
	}
}
