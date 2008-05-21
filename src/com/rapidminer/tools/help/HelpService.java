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
package com.rapidminer.tools.help;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;

import javax.help.DefaultHelpModel;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.JHelpContentViewer;
import javax.swing.AbstractButton;
import javax.swing.SingleSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.rapidminer.tools.LogService;

/**
 * This is the main help service.
 * 
 * @author Sebastian Land
 * @version $Id: HelpService.java,v 1.5 2008/05/09 19:23:23 ingomierswa Exp $
 */
public class HelpService implements ActionListener, ChangeListener {
	
	private static JHelpContentViewer helpViewer = new JHelpContentViewer();
	
	private static HelpBroker helpBroker;
	
	private static HelpService helpService = new HelpService();

	private HashMap<Object, String> actionMap;
	
	private HashMap<Object, HashMap<Integer, String>> selectionModelMap;

	
	public static void setHelpSetFile(String helpSetFile) {
		HelpSet helpSet;
		try {
			ClassLoader classLoader = HelpSet.class.getClassLoader();
			URL url = HelpSet.findHelpSet(classLoader, helpSetFile);
			helpSet = new HelpSet(classLoader, url);
		} catch (Exception ee) {
			// TODO correct error handling
			LogService.getGlobal().logWarning("Help Set not found");
			return;
		}
		helpViewer.setModel(new DefaultHelpModel(helpSet));
		helpBroker = helpSet.createHelpBroker();

	}

	public static void setHelpContext(String context) {
		try {
			helpBroker.setCurrentID(context);
			helpViewer.getModel().setCurrentID(helpBroker.getCurrentID());
		} catch (Exception ee) {
			// TODO correct error handling
			LogService.getGlobal().logWarning("HelpID " + context + " does not exist");
		}
	}

	public static JHelpContentViewer getContentViewer() {
		return helpViewer;
	}

	public static HelpBroker getHelpBroker() {
		return helpBroker;
	}

	public static synchronized void registerButton(AbstractButton button,
			String id) {
		button.addActionListener(helpService);
		helpService.registerObject(button, id);
	}

	public static synchronized void registerModel(SingleSelectionModel model,
			int index, String helpTopicId) {
		helpService.registerOnModel(model, index, helpTopicId);
	}

	public HelpService() {
		actionMap = new HashMap<Object, String>();
		selectionModelMap = new HashMap<Object, HashMap<Integer, String>>();
	}

	public void registerObject(Object object, String helpTopicId) {
		actionMap.put(object, helpTopicId);
	}

	public void registerOnModel(SingleSelectionModel model, int index,
			String helpTopicId) {
		HashMap<Integer, String> indexHelpTopicMap;
		if (selectionModelMap.containsKey(model))
			indexHelpTopicMap = selectionModelMap.get(model);
		else
			indexHelpTopicMap = new HashMap<Integer, String>();
		indexHelpTopicMap.put(index, helpTopicId);
		selectionModelMap.put(model, indexHelpTopicMap);
		model.addChangeListener(this);
		
	}

	public void actionPerformed(ActionEvent event) {
		String id = actionMap.get(event.getSource());
		setHelpContext(id);
	}

	
	public void stateChanged(ChangeEvent event) {
		HashMap<Integer, String> idMap = selectionModelMap.get(event
				.getSource());
		if (idMap != null) {
			SingleSelectionModel model = (SingleSelectionModel) event
					.getSource();
			String id = idMap.get(model.getSelectedIndex());
			setHelpContext(id);
		}
	}
	
}
