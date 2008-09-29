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
package com.rapidminer.operator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is a description of the (expected) input and (guaranteed) output
 * classes of operators. It provides easy default implementations. <br>
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: IODescription.java,v 1.4 2008/07/07 07:06:44 ingomierswa Exp $
 */
public class IODescription {

	public static final int PASS_UNUSED_INPUT_TO_OUTPUT = 1;

	public static final int DELETE_UNUSED_INPUT = 2;

	// must be ? since it is not allowed to create generic arrays with a
	// specific type
	private Class<?>[] inputClasses;

	private Class<?>[] outputClasses;

	private int outputBehaviour;

	/**
	 * Constructs a new IODescription where all input and output classes are
	 * expected exactly once. Output is the minimum set of output classes, the
	 * real output classes are determined by the outputBehaviour. Both
	 * <tt>input</tt> and <tt>ouput</tt> may be null and may contain a class
	 * more than once.
	 */
	public IODescription(Class[] input, Class[] output, int outputBehaviour) {
		this.inputClasses = input;
		this.outputClasses = output;
		if (inputClasses == null)
			inputClasses = new Class[0];
		if (outputClasses == null)
			outputClasses = new Class[0];
		this.outputBehaviour = outputBehaviour;
	}

	/** Assumes PASS_UNUSED_INPUT_TO_OUTPUT. */
	public IODescription(Class[] input, Class[] output) {
		this(input, output, PASS_UNUSED_INPUT_TO_OUTPUT);
	}

	/** Returns the classes that are expected as input. */
	public Class<?>[] getInputClasses() {
		return inputClasses;
	}

	/**
	 * Returns the output classes dependent on the outputBehaviour
	 * <ul>
	 * <li><tt>PASS_UNUSED_INPUT_TO_OUTPUT:</tt>output classes are the
	 * classes used in the constructor plus those classes in <tt>input[]</tt>
	 * that were not consumed. Classes are supposed to be consumed by the
	 * operator if they find a matching class in the input classes used in the
	 * constructor (which can be a superclass or interface)
	 * <li><tt>DELETE_UNUSED_INPUT:</tt>output classes are exactly those
	 * classes used in the constructor.
	 * </ul>
	 * In either case the output classes precede the unused input classes. Their
	 * order is conserved.
	 */
	public Class<?>[] getOutputClasses(Class<?>[] input, Operator operator) throws IllegalInputException {
		switch (outputBehaviour) {
			case PASS_UNUSED_INPUT_TO_OUTPUT:
				List<Class<?>> outputList = new LinkedList<Class<?>>();
				for (int i = 0; i < input.length; i++) {
					outputList.add(input[i]);
				}
				for (int i = 0; i < inputClasses.length; i++) {
					boolean found = false;
					Iterator<Class<?>> j = outputList.iterator();
					while (j.hasNext()) {
						if (inputClasses[i].isAssignableFrom(j.next())) {
							j.remove();
							found = true;
							break;
						}
					}
					if (!found)
						throw new IllegalInputException(operator, inputClasses[i]);
				}

				for (int i = 0; i < outputClasses.length; i++) {
					outputList.add(outputClasses[i]);
				}

				Class[] outputArray = new Class[outputList.size()];
				outputList.toArray(outputArray);
				return outputArray;
			case DELETE_UNUSED_INPUT:
				return outputClasses;
		}
		return null;
	}

	/** Returns true if oc contains a class which is a superclass of c. */
	public static boolean containsClass(Class<?> c, Class<?>[] oc) {
		for (int i = 0; i < oc.length; i++) {
			if (c.isAssignableFrom(oc[i]))
				return true;
		}
		return false;
	}

}
