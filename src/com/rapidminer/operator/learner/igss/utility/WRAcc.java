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

/** The instance-averaging utility function WRAcc.
 *  
 *  @author Dirk Dach 
 *  @version $Id: WRAcc.java,v 1.1 2007/05/27 22:02:14 ingomierswa Exp $
 */
public class WRAcc extends InstanceAveraging {
	
	/** Constructs new WRAcc with the given default probability.*/
	public WRAcc (double[] priors,int large) {
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
	
	/** Calculates the empirical variance. */
	public double variance(double totalWeight, double totalPositiveWeight, Hypothesis hypo) {
		double p0;
		if (hypo.getPrediction()==Hypothesis.POSITIVE_CLASS) {
			p0=this.priors[Hypothesis.POSITIVE_CLASS];
		}
		else {
			p0=this.priors[Hypothesis.NEGATIVE_CLASS];
		}
		double mean=this.utility(totalWeight,totalPositiveWeight,hypo);
		double innerTerm=hypo.getPositiveWeight()*Math.pow(1.0-p0-mean,2) +
						(hypo.getCoveredWeight()-hypo.getPositiveWeight())*Math.pow(0.0-p0-mean,2) +
						(totalWeight-hypo.getCoveredWeight())*Math.pow(0.0-mean,2);
		return Math.sqrt(innerTerm)/totalWeight;
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
		return ((g+conf)*(1.0-p0));
	}
}
