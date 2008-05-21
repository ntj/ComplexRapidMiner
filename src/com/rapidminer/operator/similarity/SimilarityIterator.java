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

import java.util.ArrayList;

import com.rapidminer.tools.IterationArrayList;

/**
 * Iterates over a similarity measure.
 * 
 * @author Michael Wurst
 * @version $Id: SimilarityIterator.java,v 1.3 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class SimilarityIterator {

	private final ArrayList<String> ids;

	private int counter;

	private final SimilarityMeasure sim;

	private final int numItems;

	private double buffer;

	private int delta;

	public SimilarityIterator(SimilarityMeasure sim, int numSamples) {
		this.sim = sim;
		counter = -1;
		ids = new IterationArrayList<String>(sim.getIds());
		numItems = ids.size();
		if (numSamples > 0)
			delta = (numItems * numItems) / numSamples;
		else
			delta = 1;
		if (delta < 1)
			delta = 1;
		nextVal();
	}

	public SimilarityIterator(SimilarityMeasure sim) {
		this(sim, 0);
	}

	private void nextVal() {
		String idx = "", idy = "";
		do {
			counter = counter + delta;
			if (counter < numItems * numItems) {
				idx = ids.get(counter % numItems);
				idy = ids.get(counter / numItems);
			}
		} while ((!sim.isSimilarityDefined(idx, idy)) && (counter < numItems * numItems));
		if (counter >= numItems * numItems)
			buffer = Double.NaN;
		else
			buffer = sim.similarity(idx, idy);
	}

	public double nextValue() {
		double result = buffer;
		nextVal();
		return result;
	}

	public boolean hasNext() {
		return !Double.isNaN(buffer);
	}
}
