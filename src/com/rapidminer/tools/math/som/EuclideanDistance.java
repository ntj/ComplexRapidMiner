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
 * This class provides a distance measure, equivalent to the 
 * euclidian distance measure.
 * 
 * @author Sebastian Land
 * @version $Id: EuclideanDistance.java,v 1.1 2007/05/27 22:01:56 ingomierswa Exp $
 */
public class EuclideanDistance implements DistanceFunction{

	public double getDistance(double[] point1, double[] point2) {
		double distance = 0;
		for (int i = 0; i < point1.length; i++){
			distance += Math.pow((point1[i] - point2[i]), 2);
		}
		distance = Math.sqrt(distance);
		return distance;
	}
	
	public double getDistance(int[] point1, int[] point2) {
		double distance = 0;
		for (int i = 0; i < point1.length; i++){
			distance += Math.pow(point1[i] - point2[i], 2);
		}
		distance = Math.sqrt(distance);
		return distance;
	}
	
	/** Integer distance calculation regards the wrap around of the net (hypertorus) */
	public double getDistance(int[] point1, int[] point2, int[] dimensions) {
		double distance = 0;
		for (int i = 0; i < point1.length; i++){
			int coord1 = (point1[i] < 0)? dimensions[i] + point1[i]:point1[i];	//wrapping around if incoming coordinate is negative
			int coord2 = (point2[i] < 0)? dimensions[i] + point2[i]:point2[i];
			distance += Math.pow(Math.min(Math.abs(coord1 - coord2),(coord1 + dimensions[i]- coord2) % dimensions[i]), 2);
		}
		distance = Math.sqrt(distance);
		return distance;
	}	
}
