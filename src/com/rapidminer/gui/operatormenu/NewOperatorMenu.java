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
package com.rapidminer.gui.operatormenu;

import javax.swing.Icon;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.tools.OperatorService;


/**
 * An operator menu which can be used to add a new operator to the currently
 * selected operator. This operator menu is available in the context menu of an
 * operator in tree view.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: NewOperatorMenu.java,v 1.2 2007/06/01 23:15:50 ingomierswa Exp $
 */
public class NewOperatorMenu extends OperatorMenu {

	private static final long serialVersionUID = 1L;

	private static final String NEW_OPERATOR_ICON_NAME = "icons/24/element_new.png";
	
	private static Icon newOperatorIcon = null;
	
	static {
		// init icon
		newOperatorIcon = SwingTools.createIcon(NEW_OPERATOR_ICON_NAME);
	}
	
	protected NewOperatorMenu() {
		super("New Operator", false);
		setIcon(newOperatorIcon);
	}

	public void performAction(OperatorDescription description) {
		try {
			Operator operator = OperatorService.createOperator(description);
			RapidMinerGUI.getMainFrame().getOperatorTree().insert(operator);
		} catch (Exception e) {
			SwingTools.showSimpleErrorMessage("Cannot instantiate '" + description.getName() + "'.", e);
		}
	}

}
