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

/**
 * A helper class for keeping values and names together sortable.
 * 
 * @author Ingo Mierswa
 * @version $Id: NameValue.java,v 1.2 2008/07/13 16:39:42 ingomierswa Exp $
 */
public class NameValue implements Comparable<NameValue> {

	private String name;
	
	private double value;
	
	public NameValue(String name, double value) {
		this.name  = name;
		this.value = value;
	}
	
	public int compareTo(NameValue o) {
		// decreasing!
		return Double.compare(o.value, this.value);
	}
	
	public String getName() { 
		return name; 
	}
	
	public double getValue() { 
		return value; 
	}
}
