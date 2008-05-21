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
import com.rapidminer.tools.RandomGenerator;


/**
 * Generates an Association function transaction dataset. The first four
 * attributes are correlated.
 * 
 * @author Ingo Mierswa
 * @version $Id: TransactionDatasetFunction.java,v 1.4 2008/05/09 19:22:50 ingomierswa Exp $
 */
public class TransactionDatasetFunction implements TargetFunction {

	/** Does nothing. */
	public void init(RandomGenerator random) {}

	/** Does nothing. */
	public void setTotalNumberOfExamples(int number) {}

	/** Does nothing. */
	public void setTotalNumberOfAttributes(int number) {}

	/** Since circles are used the upper and lower bounds must be the same. */
	public void setLowerArgumentBound(double lower) {}

	public void setUpperArgumentBound(double upper) {}

	public Attribute getLabel() {
		return null;
	}

	public double calculate(double[] att) throws FunctionException {
		if (att.length < 5)
			throw new FunctionException("Association function", "needs at least 5 attributes!");
		return Double.NaN;
	}

	public double[] createArguments(int number, RandomGenerator random) throws FunctionException {
		if (number < 5)
			throw new FunctionException("Association function", "needs at least 5 attributes!");
		double[] args = new double[number];
		for (int i = 0; i < args.length; i++) {
			args[i] = random.nextDouble() < 0.1 ? 1 : 0;
		}
		if ((args[1] == 1) && (random.nextDouble() < 0.8))
			args[3] = 1;
		if ((args[0] == 1) && (random.nextDouble() < 0.9))
			args[1] = 1;
		if ((args[0] == 1) && (random.nextDouble() < 0.7))
			args[2] = 1;
		return args;
	}
}
