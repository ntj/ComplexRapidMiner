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

import java.io.Serializable;
import java.util.Collection;

/**
 * Distribution is an interface for a distribution class. It describes
 * two methods, which every distribution must have
 * 
 * @author Sebastian Land
 * @version $Id: Distribution.java,v 1.5 2008/05/09 19:23:21 ingomierswa Exp $
 */
public interface Distribution extends Serializable {
	
	/** This method returns the density of the given distribution at the specified value
	 *  @param x the value which density shall be returned
	 */
	public double getProbability(double x);
	
	/** Should return an textual representation of the distribution. */
	public String toString();
	
	/** This method returns a lower bound of values. This bound should be given 
	 * by the distributions tail, for example bounds should contain 95% intervall.
	 * nominal distributions should return NaN
	 */
	public double getLowerBound();
	/** This method returns a upper bound of values. This bound should be given 
	 * by the distributions tail, for example bounds should contain 95% intervall
	 * nominal distributions should return NaN
	 */
	public double getUpperBound();
	/**
	 * This method returns a collection of possible nominal values or null if distribution
	 * is not discrete
	 */
	public Collection<Double> getValues();
	/**
	 * This method should return the total empirical weight layed onto the examples
	 * This method should return NaN if distribution is not discrete
	 */
	public double getTotalWeight();
	/**
	 * this method should return a string representation of the given value. Numerical Attributes
	 * might return a string representation of the value
	 * @param value
	 */
	public String mapValue(double value);
}
