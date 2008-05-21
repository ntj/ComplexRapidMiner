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
package com.rapidminer.gui.tools;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;


/**
* Start the corresponding action.
* 
* @author Ingo Mierswa, Helge Homburg
* @version $Id: OperatorListInfoOperatorAction.java,v 1.6 2008/05/09 19:22:59 ingomierswa Exp $
*/
public class OperatorListInfoOperatorAction extends AbstractAction {

	private static final long serialVersionUID = -8196515665251937769L;

	private static final String ICON_NAME = "information2.png";
	
	private static final Icon[] ICONS = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			ICONS[counter++] = SwingTools.createIcon(size.getSize() + "/" + ICON_NAME);
		}
	}

	private OperatorList operatorList;
	
	public OperatorListInfoOperatorAction(OperatorList operatorList, IconSize size) {
		super("Operator Info", ICONS[size.ordinal()]);
		putValue(SHORT_DESCRIPTION, "Shows information about the selected operator");
		putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_I));
		this.operatorList = operatorList;
	}

	public void actionPerformed(ActionEvent e) {
		this.operatorList.showOperatorInfo();
	}
}
