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

import com.rapidminer.gui.wizards.ConfigurationListener;
import com.rapidminer.gui.wizards.ConfigurationWizardCreator;
import com.rapidminer.tools.LogService;


/**
 * This parameter type will lead to a GUI element which can be used as initialization for a sort
 * of operator configuration wizard.
 * 
 * @author Ingo Mierswa
 * @version $Id: ParameterTypeConfiguration.java,v 1.4 2008/05/09 19:22:37 ingomierswa Exp $
 */
public class ParameterTypeConfiguration extends ParameterType {

	private static final long serialVersionUID = -3512071671355815277L;

	private Class<? extends ConfigurationWizardCreator> wizardCreatorClass;
    
    private transient ConfigurationListener wizardListener;
    
    public ParameterTypeConfiguration(Class<? extends ConfigurationWizardCreator> wizardCreatorClass, ConfigurationListener wizardListener) {
        super("configure_operator", "Configure this operator by means of a Wizard.");
        this.wizardCreatorClass = wizardCreatorClass;
        this.wizardListener = wizardListener;
    }
    
    /** Returns a new instance of the wizard creator. If anything does not work this method will return null. */
    public ConfigurationWizardCreator getWizardCreator() {
    	ConfigurationWizardCreator creator = null;
    	try {
    		creator = wizardCreatorClass.newInstance();
    	} catch (InstantiationException e) {
    		LogService.getGlobal().log("Problem during creation of wizard: " + e.getMessage(), LogService.WARNING);
    	} catch (IllegalAccessException e) {
            LogService.getGlobal().log("Problem during creation of wizard: " + e.getMessage(), LogService.WARNING);
    	}
    	return creator;
    }
    
    public ConfigurationListener getWizardListener() {
        return wizardListener;
    }
    
    public Object checkValue(Object value) {
        return null;
    }

    /** Returns null. */
    public Object getDefaultValue() {
        return null;
    }
    
    /** Does nothing. */
    public void setDefaultValue(Object value) {}
    
	public Object copyValue(Object value) {
		return value;
	}

    public String getRange() {
        return null;
    }

    /** Returns an empty string since this parameter cannot be used in XML description but is only used for
     *  GUI purposes. */
    public String getXML(String indent, String key, Object value, boolean hideDefault) {
        return "";
    }

    public boolean isNumerical() {
        return false;
    }
}
