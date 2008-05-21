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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/** Bins are a collection of {@link Bin}s providing some additional information and methods
 *  for histogram plotting.
 *  
 *  @author Ingo Mierswa
 *  @version $Id: Bins.java,v 1.5 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class Bins {

	private RectangleStyle rectangleStyle;

	private double maxWeight = 0;

	private List<Bin> binList = new LinkedList<Bin>();

	public Bins(int styleIndex, double left, double right, int number) {
		this(new RectangleStyle(styleIndex), left, right, number);
	}
	
	public Bins(RectangleStyle rectangleStyle, double left, double right, int number) {
		this.rectangleStyle = rectangleStyle;
		double delta = (right - left) / number;
		double start = left;
		for (int i = 0; i < number; i++) {
			binList.add(new Bin(start, start + delta));
			start = start + delta;
		}			
	}
	
	public RectangleStyle getRectangleStyle() {
		return rectangleStyle;
	}
	
	public void addPoint(double position, double weight) {
		for (Bin bin: binList) {
			if (bin.contains(position)) {
				double newWeight = bin.addPoint(weight);
				maxWeight = Math.max(maxWeight, newWeight);
				break;
			}
		}
	}
	public void addPoint(double position) {
		addPoint(position, 1);
	}

	public double getMaxCounter() {
		return maxWeight;
	}

	public ListIterator<Bin> getIterator() {
		return binList.listIterator();
	}

    public ListIterator<Bin> getIterator(int n) {
        return binList.listIterator(n);
    }
    
    public int getNumberOfBins() {
        return binList.size();
    }
    
	public String toString() {
		return binList.toString();
	}
}
