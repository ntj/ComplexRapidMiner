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
package com.rapidminer.operator.io;

import java.sql.SQLException;
import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.wizards.DBExampleSetWriterConfigurationWizardCreator;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeConfiguration;
import com.rapidminer.parameter.ParameterTypePassword;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.jdbc.DatabaseHandler;
import com.rapidminer.tools.jdbc.DatabaseService;


/**
 * <p>This operator writes an {@link com.rapidminer.example.ExampleSet} into an SQL
 * database. The user can specify the database connection and a table name. Please note
 * that the table will be created during writing if it does not exist.</p>
 * 
 * <p>The most convenient way of defining the necessary parameters is the 
 * configuration wizard. The most important parameters (database URL and user name) will
 * be automatically determined by this wizard. At the end, you only have to define
 * the table name and then you are ready.</p>
 *
 * <p>
 * This operator only supports the writing of the complete example set consisting of
 * all regular and special attributes and all examples. If this is not desired perform some
 * preprocessing operators like attribute or example filter before applying this operator.
 * </p>
 * 
 * @author Ingo Mierswa
 * @version $Id: DatabaseExampleSetWriter.java,v 1.2 2007/06/15 16:58:37 ingomierswa Exp $
 */
public class DatabaseExampleSetWriter extends Operator {


	/** The parameter name for &quot;Indicates the used database system&quot; */
	public static final String PARAMETER_DATABASE_SYSTEM = "database_system";

	/** The parameter name for &quot;The complete URL connection string for the database, e.g. 'jdbc:mysql://foo.bar:portnr/database'&quot; */
	public static final String PARAMETER_DATABASE_URL = "database_url";

	/** The parameter name for &quot;Database username.&quot; */
	public static final String PARAMETER_USERNAME = "username";

	/** The parameter name for &quot;Password for the database.&quot; */
	public static final String PARAMETER_PASSWORD = "password";

	/** The parameter name for &quot;Use this table if work_on_database is true or no other query is specified.&quot; */
	public static final String PARAMETER_TABLE_NAME = "table_name";

	/** The parameter name for &quot;Indicates if an existing table should be overwritten.&quot; */
	public static final String PARAMETER_OVERWRITE_EXISTING_TABLE = "overwrite_existing_table";
	public DatabaseExampleSetWriter(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		try {
			DatabaseHandler databaseHandler = getConnectedDatabaseHandler();
			databaseHandler.createTable(exampleSet, getParameterAsString(PARAMETER_TABLE_NAME), getParameterAsBoolean(PARAMETER_OVERWRITE_EXISTING_TABLE));
			databaseHandler.disconnect();
		} catch (SQLException e) {
			throw new UserError(this, e, 304, e.getMessage());
		}
	    return new IOObject[] { exampleSet };
	}

	private DatabaseHandler getConnectedDatabaseHandler() throws OperatorException, SQLException {
	    String databaseURL = getParameterAsString(PARAMETER_DATABASE_URL);
		String username = getParameterAsString(PARAMETER_USERNAME);
		String password = getParameterAsString(PARAMETER_PASSWORD);
		return DatabaseHandler.getConnectedDatabaseHandler(databaseURL, username, password, 
                DatabaseService.getJDBCProperties().get(getParameterAsInt(PARAMETER_DATABASE_SYSTEM)), this);
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeConfiguration(DBExampleSetWriterConfigurationWizardCreator.class, this);
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeCategory(PARAMETER_DATABASE_SYSTEM, "Indicates the used database system", DatabaseService.getDBSystemNames(), 0);
        type.setExpert(false);
        types.add(type);
        types.add(new ParameterTypeString(PARAMETER_DATABASE_URL, "The complete URL connection string for the database, e.g. 'jdbc:mysql://foo.bar:portnr/database'", false));
		types.add(new ParameterTypeString(PARAMETER_USERNAME, "Database username.", false));
		types.add(new ParameterTypePassword(PARAMETER_PASSWORD, "Password for the database."));
		types.add(new ParameterTypeString(PARAMETER_TABLE_NAME, "Use this table if work_on_database is true or no other query is specified."));
		types.add(new ParameterTypeBoolean(PARAMETER_OVERWRITE_EXISTING_TABLE, "Indicates if an existing table should be overwritten.", false));
		return types;
	}
}
