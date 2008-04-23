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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/** Bins are a collection of {@link Bin}s providing some additional information and methods
 *  for histogram plotting.
 *  
 *  @author Ingo Mierswa
 *  @version $Id: Bins.java,v 1.2 2007/06/20 12:30:02 ingomierswa Exp $
 */
public class Bins {

	private RectangleStyle rectangleStyle;

	private int maxCounter = 0;

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
	
	public void addPoint(double position) {
		Iterator i = binList.iterator();
		while (i.hasNext()) {
			Bin bin = (Bin) i.next();
			if (bin.contains(position)) {
				int counter = bin.addPoint();
				maxCounter = Math.max(maxCounter, counter);
				break;
			}
		}
	}

	public int getMaxCounter() {
		return maxCounter;
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
