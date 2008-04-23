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
package com.rapidminer.tools.math;

/**
 * A peak with a index and a value. The compare method returns 1 for the peak
 * with higher value.
 * 
 * @author Ingo Mierswa
 * @version $Id: Peak.java,v 1.1 2007/05/27 21:59:33 ingomierswa Exp $
 */
public class Peak implements Comparable<Peak> {

	private double index;

	private double magnitude;

	public Peak(double index, double magnitude) {
		this.index = index;
		this.magnitude = magnitude;
	}

	public double getIndex() {
		return index;
	}

	public double getMagnitude() {
		return magnitude;
	}

	public int compareTo(Peak p) {
		return (-1) * Double.compare(this.magnitude, p.magnitude);
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof Peak)) {
			return false;
		} else {
			Peak p = (Peak)o;
			return (this.index == p.index) && (this.magnitude == p.magnitude);
		}
	}

	public int hashCode() {
		return Double.valueOf(this.index).hashCode() ^ Double.valueOf(this.magnitude).hashCode();
	}
	
	public String toString() {
		return index + ": " + magnitude;
	}
}
