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
package com.rapidminer.gui.wizards;

import com.rapidminer.tools.Tools;

/**
 *  This creator can be used to create wizards for the 
 *  {@link com.rapidminer.operator.io.DatabaseExampleSource} operator.
 *
 *  @author Ingo Mierswa
 *  @version $Id: DBExampleSourceConfigurationWizardCreator.java,v 1.6 2008/08/21 17:48:16 ingomierswa Exp $
 */
public class DBExampleSourceConfigurationWizardCreator extends AbstractConfigurationWizardCreator {

	private static final long serialVersionUID = -3326459655851921317L;

	public static final String PARAMETER_SHOW_DATABASE_DRIVERS = "show_database_drivers";
	
	public static final String PARAMETER_ONLY_TABLE_NAMES = "only_table_name";

	public static final String PARAMETER_SHOW_DATABASE_CONFIGURATION = "show_database_configuration";

	public static final String PARAMETER_SYSTEM = "system";
	
	public static final String PARAMETER_SERVER = "server";
	
	public static final String PARAMETER_DB_NAME = "db_name";
	
	
	/** Necessary for construction by reflection. */
	public DBExampleSourceConfigurationWizardCreator() {}

	public String getButtonText() {
		return "Start Data Loading Wizard...";
	}
	
    public void createConfigurationWizard(ConfigurationListener listener) {
    	boolean showDrivers = true;
        boolean showOnlyTableNames = false;
        boolean showDatabaseConfiguration = true;
        String system = null;
        String server = null;
        String dbName = null;
        
        if (getParameters() != null) {
        	showDrivers = Tools.booleanValue(getParameters().get(PARAMETER_SHOW_DATABASE_DRIVERS), true);
        	showOnlyTableNames = Tools.booleanValue(getParameters().get(PARAMETER_ONLY_TABLE_NAMES), false);
        	showDatabaseConfiguration = Tools.booleanValue(getParameters().get(PARAMETER_SHOW_DATABASE_CONFIGURATION), true);
        	if (!showDatabaseConfiguration) {
        		Object systemObject = getParameters().get(PARAMETER_SYSTEM);
        		if (systemObject != null) {
        			system = systemObject.toString();
        		}
        		
        		Object serverObject = getParameters().get(PARAMETER_SERVER);
        		if (serverObject != null) {
        			server = serverObject.toString();
        		}
        		
        		Object dbNameObject = getParameters().get(PARAMETER_DB_NAME);
        		if (dbNameObject != null) {
        			dbName = dbNameObject.toString();
        		}
        	}
        }
        
        (new DBExampleSourceConfigurationWizard(listener, showDrivers, showOnlyTableNames, showDatabaseConfiguration, system, server, dbName)).setVisible(true);
    }
}
