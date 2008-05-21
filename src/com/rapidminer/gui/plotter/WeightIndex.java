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
 * The weight index is used by several plotters to keep track of weights for specific 
 * data table indices.
 * 
 * @author Ingo Mierswa
 * @version $Id: WeightIndex.java,v 1.3 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class WeightIndex implements Comparable<WeightIndex> {
	
    private int index;
    private double weight;
    
    public WeightIndex(int index, double weight) {
        this.index = index;
        this.weight = weight;
    }
    
    public int getIndex() {
    	return this.index;
    }
    
    public double getWeight() {
    	return this.weight;
    }
    
    public int compareTo(WeightIndex wi) {
        return -1 * Double.compare(this.weight, wi.weight);
    }
    
    public boolean equals(Object o) {
    	if (!(o instanceof WeightIndex)) {
    		return false;
    	} else {
    		WeightIndex other = (WeightIndex)o;
    		return (this.index == other.index) && (this.weight == other.weight);
    	}
    }
    
    public int hashCode() {
    	return Integer.valueOf(this.index).hashCode() ^ Double.valueOf(this.weight).hashCode();
    }
}
