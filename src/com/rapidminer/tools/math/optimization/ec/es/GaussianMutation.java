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
package com.rapidminer.tools.math.optimization.ec.es;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Changes the values by adding a gaussian distribution multiplied with the
 * current variance. Clips the value range to [min,max].
 * 
 * @author Ingo Mierswa
 * @version $Id: GaussianMutation.java,v 1.1 2007/05/27 22:03:33 ingomierswa Exp $
 */
public class GaussianMutation implements PopulationOperator {

	private double[] sigma;

	private double[] min, max;

    private int[] valueTypes;
    
    private Random random;
    
    
	public GaussianMutation(double[] sigma, double[] min, double[] max, int[] valueTypes, Random random) {
		this.sigma = sigma;
		this.min = min;
        this.max = max;
        this.valueTypes = valueTypes;
        this.random = random;
	}

	public void setSigma(double[] sigma) {
		this.sigma = sigma;
	}

	public double[] getSigma() {
		return this.sigma;
	}

	public void operate(Population population) {
		List<Individual> newIndividuals = new LinkedList<Individual>();
		for (int i = 0; i < population.getNumberOfIndividuals(); i++) {
			Individual clone = (Individual) population.get(i).clone();
			double[] values = clone.getValues();
			for (int j = 0; j < values.length; j++) {
				values[j] += random.nextGaussian() * sigma[j];
                if (valueTypes[j] == ESOptimization.VALUE_TYPE_INT)
                    values[j] = (int)Math.round(values[j]);
				if (values[j] < min[j])
					values[j] = min[j];
				if (values[j] > max[j])
					values[j] = max[j];
			}
			clone.setValues(values);
			newIndividuals.add(clone);
		}
		population.addAll(newIndividuals);
	}
}
