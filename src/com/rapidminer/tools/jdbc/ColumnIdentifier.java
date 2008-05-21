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
package com.rapidminer.tools.jdbc;

/**
 * This class is used to identify a column
 * 
 *
 * @author Ingo Mierswa
 * @version $Id: ColumnIdentifier.java,v 1.2 2008/05/09 19:23:22 ingomierswa Exp $
 */
public class ColumnIdentifier {

	private String tableName;
	
	private String columnName;
	
	public ColumnIdentifier(String tableName, String columnName) {
		this.tableName = tableName;
		this.columnName = columnName;
	}
	
	public String getTableName() {
		return this.tableName;
	}
	
	public String getColumnName() {
		return this.columnName;
	}
	
	public String getFullName(JDBCProperties properties, boolean singleTable) {
		if (singleTable) {
			return 
			properties.getIdentifierQuoteOpen() +
			this.columnName +
			properties.getIdentifierQuoteClose();
		} else {
			return 
			properties.getIdentifierQuoteOpen() +
			this.tableName +
			properties.getIdentifierQuoteClose() +
			"." +
			properties.getIdentifierQuoteOpen() +
			this.columnName +
			properties.getIdentifierQuoteClose();
		}
	}
	
	public String getAliasName(JDBCProperties properties, boolean singleTable) {
		if (singleTable) {
			return 
			properties.getIdentifierQuoteOpen() +
			this.columnName +
			properties.getIdentifierQuoteClose();		 	
		} else {
			return 
			properties.getIdentifierQuoteOpen() +
			this.tableName +
			"__" +
			this.columnName +
			properties.getIdentifierQuoteClose();
		}
	}	
	
	public String toString() {
		return 
		this.tableName +
		"." +
		this.columnName;
	}
}
