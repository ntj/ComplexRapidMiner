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
package com.rapidminer.tools.math.matrix;

import java.util.List;
import java.util.Random;

import com.rapidminer.tools.IterationArrayList;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.MathFunctions;


/**
 * Compares two matrics using Pearson correlation.
 * 
 * @author Michael Wurst
 * @version $Id: CorrelationMatrixComparator.java,v 1.4 2008/05/09 19:23:23 ingomierswa Exp $
 * 
 */
public class CorrelationMatrixComparator<Ex, Ey> implements MatrixComparator<Ex, Ey> {

    public final static int MIN_PAIRS = 5;

    public double compare(Matrix<Ex, Ey> m1, Matrix<Ex, Ey> m2, double samplingRate) {

        List<Ex> xLabels = new IterationArrayList<Ex>(m1.getXLabels());
        List<Ey> yLabels = new IterationArrayList<Ey>(m1.getYLabels());

        if ((m2.getNumXLabels() != m1.getNumXLabels()) || (m2.getNumYLabels() != m1.getNumYLabels())) {

            LogService.getGlobal().log("Number of entries in both matrices is not equal, using the subset", LogService.WARNING);

            if (m1.getNumXLabels() > m2.getNumXLabels())
                xLabels = new IterationArrayList<Ex>(m2.getXLabels());
            else
                xLabels = new IterationArrayList<Ex>(m1.getXLabels());

            if (m1.getNumYLabels() > m2.getNumYLabels())
                yLabels = new IterationArrayList<Ey>(m2.getYLabels());
            else
                yLabels = new IterationArrayList<Ey>(m1.getYLabels());
        }

        double[] r1, r2;

        int numXLabels = xLabels.size();
        int numYLabels = yLabels.size();

        int numPairs = (int) Math.round(((double) xLabels.size() * yLabels.size()) * samplingRate);

        Random rng = RandomGenerator.getGlobalRandomGenerator();
        boolean r1AllEqual = true;
        boolean r2AllEqual = true;

        numPairs = Math.max(MIN_PAIRS, numPairs);
        numPairs = Math.min(xLabels.size() * yLabels.size(), numPairs);

        if (numPairs < MIN_PAIRS) {
            LogService.getGlobal().log("Too few items to compare", LogService.WARNING);
            return 0.0;
        }
        r1 = new double[numPairs];
        r2 = new double[numPairs];

        for (int i = 0; i < numPairs;) {

            Ex x = xLabels.get(rng.nextInt(numXLabels));
            Ey y = yLabels.get(rng.nextInt(numYLabels));

            double v1 = m1.getEntry(x, y);
            double v2 = m2.getEntry(x, y);

            if (!Double.isInfinite(v1) && !Double.isNaN(v1) && !Double.isInfinite(v2) && !Double.isNaN(v2)) {

                r1[i] = v1;
                r2[i] = v2;
                if (i > 0) {

                    if (r1[i - 1] != r1[i])
                        r1AllEqual = false;
                    if (r2[i - 1] != r2[i])
                        r2AllEqual = false;

                }
                i++;
            }

        }

        if ((!r1AllEqual) && (!r2AllEqual))
            return MathFunctions.correlation(r1, r2);
        else if (r1AllEqual && r2AllEqual)
            if (r1[0] == r2[0])
                return 1.0;
            else
                return 0.0;
        else
            return 0.0;
    }

    public boolean getReciprogalFitness() {

        return false;
    }
}
