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

import java.util.List;

import com.rapidminer.tools.IterationArrayList;
import com.rapidminer.tools.math.matrix.ExtendedSparseMatrix;

/**
 * A representation of a similarity measure as sparse matrix.
 * 
 * @author Michael Wurst
 * @version $Id: SimilarityMatrix.java,v 1.4 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class SimilarityMatrix extends ExtendedSparseMatrix<String, String> {

	private static final long serialVersionUID = 1746447963630192902L;

	/**
	 * Constructor for SimilarityMatrix.
	 */
	public SimilarityMatrix(SimilarityMeasure sim) {
		super();
		List<String> ids = new IterationArrayList<String>(sim.getIds());
		for (int i = 0; i < ids.size(); i++) {
			for (int j = 0; j < ids.size(); j++) {
				if (sim.isSimilarityDefined(ids.get(i), ids.get(j)))
					setEntry(ids.get(i), ids.get(j), sim.similarity(ids.get(i), ids.get(j)));
			}
		}
	}
}
