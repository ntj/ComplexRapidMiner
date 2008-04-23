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
package com.rapidminer.operator.generator;

import com.rapidminer.example.Attribute;
import com.rapidminer.tools.RandomGenerator;


/**
 * A target function which is used for the {@link ExampleSetGenerator} operator. All target function
 * will need an empty constructor since they are initialized via reflection.
 * 
 * @author Ingo Mierswa
 * @version $Id: TargetFunction.java,v 1.1 2007/05/27 21:58:46 ingomierswa Exp $
 */
public interface TargetFunction {

	/**
	 * Will be thrown if an error occurrs during the calculation of the target
	 * function.
	 */
	public static class FunctionException extends Exception {

		private static final long serialVersionUID = -990633489806141677L;

		private String functionName;

		public FunctionException(String functionName, String message) {
			super(message);
			this.functionName = functionName;
		}

		public String getFunctionName() {
			return functionName;
		}
	}

	/** Should be called before the data is created. */
	public void init(RandomGenerator random);

	/** Calculates the target function on arguments. */
	public double calculate(double[] args) throws FunctionException;

	/** Returns the label attribute. */
	public Attribute getLabel();

	/** Creates and returns a number of arguments. */
	public double[] createArguments(int dimension, RandomGenerator random) throws FunctionException;

	/** Sets the lower bound for the arguments. */
	public void setLowerArgumentBound(double lower);

	/** Sets the upper bound for the arguments. */
	public void setUpperArgumentBound(double upper);

	/**
	 * Sets the maximal number of examples. This might be used by some target
	 * functions in order to create proper arguments.
	 */
	public void setTotalNumberOfExamples(int number);

	/**
	 * Sets the maximal number of attributes. This might be used by some target
	 * functions in order to create proper arguments.
	 */
	public void setTotalNumberOfAttributes(int number);

}
