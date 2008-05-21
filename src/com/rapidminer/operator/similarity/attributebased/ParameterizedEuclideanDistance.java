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
package com.rapidminer.operator.similarity.attributebased;

import Jama.Matrix;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;

/**
 * Calculates for two vectors e1, e2 and a matrix m the distance sqrt( (e1 - e2)^T * m * (e1 - e2) )
 * 
 * @author Alexander Daxenberger
 * @version $Id: ParameterizedEuclideanDistance.java,v 1.4 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class ParameterizedEuclideanDistance extends AbstractRealValueBasedSimilarity {

    private static final long serialVersionUID = -6838958371319386710L;

    private Matrix m;

    private double det;

    public ParameterizedEuclideanDistance(ExampleSet es) throws OperatorException {
        int numberOfAttributes = es.getAttributes().size();
        this.init(es);
        this.m = Matrix.identity(numberOfAttributes, numberOfAttributes);
        this.det = this.m.det();
    }

    public boolean isDistance() {
        return true;
    }

    /**
     * Calculates sqrt( (e1 - e2)^T * m * (e1 - e2) )
     * 
     * @param e1 double[]
     * @param e2 double[]
     * @return double
     */
    public double similarity(double[] e1, double[] e2) {
        double[] diff = new double[e1.length];
        double res = 0.0;
        double sum;

        for (int i = 0; i < diff.length; i++) {
            diff[i] = e1[i] - e2[i];
        }

        for (int j = 0; j < diff.length; j++) {
            sum = 0.0;
            for (int i = 0; i < diff.length; i++) {
                sum += diff[i] * this.m.get(i, j);
            }
            res += sum * diff[j];
        }

        return Math.sqrt(res);
    }

    public Matrix getMatrix() {
        return this.m;
    }

    public double getDeterminant() {
        return this.det;
    }

    public void setMatrix(Matrix m) {
        this.m = m;
        this.det = m.det();
    }
}
