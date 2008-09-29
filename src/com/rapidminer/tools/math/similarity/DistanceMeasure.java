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
package com.rapidminer.tools.math.similarity;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;

/**
 * This interfaces defines the methods for all similarity measures. Classes implementing this 
 * interface are not allowed to have a constructer, instead should use the init method.
 * 
 * @author Sebastian Land
 * @version $Id: DistanceMeasure.java,v 1.4 2008/08/05 09:40:31 stiefelolm Exp $
 */
public abstract class DistanceMeasure {
	
	/**
	 * Before using a similarity measure, it is needed to initialise. Subclasses might use initialising 
	 * for remembering the exampleset properties like attribute type.
	 * @param exampleSet the exampleset 
	 */
	public abstract void init(ExampleSet exampleSet) throws OperatorException;
	
	/**
	 * This method does the calculation of the distance between two double arrays. The meanings of
	 * the double values might be remembered from the init method.
	 * @param value1
	 * @param value2
	 * @return the distance
	 */
	public abstract double calculateDistance(double[] value1, double[] value2);

	/**
	 * This method does the similarity of the distance between two double arrays. The meanings of
	 * the double values might be remembered from the init method.
	 * @param value1
	 * @param value2
	 * @return the distance
	 */
	public abstract double calculateSimilarity(double[] value1, double[] value2);
	
	/**
	 * This method returns a boolean wheter this measure is a distance measure
	 * @return true if is distance
	 */
	public boolean isDistance() {
		return true;
	}

	/**
	 * This method returns a boolean wheter this measure is a similarity measure
	 * @return true if is similarity
	 */
	public final boolean isSimilarity() {
		return !isDistance();
	}

	/**
	 * This is a convinient method for calculating the distance between examples.
	 * All attributes will be used to form a double array, used for the calculateDistance method. 
	 * @return the distance
	 */
	public final double calculateDistance(Example firstExample, Example secondExample) {
		Attributes attributes = firstExample.getAttributes();
		double[] firstValues = new double[attributes.size()];
		double[] secondValues = new double[attributes.size()];
		
		int i = 0;
		for (Attribute attribute: attributes) {
			firstValues[i] = firstExample.getValue(attribute);
			secondValues[i] = secondExample.getValue(attribute);
			i++;
		}
		
		return calculateDistance(firstValues, secondValues);
	}
	
	/**
	 * This is a convinient method for calculating the similarity between examples.
	 * All attributes will be used to form a double array, used for the calculateDistance method. 
	 * @return the distance
	 */
	public final double calculateSimilarity(Example firstExample, Example secondExample) {
		Attributes attributes = firstExample.getAttributes();
		double[] firstValues = new double[attributes.size()];
		double[] secondValues = new double[attributes.size()];
		
		int i = 0;
		for (Attribute attribute: attributes) {
			firstValues[i] = firstExample.getValue(attribute);
			secondValues[i] = secondExample.getValue(attribute);
			i++;
		}
		
		return calculateSimilarity(firstValues, secondValues);
	}
}
