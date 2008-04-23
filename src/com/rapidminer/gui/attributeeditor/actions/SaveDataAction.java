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
package com.rapidminer.gui.attributeeditor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import com.rapidminer.gui.attributeeditor.AttributeEditor;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.SwingTools;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: SaveDataAction.java,v 1.1 2007/05/27 22:02:54 ingomierswa Exp $
 */
public class SaveDataAction extends AbstractAction {

	private static final long serialVersionUID = -4272773008253836867L;

	private static final String ICON_NAME = "data_disk.png";
	
	private static final Icon[] ICONS = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			ICONS[counter++] = SwingTools.createIcon("icons/" + size.getSize() + "/" + ICON_NAME);
		}
	}

	private AttributeEditor attributeEditor;
	
	public SaveDataAction(AttributeEditor attributeEditor, IconSize size) {
		super("Save Data as...", ICONS[size.ordinal()]);
		putValue(SHORT_DESCRIPTION, "Save the contents of the table to a single data file");
		putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_V));
		this.attributeEditor = attributeEditor;
	}

	public void actionPerformed(ActionEvent e) {
		File file = SwingTools.chooseFile(this.attributeEditor, null, false, "dat", "data file");
		if (file != null) {
			try {
				attributeEditor.writeData(file);
			} catch (java.io.IOException ex) {
				JOptionPane.showMessageDialog(this.attributeEditor, e.toString(), "Error saving data file " + file, JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
