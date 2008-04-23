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
package com.rapidminer.gui.tools;

import java.io.File;
import java.util.Arrays;

import javax.swing.filechooser.FileFilter;

/**
 * A file filter for a given set of extensions. This filter matches all files
 * which has one of the given extensions.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: SimpleFileFilter.java,v 1.1 2007/05/27 21:59:31 ingomierswa Exp $
 */
public class SimpleFileFilter extends FileFilter {

	private String[] extensions;

	private String description;

	private int id;

	public SimpleFileFilter(String description, String extension) {
		this(description, extension == null ? null : new String[] { extension }, -1);
	}

	public SimpleFileFilter(String description, String extension, int id) {
		this(description, extension == null ? null : new String[] { extension }, id);
	}

	/**
	 * Creates a FileFilter that filters based on a list of extensions.
	 * 
	 * @param id
	 *            Can be used to identify the filter
	 */
	public SimpleFileFilter(String description, String[] extensions, int id) {
		this.description = description;
		this.extensions = extensions;
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public boolean accept(File f) {
		if (f.isDirectory())
			return true;
		if (extensions == null)
			return true;
		for (int i = 0; i < extensions.length; i++)
			if (f.getName().endsWith(extensions[i]))
				return true;
		return false;
	}

	public int getId() {
		return id;
	}
	
	public String getExtension() {
		if ((extensions != null) && (extensions.length == 1)) {
			return extensions[0];
		} else {
			return null;
		}
	}
	
	public String toString() {
		return "File filter for " + Arrays.asList(extensions) + " (" + getDescription() + ")";
	}
}
