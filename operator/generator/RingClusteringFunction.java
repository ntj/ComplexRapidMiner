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


/**
 * Generates a data set for two attributes. The data build three rings which
 * are around each other.
 * 
 * @author Ingo Mierswa
 * @version $Id: RingClusteringFunction.java,v 1.3 2008/05/09 19:22:50 ingomierswa Exp $
 */
public class RingClusteringFunction implements TargetFunction {

	private double bound = 10.0d;

	private Attribute label = AttributeFactory.createAttribute("label", Ontology.NOMINAL);

    private RandomGenerator random;
    
	public RingClusteringFunction() {
		label.getMapping().mapString("core");
		label.getMapping().mapString("first_ring");
		label.getMapping().mapString("second_ring");
	}

	/** Does nothing. */
	public void init(RandomGenerator random) {
	    this.random = random;
    }

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
		return label;
	}

	public double calculate(double[] att) throws FunctionException {
		if (att.length != 2)
			throw new FunctionException("Ring clustering function", "must have 2 attributes!");
		if (random.nextDouble() < 0.05) {
			int type = random.nextInt(3);
			switch (type) {
				case 0:
					return getLabel().getMapping().mapString("core");
				case 1:
					return getLabel().getMapping().mapString("first_ring");
				case 2:
					return getLabel().getMapping().mapString("second_ring");
				default:
					return getLabel().getMapping().mapString("core");
			}
		} else {
			double radius = Math.sqrt(att[0] * att[0] + att[1] * att[1]);
			if (radius < bound / 3.0d)
				return getLabel().getMapping().mapString("core");
			else if (radius < 2 * bound / 3.0d)
				return getLabel().getMapping().mapString("first_ring");
			else
				return getLabel().getMapping().mapString("second_ring");
		}
	}

	public double[] createArguments(int number, RandomGenerator random) throws FunctionException {
		if (number != 2)
			throw new FunctionException("Ring clustering function", "must have 2 attributes!");
		double[] args = new double[number];
		int type = random.nextInt(3);
		double radius = 0.0d;
		switch (type) {
			case 0:
				radius = random.nextGaussian();
				break;
			case 1:
				radius = bound / 2.0d + random.nextGaussian();
				break;
			case 2:
				radius = bound + random.nextGaussian();
				break;
			default:
				radius = random.nextGaussian();
				break;
		}
		double angle = random.nextDouble() * 2 * Math.PI;
		args[0] = radius * Math.cos(angle);
		args[1] = radius * Math.sin(angle);
		return args;
	}
}
