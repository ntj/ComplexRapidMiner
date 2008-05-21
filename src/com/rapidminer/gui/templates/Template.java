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
package com.rapidminer.gui.templates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.tools.LogService;

/**
 * A template process consisting of name, short description, a name for an
 * process file and a list parameters given as String pairs (operator, key).
 * Templates must look like this:
 * 
 * <pre>
 *   one line for the name
 *   one line of html description
 *   one line for the process file name
 *   Rest of the file: some important parameters in the form operatorname.parametername
 * </pre>
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: Template.java,v 1.6 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class Template {

	private String name = "unnamed";

	private String description = "none";

	private String configFile;

	private List<String[]> parameters = new LinkedList<String[]>();

	private File templateFile = null;

	public Template() {}

	public Template(File file) throws InstantiationException {
		this.templateFile = file;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(templateFile));
			name = in.readLine();
			description = in.readLine();
			configFile = in.readLine();
			String line = null;
			while ((line = in.readLine()) != null) {
				parameters.add(line.split("\\."));
			}
		} catch (IOException e) {
			LogService.getGlobal().logError("Cannot read template file: " + file);
			throw new InstantiationException("Cannot read template file: " + file);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LogService.getGlobal().logError("Cannot close stream to template file: " + e.getMessage());
				}
			}
		}
	}

	public Template(String name, String description, String configFile, List<String[]> parameters) {
		this.name = name;
		this.description = description;
		this.configFile = configFile;
		this.parameters = parameters;
	}

	public File getFile() {
		return templateFile;
	}

	public String getFilename() {
		return configFile;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<String[]> getParameters() {
		return parameters;
	}

	public String toHTML() {
		return "<b>" + name + "</b><br />" + description;
	}

	public void save(File file) throws IOException {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(file));
			out.println(name);
			out.println(description);
			out.println(configFile);
			Iterator<String[]> i = parameters.iterator();
			while (i.hasNext()) {
				String[] pair = i.next();
				out.println(pair[0] + "." + pair[1]);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (out != null) {
				out.close();		
			}
		}
	}
}
