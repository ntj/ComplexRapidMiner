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
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.Averagable;


/**
 * Computes either the Spearman (rho) or Kendall (tau-b) rank
 * correlation between the actual label and predicted values of
 * an example set.  Since ranking is involved, neither correlation
 * is averageable.
 * 
 * @author Paul Rubin
 * @version $Id: RankCorrelation.java,v 1.1 2007/05/27 21:59:12 ingomierswa Exp $
 */
public class RankCorrelation extends MeasuredPerformance {

	private static final long serialVersionUID = 6908369375703683363L;

	public static final String[] NAMES = {
		"spearman_rho",
		"kendall_tau"
	};
	
	public static final String[] DESCRIPTIONS = {
		"The rank correlation between the actual and predicted labels, using Spearman's rho.",
		"The rank correlation between the actual and predicted labels, using Kendall's tau-b."
	};
	
	public static final int RHO = 0;
	public static final int TAU = 1;
	
	private int counter = 0; // example count

	private double value = Double.NaN;

	private int type = RHO;

	
	/** Default constructor */
	public RankCorrelation() {
		this(RHO);
	}

	/**
	 * Constructor with user-specified choice of correlation coefficient.
	 * User specifies RHO or TAU.
	 * 
	 * @param type 
	 *     coefficient type with coefficient choice
	 */
	public RankCorrelation(int type) {
		this.type = type;
	}

	public RankCorrelation(RankCorrelation rc) {
		super(rc);
		this.type = rc.type;
		this.value = rc.value;
		this.counter = rc.counter;
	}

	/** Does nothing. Everything is done in {@link #startCounting(ExampleSet)}. */
	public void countExample(Example example) {}

	public String getDescription() {
		return DESCRIPTIONS[type];
	}

	public int getExampleCount() {
		return counter;
	}

	public double getFitness() {
		return getMikroAverage();
	}


	/** Averaging across instances of RankCorrelation is unsupported (?)
	 *  For now just build the usual average by summing up the values... */
	protected void buildSingleAverage(Averagable averagable) {
		RankCorrelation other = (RankCorrelation) averagable;
		this.counter += other.counter;
		this.value += other.value;
	}

	public double getMikroAverage() {
		return value;
	}

	public double getMikroVariance() {
		return Double.NaN;
	}

	public String getName() {
		return NAMES[type];
	}

	/** Computes whichever of rho and tau was requested. */
	public void startCounting(ExampleSet eSet) throws OperatorException {
		this.counter = eSet.size();
		if (type == RHO)
			this.value = RankStatistics.rho(eSet, eSet.getAttributes().getLabel(), eSet.getAttributes().getPredictedLabel());
		else
			this.value = RankStatistics.tau_b(eSet, eSet.getAttributes().getLabel(), eSet.getAttributes().getPredictedLabel());
		
	}
}
