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
package com.rapidminer.operator.meta;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.rapidminer.Process;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.tools.XMLException;


/** This operator can be used to embed a complete process definition into the current 
 *  process definition. 
 *  The process must have been written into a file before and will be loaded and 
 *  executed when the current process reaches this operator. Optionally, the input
 *  of this operator can be used as input for the embedded process. In both cases,
 *  the output of the process will be delivered as output of this operator. Please note
 *  that validation checks will not work for process containing an operator of this
 *  type since the check cannot be performed without actually loading the process.
 * 
 * @author Ingo Mierswa
 * @version $Id: ProcessEmbeddingOperator.java,v 1.4 2008/05/09 19:22:38 ingomierswa Exp $
 */
public class ProcessEmbeddingOperator extends Operator {


	/** The parameter name for &quot;The process file which should be encapsulated by this operator&quot; */
	public static final String PARAMETER_PROCESS_FILE = "process_file";

	/** The parameter name for &quot;Indicates if the operator input should be used as input of the process&quot; */
	public static final String PARAMETER_USE_INPUT = "use_input";
	public ProcessEmbeddingOperator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		File processFile = getParameterAsFile(PARAMETER_PROCESS_FILE);
		Process process = null;
		try {
			process = new Process(processFile);
		} catch (IOException e) {
			throw new UserError(this, 302, processFile, e.getMessage());
		} catch (XMLException e) {
			throw new UserError(this, 401, e.getMessage());
		}
		
		IOContainer result = null;
		if (getParameterAsBoolean(PARAMETER_USE_INPUT))
			result = process.run(getInput());
		else
			result = process.run();
		return result.getIOObjects();
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return new Class[0];
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_PROCESS_FILE, "The process file which should be encapsulated by this operator", "xml", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_INPUT, "Indicates if the operator input should be used as input of the process", false));
		return types;
	}
}
