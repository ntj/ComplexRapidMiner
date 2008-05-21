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
package com.rapidminer.gui.operatormenu;

import javax.swing.Icon;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.tools.OperatorService;


/**
 * An operator menu which can be used to replace the currently selected operator
 * by one of the same type. Simple operators can be by other simple operators or
 * operator chains, operator chains can only be replaced by other chains. This
 * operator menu is available in the context menu of an operator in tree view.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ReplaceOperatorMenu.java,v 2.12 2006/03/21 15:35:40 ingomierswa
 *          Exp $
 */
public class ReplaceOperatorMenu extends OperatorMenu {

	private static final long serialVersionUID = 1L;

	private static final String REPLACE_OPERATOR_ICON_NAME = "24/element_replace.png";
	
	private static Icon replaceOperatorIcon = null;
	
	static {
		// init icon
		replaceOperatorIcon = SwingTools.createIcon(REPLACE_OPERATOR_ICON_NAME);
	}
	
	protected ReplaceOperatorMenu(boolean onlyChains) {
		super("Replace Operator", onlyChains);
		setIcon(replaceOperatorIcon);
	}

	public void performAction(OperatorDescription description) {
		try {
			Operator operator = OperatorService.createOperator(description);
			RapidMinerGUI.getMainFrame().getOperatorTree().replace(operator);
		} catch (Exception e) {
			SwingTools.showSimpleErrorMessage("Cannot instantiate '" + description.getName() + "'.", e);
		}
	}

}
