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
package com.rapidminer.operator.performance;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.Averagable;


/**
 * Measures the length of an example set (i.e. the number of attributes).
 * 
 * @author Ingo Mierswa
 * @version $Id: MDLCriterion.java,v 1.1 2007/05/27 21:59:12 ingomierswa Exp $
 */
public class MDLCriterion extends MeasuredPerformance {

	private static final long serialVersionUID = -5023462349084083154L;

	/** The possible optimization directions. */
	public static final String[] DIRECTIONS = { "minimization", "maximization" };

	/**
	 * Indicates that the fitness should be higher for smaller numbers of
	 * features.
	 */
	public static final int MINIMIZATION = 0;

	/**
	 * Indicates that the fitness should be higher for larger numbers of
	 * features.
	 */
	public static final int MAXIMIZATION = 1;

	/** The length of this example set. */
	private int length;

	/** A counter for average building. */
	private int counter = 1;

	/**
	 * Indicates if the fitness should be higher or smaller depending on the
	 * number of features.
	 */
	private int direction = MINIMIZATION;

	public MDLCriterion() {}

	public MDLCriterion(int direction) {
		this();
		this.direction = direction;
	}

	public MDLCriterion(MDLCriterion mdl) {
		super(mdl);
		this.length = mdl.length;
		this.counter = mdl.counter;
		this.direction = mdl.direction;
	}

	public String getName() {
		return "number_of_attributes";
	}

	public String getDescription() {
		return "Measures the length of an example set (i.e. the number of attributes).";
	}

	public void startCounting(ExampleSet eSet) throws OperatorException {
		super.startCounting(eSet);
		if (eSet instanceof AttributeWeightedExampleSet) {
			this.length = ((AttributeWeightedExampleSet) eSet).getNumberOfUsedAttributes();
		} else {
			this.length = eSet.getAttributes().size();
		}
	}

	public int getExampleCount() {
		return 1;
	}

	public void countExample(Example example) {}

	public double getFitness() {
		switch (direction) {
			case MINIMIZATION:
				return (-1) * (double) length / counter;
			case MAXIMIZATION:
				return (double) length / (double) counter;
			default:
				return Double.NaN; // cannot happen
		}
	}

	public double getMikroAverage() {
		return (double) (length) / (double) counter;
	}

	public double getMikroVariance() {
		return Double.NaN;
	}

	public void buildSingleAverage(Averagable averagable) {
		MDLCriterion other = (MDLCriterion) averagable;
		this.length += other.length;
		this.counter++;
	}
}
