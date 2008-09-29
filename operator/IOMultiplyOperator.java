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
import java.util.Set;

import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.OperatorService;


/**
 * In some cases you might want to apply different parts of the process on
 * the same input object. You can use this operator to create <code>k</code>
 * copies of the given input object.
 * 
 * @author Ingo Mierswa
 * @version $Id: IOMultiplyOperator.java,v 1.4 2006/03/27 13:21:58 ingomierswa
 *          Exp $
 */
public class IOMultiplyOperator extends Operator {

	public static final String PARAMETER_NUMBER_OF_COPIES = "number_of_copies";
	
	public static final String PARAMETER_IO_OBJECT = "io_object";
	
	public static final String PARAMETER_MULTIPLY_TYPE = "multiply_type";
	
	public static final String PARAMETER_MULTIPLY_WHICH = "multiply_which";
	
	private static final String[] MULTIPLY_TYPES = new String[] { "multiply_one", "multiply_all" };

	private static final int MULTIPLY_ONE = 0;

	private static final int MULTIPLY_ALL = 1;

	private String[] objectArray = null;

	public IOMultiplyOperator(OperatorDescription description) {
		super(description);
	}

	private Class<IOObject> getSelectedClass() throws UndefinedParameterError {
		int ioType = getParameterAsInt(PARAMETER_IO_OBJECT);
		if (objectArray != null)
			return OperatorService.getIOObjectClass(objectArray[ioType]);
		else
			return null;
	}

	public IOObject[] apply() throws OperatorException {
		List<IOObject> result = new LinkedList<IOObject>();
		Class<IOObject> clazz = getSelectedClass();
		int numberOfCopies = getParameterAsInt(PARAMETER_NUMBER_OF_COPIES);
		if (clazz != null) {
			switch (getParameterAsInt(PARAMETER_MULTIPLY_TYPE)) {
				case MULTIPLY_ONE:
					int number = getParameterAsInt(PARAMETER_MULTIPLY_WHICH);
					IOObject ioObject = getInput(clazz, (number - 1));
					addCopies(result, ioObject, numberOfCopies);
					break;
				case MULTIPLY_ALL:
					try {
						while (true) {
							ioObject = getInput(clazz);
							addCopies(result, ioObject, numberOfCopies);
						}
					} catch (MissingIOObjectException e) {}
					break;
			}
		}
		IOObject[] resultArray = new IOObject[result.size()];
		result.toArray(resultArray);
		return resultArray;
	}

	private void addCopies(List<IOObject> result, IOObject ioObject, int numberOfCopies) {
		result.add(ioObject);
		for (int i = 0; i < numberOfCopies; i++) {
			result.add(ioObject.copy());
		}
	}

	public Class<?>[] getInputClasses() {
		Class clazz = null;
		try {
			clazz = getSelectedClass();
		} catch (NullPointerException e) {
			// hack to allow parameter retrieval in getInputClasses before
			// initialization has finished
			// after init (i.e. during process runtime) this method of course
			// works...
		} catch (ArrayIndexOutOfBoundsException e) {
			// dito
		} catch (UndefinedParameterError e) {
			// dito
		}
		if (clazz != null) {
			return new Class[] { clazz };
		} else {
			return new Class[0];
		}
	}

	public Class<?>[] getOutputClasses() {
		return getInputClasses();
	}	
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		Set<String> ioObjects = OperatorService.getIOObjectsNames();
		this.objectArray = new String[ioObjects.size()];
		Iterator<String> i = ioObjects.iterator();
		int index = 0;
		while (i.hasNext()) {
			objectArray[index++] = i.next();
		}
		ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_OF_COPIES, "The number of copies which should be created.", 1, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeCategory(PARAMETER_IO_OBJECT, "The class of the object(s) which should be multiplied.", objectArray, 0);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeCategory(PARAMETER_MULTIPLY_TYPE, "Defines the type of multiplying.", MULTIPLY_TYPES, MULTIPLY_ONE);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_MULTIPLY_WHICH, "Defines which input object should be multiplied (only used for deletion type 'multiply_one').", 1, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
