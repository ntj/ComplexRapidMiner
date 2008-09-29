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

/**
 *  This creator can be used to create wizards for the {@link com.rapidminer.operator.io.ExampleSource}
 *  operator.
 *
 *  @author Ingo Mierswa
 *  @version $Id: ExampleSourceConfigurationWizardCreator.java,v 1.5 2008/08/15 19:15:00 ingomierswa Exp $
 */
public class ExampleSourceConfigurationWizardCreator extends AbstractConfigurationWizardCreator {

	private static final long serialVersionUID = 2690870634059674565L;

	/** Necessary for construction by reflection. */
	public ExampleSourceConfigurationWizardCreator() {}

	public String getButtonText() {
		return "Start Data Loading Wizard...";
	}
	
    public void createConfigurationWizard(ConfigurationListener listener) {
        (new ExampleSourceConfigurationWizard(listener)).setVisible(true);
    }
}
