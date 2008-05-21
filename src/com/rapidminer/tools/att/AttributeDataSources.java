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
package com.rapidminer.tools.att;

import java.io.File;
import java.util.List;

/**
 * A container class for all attribute data sources and a default source file.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: AttributeDataSources.java,v 2.10 2006/03/23 17:48:25
 *          ingomierswa Exp $
 */
public class AttributeDataSources {

	/** A list of attribute data sources. */
	private List<AttributeDataSource> attributeDataSources;

	/** The default source file. */
	private File defaultSource;

	public AttributeDataSources(List<AttributeDataSource> attributeDataSources, File defaultSource) {
		this.attributeDataSources = attributeDataSources;
		this.defaultSource = defaultSource;
	}

	public List<AttributeDataSource> getDataSources() {
		return attributeDataSources;
	}

	public File getDefaultSource() {
		return defaultSource;
	}

	public String toString() {
		return attributeDataSources.toString() + ", default file: " + defaultSource;
	}
}
