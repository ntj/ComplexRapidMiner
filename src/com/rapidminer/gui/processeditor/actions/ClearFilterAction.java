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
package com.rapidminer.gui.processeditor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.rapidminer.gui.processeditor.NewOperatorGroupTree;
import com.rapidminer.gui.tools.SwingTools;

/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: ClearFilterAction.java,v 1.4 2008/05/09 19:23:02 ingomierswa Exp $
 */
public class ClearFilterAction extends AbstractAction {

	private static final long serialVersionUID = -5145325683059274310L;
	
	private static final Icon CLEAR_ICON;
	
	static {
		CLEAR_ICON = SwingTools.createIcon("16/delete2.png");
	}
	
	private NewOperatorGroupTree operatorGroupTree;
	
	public ClearFilterAction(NewOperatorGroupTree operatorGroupTree) {
		super("X", CLEAR_ICON);
		putValue(SHORT_DESCRIPTION, "Clear the current filter for the new operator tree above.");
		this.operatorGroupTree = operatorGroupTree;
	}

	public void actionPerformed(ActionEvent e) {
		this.operatorGroupTree.clearFilter();
	}
}
