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

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.rapidminer.gui.attributeeditor.AttributeEditor;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.SwingTools;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: GuessTypeAction.java,v 1.1 2007/05/27 22:02:54 ingomierswa Exp $
 */
public class GuessTypeAction extends AbstractAction {

	private static final long serialVersionUID = -5897840150549169907L;

	private static final String ICON_NAME = "unknown.png";
	
	private static final Icon[] ICONS = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			ICONS[counter++] = SwingTools.createIcon("icons/" + size.getSize() + "/" + ICON_NAME);
		}
	}

	private AttributeEditor attributeEditor;
	
	public GuessTypeAction(AttributeEditor attributeEditor, IconSize size) {
		super("Guess value type", ICONS[size.ordinal()]);
		putValue(SHORT_DESCRIPTION, "Guess the value type of the current column based on the data.");
		putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_G));
		this.attributeEditor = attributeEditor;
	}

	public void actionPerformed(ActionEvent e) {
		this.attributeEditor.guessColumnType();
	}
}
