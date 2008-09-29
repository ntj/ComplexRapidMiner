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

import java.io.Serializable;

/**
 * The basic interface for all distributions of variables.
 * 
 * @author Tobias Malbrecht
 * @version $Id: Distribution.java,v 1.3 2008/07/13 23:25:24 ingomierswa Exp $
 */
public interface Distribution extends Serializable {

	public boolean isDiscrete();
	
	public boolean isContinuous();
	
	public String getAttributeName();
	
	/**
	 * This method returns the density of the given distribution at
	 * the specified value.
	 * @param value the value which density shall be returned
	 */
	public double getProbability(double value);
	
	/**
	 * this method should return a string representation of the given
	 * value. Numerical Attributes might return a string representation
	 * of the value.
	 * @param value
	 */
	public String mapValue(double value);
	
	/**
	 * Should return an textual representation of the distribution.
	 */
	public String toString();
}
