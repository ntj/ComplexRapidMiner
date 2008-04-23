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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.tools.GroupTree;
import com.rapidminer.tools.OperatorService;


/**
 * This is the abstract superclass for all menu containing operators. An
 * operator menu can be used for selecting new operators (see
 * {@link NewOperatorMenu}) or replacing operators (see
 * {@link ReplaceOperatorMenu}).
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: OperatorMenu.java,v 1.1 2007/05/27 22:04:00 ingomierswa Exp $
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
				Image icon = description.getIcon();
				if (icon == null)
					item = new JMenuItem(description.getName());
				else
					item = new JMenuItem(description.getName(), new ImageIcon(icon));
				item.setToolTipText(SwingTools.transformToolTipText(description.getDescription()));
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
