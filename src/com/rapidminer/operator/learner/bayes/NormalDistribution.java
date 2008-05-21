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
package com.rapidminer.operator.learner.bayes;

import java.util.Collection;

import com.rapidminer.tools.Tools;


/**
 * NormalDistribution is a distribution, calculating the probaility 
 * for a given value from an gaussian normal distribution.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: NormalDistribution.java,v 1.8 2008/05/09 19:23:21 ingomierswa Exp $
 */
public class NormalDistribution implements Distribution {

	private static final long serialVersionUID = -1819042904676198636L;

	private double mean;

	private double variance;

	private double scaleFactor;

	public NormalDistribution(double mean, double variance) {
		this.mean = mean;
		this.variance = variance;
		this.scaleFactor = 1 / (variance * Math.sqrt(2 * Math.PI));
	}

	public double getProbability(double x) {
		double distr = Math.exp(-0.5 * (Math.pow((x - mean) / variance, 2)));
		return scaleFactor * distr;
	}

	public String toString() {
		return ("Numerical --> mean: " + Tools.formatNumber(mean) + ", standard deviation: " + Tools.formatNumber(variance));
	}

	public double getLowerBound() {
		return mean - 5 * variance;
	}

	public double getUpperBound() {
		return mean + 5 * variance;
	}

	public Collection<Double> getValues() {
		return null;
	}
	public double getTotalWeight() {
		return Double.NaN;
	}
	public String mapValue(double value) {
		return Double.toString(value);
	}
}
