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
package com.rapidminer.tools.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.LoggingHandler;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;


/**
 * <p>This class hides the database. Using
 * {@link DatabaseHandler#connect(String,String,boolean)}, you can extablish a
 * connection to the database. Once connected, queries and updates are possible.</p>
 * 
 * <p>Please note that the queries does not end with the statement terminator (e.g. 
 * ; for Oracle or GO for Sybase). The JDBC driver will automatically add the correct
 * terminator.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: DatabaseHandler.java,v 1.2 2007/05/28 21:23:34 ingomierswa Exp $
 */
public class DatabaseHandler {

	/** Used for logging purposes. */
	private String databaseURL;

    /** The properties of this JDBC driver and connection. */
    private JDBCProperties properties;
    
	/** The 'singleton' connection. Each database handler can handle one single connection to 
	 *  a database. Will be null before the connection is established and will also be null
	 *  after {@link #disconnect()} was invoked. */
	private Connection connection;
    
	
	/**
	 * Constructor of the database handler. This constructor expects the URL definition
     * of the database which is needed by the System DriverManager to create an appropriate
     * driver. Please note that this database handler still must be connected via invoking
     * the method {@link #connect(String, String, boolean)}. If you want to directly use
     * a connected database handler you might use the static method 
     * {@link #getConnectedDatabaseHandler(String, String, String, JDBCProperties, LoggingHandler)} instead.
	 */
	public DatabaseHandler(String databaseURL, JDBCProperties properties) {
		this.databaseURL = databaseURL;
        this.properties = properties;
		connection = null;
	}
    
	/** Returns a connected database handler instance from the given connection data. If the password
	 *  is null, it will be queries by the user during this method. */
	public static DatabaseHandler getConnectedDatabaseHandler(String databaseURL, String username, String password, JDBCProperties properties, LoggingHandler logging) throws OperatorException, SQLException {
		if (password == null) {
			password = RapidMiner.getInputHandler().inputPassword("Password for user '" + username + "' required");
		}

		DatabaseHandler databaseHandler = new DatabaseHandler(databaseURL, properties);
		logging.log("Connecting to '" + databaseURL + "'.");
		databaseHandler.connect(username, password, true);
		return databaseHandler;
	}
	
	/**
	 * Establishes a connection to the database. Afterwards, queries and updates
	 * can be executed using the methods this class provides.
	 * 
	 * @param username
	 *            Name with which to log in to the database. Might be null.
	 * @param passwd
	 *            Password with which to log in to the database. Might be null.
	 * @param autoCommit
	 *            If TRUE, all changes to the database will be committed
	 *            automatically. If FALSE, the commit()-Method has to be called
	 *            to make changes permanent.
	 */
	public void connect(String username, String passwd, boolean autoCommit) throws SQLException {
		if (connection != null) {
			throw new SQLException("connect: Connection to database '" + databaseURL + "' already exists!");
		}
		if (username == null) {
			connection = DriverManager.getConnection(databaseURL);
		} else {
			connection = DriverManager.getConnection(databaseURL, username, passwd);
		}
		connection.setAutoCommit(autoCommit);
	}

	/** Closes the connection to the database. */
	public void disconnect() throws SQLException {
		if (connection == null) {
			throw new SQLException("Disconnect: was not connected.");
		}
		connection.close();
		connection = null;
	}

	/** Returns the connection. Might be used in order to create statements. The return
	 *  value might be null if this database handler is not connected. */
	public Connection getConnection() {
		return connection;
	}
	
	/** Create a statement where result sets will have the properties 
	 *  TYPE_SCROLL_INSENSITIVE and CONCUR_UPDATABLE. This means that the
	 *  ResultSet is scrollable, but not updatable. It will not show changes to
	 *  the database made by others after this ResultSet was obtained.
	 *  Will throw an {@link SQLException} if the handler is not connected. */
	public Statement createStatement() throws SQLException {
		if (connection == null) {
			throw new SQLException("Could not create a statement for '" + databaseURL + "': not connected.");
		}
		return connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}
	
    /**
     * Makes all changes to the database permanent. Invoking this method explicitely is
     * usually not necessary if the connection was created with AUTO_COMMIT set to true.
     */
    public void commit() throws SQLException {
        if ((connection == null) || connection.isClosed()) {
            throw new SQLException("Could not commit: no open connection to database '" + databaseURL + "' !");
        }
        connection.commit();
    }
    
	/**
	 * Executes the given SQL-Query. Only SQL-statements starting with "SELECT"
	 * (disregarding case) will be accepted. Any errors will result in an
	 * SQL-Exception being thrown.
	 * 
	 * @param sqlQuery An SQL-String.
	 * @return A ResultSet-Object with the results of the query. The ResultSet
	 *         is scrollable, but not updatable. It will not show changes to
	 *         the database made by others after this ResultSet was obtained.
	 *
	 * @deprecated Use the method {@link #createStatement()} instead and perform the queries explicitely since this method would not allow closing the statement
	 */
	@Deprecated
	public ResultSet query(String sqlQuery) throws SQLException {
		if (!sqlQuery.toLowerCase().startsWith("select")) {
			throw new SQLException("Query: Only SQL-Statements starting with SELECT are allowed: " + sqlQuery);
		}

		Statement st = createStatement();
		ResultSet rs = st.executeQuery(sqlQuery);
		return rs;
	}

    
	/** Adds a column for the given attribute to the table with name tableName. */
	public void addColumn(Attribute attribute, String tableName) throws SQLException {
		Statement st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		String query = "ALTER TABLE " + tableName + " ADD COLUMN " + getDatabaseName(attribute) + " " + (attribute.isNominal() ? (properties.getVarcharName() + "(256)") : properties.getRealName());
		st.execute(query);
        st.close();
	}

	/**
	 * Removes the column of the given attribute from the table with name
	 * tableName.
	 */
	public void removeColumn(Attribute attribute, String tableName) throws SQLException {
        Statement st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		String query = "ALTER TABLE " + tableName + " DROP COLUMN " + getDatabaseName(attribute);
		st.execute(query);
        st.close();
	}

	/** Creates a new table in this connection and fills it with the provided data. 
	 * 
	 *  @throws SQLException if the table should be overwritten but a table with this name already exists
	 */
	public void createTable(ExampleSet exampleSet, String tableName, boolean overwrite) throws SQLException {
		// either drop the table or throw an exception (depending on the parameter 'overwrite')
		Statement statement = createStatement();
		boolean exists = false;
		try {
            // check if table already exists (no exception and more than zero columns :-)
			ResultSet existingResultSet = statement.executeQuery("SELECT * FROM " + tableName + " WHERE 0 = 1");
            if (existingResultSet.getMetaData().getColumnCount() > 0)
                exists = true;
			existingResultSet.close();
		} catch (SQLException e) {
			// exception will be throw if table does not exist
		}
        if (exists) {
        	if (overwrite) {
        		statement.executeUpdate("DROP TABLE " + tableName);
        	} else {
        		throw new SQLException("Table with name '"+tableName+"' already exists and overwriting mode is not activated." + Tools.getLineSeparator() + 
        				               "Please change table name or activate overwriting mode.");
        	}
        }
		
        // create new table
        exampleSet.recalculateAllAttributeStatistics(); // necessary for updating the possible nominal values
        String createTableString = getCreateTableString(exampleSet, tableName);
		statement.executeUpdate(createTableString);
        
        // fill table
		for (Example example : exampleSet) {
			String insertIntoTableString = 
				getInsertIntoTableString(tableName, example, exampleSet.getAttributes().allAttributeRoles());
			statement.executeUpdate(insertIntoTableString);
		}
		
		// close the statement
		statement.close();		
	}
	
	private String getInsertIntoTableString(String tableName, Example example, Iterator<AttributeRole> attributes) {
		StringBuffer result = new StringBuffer("INSERT INTO " + tableName + " VALUES (");
		boolean first = true;
		while (attributes.hasNext()) {
			if (!first)
				result.append(", ");
			first = false;
			Attribute attribute = attributes.next().getAttribute();
			double value = example.getValue(attribute);
			if (Double.isNaN(value)) {
					result.append("NULL");
			} else {
				if (attribute.isNominal()) {
					result.append("'" + attribute.getMapping().mapIndex((int)value) + "'");
				} else {
					result.append(example.getValue(attribute));
				}
			}
		}
		result.append(")");
		return result.toString();
	}
	
	private String getCreateTableString(ExampleSet exampleSet, String tableName) {
		// define all attribute names and types
		StringBuffer result = new StringBuffer();
		result.append("CREATE TABLE " + tableName + "(");
		Iterator<AttributeRole> a = exampleSet.getAttributes().allAttributeRoles();
		boolean first = true;
		while (a.hasNext()) {
			if (!first)
				result.append(", ");
			first = false;
			AttributeRole attributeRole = a.next();
			result.append(getCreateAttributeString(attributeRole));
		}
		
		// set primary key
		Attribute idAttribute = exampleSet.getAttributes().getId(); 
		if (idAttribute != null) {
			result.append(", PRIMARY KEY( " + getDatabaseName(idAttribute) + " )");
		}
		
		result.append(")");
		return result.toString();
	}
	
	/** Creates the name and type string for the given attribute. Id attributes
	 *  must not be null.
	 */
	private String getCreateAttributeString(AttributeRole attributeRole) {
		Attribute attribute = attributeRole.getAttribute();
		StringBuffer result = new StringBuffer(getDatabaseName(attribute) + " ");
		if (attribute.isNominal()) {
			int varCharLength = 1; // at least length 1
			for (String value : attribute.getMapping().getValues()) {
				varCharLength = Math.max(varCharLength, value.length());
			}
			result.append(properties.getVarcharName() + "(" + varCharLength + ")");
		} else {
			if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), Ontology.INTEGER)) {
				result.append(properties.getIntegerName());
			} else {
				result.append(properties.getRealName());
			}
		}
		
		// id must not be null
		if (attributeRole.isSpecial())
			if (attributeRole.getSpecialName().equals(Attributes.ID_NAME))
				result.append(" NOT NULL");
		
		return result.toString();
	}
	
    /**
     * Counts the number of records in the given result set.
     * 
     * @param rs the ResultSet.
     */
    public int countRecords(ResultSet rs) throws SQLException {
        int numberOfRows = 0;

        // move the cursor in the ResultSet to the beginning:
        rs.beforeFirst();
        while (rs.next()) {
            numberOfRows++;
        }
        rs.beforeFirst();

        return numberOfRows;
    }
    
	/**
	 * Returns for the given SQL-type the name of the corresponding RapidMiner-Type
	 * from com.rapidminer.tools.Ontology.
	 */
	public static int getRapidMinerTypeIndex(int sqlType) {
		switch (sqlType) {
			case Types.BIGINT:
			case Types.INTEGER:
			case Types.TINYINT:
			case Types.SMALLINT:
				return Ontology.INTEGER;

			case Types.FLOAT:
			case Types.REAL:
			case Types.DECIMAL:
			case Types.DOUBLE:
				return Ontology.REAL;

			case Types.NUMERIC:
				return Ontology.NUMERICAL;

			case Types.CHAR:
			case Types.VARCHAR:
			case Types.CLOB:
			case Types.BINARY:
			case Types.BIT:
			case Types.LONGVARBINARY:
			case Types.BLOB:
			case Types.JAVA_OBJECT:
			case Types.STRUCT:
			case Types.VARBINARY:
			case Types.LONGVARCHAR:
				return Ontology.NOMINAL;

			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				return Ontology.ORDERED;

			default:
				return Ontology.NOMINAL;
		}
	}

	/**
	 * Creates a list of attributes reflecting the result set's column meta
	 * data.
	 */
	public static List<Attribute> createAttributes(ResultSet rs) throws SQLException {
		List<Attribute> attributes = new LinkedList<Attribute>();

		if (rs == null) {
			throw new IllegalArgumentException("Cannot create attributes: ResultSet must not be null!");
		}

		ResultSetMetaData metadata;
		try {
			metadata = rs.getMetaData();
		} catch (NullPointerException npe) {
			throw new RuntimeException("Could not create attribute list: ResultSet object seems closed.");
		}

		int numberOfColumns = metadata.getColumnCount();

		for (int column = 1; column <= numberOfColumns; column++) {
			String name = metadata.getColumnName(column);
			Attribute attribute = AttributeFactory.createAttribute(name, getRapidMinerTypeIndex(metadata.getColumnType(column)));
			attributes.add(attribute);
		}

		return attributes;
	}

	public static String getDatabaseName(Attribute attribute) {
		String name = attribute.getName();
		//name = name.toUpperCase();
        //name = name.replaceAll("\\\\s", "_");
		//name = name.replaceAll("\\(", "_");
		//name = name.replaceAll("\\)", "_");
        name = name.replaceAll("\\W", "_"); // replace non-word characters
		return name;
	}

    public Map<String, List<String>> getAllTableMetaData() throws SQLException {
        if ((connection == null) || connection.isClosed()) {
            throw new SQLException("Could not retrieve all table names: no open connection to database '" + databaseURL + "' !");
        }

        DatabaseMetaData metaData = connection.getMetaData();
        String[] types = new String[] { "TABLE" };
        ResultSet tableNames = metaData.getTables(null, null, "%", types);
        List<String> tableNameList = new LinkedList<String>();
        while (tableNames.next()) {
            String tableName    = tableNames.getString(3);
            tableNameList.add(tableName);
        }
        
        Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();
        Iterator<String> i = tableNameList.iterator();
        while (i.hasNext()) {
            String tableName = i.next();
            try {
                // test: will fail if user can not use this table
                List<String> columnNames = getAllColumnNames(tableName);
                result.put(tableName, columnNames);
            } catch (SQLException e) {
            	// does nothing
            } 
        }
        return result;
    }
    
    private List<String> getAllColumnNames(String tableName) throws SQLException {
        if (tableName == null) {
            throw new SQLException("Cannot read column names: table name must not be null!");
        }

        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);
        List<String> result = new LinkedList<String>();

        ResultSetMetaData metadata;
        try {
            metadata = rs.getMetaData();
        } catch (NullPointerException npe) {
            throw new SQLException("Could not create column name list: ResultSet object seems closed.");
        }

        int numberOfColumns = metadata.getColumnCount();

        for (int column = 1; column <= numberOfColumns; column++) {
            String name = metadata.getColumnName(column);
            result.add(tableName + "." + name);
        }
        statement.close();
        return result;       
    }
}
