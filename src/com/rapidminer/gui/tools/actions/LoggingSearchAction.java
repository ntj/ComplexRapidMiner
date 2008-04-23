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
package com.rapidminer.gui.tools.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.LoggingViewer;
import com.rapidminer.gui.tools.SwingTools;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: LoggingSearchAction.java,v 1.1 2007/05/27 22:03:12 ingomierswa Exp $
 */
public class LoggingSearchAction extends AbstractAction {

	private static final long serialVersionUID = -8380073257252178693L;

	private static final String ICON_NAME = "find.png";
	
	private static final Icon[] ICONS = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			ICONS[counter++] = SwingTools.createIcon("icons/" + size.getSize() + "/" + ICON_NAME);
		}
	}

	private LoggingViewer loggingViewer;
	
	public LoggingSearchAction(LoggingViewer loggingViewer, IconSize size) {
		super("Search...", ICONS[size.ordinal()]);
		this.loggingViewer = loggingViewer;
		putValue(SHORT_DESCRIPTION, "Search text in the logging output");
		putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_F));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		this.loggingViewer.performSearch();
	}
}
