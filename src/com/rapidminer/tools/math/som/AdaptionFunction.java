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
package com.rapidminer.tools.math.som;

/**
 * This interface provides the methods for adapting nodes to an input.
 * 
 * @author Sebastian Land
 * @version $Id: AdaptionFunction.java,v 1.1 2007/05/27 22:01:56 ingomierswa Exp $
 */
public interface AdaptionFunction {
	
	/**
	 * This method returns the new value of a node, after it had adopted to a stimulus.
	 * The stimulus and the old node value are given. The distance from the impact of the 
	 * stimulus may influence the strength of the adoption to the stimulus, as may the 
	 * current trainingsphase (time) and the total number of trainingsphases (timemax)
	 */
	public double[] adapt(double[] stimulus, double[] nodeValue, double distanceFromImpact, int time, int timemax);
	
	/**
	 * This method returns the maximum adaption radius, given the input stimulus, the current
	 * trainingsphase (time) and the total number of trainingsphases (timemax). It may be used
	 * for finding all nodes effected by the stimulus.
	 */	
	public double getAdaptionRadius(double[] stimulus, int time, int timemax);
}
