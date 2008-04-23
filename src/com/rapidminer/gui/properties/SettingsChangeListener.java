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
package com.rapidminer.gui.properties;

import java.util.Properties;

/**
 * This change listener listens for settings changes, i.e. for changes
 * of the global program settings.
 * 
 * @author Ingo Mierswa
 * @version $Id: SettingsChangeListener.java,v 1.1 2007/05/27 21:59:26 ingomierswa Exp $
 */
public interface SettingsChangeListener {

	/** This method will be called after a settings change. */
    public void settingsChanged(Properties properties);
    
}
