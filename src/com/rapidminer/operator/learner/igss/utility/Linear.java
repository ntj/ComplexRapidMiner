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
package com.rapidminer.operator.learner.igss.utility;

import com.rapidminer.operator.learner.igss.hypothesis.Hypothesis;

/** The utility function Linear. 
 * 
 *  @author Dirk Dach 
 *  @version $Id: Linear.java,v 1.1 2007/05/27 22:02:15 ingomierswa Exp $
 */
public class Linear extends AbstractUtility {
	
	/** Constructs a new Linear with the given default probability.*/
	public Linear (double[] priors, int large) {
		super(priors,large);
	}
	
	/** Calculates the utility for the given number of examples,positive examples and hypothesis.*/
	public double utility (double totalWeight, double totalPositiveWeight, Hypothesis hypo) {
		double g=hypo.getCoveredWeight()/totalWeight;
		double p=hypo.getPositiveWeight()/hypo.getCoveredWeight();
		if (hypo.getPrediction()==Hypothesis.POSITIVE_CLASS) {
			return g*(p-this.priors[Hypothesis.POSITIVE_CLASS]);
		}
		else {
			return g*(p-this.priors[Hypothesis.NEGATIVE_CLASS]);
		}
	}

	/** Calculate confidence intervall without a specific rule. */
	public double conf (double totalExampleWeight, double delta) {
		double inverseNormal=inverseNormal(1-delta/4);
		return inverseNormal/Math.sqrt(totalExampleWeight) + Math.pow(inverseNormal,2.0d)/(4.0d*totalExampleWeight);
	}
	
	/** Calculate confidence intervall for a specific rule.*/
	public double conf (double totalExampleWeight, double totalPositiveWeight, Hypothesis hypo, double delta) {
		double g=hypo.getCoveredWeight()/totalExampleWeight;
		double p=hypo.getPositiveWeight()/hypo.getCoveredWeight();
		double sg=variance(g,totalExampleWeight);
		double sp=variance(p,hypo.getCoveredWeight());
		double inverseNormal=inverseNormal(1-delta/4);
		return inverseNormal*(sg+sp+inverseNormal*sg*sp);
	}
	
	/** Calculates the variance for a binomial distribution. */
	private double variance(double p, double totalExampleWeight) {
		return (p*(1.0d-p))/totalExampleWeight;
	}
	
	/** Calculate confidence intervall without a specific rule for small m. */
	public double confSmallM (double totalExampleWeight, double delta) {
		return 3.0d*Math.sqrt(Math.log(4.0d/delta)/(2.0d*totalExampleWeight));
	}
	
	/** Returns an upper bound for the utility of refinements for the given hypothesis. */
	public double getUpperBound(double totalWeight, double totalPositiveWeight, Hypothesis hypo, double delta) {
		double p0;
		if (hypo.getPrediction()==Hypothesis.POSITIVE_CLASS) {
			p0=this.priors[Hypothesis.POSITIVE_CLASS];
		}
		else {
			p0=this.priors[Hypothesis.NEGATIVE_CLASS];
		}
		Utility cov=new Coverage(this.priors,this.large);
		Hypothesis h=hypo.clone();
		h.setCoveredWeight(hypo.getPositiveWeight()); // all fp become tn
		double g=cov.utility(totalWeight,totalPositiveWeight,h);
		double conf=cov.confidenceIntervall(totalWeight,delta);
		return ((g+conf)*(1.0d-p0));
	}
}
