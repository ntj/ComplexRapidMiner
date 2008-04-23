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
package com.rapidminer.operator.learner.functions.kernel.evosvm;

/**
 * This function must be maximized for the search for an optimal hyperplane for
 * classification.
 * 
 * @author Ingo Mierswa
 * @version $Id: ClassificationOptimizationFunction.java,v 1.5 2006/03/21
 *          15:35:48 ingomierswa Exp $
 */
public class ClassificationOptimizationFunction implements OptimizationFunction {

	private boolean multiobjective;
	
	public ClassificationOptimizationFunction(boolean multiobjective) {
		this.multiobjective = multiobjective;
	}
	
	public double[] getFitness(double[] alphas, double[] ys, Kernel kernel) {
		double sum = 0.0d;
		double alphaLabelSum = 0.0d;
		int numberSV = 0;
		for (int i = 0; i < ys.length; i++) {
			sum += alphas[i];
			alphaLabelSum += ys[i] * alphas[i];
			if (alphas[i] > 0)
				numberSV++;
		}
		
		double matrixSum = 0.0d;
		for (int i = 0; i < ys.length; i++) {
			if (alphas[i] == 0.0d)
				continue;
			for (int j = 0; j < ys.length; j++) {
				if (alphas[j] == 0.0d)
					continue;
				matrixSum += (alphas[i] * alphas[j] * ys[i] * ys[j] * kernel.getDistance(i, j));
			}
		}

		alphaLabelSum = -Math.abs(alphaLabelSum);
		
		if (multiobjective)
			//return new double[] { sum, -matrixSum };
			return new double[] { sum, -matrixSum, alphaLabelSum };
		    //return new double[] { sum, (sum - 0.5d * matrixSum) };
		else
			return new double[] { sum - 0.5d * matrixSum };
	}
}
