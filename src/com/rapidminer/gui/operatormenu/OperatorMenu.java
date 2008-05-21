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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.tools.GroupTree;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.Tools;


/**
 * This is the abstract superclass for all menu containing operators. An
 * operator menu can be used for selecting new operators (see
 * {@link NewOperatorMenu}) or replacing operators (see
 * {@link ReplaceOperatorMenu}).
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: OperatorMenu.java,v 1.5 2008/05/09 19:23:19 ingomierswa Exp $
 */
public abstract class OperatorMenu extends JMenu {

	public static final OperatorMenu NEW_OPERATOR_MENU = new NewOperatorMenu();

	public static final OperatorMenu REPLACE_OPERATOR_MENU = new ReplaceOperatorMenu(false);

	public static final OperatorMenu REPLACE_OPERATORCHAIN_MENU = new ReplaceOperatorMenu(true);

	protected OperatorMenu(String name, boolean onlyChains) {
		super(name);
		addMenu(OperatorService.getGroups(), this, onlyChains);
	}

	public void addMenu(GroupTree group, JMenu menu, boolean onlyChains) {
		Iterator i = group.getSubGroups().iterator();
		while (i.hasNext()) {
			GroupTree subGroup = (GroupTree) i.next();
			JMenu subMenu = new JMenu(subGroup.getName());
			addMenu(subGroup, subMenu, onlyChains);
			if (subMenu.getItemCount() > 0)
				menu.add(subMenu);
		}
		i = group.getOperatorDescriptions().iterator();
		while (i.hasNext()) {
			final OperatorDescription description = (OperatorDescription) i.next();
			if ((!onlyChains) || OperatorChain.class.isAssignableFrom(description.getOperatorClass())) {
				JMenuItem item = null;
				Icon icon = description.getIcon();
				if (icon == null)
					item = new JMenuItem(description.getName());
				else
					item = new JMenuItem(description.getName(), icon);
				String descriptionText = description.getLongDescriptionHTML();
				if (descriptionText == null) {
					descriptionText = description.getShortDescription();
				}
				
				StringBuffer toolTipText = new StringBuffer("<b>Description: </b>" + descriptionText);
				Operator operator = null;
				try {
					operator = description.createOperatorInstance();
				} catch (OperatorCreationException e1) {
					// do nothing
				}
		        if (operator != null) {
		        	toolTipText.append(Tools.getLineSeparator() + "<b>Input:</b> " + SwingTools.getStringFromClassArray(operator.getInputClasses()));
		        	toolTipText.append(Tools.getLineSeparator() + "<b>Output:</b> " + SwingTools.getStringFromClassArray(operator.getOutputClasses()));
		        }
				item.setToolTipText(SwingTools.transformToolTipText(toolTipText.toString()));
				item.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						performAction(description);
					}
				});
				menu.add(item);
			}
		}
	}

	public abstract void performAction(OperatorDescription description);
}
