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
package com.rapidminer.gui.dialog.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.rapidminer.gui.dialog.boxviewer.BoxViewerDialog;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.SwingTools;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: BoxViewerPrintAction.java,v 1.2 2007/06/07 17:12:24 ingomierswa Exp $
 */
public class BoxViewerPrintAction extends AbstractAction {

	private static final long serialVersionUID = 447825818092386561L;

	private static final String ICON_NAME = "printer.png";
	
	private static final Icon[] ICONS = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			ICONS[counter++] = SwingTools.createIcon("icons/" + size.getSize() + "/" + ICON_NAME);
		}
	}
	
	private BoxViewerDialog boxViewer;
	
	public BoxViewerPrintAction(BoxViewerDialog boxViewer, IconSize size) {
		super("Print", ICONS[size.ordinal()]);
		this.boxViewer = boxViewer;
		putValue(SHORT_DESCRIPTION, "Prints the current view.");
		putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_P));
	}

	public void actionPerformed(ActionEvent e) {
		this.boxViewer.printProcess();
	}
}
