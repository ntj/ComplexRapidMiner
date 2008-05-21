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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import com.rapidminer.operator.meta.branch.ProcessBranch;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeText;
import com.rapidminer.parameter.TextType;

/**
 * This operator simply writed the specified text into the specified file. This can
 * be useful in combination with the {@link ProcessBranch} operator. For example,
 * one could write the success or non-success of a process into the same file
 * depending on the condition specified by a process branch.
 *
 * @author Ingo Mierswa
 * @version $Id: FileEchoOperator.java,v 1.2 2008/05/09 19:23:18 ingomierswa Exp $
 */
public class FileEchoOperator extends Operator {

	public static final String PARAMETER_FILE = "file";
	
	public static final String PARAMETER_TEXT = "text";
	
	
	public FileEchoOperator(OperatorDescription description) {
		super(description);
	}
	
	public IOObject[] apply() throws OperatorException {
		File file = getParameterAsFile(PARAMETER_FILE);
		String text = getParameterAsString(PARAMETER_TEXT);

		PrintWriter out = null;
		try {
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), getEncoding()));
			out.println(text);
		} catch (IOException e) {
			throw new UserError(this, 303, file.getName(), e);
		} finally {
			if (out != null)
				out.close();
		}
		return new IOObject[0];
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return new Class[0];
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_FILE, "The file into which this operator should write the specified text.", "out", false));
		types.add(new ParameterTypeText(PARAMETER_TEXT, "The text which should be written into the file.", TextType.PLAIN, false));
		return types;
	}
}
