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
package com.rapidminer.tools.math;

import Jama.Matrix;

/**
 * This class can be used to calculate the coefficients of a (weighted) linear
 * regression. It uses the class Matrix from the Jama package for this purpose.
 * It is also possible to apply Ridge Regression which is a sort of regularization
 * well suited especially for ill-posed problems. Please note that for the dependent
 * matrix Y only one column is allowed.
 * 
 * @author Ingo Mierswa
 * @version $Id: LinearRegression.java,v 1.3 2008/05/09 19:23:02 ingomierswa Exp $
 */
public class LinearRegression {

	/** The resulting coefficients which will be calculated during construction. */
	private double[] coefficients = null;

	/** Performs a linear ridge regression. */
	public LinearRegression(Matrix x, Matrix y, double ridge) {
		calculate(x, y, ridge);
	}

	/** Performs a weighted linear ridge regression. */
	public LinearRegression(Matrix x, Matrix y, double[] weights, double ridge) {
		Matrix weightedIndependent = new Matrix(x.getRowDimension(), x.getColumnDimension());
		Matrix weightedDependent = new Matrix(x.getRowDimension(), 1);
		for (int i = 0; i < weights.length; i++) {
			double sqrtWeight = Math.sqrt(weights[i]);
			for (int j = 0; j < x.getColumnDimension(); j++)
				weightedIndependent.set(i, j, x.get(i, j) * sqrtWeight);
			weightedDependent.set(i, 0, y.get(i, 0) * sqrtWeight);
		}

		calculate(weightedIndependent, weightedDependent, ridge);
	}

	/** Calculates the regression coefficients. */
	private void calculate(Matrix x, Matrix y, double ridge) {
		int numberOfColumns = x.getColumnDimension();
		coefficients = new double[numberOfColumns];
		Matrix xTransposed = x.transpose();
		Matrix result;
		boolean finished = false;
		while (!finished) {
			Matrix xTx = xTransposed.times(x);
			
			// Set ridge regression adjustment
			for (int i = 0; i < numberOfColumns; i++)
				xTx.set(i, i, xTx.get(i, i) + ridge);
			
			// Carry out the regression
			Matrix xTy = xTransposed.times(y);
			for (int i = 0; i < numberOfColumns; i++)
				coefficients[i] = xTy.get(i, 0);

			try {
				result = xTx.solve(new Matrix(coefficients, coefficients.length));
				for (int i = 0; i < numberOfColumns; i++)
					coefficients[i] = result.get(i, 0);
				finished = true;
			} catch (Exception ex) {
				ridge *= 10;
				finished = false;
			}
		}
	}

	/** Returns the calculated coefficients. */
	public double[] getCoefficients() {
		return coefficients;
	}
}
