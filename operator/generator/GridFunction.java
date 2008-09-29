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
package com.rapidminer.operator.generator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;


/** Generates a uniformly distributed grid in the given dimensions. 
 *  
 *  @author Ingo Mierswa
 *  @version $Id: GridFunction.java,v 1.3 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class GridFunction implements TargetFunction {

	private double lowerBound = -10.0d;

	private double upperBound = 10.0d;

	private int numberOfExamples;

	private int numberOfAttributes;

	private int[] counter;

	private int maxCounter;

	private double dimDistance;

	public void init(RandomGenerator random) {
		this.counter = new int[numberOfAttributes];
		this.maxCounter = (int) Math.round(Math.exp(Math.log(numberOfExamples) / numberOfAttributes));
		this.dimDistance = (upperBound - lowerBound) / this.maxCounter;
	}

	public void setTotalNumberOfExamples(int number) {
		this.numberOfExamples = number;
	}

	public void setTotalNumberOfAttributes(int number) {
		this.numberOfAttributes = number;
	}

	public void setLowerArgumentBound(double lower) {
		this.lowerBound = lower;
	}

	public void setUpperArgumentBound(double upper) {
		this.upperBound = upper;
	}

	public Attribute getLabel() {
		return AttributeFactory.createAttribute("label", Ontology.REAL);
	}

	public double calculate(double[] att) throws FunctionException {
		return 0.0d;
	}

	public double[] createArguments(int number, RandomGenerator random) throws FunctionException {
		if (number <= 0)
			throw new FunctionException("Grid function", "must have at least one attribute!");
		double[] args = new double[number];
		for (int i = 0; i < args.length; i++) {
			args[i] = lowerBound + (dimDistance / 2.0d) + counter[i] * dimDistance;
		}
		incrementCounter(counter, 0);
		return args;
	}

	private void incrementCounter(int[] counter, int pos) {
		counter[pos]++;
		if (counter[pos] >= maxCounter) {
			counter[pos] = 0;
			if (pos < counter.length - 1) {
				incrementCounter(counter, pos + 1);
			}
		}
	}
}
