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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
 * Writes a parameter set into a file. This can be created by one of the
 * parameter optimization operators, e.g.
 * {@link com.rapidminer.operator.meta.GridSearchParameterOptimizationOperator}.
 * It can then be applied to the operators of the process using a
 * {@link com.rapidminer.operator.meta.ParameterSetter}.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: ParameterSetWriter.java,v 1.6 2006/03/27 13:22:01 ingomierswa
 *          Exp $
 */
public class ParameterSetWriter extends Operator {


	/** The parameter name for &quot;A file containing a parameter set.&quot; */
	public static final String PARAMETER_PARAMETER_FILE = "parameter_file";
	private static final Class[] INPUT_CLASSES = new Class[] { ParameterSet.class };

	public ParameterSetWriter(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ParameterSet parameterSet = getInput(ParameterSet.class);
		File parameterFile = getParameterAsFile(PARAMETER_PARAMETER_FILE);
        PrintWriter out = null;
		try {
            out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(parameterFile), getEncoding()));
			parameterSet.writeParameterSet(out, getEncoding());
		} catch (IOException e) {
			throw new UserError(this, 303, e, new Object[] { parameterFile, e.getMessage() });
		} finally {
            if (out != null)
                out.close();
        }
		return new IOObject[] { parameterSet };
	}

	public Class<?>[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class<?>[] getOutputClasses() {
		return INPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_PARAMETER_FILE, "A file containing a parameter set.", "par", false));
		return types;
	}

}
