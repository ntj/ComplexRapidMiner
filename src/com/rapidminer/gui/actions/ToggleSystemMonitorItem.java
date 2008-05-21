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
package com.rapidminer.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.SwingTools;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: ToggleSystemMonitorItem.java,v 1.4 2008/05/09 19:22:46 ingomierswa Exp $
 */
public class ToggleSystemMonitorItem extends JCheckBoxMenuItem implements ActionListener {

	private static final long serialVersionUID = 7945709546573376228L;

	private static final String ICON_NAME = "cpu_view.png";
	
	private static final Icon[] ICONS = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			ICONS[counter++] = SwingTools.createIcon(size.getSize() + "/" + ICON_NAME);
		}
	}

	private MainFrame mainFrame;
	
	public ToggleSystemMonitorItem(MainFrame mainFrame, IconSize size) {
		super("Show Memory Monitor");
		addActionListener(this);
		setToolTipText("Indicates if the memory monitor should be shown right from the message viewer");
		setIcon(ICONS[size.ordinal()]);
		setSelected(true);
		this.mainFrame = mainFrame;
	}

	public void actionPerformed(ActionEvent e) {
		this.mainFrame.toggleSystemMonitor(isSelected());
	}
}
