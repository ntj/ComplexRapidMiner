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

import com.rapidminer.operator.AbstractIOObject;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;


/**
 * Reads a performance vector from a given file. This performance vector must
 * have been written before with a {@link PerformanceWriter}.
 * 
 * @author Ingo Mierswa
 * @version $Id: PerformanceLoader.java,v 1.4 2006/03/27 13:22:01 ingomierswa
 *          Exp $
 */
public class PerformanceLoader extends Operator {


	/** The parameter name for &quot;Filename for the performance file.&quot; */
	public static final String PARAMETER_PERFORMANCE_FILE = "performance_file";
	public PerformanceLoader(OperatorDescription description) {
		super(description);
	}

	/** Writes the attribute set to a file. */
	public IOObject[] apply() throws OperatorException {
		File performanceFile = getParameterAsFile(PARAMETER_PERFORMANCE_FILE);
		PerformanceVector performance = null;
		try {
			InputStream in = new FileInputStream(performanceFile);
			performance = (PerformanceVector)AbstractIOObject.read(in);
			in.close();
		} catch (IOException e) {
			throw new UserError(this, e, 303, new Object[] { performanceFile, e.getMessage() });
		}

		return new IOObject[] { performance };
	}

	public Class<?>[] getInputClasses() {
		return new Class[0];
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { PerformanceVector.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_PERFORMANCE_FILE, "Filename for the performance file.", "per", false));
		return types;
	}
}
