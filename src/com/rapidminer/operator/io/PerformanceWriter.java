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
import java.io.OutputStream;
import java.util.List;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;


/**
 * Writes the input performance vector in a given file. You also might want to
 * use the {@link com.rapidminer.operator.io.ResultWriter} operator which
 * writes all current results in the main result file.
 * 
 * @author Ingo Mierswa
 * @version $Id: PerformanceWriter.java,v 1.9 2006/03/27 13:22:01 ingomierswa
 *          Exp $
 */
public class PerformanceWriter extends Operator {


	/** The parameter name for &quot;Filename for the performance file.&quot; */
	public static final String PARAMETER_PERFORMANCE_FILE = "performance_file";
	public PerformanceWriter(OperatorDescription description) {
		super(description);
	}

	/** Writes the attribute set to a file. */
	public IOObject[] apply() throws OperatorException {
		File performanceFile = getParameterAsFile(PARAMETER_PERFORMANCE_FILE);
		PerformanceVector performance = getInput(PerformanceVector.class);

		OutputStream out = null;
		try {
			out = new FileOutputStream(performanceFile);
			performance.write(out);
		} catch (IOException e) {
			throw new UserError(this, e, 303, new Object[] { performanceFile, e.getMessage() });
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logError("Cannot close stream to file " + performanceFile);
				}
			}
		}

		return new IOObject[] { performance };
	}

	public Class[] getInputClasses() {
		return new Class[] { PerformanceVector.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { PerformanceVector.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_PERFORMANCE_FILE, "Filename for the performance file.", "per", false));
		return types;
	}
}
