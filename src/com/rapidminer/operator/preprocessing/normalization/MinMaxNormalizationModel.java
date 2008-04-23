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

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.operator.AbstractModel;
import com.rapidminer.operator.OperatorException;


/**
 * A simple model which can be used to transform all regular attributes into
 * a value range between the given min and max values.
 * 
 * @author Ingo Mierswa
 * @version $Id: MinMaxNormalizationModel.java,v 1.4 2007/07/13 22:52:14 ingomierswa Exp $
 */
public class MinMaxNormalizationModel extends AbstractModel {

	private static final long serialVersionUID = 5620317015578777169L;

	/** The minimum value for each attribute after normalization. */
	private double min;

	/** The maximum value for each attribute after normalization. */
	private double max;

	/** Create a new normalization model. */
	public MinMaxNormalizationModel(ExampleSet exampleSet, double min, double max) {
        super(exampleSet);
		this.min = min;
		this.max = max;
	}

	/** Performs the transformation. */
	public void apply(ExampleSet exampleSet) throws OperatorException {
		exampleSet.recalculateAllAttributeStatistics();
		Iterator<Example> reader = exampleSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			for (Attribute attribute : exampleSet.getAttributes()) {
				if (!attribute.isNominal()) {
					double value = example.getValue(attribute);
					double minA = exampleSet.getStatistics(attribute, Statistics.MINIMUM);
					double maxA = exampleSet.getStatistics(attribute, Statistics.MAXIMUM);
					// if max = min or minA = maxA, all values are the same and
					// are normalized to 0.0
					double result = 0.0d;
					if (((maxA - minA) != 0.0d) && ((max - min) != 0.0d)) {
						result = (value - minA) / (maxA - minA) * (max - min) + min;
					}
					example.setValue(attribute, result);
				}
			}
		}
	}

	/**
	 * Returns a nicer name. Necessary since this model is defined as inner
	 * class.
	 */
	public String getName() {
		return "MinMaxNormalizationModel";
	}

	/** Returns a string representation of this model. */
	public String toString() {
		return "Normalize between " + this.min + " and " + this.max;
	}
}
