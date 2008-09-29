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
 *  @version $Id: DBExampleSetWriterConfigurationWizardCreator.java,v 1.6 2008/08/21 17:48:16 ingomierswa Exp $
 */
public class DBExampleSetWriterConfigurationWizardCreator extends AbstractConfigurationWizardCreator {

	private static final long serialVersionUID = -3326459655851921317L;

	/** Necessary for construction by reflection. */
	public DBExampleSetWriterConfigurationWizardCreator() {}

	public String getButtonText() {
		return "Start Data Writing Wizard...";
	}
	
    public void createConfigurationWizard(ConfigurationListener listener) {
    	boolean showDrivers = true;
        boolean showDatabaseConfiguration = true;
        String system = null;
        String server = null;
        String dbName = null;
        
        if (getParameters() != null) {
        	showDrivers = Tools.booleanValue(getParameters().get(DBExampleSourceConfigurationWizardCreator.PARAMETER_SHOW_DATABASE_DRIVERS), true);
        	showDatabaseConfiguration = Tools.booleanValue(getParameters().get(DBExampleSourceConfigurationWizardCreator.PARAMETER_SHOW_DATABASE_CONFIGURATION), true);
        	if (!showDatabaseConfiguration) {
        		Object systemObject = getParameters().get(DBExampleSourceConfigurationWizardCreator.PARAMETER_SYSTEM);
        		if (systemObject != null) {
        			system = systemObject.toString();
        		}
        		
        		Object serverObject = getParameters().get(DBExampleSourceConfigurationWizardCreator.PARAMETER_SERVER);
        		if (serverObject != null) {
        			server = serverObject.toString();
        		}
        		
        		Object dbNameObject = getParameters().get(DBExampleSourceConfigurationWizardCreator.PARAMETER_DB_NAME);
        		if (dbNameObject != null) {
        			dbName = dbNameObject.toString();
        		}
        	}
        }
        
        (new DBExampleSetWriterConfigurationWizard(listener, showDrivers, showDatabaseConfiguration, system, server, dbName)).setVisible(true);
    }
}
