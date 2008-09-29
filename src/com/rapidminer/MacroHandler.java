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
package com.rapidminer;

import java.util.HashMap;
import java.util.Map;

/**
 * This class can be used to store macros for an process which can be defined
 * by the operator {@link com.rapidminer.operator.MacroDefinitionOperator}. It also
 * defines some standard macros like the process path or file name.
 * 
 * @author Ingo Mierswa
 * @version $Id: MacroHandler.java,v 1.5 2008/06/09 23:40:52 ingomierswa Exp $
 */
public class MacroHandler {

	// TODO: remove experiment macros later
	private static final String[] PREDEFINED_MACROS = {
		"experiment_name",
		"experiment_file",
		"experiment_path",
		"process_name",
		"process_file",
		"process_path"
	};
	
	// TODO: remove experiment constants later
	private static final int EXPERIMENT_NAME = 0;
	private static final int EXPERIMENT_FILE = 1;
	private static final int EXPERIMENT_PATH = 2;
	private static final int PROCESS_NAME = 3;
	private static final int PROCESS_FILE = 4;
	private static final int PROCESS_PATH = 5;
	
	private Process process;
	
	private Map<String, String> macroMap = new HashMap<String, String>();
	
	public MacroHandler(Process process) {
		this.process = process;
	}
	
	public void clear() {
		this.macroMap.clear();
	}
	
	public void addMacro(String macro, String value) {
		this.macroMap.put(macro, value);
	}

	public void removeMacro(String macro) {
		this.macroMap.remove(macro);
	}

	public String getMacro(String macro) {
		for (int i = 0; i < PREDEFINED_MACROS.length; i++) {
			if (PREDEFINED_MACROS[i].equals(macro)) {
				switch (i) {
				case EXPERIMENT_NAME:
				case PROCESS_NAME:
					String fileName = process.getProcessFile().getName();
					return fileName.substring(0, fileName.lastIndexOf("."));
				case EXPERIMENT_FILE:
				case PROCESS_FILE:
					return process.getProcessFile().getName();
				case EXPERIMENT_PATH:
				case PROCESS_PATH:
					return process.getProcessFile().getAbsolutePath();
				}
			}
		}
		return this.macroMap.get(macro);
	}
	
	public String toString() {
		return this.macroMap.toString();
	}
}
