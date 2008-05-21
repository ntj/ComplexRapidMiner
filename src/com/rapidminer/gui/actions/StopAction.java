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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import com.rapidminer.gui.ConditionalAction;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.SwingTools;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: StopAction.java,v 1.5 2008/05/09 19:22:46 ingomierswa Exp $
 */
public class StopAction extends ConditionalAction {

	private static final long serialVersionUID = 1181186062812851781L;

	private static final String ICON_NAME = "media_stop.png";
	
	private static final Icon[] ICONS = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			ICONS[counter++] = SwingTools.createIcon(size.getSize() + "/" + ICON_NAME);
		}
	}
		
	private MainFrame mainFrame;
	
	public StopAction(MainFrame mainFrame, IconSize size) {
		super("Stop", ICONS[size.ordinal()]);
		putValue(SHORT_DESCRIPTION, "Stop the execution of the current process");
		putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_S));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.CTRL_MASK));
		setCondition(PROCESS_RUNNING, MANDATORY);
		this.mainFrame = mainFrame;
	}

	public void actionPerformed(ActionEvent e) {
		this.mainFrame.stopProcess();
	}
}
