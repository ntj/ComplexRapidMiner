/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2008 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.operator.similarity;

import java.util.Iterator;

import com.rapidminer.tools.IterationArrayList;
import com.rapidminer.tools.math.matrix.AbstractMatrix;

/**
 * Creates a matrix from a similarity measure, that is evaluated lazy. 
 * NOTE: This matrix does implement only a small subset of the extended access
 * matrix methods!!!!!
 * 
 * @author Michael Wurst
 * @version $Id: LazySimilarityMatrix.java,v 1.5 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class LazySimilarityMatrix extends AbstractMatrix<String, String> {

	private static final long serialVersionUID = 1408219708147371126L;
	
	private SimilarityMeasure sim;

	/**
	 * Create a new similarity matrix.
	 * 
	 * @param sim
	 *            the underlying similarity
	 */
	public LazySimilarityMatrix(SimilarityMeasure sim) {
		super();
		this.sim = sim;
	}

	public Iterator<String> getXLabels() {
		return sim.getIds();
	}

	public Iterator<String> getYLabels() {
		return sim.getIds();
	}

	public int getNumXLabels() {
		return (new IterationArrayList<String>(sim.getIds())).size();
	}

	public int getNumYLabels() {
		return (new IterationArrayList<String>(sim.getIds())).size();
	}

	public double getEntry(String x, String y) {
		if (sim.isSimilarityDefined(x, y))
			return sim.similarity(x, y);
		else
			return Double.NaN;
	}

	public void incEntry(String x, String y, double val) {
		throw new Error("Method not implemented");
	}

	public void setEntry(String x, String y, double val) {
		throw new Error("Method not implemented");
	}
}
