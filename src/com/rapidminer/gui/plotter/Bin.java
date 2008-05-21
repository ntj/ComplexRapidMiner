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
package com.rapidminer.gui.plotter;

/** Used to collect values for, e.g., a histogram.
 * 
 * @author Ingo Mierswa
 * @version $Id: DistributionPlotter.java,v 2.12 2006/03/27 13:21:58 ingomierswa
 *          Exp $
 */
public class Bin {

	private double left;

	private double right;

	private double weightCounter = 0;

	public Bin(double left, double right) {
		this.left = left;
		this.right = right;
	}

	public boolean contains(double position) {
		// rounding error hack
		return ((position >= left) && (position <= (right + 0.00000001)));
	}

	public double addPoint(double weight) {
		weightCounter += weight;
		return weightCounter;
	}
	
	public double getCounter() {
		return weightCounter;
	}

	public double getRight() {
		return right;
	}

	public double getLeft() {
		return left;
	}

	public String toString() {
		return "(" + left + "," + right + ") = " + weightCounter;
	}
}
