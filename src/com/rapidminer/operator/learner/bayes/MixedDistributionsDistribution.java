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

import java.util.ArrayList;
import java.util.Collection;

import com.rapidminer.tools.Tools;

/**
 * This distribution is built from a set of other distributions.
 * 
 * @author Sebastian Land
 * @version $Id: MixedDistributionsDistribution.java,v 1.5 2008/05/09 19:23:21 ingomierswa Exp $
 */
public class MixedDistributionsDistribution implements Distribution {

	private static final long serialVersionUID = 8852938552268669069L;

	private ArrayList<Distribution> distributions;

	public MixedDistributionsDistribution(ArrayList<Distribution> distributions) {
		this.distributions = distributions;
	}

	public MixedDistributionsDistribution() {
		this.distributions = new ArrayList<Distribution>();
	}

	public double getProbability(double x) {
		double probability = 0;
		for (Distribution distribution: distributions) {
			probability += distribution.getProbability(x);
		}
		return probability / distributions.size();
	}

	public void addDistribution(Distribution distribution) {
		this.distributions.add(distribution);
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Using " + distributions.size() + " distributions to estimate real distribution");
		buffer.append(Tools.getLineSeparator());
		for (Distribution distribution: distributions) {
			buffer.append(distribution.toString());
			buffer.append(Tools.getLineSeparator());
		}
		return buffer.toString();
	}

	public void addDistributions(Collection<Distribution> distributions) {
		this.distributions.addAll(distributions);
	}

	public double getLowerBound() {
		double lowerBound = Double.POSITIVE_INFINITY;
		for (Distribution distribution: distributions) {
			lowerBound = Math.min(lowerBound, distribution.getLowerBound());	
		}
		return lowerBound;
	}

	public double getUpperBound() {
		double upperBound = Double.NEGATIVE_INFINITY;
		for (Distribution distribution: distributions) {
			upperBound = Math.max(upperBound, distribution.getUpperBound());	
		}
		return upperBound;	}

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
