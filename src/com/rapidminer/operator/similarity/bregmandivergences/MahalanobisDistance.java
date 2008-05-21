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
package com.rapidminer.operator.similarity.bregmandivergences;

import Jama.Matrix;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

/**
 * The &quot;Mahalanobis distance &quot;.
 * 
 * @author Regina Fritsch
 * @version $Id: MahalanobisDistance.java,v 1.2 2008/05/09 19:23:19 ingomierswa Exp $
 */
public class MahalanobisDistance extends AbstractBregmanDivergence {
	
	//private double[][] data;
	protected ExampleSet exampleSet;

	public MahalanobisDistance(ExampleSet es) throws InstantiationException {
		super(es);
		exampleSet = es;
	}

	public double distance(Example x, double[] y) {
		double result = 0;
		
		// x-y to matrix
		double[] help = vectorSubtraction(x, y);
		double[][] helpMatrix = new double[help.length][1];
		for (int i = 0; i < help.length; i++) {
			helpMatrix [i][0] = help[i];
		}
		Matrix xy = new Matrix(helpMatrix);
		
		Matrix covarianceMatrix = com.rapidminer.tools.math.matrix.CovarianceMatrix.getCovarianceMatrix(exampleSet);

		// compute the mahalanobis distance
		covarianceMatrix = ((xy.transpose()).times(covarianceMatrix.inverse())).times(xy);

		helpMatrix = covarianceMatrix.getArray();

		result = helpMatrix[0][0];

		return result;
	}
	

	public boolean isApplicable(Example x) {
		return true;
	}

}
