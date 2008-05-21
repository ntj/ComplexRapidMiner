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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.meta.ParameterSet;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;


/**
 * Reads a set of parameters from a file that was written by a
 * {@link com.rapidminer.operator.meta.ParameterOptimizationOperator}. It can
 * then be applied to the operators of the process using a
 * {@link com.rapidminer.operator.meta.ParameterSetter}.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: ParameterSetLoader.java,v 1.12 2006/03/27 13:22:01 ingomierswa
 *          Exp $
 */
public class ParameterSetLoader extends Operator {


	/** The parameter name for &quot;A file containing a parameter set.&quot; */
	public static final String PARAMETER_PARAMETER_FILE = "parameter_file";
	private static final Class[] OUTPUT_CLASSES = new Class[] { ParameterSet.class };

	public ParameterSetLoader(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ParameterSet parameterSet = null;
		File parameterFile = getParameterAsFile(PARAMETER_PARAMETER_FILE);
		InputStream in = null;
		try {
            in = new FileInputStream(parameterFile);
			parameterSet = ParameterSet.readParameterSet(in);
		} catch (IOException e) {
			throw new UserError(this, 302, e, new Object[] { parameterFile, e.getMessage() });
		} finally {
			if (in != null) {
	            try {
					in.close();
				} catch (IOException e) {
					logError("Cannot close stream to file " + parameterFile);
				}
			}
		}

		return new IOObject[] { parameterSet };
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_PARAMETER_FILE, "A file containing a parameter set.", "par", false));
		return types;
	}

}
