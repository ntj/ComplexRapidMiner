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
package com.rapidminer.example.table;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.rapidminer.example.Attribute;
import com.rapidminer.tools.jdbc.DatabaseHandler;


/**
 * Reads datarows from a data base.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: DatabaseDataRow.java,v 1.1 2007/05/27 22:01:19 ingomierswa Exp $
 */
public class DatabaseDataRow extends DataRow {

	private static final long serialVersionUID = 4043965829002723585L;

	/** The result set which backs this data row. */
	private transient ResultSet resultSet;

	/** The current row of the result set. */
	private int row;
    
	/** The last attribute for which a query should be / was performed. */
	private Attribute lastAttribute = null;
	
	/**
	 * Creates a data row from the given result set. The current row of the
	 * result set if used as data source.
	 */
	public DatabaseDataRow(ResultSet resultSet) throws SQLException {
		this.resultSet = resultSet;
		this.row = resultSet.getRow();
	}

	/** Ensures that the current row is the current row of the result set. */
	private void ensureRowCorrect() throws SQLException {
		if (row != resultSet.getRow()) {
			throw new RuntimeException("DatabaseDataRow: ResultSet was modified since creation of row!");
		}
	}

	/** Returns the desired data for the given attribute. */
	public double get(Attribute attribute) {
		try {
			ensureRowCorrect();
		} catch (SQLException e) {
			throw new RuntimeException("Cannot read data: " + e);
		}
		this.lastAttribute = attribute;
		double value = attribute.getValue(this);
		this.lastAttribute = null;
		return value;
	}

	/** Sets the given data for the given attribute. */
	public void set(Attribute attribute, double value) {
		try {
			ensureRowCorrect();
		} catch (SQLException e) {
			throw new RuntimeException("Cannot update data: " + e, e);
		}
		this.lastAttribute = attribute;
		attribute.setValue(this, value);
		this.lastAttribute = null;
	}
	
	/* pp */ double get(int index, double defaultValue) {
		if (lastAttribute == null) {
			throw new RuntimeException("Cannot read data, please use get(Attribute) method instead of get(int, double) in DatabaseDataRow.");
		} else {
			try {
				return readColumn(this.resultSet, lastAttribute);
			} catch (SQLException e) {
				throw new RuntimeException("Cannot read data: " + e, e);
			}
		}
	}


	/* pp */ void set(int index, double value, double defaultValue) {
		try {
			String name = DatabaseHandler.getDatabaseName(this.lastAttribute);
			if (Double.isNaN(value)) {
				resultSet.updateNull(name);
			} else {
				if (this.lastAttribute.isNominal()) {
					resultSet.updateString(name, this.lastAttribute.getMapping().mapIndex((int) value));
				} else {
					resultSet.updateDouble(name, value);
				}
			}
			resultSet.updateRow();
		} catch (SQLException e) {
			throw new RuntimeException("Cannot update data: " + e, e);
		}
	}
	
	/** Does nothing. */
	/* pp */ void ensureNumberOfColumns(int numberOfColumns) {}

	/** Does nothing. */
	public void trim() {}

	public String toString() {
		return "Database Data Row";
	}
	
	/** Reads the data for the given attribute from the result set. */
	public static double readColumn(ResultSet resultSet, Attribute attribute) throws SQLException {
		String name = DatabaseHandler.getDatabaseName(attribute);
		if (attribute.isNominal()) {
			String dbString = resultSet.getString(name);
			if (dbString == null)
				return Double.NaN;
			return attribute.getMapping().mapString(dbString);
		} else {
			double value = resultSet.getDouble(name);
			if (resultSet.wasNull()) {
				return Double.NaN;
			} else {
                return value;
			}
		}
	}
}
