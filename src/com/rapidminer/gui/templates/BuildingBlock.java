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
package com.rapidminer.gui.templates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
 * @version $Id: BuildingBlock.java,v 1.1 2007/05/27 22:03:40 ingomierswa Exp $
 */
public class BuildingBlock implements Comparable<BuildingBlock> {

	private String name = "unnamed";

	private String description = "none";

	private String xmlDescription;

	private File buildingBlockFile;
	
	private String iconPath;
	
	public BuildingBlock(File file) throws IOException {
		this.buildingBlockFile = file;
		BufferedReader in = new BufferedReader(new FileReader(buildingBlockFile));
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
		in.close();
	}

	public BuildingBlock(String name, String description, String iconPath, String xmlDescription) {
		this.name = name;
		this.description = description;
		this.iconPath = iconPath;
		this.xmlDescription = xmlDescription;
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
		PrintWriter out = new PrintWriter(new FileWriter(file));
		out.println(name);
		out.println(description);
		out.println(iconPath);
		out.println(xmlDescription);
		out.close();
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
