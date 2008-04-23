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
package com.rapidminer.operator.learner.igss;

import com.rapidminer.operator.learner.igss.hypothesis.Hypothesis;

/**
 * Object of this class represent results found by the IGSS algorithm. Stored are the found hypothesis, the utility of the hypothesis, the used weight and the size of the confidence intervall.
 * 
 * @author Dirk Dach
 * @version $Id: Result.java,v 1.1 2007/05/27 22:03:40 ingomierswa Exp $
 */
public class Result {

    /** The hypothesis stored in this result. */
    private Hypothesis hypo;

    /** total weight needed before this result was found */
    private double totalWeight;

    /** total positive weight needed before this result was found */
    private double totalPositiveWeight;

    /** The utility of this result's hypothesis */
    private double utility;

    /** The size of the confidence intervall of this result's hypothesis */
    private double confidence;

    public Result(Hypothesis h, double totalExampleWeight, double totalPositiveWeight, double u, double c) {
        this.hypo = h;
        this.totalWeight = totalExampleWeight;
        this.totalPositiveWeight = totalPositiveWeight;
        this.utility = u;
        this.confidence = c;
    }

    /** Returns the stored hypothesis. */
    public Hypothesis getHypothesis() {
        return hypo;
    }

    /** Returns the stored utility. */
    public double getUtility() {
        return utility;
    }

    /** Returns the stored size of the confidence intervall. */
    public double getConfidence() {
        return confidence;
    }

    /** Returns the stored positive weight. */
    public double getTotalPositiveWeight() {
        return totalPositiveWeight;
    }

    /** Returns the stored total weight. */
    public double getTotalWeight() {
        return totalWeight;
    }

    /** Returns true if the same hypothesis is stored by both results. */
    public boolean equals(Object o) {
		if (!(o instanceof Result))
			return false;
        Result otherResult = (Result) o;
        if (otherResult.getHypothesis().equals(this.getHypothesis())) {
            return true;
        } else {
            return false;
        }
    }
    
    public int hashCode() {
    	return this.hypo.hashCode();
    }
}
