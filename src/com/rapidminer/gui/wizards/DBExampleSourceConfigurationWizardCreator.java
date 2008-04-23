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
package com.rapidminer.gui.wizards;

/**
 *  This creator can be used to create wizards for the 
 *  {@link com.rapidminer.operator.io.DatabaseExampleSource} operator.
 *
 *  @author Ingo Mierswa
 *  @version $Id: DBExampleSourceConfigurationWizardCreator.java,v 1.1 2007/05/27 22:02:06 ingomierswa Exp $
 */
public class DBExampleSourceConfigurationWizardCreator implements ConfigurationWizardCreator {

	private static final long serialVersionUID = -3326459655851921317L;

	/** Necessary for construction by reflection. */
	public DBExampleSourceConfigurationWizardCreator() {}

    public void createConfigurationWizard(ConfigurationListener listener) {
        (new DBExampleSourceConfigurationWizard(listener)).setVisible(true);
    }
}
