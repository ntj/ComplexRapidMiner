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
package com.rapidminer.operator.preprocessing.discretization;

import com.rapidminer.example.Example;

/**
 * A helperclass for the preprocessing operator FrequencyDiscretizer. It allows
 * to store an example with one numerical attribute value. Due to the
 * implementation of the Comparable interface, arrays of this class may be sorted
 * by Arrays.sort() with respect to the values. So it is possible to sort without loosing
 * the connection between value and its example.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: FrequencyDiscretizerExample.java,v 1.1 2007/05/27 22:02:53 ingomierswa Exp $
 */
public class FrequencyDiscretizerExample implements Comparable<FrequencyDiscretizerExample> {

	private double attributeValue;

	private Example attributeExample;

	public FrequencyDiscretizerExample(double value, Example example) {
		this.attributeValue = value;
		this.attributeExample = example;
	}

	public double getValue() {
		return this.attributeValue;
	}

	public Example getExample() {
		return this.attributeExample;
	}

	public int compareTo(FrequencyDiscretizerExample toCompare) {
		return Double.compare(this.getValue(), toCompare.getValue());
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof FrequencyDiscretizerExample)) {
			return false;
		} else {
			return this.attributeValue == ((FrequencyDiscretizerExample)o).attributeValue;
		}
	}
	
	public int hashCode() {
		return Double.valueOf(this.attributeValue).hashCode();
	}
}
