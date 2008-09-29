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
package com.rapidminer.parameter;


/**
 * A parameter type for parameter matrices. Operators ask for the matrix of the
 * specified values with
 * {@link com.rapidminer.operator.Operator#getParameterAsMatrix(String)}.
 * 
 * @author Helge Homburg, Ingo Mierswa
 * @version $Id: ParameterTypeMatrix.java,v 1.5 2008/07/13 11:00:57 ingomierswa Exp $
 */
public class ParameterTypeMatrix extends ParameterTypeString {

	private static final long serialVersionUID = 0L;
	
	private boolean isSquared = false;
	
	private String baseName;
	
	private String rowBaseName;
	
	private String columnBaseName;
	
	public ParameterTypeMatrix(String key, String description, String baseName, String rowBaseName, String columnBaseName, boolean isSquared) {
		this(key, description, baseName, rowBaseName, columnBaseName, isSquared, true);
	}
	
	public ParameterTypeMatrix(String key, String description,  String baseName, String rowBaseName, String columnBaseName, boolean isSquared, boolean isOptional) {
		super(key, description, isOptional);
		this.isSquared = isSquared;
		this.baseName = baseName;
		this.rowBaseName = rowBaseName;
		this.columnBaseName = columnBaseName;
	}

	public boolean isSquared() {
		return isSquared;
	}

	public void setSquared(boolean isSquared) {
		this.isSquared = isSquared;
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	public String getRowBaseName() {
		return rowBaseName;
	}

	public void setRowBaseName(String rowBaseName) {
		this.rowBaseName = rowBaseName;
	}

	public String getColumnBaseName() {
		return columnBaseName;
	}

	public void setColumnBaseName(String columnBaseName) {
		this.columnBaseName = columnBaseName;
	}
}
