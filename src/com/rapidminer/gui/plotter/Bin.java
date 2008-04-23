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

	private int counter = 0;

	public Bin(double left, double right) {
		this.left = left;
		this.right = right;
	}

	public boolean contains(double position) {
		// rounding error hack
		return ((position >= left) && (position <= (right + 0.00000001)));
	}

	public int addPoint() {
		counter++;
		return counter;
	}

	public int getCounter() {
		return counter;
	}

	public double getRight() {
		return right;
	}

	public double getLeft() {
		return left;
	}

	public String toString() {
		return "(" + left + "," + right + ") = " + counter;
	}
}
