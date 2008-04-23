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
 * The RitterAdaption provides an implementation of the AdaptionFunction interface for calculation the adaption of a
 * node to an input stimulus.
 * 
 * @author Sebastian Land
 * @version $Id: RitterAdaption.java,v 1.1 2007/05/27 22:01:56 ingomierswa Exp $
 */
public class RitterAdaption implements AdaptionFunction {

	private double learnRateStart = 0.8;

	private double learnRateEnd = 0.01;

	private double adaptionRadiusStart = 5;

	private double adaptionRadiusEnd = 1;

	private int lastTime = -1;

	private double learnRateCurrent;

	private double adaptionRadiusCurrent;

	public double[] adapt(double[] stimulus, double[] nodeValue, double distanceFromImpact, int time, int maxtime) {
		// calculating time dependent variables only if time has changed
		if (lastTime != time) {
			lastTime = time;
			learnRateCurrent = learnRateStart * Math.pow((learnRateEnd / learnRateStart), (((double) time) / ((double) maxtime)));
			adaptionRadiusCurrent = getAdaptionRadius(time, maxtime);
			// System.out.println("T="+time+ " \t lr: "+ learnRateCurrent);
			// System.out.println("\t ar: "+adaptionRadiusCurrent);
		}
		double distanceWeightCurrent = Math.exp(-Math.pow(distanceFromImpact, 2) / (2 * Math.pow(adaptionRadiusCurrent, 2)));
		double weightNew[] = nodeValue.clone();
		if (distanceWeightCurrent > 0.5) {
			for (int i = 0; i < weightNew.length; i++) {
				weightNew[i] += learnRateCurrent * distanceWeightCurrent * (stimulus[i] - nodeValue[i]);
				if (weightNew[i] > 10) {
					weightNew[i] = weightNew[i];
				}
			}
		}
		return weightNew;
	}

	public double getAdaptionRadius(double[] stimulus, int time, int maxtime) {
		return getAdaptionRadius(time, maxtime);
	}

	private double getAdaptionRadius(int time, int maxtime) {
		return adaptionRadiusStart * Math.pow((adaptionRadiusEnd / adaptionRadiusStart), (((double) time) / ((double) maxtime)));
	}

	public void setAdaptionRadiusStart(double start) {
		this.adaptionRadiusStart = start;
	}

	public void setAdaptionRadiusEnd(double end) {
		this.adaptionRadiusEnd = end;
	}

	public void setLearnRateStart(double start) {
		this.learnRateStart = start;
	}

	public void setLearnRateEnd(double end) {
		this.learnRateEnd = end;
	}
}
