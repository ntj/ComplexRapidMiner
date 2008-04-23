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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JTabbedPane;

import com.rapidminer.RapidMiner;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.ParameterService;


/**
 * The tabs for the different groups of RapidMiner settings. Each tab contains a
 * {@link SettingsPropertyTable} for the settings in this group.
 * 
 * @author Ingo Mierswa
 * @version $Id: SettingsTabs.java,v 1.2 2007/06/06 00:57:38 ingomierswa Exp $
 */
public class SettingsTabs extends JTabbedPane {

	private static final long serialVersionUID = -229446448782516589L;

	private List<SettingsPropertyTable> tables = new LinkedList<SettingsPropertyTable>();

	public SettingsTabs() {
		Set<ParameterType> allProperties = RapidMiner.getRapidMinerProperties();
		SortedMap<String, List<ParameterType>> groups = new TreeMap<String, List<ParameterType>>();
		Iterator<ParameterType> i = allProperties.iterator();
		while (i.hasNext()) {
			ParameterType type = i.next();
			String key = type.getKey();
			String[] parts = key.split("\\.");
			String group = parts[1];
			List<ParameterType> list = groups.get(group);
			if (list == null) {
				list = new LinkedList<ParameterType>();
				groups.put(group, list);
			}
			list.add(type);
		}

		Iterator<Map.Entry<String,List<ParameterType>>> it =
						groups.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String,List<ParameterType>> e = it.next();
			String group = e.getKey();
			List<ParameterType> groupList = e.getValue();
			SettingsPropertyTable table = new SettingsPropertyTable(groupList);
			tables.add(table);
			String name = new String(new char[] { group.charAt(0) }).toUpperCase() + group.substring(1, group.length());
			addTab(name, new ExtendedJScrollPane(table));
		}
	}

	public void applyProperties() {
		Iterator i = tables.iterator();
		while (i.hasNext()) {
			((SettingsPropertyTable) i.next()).applyProperties();
		}
	}

	public void save() throws IOException {
		File configFile = ParameterService.getUserConfigFile("rapidminerrc" + "." + System.getProperty("os.name"));
		PrintWriter out = new PrintWriter(new FileWriter(configFile));
		Iterator i = tables.iterator();
		while (i.hasNext()) {
			((SettingsPropertyTable) i.next()).writeProperties(out);
		}
		out.close();
	}
}
