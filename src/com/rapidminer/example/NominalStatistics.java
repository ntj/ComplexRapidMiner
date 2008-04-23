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
package com.rapidminer.example;

import java.util.Arrays;

import com.rapidminer.tools.LogService;

/** Attribute statistics object for nominal attributes. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: NominalStatistics.java,v 1.5 2007/07/13 22:52:14 ingomierswa Exp $
 */
public class NominalStatistics implements Statistics {

    private static final long serialVersionUID = -7644523717916796701L;

    private int mode = -1;
    
    private int maxCounter = 0;

    private int[] scores;

    private Attribute attribute;
    
    public NominalStatistics() {}
    
    /** Clone constructor. */
    private NominalStatistics(NominalStatistics other) {
        this.mode = other.mode;
        this.maxCounter = other.maxCounter;
        if (other.scores != null) {
            this.scores = new int[other.scores.length];
            for (int i = 0; i < this.scores.length; i++)
                this.scores[i] = other.scores[i];
        }
        this.attribute = other.attribute;
    }
    
    /** Returns a clone of this statistics object. The attribute is only cloned by reference. */
    public Object clone() {
        return new NominalStatistics(this);
    }
    
    public void startCounting(Attribute attribute) {
        this.attribute = attribute;
        this.scores = new int[attribute.getMapping().size()];
        this.mode = -1;
        this.maxCounter = 0;
    }
    
    public void count(double doubleIndex) {
        if (!Double.isNaN(doubleIndex)) {
            int index = (int)doubleIndex;
            // more values than before? Increase Array size...
            if (index >= scores.length) {
                int[] newScores = new int[index + 1];
                System.arraycopy(scores, 0, newScores, 0, scores.length);
                scores = newScores;
            }
            scores[index]++;
            if (scores[index] > maxCounter) {
                maxCounter = scores[index];
                mode = index;
            }
        }
    }
    
    public boolean handleStatistics(String name) {
        return 
            MODE.equals(name) ||
            COUNT.equals(name) ||
            LEAST.equals(name);
    }

    public double getStatistics(String name, String parameter) {
        if (MODE.equals(name)) {
            return this.mode;
        } else if (COUNT.equals(name)) {
            if (parameter != null) {
                return getValueCount(parameter);
            } else {
                LogService.getGlobal().log("Cannot calculate statistics COUNT, no value given...", LogService.WARNING);
                return Double.NaN;
            }
        } if (LEAST.equals(name)) {
            int minCounter = Integer.MAX_VALUE;
            int least = 0;
            for (int i = 0; i < scores.length; i++) {
                if (scores[i] < minCounter) {
                    minCounter = scores[i];
                    least = i;
                }
            }
            return least;
        } else {
            LogService.getGlobal().log("Cannot calculate statistics, unknown type: " + name, LogService.WARNING);
            return Double.NaN;
        }
    }
    
    private int getValueCount(String value) {
    	if ((attribute != null) && (attribute.getMapping() != null)) {
    		int index = attribute.getMapping().getIndex(value);
    		if (index < 0) {
    			return -1;
    		} else {
    			return scores[index];
    		}
    	} else {
    		return -1;
    	}
    }
    
    public String toString() {
        return "Counts: " + Arrays.toString(scores);
    }
}
