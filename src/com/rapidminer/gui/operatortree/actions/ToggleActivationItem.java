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
package com.rapidminer.gui.operatortree.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;

import com.rapidminer.gui.operatortree.OperatorTree;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: ToggleActivationItem.java,v 1.1 2007/05/27 22:02:37 ingomierswa Exp $
 */
public class ToggleActivationItem extends JCheckBoxMenuItem implements ActionListener {

	private static final long serialVersionUID = -115721139040021914L;

	private OperatorTree operatorTree;
	
	public ToggleActivationItem(OperatorTree operatorTree, boolean state) {
		super("Enable Operator", state);
		this.operatorTree = operatorTree;
		setMnemonic(KeyEvent.VK_E);
		addActionListener(this);
		setToolTipText("Enable or disable the selected operator");
	}

	public void actionPerformed(ActionEvent e) {
		this.operatorTree.toggleOperatorActivation(getState());
	}
}
