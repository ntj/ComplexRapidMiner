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
package com.rapidminer.gui.operatortree.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;

import com.rapidminer.BreakpointListener;
import com.rapidminer.gui.operatortree.OperatorTree;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.SwingTools;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: ToggleBreakpointItem.java,v 1.4 2008/05/09 19:23:18 ingomierswa Exp $
 */
public class ToggleBreakpointItem extends JCheckBoxMenuItem implements ActionListener {

	private static final long serialVersionUID = 1727841552148351670L;

	private static final String UP_ICON_NAME     = "breakpoint_up.png";
	private static final String WITHIN_ICON_NAME = "breakpoint.png";
	private static final String DOWN_ICON_NAME   = "breakpoint_down.png";
	
	private static final Icon[] UP_ICONS     = new Icon[IconSize.values().length];
	private static final Icon[] WITHIN_ICONS = new Icon[IconSize.values().length];
	private static final Icon[] DOWN_ICONS   = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			UP_ICONS[counter++] = SwingTools.createIcon(size.getSize() + "/" + UP_ICON_NAME);
		}
		
		counter = 0;
		for (IconSize size : IconSize.values()) {
			WITHIN_ICONS[counter++] = SwingTools.createIcon(size.getSize() + "/" + WITHIN_ICON_NAME);
		}
		
		counter = 0;
		for (IconSize size : IconSize.values()) {
			DOWN_ICONS[counter++] = SwingTools.createIcon(size.getSize() + "/" + DOWN_ICON_NAME);
		}
	}

	private int position;

	private OperatorTree operatorTree;
	
	public ToggleBreakpointItem(OperatorTree operatorTree, int position, IconSize size) {
		super("Breakpoint " + BreakpointListener.BREAKPOINT_POS_NAME_UPPERCASE[position]);
		this.position = position;
		this.operatorTree = operatorTree;
		addActionListener(this);
		setToolTipText("Toggle breakpoint " + BreakpointListener.BREAKPOINT_POS_NAME[position] + " for the selected operator");
		switch (position) {
		case BreakpointListener.BREAKPOINT_BEFORE:
			setMnemonic(KeyEvent.VK_B);
            setIcon(UP_ICONS[size.ordinal()]);
			break;
		case BreakpointListener.BREAKPOINT_WITHIN:
			setMnemonic(KeyEvent.VK_W);
            setIcon(WITHIN_ICONS[size.ordinal()]);
			break;
		case BreakpointListener.BREAKPOINT_AFTER:
			setMnemonic(KeyEvent.VK_A);
            setIcon(DOWN_ICONS[size.ordinal()]);
			break;
		}
	}

	public void actionPerformed(ActionEvent e) {
		this.operatorTree.toggleBreakpoint(position, getState());
	}
}
