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
 * @version $Id: BoxViewerExportAction.java,v 1.5 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class BoxViewerExportAction extends AbstractAction {

	private static final long serialVersionUID = 70024710795760783L;

	private static final String ICON_NAME = "export1.png";
	
	private static final Icon[] ICONS = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			ICONS[counter++] = SwingTools.createIcon(size.getSize() + "/" + ICON_NAME);
		}
	}
	
	private BoxViewerDialog boxViewer;
	
	public BoxViewerExportAction(BoxViewerDialog boxViewer, IconSize size) {
		super("Export...", ICONS[size.ordinal()]);
		this.boxViewer = boxViewer;
		putValue(SHORT_DESCRIPTION, "Export view to graphics file");
		putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_E));
	}

	public void actionPerformed(ActionEvent e) {
		this.boxViewer.exportProcess();
	}
}
