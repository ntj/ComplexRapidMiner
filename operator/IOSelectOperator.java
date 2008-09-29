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
import java.util.List;
import java.util.Set;

import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.OperatorService;


/**
 * <p>This operator allows to choose special IOObjects from the given input. Bringing an IOObject to
 * the front of the input queue allows the next operator to directly perform its action on the
 * selected object. Please note that counting for the parameter value
 * starts with one, but usually the IOObject which was added at last gets the number one, the object
 * added directly before get number two and so on.</p>
 * 
 * <p>The user can specify with the parameter delete_others what will happen to the non-selected input objects
 * of the specified type: if this parameter is set to true, all other IOObjects of the specified type
 * will be removed by this operator. Otherwise (default), the objects will all be kept and the selected
 * objects will just be brought into front.</p>
 *  
 * @author Thomas Harzer, Ingo Mierswa
 * @version $Id: IOSelectOperator.java,v 1.5 2008/07/07 07:06:44 ingomierswa Exp $
 */
public class IOSelectOperator extends Operator {

	public static final String PARAMETER_IO_OBJECT = "io_object";
	
	public static final String PARAMETER_SELECT_WHICH = "select_which";
	
	public static final String PARAMETER_DELETE_OTHERS = "delete_others";
	
	private String[] objectArray = null;

	public IOSelectOperator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		Class<IOObject> clazz = getSelectedClass();
		int number = getParameterAsInt(PARAMETER_SELECT_WHICH);
		IOObject myObject = getInput(clazz, (number - 1));
		if (getParameterAsBoolean(PARAMETER_DELETE_OTHERS)) {
			try {
				while (true) {
					getInput(clazz);
				}
			} catch (MissingIOObjectException e) {}
		}
		return new IOObject[] { myObject };
	}

	private Class<IOObject> getSelectedClass() throws UndefinedParameterError {
		int ioType = getParameterAsInt(PARAMETER_IO_OBJECT);
		if (objectArray != null)
			return OperatorService.getIOObjectClass(objectArray[ioType]);
		else
			return null;
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
		return new Class[] { IOObject.class };
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
		ParameterType type = new ParameterTypeCategory(PARAMETER_IO_OBJECT, "The class of the object(s) which should be removed.", objectArray, 0);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_SELECT_WHICH, "Defines which input object should be selected.", 1, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_DELETE_OTHERS, "Indicates if the other non-selected objects should be deleted.", false));
		return types;
	}
}
