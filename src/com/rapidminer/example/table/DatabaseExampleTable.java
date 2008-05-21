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
package com.rapidminer.example.table;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.jdbc.DatabaseHandler;


/**
 * This class is another data supplier for example sets. For performance reasons
 * one should use a {@link MemoryExampleTable} if the data is small enough for
 * the main memory. Additionally, directly working on databases is highly experimental and
 * therefore usually not recommended.
 * 
 * @author Ingo Mierswa
 * @version $Id: DatabaseExampleTable.java,v 2.14 2006/03/27 13:21:58
 *          ingomierswa Exp $
 */
public class DatabaseExampleTable extends AbstractExampleTable {

    private static final long serialVersionUID = -3683705313093987482L;

	private transient ResultSet resultSet;
    
	private transient DatabaseHandler databaseHandler;

	private transient Statement statement;
	
	private String tableName;

	private int size = 0;
    
	private DatabaseExampleTable(List<Attribute> attributes, DatabaseHandler databaseHandler, String tableName) throws SQLException {
		super(attributes);
		this.databaseHandler = databaseHandler;
		this.tableName = tableName;
		this.resetResultSet();
	}

	public static DatabaseExampleTable createDatabaseExampleTable(DatabaseHandler databaseHandler, String tableName) throws SQLException {
		// derive attribute list
    	Statement statement = databaseHandler.createStatement();
        ResultSet rs = statement.executeQuery(
        		"SELECT * FROM " + 
        		databaseHandler.getProperties().getIdentifierQuoteOpen() + 
        		tableName + 
        		databaseHandler.getProperties().getIdentifierQuoteClose());
		List<Attribute> attributes = DatabaseHandler.createAttributes(rs);
		statement.close();
		
		// create database example table
		DatabaseExampleTable table = new DatabaseExampleTable(attributes, databaseHandler, tableName);
		return table;
	}
    
    private void resetResultSet() throws SQLException {
    	if (statement != null) {
    		statement.close();
    		statement = null;
    	}
    	this.statement = this.databaseHandler.createStatement();
        this.resultSet = this.statement.executeQuery(
        		"SELECT * FROM " + 
        		databaseHandler.getProperties().getIdentifierQuoteOpen() + 
        		tableName + 
        		databaseHandler.getProperties().getIdentifierQuoteClose());
    }
    
	public DataRowReader getDataRowReader() {
		try {
            return new DatabaseDataRowReader(resultSet);
		} catch (SQLException e) {
			throw new RuntimeException("Error while creating database DataRowReader: " + e, e);
		}
	}

	/**
	 * Returns the data row with the desired row index.
	 */
	public DataRow getDataRow(int index) {
        try {
            this.resultSet.absolute(index + 1);
            DatabaseDataRow dataRow = new DatabaseDataRow(resultSet);
            return dataRow;
        } catch (SQLException e) {
            LogService.getGlobal().log("Cannot retrieve data row with absolute row index: " + e.getMessage(), LogService.WARNING);
        }
        return null;
	}

	public int addAttribute(Attribute attribute) {
		int index = super.addAttribute(attribute);
		
        // will be invoked by super constructor, hence this check
        if (databaseHandler == null)
			return index;
        
		try {
            close();
			databaseHandler.addColumn(attribute, tableName);
            resetResultSet();
		} catch (SQLException e) {
			throw new RuntimeException("Error while adding a column '" + attribute.getName() + "'to database: " + e, e);
		}
		return index;
	}

	public void removeAttribute(Attribute attribute) {
		super.removeAttribute(attribute);
		try {
            close();
			databaseHandler.removeColumn(attribute, tableName);
            resetResultSet();
		} catch (SQLException e) {
			throw new RuntimeException("Error while removing a column '"+attribute.getName()+"' from database: " + e, e);
		}
	}

	public int size() {
        try {
            this.size = this.databaseHandler.countRecords(resultSet);
        } catch (SQLException e) {
            LogService.getGlobal().log("DatabaseExampleTable: cannot count number of records: " + e.getMessage(), LogService.WARNING);
        }
        return this.size;
	}
    
    private void close() {
        if (this.statement != null) {
            try {
                this.statement.close();
                this.statement = null;
            } catch (SQLException e) {
                LogService.getGlobal().log("DatabaseExampleTable: cannot close result set: " + e.getMessage(), LogService.WARNING);
            }
        }
    }
    
    protected void finalize() {
        close();
    }
}
