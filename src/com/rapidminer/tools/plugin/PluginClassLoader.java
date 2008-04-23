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
package com.rapidminer.tools.plugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * The class loader for a plugin (extending URLClassLoader). Since a plugin
 * might depend on other plugins the URLs of these plugins are also added to the
 * current class loader.
 * 
 * @author Ingo Mierswa
 * @version $Id: PluginClassLoader.java,v 1.6 2006/03/21 15:35:54 ingomierswa
 *          Exp $
 */
public class PluginClassLoader extends URLClassLoader {

	public PluginClassLoader(URL[] urls) {
		super(urls);
	}

	
	public void addDependingURL(URL url) {
		addURL(url);
	}
	
	
	public String toString() {
		return "PluginClassLoader (" + Arrays.asList(getURLs()) + ")";
	}
}
