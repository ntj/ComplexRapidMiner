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
package com.rapidminer.operator.validation.clustering.itemdistribution;

/**
 * Normalized Gini Coefficient.
 * 
 * @author Michael Wurst
 * @version $Id: GiniCoefficient.java,v 1.1 2007/05/27 22:03:39 ingomierswa Exp $
 * 
 */
public class GiniCoefficient implements ItemDistributionMeasure {

    public double evaluate(int[] x, int n) {

        double sum = 0;
        for (int i = 0; i < x.length; i++)
            sum = sum + x[i];

        double mean = sum / n;

        sum = 0;
        for (int i = 0; i < x.length; i++)
            for (int j = 0; j < x.length; j++)
                sum = sum + Math.abs(x[i] - x[j]);

        if (mean == 0)
            return 0.0;

        return 1 - (sum / (2 * mean * (n * ((double) n - 1))));
    }

}
