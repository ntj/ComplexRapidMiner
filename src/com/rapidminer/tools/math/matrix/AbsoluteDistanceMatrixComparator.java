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
package com.rapidminer.tools.math.matrix;

import java.util.List;
import java.util.Random;

import com.rapidminer.tools.IterationArrayList;
import com.rapidminer.tools.RandomGenerator;


/**
 * Compare matrices using absolute distance.
 * 
 * @author Michael Wurst
 * @version $Id: AbsoluteDistanceMatrixComparator.java,v 1.1 2007/05/27 22:03:34 ingomierswa Exp $
 * 
 */
public class AbsoluteDistanceMatrixComparator<Ex, Ey> implements MatrixComparator<Ex, Ey> {

    public double compare(Matrix<Ex, Ey> m1, Matrix<Ex, Ey> m2, double samplingRate) {

        List<Ex> xLabels = new IterationArrayList<Ex>(m1.getXLabels());
        List<Ey> yLabels = new IterationArrayList<Ey>(m1.getYLabels());

        int counter = 0;
        double dist = 0.0;

        Random rng = RandomGenerator.getGlobalRandomGenerator();

        for (int i = 0; i < xLabels.size(); i++) {

            Ex x = xLabels.get(i);

            for (int j = 0; j < yLabels.size(); j++) {

                if (rng.nextDouble() < samplingRate) {

                    Ey y = yLabels.get(j);
                    double d = Math.abs(m1.getEntry(x, y) - m2.getEntry(x, y));
                    if (!Double.isInfinite(d) && !Double.isNaN(d)) {
                        counter++;
                        dist = dist + d;
                    }

                }

            }

        }

        return dist / counter;
    }

    public boolean getReciprogalFitness() {

        return true;
    }
}
