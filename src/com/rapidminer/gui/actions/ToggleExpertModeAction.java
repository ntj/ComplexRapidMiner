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
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.SwingTools;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: ToggleExpertModeAction.java,v 1.5 2008/05/09 19:22:46 ingomierswa Exp $
 */
public class ToggleExpertModeAction extends AbstractAction {

	private static final long serialVersionUID = 5545547954042596599L;

	private static final String EXPERT_ICON_NAME   = "graduate.png";
	private static final String BEGINNER_ICON_NAME = "user1.png";
	
	private static final Icon[] EXPERT_MODE_ICONS   = new Icon[IconSize.values().length];
	private static final Icon[] BEGINNER_MODE_ICONS = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			EXPERT_MODE_ICONS[counter++] = SwingTools.createIcon(size.getSize() + "/" + EXPERT_ICON_NAME);
		}
		
		counter = 0;
		for (IconSize size : IconSize.values()) {
			BEGINNER_MODE_ICONS[counter++] = SwingTools.createIcon(size.getSize() + "/" + BEGINNER_ICON_NAME);
		}
	}
		
	private IconSize iconSize;

	private MainFrame mainFrame;
	
	public ToggleExpertModeAction(MainFrame mainFrame, IconSize size) {
		super("Expert Mode");
		this.iconSize = size;
		putValue(SHORT_DESCRIPTION, "Switch to expert mode (all available parameters are visible)");
		putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_E));
		putValue(SMALL_ICON, EXPERT_MODE_ICONS[size.ordinal()]);
		this.mainFrame = mainFrame;
	}

	public void actionPerformed(ActionEvent e) {
		this.mainFrame.toggleExpertMode();
	}

	public void updateIcon() {
		if (this.mainFrame.getPropertyTable().isExpertMode()) {
			putValue(SMALL_ICON, BEGINNER_MODE_ICONS[iconSize.ordinal()]);
			putValue(SHORT_DESCRIPTION, "Switch to beginner mode (only the most important parameters are shown)");
		} else {
			putValue(SMALL_ICON, EXPERT_MODE_ICONS[iconSize.ordinal()]);
			putValue(SHORT_DESCRIPTION, "Switch to expert mode (all available parameters are visible)");
		}
	}
}
