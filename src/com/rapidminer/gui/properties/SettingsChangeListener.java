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
package com.rapidminer.gui.properties;

import java.util.Properties;

/**
 * This change listener listens for settings changes, i.e. for changes
 * of the global program settings.
 * 
 * @author Ingo Mierswa
 * @version $Id: SettingsChangeListener.java,v 1.3 2008/05/09 19:22:46 ingomierswa Exp $
 */
public interface SettingsChangeListener {

	/** This method will be called after a settings change. */
    public void settingsChanged(Properties properties);
    
}
