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
package com.rapidminer.gui.tools;

import java.util.LinkedHashMap;

import javax.swing.DefaultListModel;


/**
 * Provides an extended list model which holds data for a {@link ExtendedJList}.
 * Maintains a hash map which provides tooltips for list entries.
 * 
 * @author Tobias Malbrecht
 * @version $Id: ExtendedListModel.java,v 1.2 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class ExtendedListModel extends DefaultListModel {
	public static final long serialVersionUID = 90320323118402L;
	
	private LinkedHashMap<Object, String> toolTipMap;
	
	public ExtendedListModel() {
		super();
		toolTipMap = new LinkedHashMap<Object, String>();
	}
	
	/** Adds another list entry and the corresponding tooltip. */
	public void addElement(Object object, String tooltip) {
		super.addElement(object);
		toolTipMap.put(object, tooltip);
	}
	
	/** Removes a list entry. */
	public boolean removeElement(Object object) {
		toolTipMap.remove(object);
		return super.removeElement(object);
	}
	
	/** Returns the tooltip corresponding to a list entry. */
	public String getToolTip(Object object) {
		return toolTipMap.get(object);
	}
	
	/** Returns the tooltip corresponding to a list entry specified as index. */
	public String getToolTip(int index) {
		if (index < 0) {
			return null;
		} else {
			return toolTipMap.get(get(index));
		}
	}
}
