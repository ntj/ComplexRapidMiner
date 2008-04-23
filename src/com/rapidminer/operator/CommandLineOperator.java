/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Tools;


/**
 * This operator executes a system command. The command and all its arguments
 * are specified by the parameter <code>command</code>. The standard output
 * stream and the error stream of the process can be redirected to the logfile. <br/>
 * Please note also that the command is system dependent. Characters that have
 * special meaning on the shell like e.g. the pipe symbol or brackets and braces
 * do not have a special meaning to Java. <br/> The method
 * <code>Runtime.exec(String)</code> is used to execute the command. Please
 * note, that this (Java) method parses the string into tokens before it is
 * executed. These tokens are <em>not</em> interpreted by a shell (which?). If
 * the desired command involves piping, redirection or other shell features, it
 * is best to create a small shell script to handle this.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: CommandLineOperator.java,v 2.15 2006/03/27 13:21:58 ingomierswa
 *          Exp $
 */
public class CommandLineOperator extends Operator {

	public static final String PARAMETER_COMMAND = "command";
	
	public static final String PARAMETER_LOG_STDOUT = "log_stdout";
		
	public static final String PARAMETER_LOG_STDERR = "log_stderr";
	
	private static final Class[] INPUT_CLASSES = {};

	private static final Class[] OUTPUT_CLASSES = {};

	public CommandLineOperator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		String command = getParameterAsString(PARAMETER_COMMAND);
		boolean logOut = getParameterAsBoolean(PARAMETER_LOG_STDOUT);
		boolean logErr = getParameterAsBoolean(PARAMETER_LOG_STDERR);

		try {
			Process process = Runtime.getRuntime().exec(command);
			if (logErr)
				logOutput("stderr:", process.getErrorStream());
			if (logOut)
				logOutput("stdout:", process.getInputStream());
			Tools.waitForProcess(this, process, command);
			log("Program exited succesfully.");
		} catch (IOException e) {
			throw new UserError(this, e, 310, new Object[] { command, e.getMessage() });
		}
		return new IOObject[0];
	}

	/** Sends the output to the LogService. */
	private void logOutput(String message, InputStream in) throws IOException {
		BufferedReader bin = new BufferedReader(new InputStreamReader(in));
		String line = null;
		StringBuffer buffer = new StringBuffer(message);
		while ((line = bin.readLine()) != null) {
			buffer.append(Tools.getLineSeparator());
			buffer.append(line);
		}
		logNote(buffer.toString());
	}

	/** no input */
	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	/** no output */
	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}	
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_COMMAND, "Command to execute.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_LOG_STDOUT, "If set to true, the stdout stream of the command is redirected to the logfile.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_LOG_STDERR, "If set to true, the stderr stream of the command is redirected to the logfile.", true));
		return types;
	}

}
