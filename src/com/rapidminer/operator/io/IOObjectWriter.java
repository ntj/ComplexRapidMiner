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
package com.rapidminer.operator.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.OperatorService;

import static com.rapidminer.operator.io.OutputTypes.*;

/**
 * Generic writer for all types of IOObjects. Writes one of the input objects into a given file. 
 * 
 * @author Ingo Mierswa
 * @version $Id: IOObjectWriter.java,v 1.7 2008/05/09 19:22:37 ingomierswa Exp $
 */
public class IOObjectWriter extends Operator {


	/** The parameter name for &quot;Filename of the object file.&quot; */
	public static final String PARAMETER_OBJECT_FILE = "object_file";

	/** The parameter name for &quot;The class of the object(s) which should be saved.&quot; */
	public static final String PARAMETER_IO_OBJECT = "io_object";

	/** The parameter name for &quot;Defines which input object should be written.&quot; */
	public static final String PARAMETER_WRITE_WHICH = "write_which";

	/** The parameter name for &quot;Indicates the type of the output&quot; */
	public static final String PARAMETER_OUTPUT_TYPE = "output_type";

	public static final String PARAMETER_CONTINUE_ON_ERROR = "continue_on_error";
	private String[] objectArray = null;
	
	public IOObjectWriter(OperatorDescription description) {
		super(description);
	}

	private Class<IOObject> getSelectedClass() throws UndefinedParameterError {
		int ioType = getParameterAsInt(PARAMETER_IO_OBJECT);
		if (objectArray != null)
			return OperatorService.getIOObjectClass(objectArray[ioType]);
		else
			return null;
	}
	
	/** Writes the attribute set to a file. */
	public IOObject[] apply() throws OperatorException {
		Class<IOObject> clazz = getSelectedClass();
		if (clazz != null) {
			int number = getParameterAsInt(PARAMETER_WRITE_WHICH);
			IOObject object = getInput().get(clazz, (number - 1));
			File objectFile = getParameterAsFile(PARAMETER_OBJECT_FILE);

			int outputType = getParameterAsInt(PARAMETER_OUTPUT_TYPE);
			switch (outputType) {
			case OUTPUT_TYPE_XML:
				OutputStream out = null;
				try {
					out = new FileOutputStream(objectFile);
					object.write(out);
				} catch (IOException e) {
					if (!getParameterAsBoolean(PARAMETER_CONTINUE_ON_ERROR)) {
						throw new UserError(this, e, 303, new Object[] { objectFile, e.getMessage() });
					} else {
						logError("Could not write IO Object to file " + objectFile + ": " + e.getMessage());
					}
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							logError("Cannot close stream to file " + objectFile);
						}
					}
				}
				break;
			case OUTPUT_TYPE_XML_ZIPPED:
				out = null;
				try {
					out = new GZIPOutputStream(new FileOutputStream(objectFile));
					object.write(out);
				} catch (IOException e) {
					if (!getParameterAsBoolean(PARAMETER_CONTINUE_ON_ERROR)) {
						throw new UserError(this, e, 303, new Object[] { objectFile, e.getMessage() });
					} else {
						logError("Could not write IO Object to file " + objectFile + ": " + e.getMessage());
					}
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							logError("Cannot close stream to file " + objectFile);
						}
					}
				}
				break;
			case OUTPUT_TYPE_BINARY:
				ObjectOutputStream objectOut = null;
				try {
					objectOut = new ObjectOutputStream(new FileOutputStream(objectFile));
					objectOut.writeObject(object);
				} catch (IOException e) {
					if (!getParameterAsBoolean(PARAMETER_CONTINUE_ON_ERROR)) {
						throw new UserError(this, e, 303, new Object[] { objectFile, e.getMessage() });
					} else {
						logError("Could not write IO Object to file " + objectFile + ": " + e.getMessage());
					}
				} finally {
					if (objectOut != null) {
						try {
							objectOut.close();
						} catch (IOException e) {
							logError("Cannot close stream to file " + objectFile);
						}
					}
				}
				break;
			default:
				break;
			}	
		}
		return new IOObject[0];
	}

	public Class[] getInputClasses() {
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
	
	public Class[] getOutputClasses() {
		return getInputClasses();
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_OBJECT_FILE, "Filename of the object file.", "ioo", false));
		Set<String> ioObjects = OperatorService.getIOObjectsNames();
		this.objectArray = new String[ioObjects.size()];
		Iterator<String> i = ioObjects.iterator();
		int index = 0;
		while (i.hasNext()) {
			objectArray[index++] = i.next();
		}
		ParameterType type = new ParameterTypeCategory(PARAMETER_IO_OBJECT, "The class of the object(s) which should be saved.", objectArray, 0);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_WRITE_WHICH, "Defines which input object should be written.", 1, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);
		
        types.add(new ParameterTypeCategory(PARAMETER_OUTPUT_TYPE, "Indicates the type of the output", OUTPUT_TYPES, OutputTypes.OUTPUT_TYPE_XML_ZIPPED));
		types.add(new ParameterTypeBoolean(PARAMETER_CONTINUE_ON_ERROR, "Defines behavior on errors", false));
        return types;
	}
}
