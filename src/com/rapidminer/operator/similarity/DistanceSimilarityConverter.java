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
package com.rapidminer.operator.similarity;

import java.util.Iterator;

import com.rapidminer.operator.AbstractIOObject;
import com.rapidminer.operator.IOObject;

/**
 * Converts a distance measure into a similarity measure and the other way around.
 * 
 * @author Michael Wurst
 * @version $Id: DistanceSimilarityConverter.java,v 1.2 2007/06/20 12:30:02 ingomierswa Exp $
 */
public class DistanceSimilarityConverter extends AbstractIOObject implements SimilarityMeasure {

	private static final long serialVersionUID = -3699572317989121330L;

	private final SimilarityMeasure sim;

	/**
	 * Create a distance measure from a similarity measure or the other way round.
	 * 
	 * @param sim
	 *            the corresponding similarity/distance measure
	 */
	public DistanceSimilarityConverter(final SimilarityMeasure sim) {
		super();
		this.sim = sim;
	}

	public IOObject copy() {
		return this;
	}

	public double similarity(String x, String y) {
		double v = sim.similarity(x, y);
		return -v;
	}

	public boolean isSimilarityDefined(String x, String y) {
		return sim.isSimilarityDefined(x, y);
	}

	public Iterator<String> getIds() {
		return sim.getIds();
	}
    
    public int getNumberOfIds() {
        return sim.getNumberOfIds();
    }

	public String explainSimilarity(String x, String y) {
		return sim.explainSimilarity(x, y);
	}

	public boolean isDistance() {
		return !sim.isDistance();
	}
}
