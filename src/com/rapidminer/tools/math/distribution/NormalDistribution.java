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
package com.rapidminer.tools.math.distribution;

import com.rapidminer.tools.Tools;


/**
 * This class represents a gaussian normal distribution.
 * 
 * @author Tobias Malbrecht
 * @version $Id: NormalDistribution.java,v 1.1 2008/06/03 14:17:47 tobiasmalbrecht Exp $
 */
public class NormalDistribution extends ContinuousDistribution {

	private static final long serialVersionUID = -1819042904676198636L;

	private static final double BOUND_FACTOR = 5;
	
	private double mean;

	private double standardDeviation;
	
 	public NormalDistribution(double mean, double standardDeviation) {
		this.mean = mean;
		this.standardDeviation = standardDeviation;
	}

 	public String getAttributeName() {
 		return null;
 	}
 	
	public static double getProbability(double mean, double standardDeviation, double value) {
		return Math.exp(-0.5 * (Math.pow((value - mean) / standardDeviation, 2))) / (standardDeviation * Math.sqrt(2 * Math.PI));
	}

	public static final double getLowerBound(double mean, double standardDeviation) {
		return mean - BOUND_FACTOR * standardDeviation;
	}

	public static final double getUpperBound(double mean, double standardDeviation) {
		return mean + BOUND_FACTOR * standardDeviation;
	}
	
	public double getProbability(double value) {
		return getProbability(mean, standardDeviation, value);
	}

	public double getMean() {
		return mean;
	}
	
	public double getStandardDeviation() {
		return standardDeviation;
	}
	
	public double getVariance() {
		return Math.pow(standardDeviation, 2);
	}
	
	public double getLowerBound() {
		return getLowerBound(mean, standardDeviation);
	}

	public double getUpperBound() {
		return getUpperBound(mean, standardDeviation);
	}
	
	public String toString() {
		return ("Normal distribution --> mean: " + Tools.formatNumber(mean) + ", standard deviation: " + Tools.formatNumber(standardDeviation));
	}
}
