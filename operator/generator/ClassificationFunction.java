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
 * A target function for classification labels, i.e. non-continous nominal
 * labels.
 * 
 * @author Ingo Mierswa
 * @version $Id: ClassificationFunction.java,v 1.3 2008/05/09 19:22:51 ingomierswa Exp $
 */
public abstract class ClassificationFunction implements TargetFunction {

	protected double lower = -10.0d;

	protected double upper = 10.0d;

	Attribute label = AttributeFactory.createAttribute("label", Ontology.NOMINAL);

	public ClassificationFunction() {
		label.getMapping().mapString("negative");
		label.getMapping().mapString("positive");
	}

	/** Does nothing. */
	public void init(RandomGenerator random) {}

	/** Does nothing. */
	public void setTotalNumberOfExamples(int number) {}

	/** Does nothing. */
	public void setTotalNumberOfAttributes(int number) {}

	public void setLowerArgumentBound(double lower) {
		this.lower = lower;
	}

	public void setUpperArgumentBound(double upper) {
		this.upper = upper;
	}

	public Attribute getLabel() {
		return label;
	}

	public double[] createArguments(int dimension, RandomGenerator random) {
		double[] args = new double[dimension];
		for (int i = 0; i < args.length; i++)
			args[i] = random.nextDoubleInRange(lower, upper);
		return args;
	}
}
