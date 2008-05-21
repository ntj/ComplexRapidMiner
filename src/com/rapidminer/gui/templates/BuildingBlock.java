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

import com.rapidminer.tools.LogService;

/**
 * A building block consisting of a name, a short description, and the XML description 
 * for the building block. Templates must look like this:
 * 
 * <pre>
 *   one line for the name
 *   one line of html description
 *   one line for the XML description
 * </pre>
 * 
 * @author Ingo Mierswa
 * @version $Id: BuildingBlock.java,v 1.6 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class BuildingBlock implements Comparable<BuildingBlock> {

	private String name = "unnamed";

	private String description = "none";

	private String xmlDescription;

	private File buildingBlockFile;
	
	private String iconPath;
	
	public BuildingBlock(File file) throws InstantiationException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			init(in);
			this.buildingBlockFile = file;
		} catch (IOException e) {
			LogService.getGlobal().logError("Cannot read building block file: " + e.getMessage());
			throw new InstantiationException("Cannot instantiate building block: " + e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LogService.getGlobal().logError("Cannot close stream to building block file: " + e.getMessage());
				}
			}
		}
	}

	public BuildingBlock(BufferedReader in) throws IOException {
		init(in);
	}
	
	public BuildingBlock(String name, String description, String iconPath, String xmlDescription) {
		this.name = name;
		this.description = description;
		this.iconPath = iconPath;
		this.xmlDescription = xmlDescription;
	}

	private void init(BufferedReader in) throws IOException {
		this.name = in.readLine();
		this.description = in.readLine();
		this.iconPath = in.readLine();
		// rest is XML
		String line = null;
		StringBuffer result = new StringBuffer();
		while ((line = in.readLine()) != null) {
			result.append(line);
		}
		this.xmlDescription = result.toString();		
	}
	
	public File getFile() {
		return buildingBlockFile;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getIconPath() {
		return iconPath;
	}
	
	public String getXML() {
		return xmlDescription;
	}
	
	public void save(File file) throws IOException {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(file));
			out.println(name);
			out.println(description);
			out.println(iconPath);
			out.println(xmlDescription);
		} catch (IOException e) {
			throw e;
		} finally {
			if (out != null) {
				out.close();		
			}
		}
	}
	
	public String toString() {
		return name;
	}
	
	public int compareTo(BuildingBlock buildingBlock) {
		return name.compareTo(buildingBlock.name);
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof BuildingBlock)) {
			return false;
		} else {
			return this.name.equals(((BuildingBlock)o).name);
		}
	}
	
	public int hashCode() {
		return this.name.hashCode();
	}
}
