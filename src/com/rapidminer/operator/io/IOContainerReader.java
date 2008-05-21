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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.XMLSerialization;


/**
 * Reads all elements of an {@link IOContainer} from a file. The file must be written
 * by an {@link IOContainerWriter}.
 * 
 * The operator additionally supports to read text from a logfile, which will be
 * given to the RapidMiner {@link LogService}. Hence, if you add a IOContainerWriter
 * to the end of an process and set the logfile in the process root
 * operator, the output of applying the IOContainerReader will be quite similar
 * to what the original process displayed.
 * 
 * @see com.rapidminer.operator.IOContainer
 * 
 * @author Stefan Rueping, Ingo Mierswa
 * @version $Id: IOContainerReader.java,v 1.6 2006/03/27 13:22:01 ingomierswa
 *          Exp $
 */
public class IOContainerReader extends Operator {


	/** The parameter name for &quot;Name of file to write the output to.&quot; */
	public static final String PARAMETER_FILENAME = "filename";

	/** The parameter name for &quot;Append or prepend the contents of the file to this operators input or replace this operators input?&quot; */
	public static final String PARAMETER_METHOD = "method";

	/** The parameter name for &quot;Name of file to read log information from (optional).&quot; */
	public static final String PARAMETER_LOGFILE = "logfile";
	public static final String[] METHODS = { "append", "prepend", "replace" };

	public static final int APPEND = 0;

	public static final int PREPEND = 1;

	public static final int REPLACE = 2;

	public IOContainerReader(OperatorDescription description) {
		super(description);
	}

	public Class[] getInputClasses() {
		return new Class[0];
	};

	public Class[] getOutputClasses() {
		return new Class[0];
	};

	public IOObject[] apply() throws OperatorException {
		IOContainer input = getInput();
		File file = getParameterAsFile(PARAMETER_FILENAME);

		// read IOContainer
		InputStream in;
		try {
			in = new GZIPInputStream(new FileInputStream(file));
		} catch (IOException e1) {
			try {
				// maybe already uncompressed?
				in = new FileInputStream(file);
			} catch (IOException e) {
				throw new UserError(this, e, 302, new Object[] { file, e.getMessage() });
			}
		}
		IOContainer content = null;
		try {
			content = (IOContainer)XMLSerialization.getXMLSerialization().fromXML(in);
			in.close();
		} catch (IOException e) {
			throw new UserError(this, e, 302, new Object[] { file, e.getMessage() });
		}
		
		// append or prepend input objects of this operator
		switch (getParameterAsInt(PARAMETER_METHOD)) {
			case APPEND:
				content = content.append(input.getIOObjects());
				break;
			case PREPEND:
				content = content.prepend(input.getIOObjects());
				break;
		}

		// read and add log messages
		if (getParameterAsString(PARAMETER_LOGFILE) != null) {
			File logFile = getParameterAsFile(PARAMETER_LOGFILE);
			BufferedReader logIn = null;
			try {
				logIn = new BufferedReader(new FileReader(logFile));
				String line = null;
				while ((line = logIn.readLine()) != null) {
					log(line);
				}
			} catch (IOException e) {
				// only warn
				logWarning("Could not read from logfile: " + e.toString());
			} finally {
				if (logIn != null) {
					try {
						logIn.close();
					} catch (IOException e) {
						logError("Cannot close stream to file " +  logFile);
					}
				}
			}
		}

		return content.getIOObjects();
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeFile(PARAMETER_FILENAME, "Name of file to write the output to.", "ioc", false);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeCategory(PARAMETER_METHOD, "Append or prepend the contents of the file to this operators input or replace this operators input?", METHODS, APPEND);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeFile(PARAMETER_LOGFILE, "Name of file to read log information from (optional).", "log", true);
		types.add(type);
		return types;
	}
}
