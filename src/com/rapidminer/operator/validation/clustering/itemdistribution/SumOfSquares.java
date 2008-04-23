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
 * Calculates an item distribution measure by summing up the squares of the fraction of items in each cluster. The result is inverted, thus the higher the value, the better the items are distributed.
 * 
 * @author Michael Wurst
 * @version $Id: SumOfSquares.java,v 1.1 2007/05/27 22:03:39 ingomierswa Exp $
 * 
 */
public class SumOfSquares implements ItemDistributionMeasure {

    public double evaluate(int[] x, int n) {

        double result = 0;
        for (int i = 0; i < x.length; i++)
            result = result + (((double) x[i]) / n) * (((double) x[i]) / n);

        return result;
    }

}
