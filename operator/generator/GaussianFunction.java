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


/** Generates a gaussian distribution for all attributes. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: GaussianFunction.java,v 1.3 2008/05/09 19:22:50 ingomierswa Exp $
 */
public class GaussianFunction implements TargetFunction {

	private double bound = 10.0d;

	/** Does nothing. */
	public void init(RandomGenerator random) {}

	/** Does nothing. */
	public void setTotalNumberOfExamples(int number) {}

	/** Does nothing. */
	public void setTotalNumberOfAttributes(int number) {}

	/** Since circles are used the upper and lower bounds must be the same. */
	public void setLowerArgumentBound(double lower) {
		this.bound = Math.max(this.bound, Math.abs(lower));
	}

	public void setUpperArgumentBound(double upper) {
		this.bound = Math.max(this.bound, Math.abs(upper));
	}

	public Attribute getLabel() {
		return AttributeFactory.createAttribute("label", Ontology.REAL);
	}

	public double calculate(double[] att) throws FunctionException {
		return 0.0d;
	}

	public double[] createArguments(int number, RandomGenerator random) throws FunctionException {
		double[] args = new double[number];
		for (int i = 0; i < args.length; i++)
			args[i] = random.nextGaussian() * bound;
		return args;
	}
}
