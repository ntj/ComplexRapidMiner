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
import java.util.zip.GZIPOutputStream;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.tools.XMLSerialization;


/**
 * Writes all elements of the current {@link IOContainer}, i.e. all objects
 * passed to this operator, to a file. Although this operator uses an XML serialization 
 * mechanism, the files produced for different RapidMiner versions might not be compatible. At least 
 * different Java versions should not be a problem anymore.
 * 
 * @see com.rapidminer.operator.IOContainer
 * 
 * @author Stefan Rueping, Ingo Mierswa
 * @version $Id: IOContainerWriter.java,v 1.6 2006/03/27 13:22:01 ingomierswa
 *          Exp $
 */
public class IOContainerWriter extends Operator {


	/** The parameter name for &quot;Name of file to write the output to.&quot; */
	public static final String PARAMETER_FILENAME = "filename";

	/** The parameter name for &quot;Indicates if the file content should be zipped.&quot; */
	public static final String PARAMETER_ZIPPED = "zipped";
	public IOContainerWriter(OperatorDescription description) {
		super(description);
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return new Class[0];
	}

	public IOObject[] apply() throws OperatorException {
		IOContainer input = getInput();
		File file = getParameterAsFile(PARAMETER_FILENAME);
		OutputStream out = null;
		try {
			if (getParameterAsBoolean(PARAMETER_ZIPPED)) {
				out = new GZIPOutputStream(new FileOutputStream(file));
			} else {
				out = new FileOutputStream(file);
			}
			XMLSerialization.getXMLSerialization().writeXML(input, out);
			log(getName() + ": Input IOContainer written to file" + file);
		} catch (IOException e) {
			throw new UserError(this, e, 303, new Object[] { file, e.getMessage() });
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logError("Cannot close stream to file " + file);
				}
			}
		}
		return (new IOObject[0]);
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeFile(PARAMETER_FILENAME, "Name of file to write the output to.", "ioc", false);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_ZIPPED, "Indicates if the file content should be zipped.", true));		
		return types;
	}
}
