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
package com.rapidminer.operator.preprocessing.normalization;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.AbstractModel;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Tools;


/** This model performs a z-Transformation on the given example set. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: ZTransformationModel.java,v 1.2 2007/07/13 22:52:14 ingomierswa Exp $
 */
public class ZTransformationModel extends AbstractModel {

	private static final long serialVersionUID = 7739929307307501706L;

	/**
     * This helper class stores the information about the mean and variance
     * for an attribute.
     */
    private static class MeanVariance implements Serializable {

		private static final long serialVersionUID = -5414956194248271071L;

		/** The mean value. */
        double mean = 0.0d;

        /** The variance value. */
        double variance = 1.0d;

        /** Creates a new MeanVariance helper object. */
        public MeanVariance(double mean, double variance) {
            this.mean = mean;
            this.variance = variance;
        }
    }
    
	/**
	 * This map contains all information about the mean and variance values
	 * for all attributes. Maps attribute names to MeanVariance objects.
	 */
	private Map<String, MeanVariance> meanVarianceMap = new HashMap<String, MeanVariance>();


    
	public ZTransformationModel(ExampleSet exampleSet) {
		super(exampleSet);
	}

	/**
	 * Adds the mean and variance information for an attribute with the
	 * given name.
	 */
	public void addMeanVariance(String name, double mean, double variance) {
		meanVarianceMap.put(name, new MeanVariance(mean, variance));
	}

	/** Performs the transformation. */
	public void apply(ExampleSet exampleSet) throws OperatorException {
		Iterator<Example> r = exampleSet.iterator();
		while (r.hasNext()) {
			Example example = r.next();
			Iterator i = meanVarianceMap.keySet().iterator();
			while (i.hasNext()) {
				String name = (String) i.next();
				MeanVariance mv = meanVarianceMap.get(name);
				Attribute attribute = exampleSet.getAttributes().get(name);
				if (mv.variance <= 0) {
					example.setValue(attribute, 0);
				} else {
					double newValue = (example.getValue(attribute) - mv.mean) / (Math.sqrt(mv.variance));
					example.setValue(attribute, newValue);
				}
			}
		}
	}

	/**
	 * Returns a nicer name. Necessary since this model is defined as inner
	 * class.
	 */
	public String getName() {
		return "Z-Transformation";
	}

	/** Returns a string representation of this model. */
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("Normalize " + meanVarianceMap.size() + " attributes to mean 0 and variance 1." + Tools.getLineSeparator() + "Using");
		Iterator i = meanVarianceMap.keySet().iterator();
		int counter = 0;
		while (i.hasNext()) {
			if (counter > 4) {
				result.append(Tools.getLineSeparator() + "... " + (meanVarianceMap.size() - 5) + " more attributes ...");
				break;
			}
			String name = (String) i.next();
			MeanVariance mv = meanVarianceMap.get(name);
			result.append(Tools.getLineSeparator() + name + " --> mean: " + mv.mean + ", variance: " + mv.variance);
			counter++;
		}
		return result.toString();
	}
}
