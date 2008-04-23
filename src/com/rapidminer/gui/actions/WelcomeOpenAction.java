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
package com.rapidminer.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.tools.SwingTools;

/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: WelcomeOpenAction.java,v 1.2 2007/06/07 17:12:19 ingomierswa Exp $
 */
public class WelcomeOpenAction extends AbstractAction {

	private static final long serialVersionUID = 1358354112149248404L;

	private static final String ICON_NAME = "folder_add.png";
	
	private static Icon icon = null;
	
	static {
		icon = SwingTools.createIcon("icons/48/" + ICON_NAME);
	}
		
	private MainFrame mainFrame;
	
	public WelcomeOpenAction(MainFrame mainFrame) {
		super("Open", icon);
		putValue(SHORT_DESCRIPTION, "Open an existing process definition");
		this.mainFrame = mainFrame;
	}

	public void actionPerformed(ActionEvent e) {
		this.mainFrame.changeMode(MainFrame.EDIT_MODE);
		mainFrame.open();
	}
}
