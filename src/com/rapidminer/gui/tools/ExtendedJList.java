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

import java.awt.event.MouseEvent;

import javax.swing.JList;


/**
 * Extended JList which provides tool tips in combination with
 * an {@link ExtendedListModel}.
 * 
 * @author Tobias Malbrecht
 * @version $Id: ExtendedJList.java,v 1.2 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class ExtendedJList extends JList {
	public static final long serialVersionUID = 9032182018402L;
	
	public ExtendedJList(ExtendedListModel model) {
		super(model);
	}
	
	/** Returns the tooltip of a list entry. */
	public String getToolTipText(MouseEvent e) {
		int index = locationToIndex(e.getPoint());
		return ((ExtendedListModel)getModel()).getToolTip(index);
	}
}
