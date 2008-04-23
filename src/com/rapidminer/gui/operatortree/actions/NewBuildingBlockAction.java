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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.StringReader;

import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.rapidminer.gui.ConditionalAction;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.operatortree.OperatorTree;
import com.rapidminer.gui.templates.BuildingBlock;
import com.rapidminer.gui.templates.NewBuildingBlockDialog;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: NewBuildingBlockAction.java,v 1.2 2007/06/01 23:15:50 ingomierswa Exp $
 */
public class NewBuildingBlockAction extends ConditionalAction {

	private static final long serialVersionUID = 3466426013029085115L;

	private static final String ICON_NAME = "box_new.png";
	
	private static final Icon[] ICONS = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			ICONS[counter++] = SwingTools.createIcon("icons/" + size.getSize() + "/" + ICON_NAME);
		}
	}

	private OperatorTree operatorTree;
	
	public NewBuildingBlockAction(OperatorTree operatorTree, IconSize size) {
		super("New BB...", ICONS[size.ordinal()]);
		putValue(SHORT_DESCRIPTION, "Insert a new building block");
		putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_B));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK));
		setCondition(OPERATOR_CHAIN_SELECTED, MANDATORY);
		this.operatorTree = operatorTree;
	}

	public void actionPerformed(ActionEvent e) {
		Operator selectedOperator = this.operatorTree.getSelectedOperator();
		if (selectedOperator != null) {
			NewBuildingBlockDialog dialog = new NewBuildingBlockDialog(RapidMinerGUI.getMainFrame());
			dialog.setVisible(true);
			if (dialog.isOk()) {
				try {
					BuildingBlock buildingBlock = dialog.getSelectedBuildingBlock();
					if (buildingBlock != null) {
						String xmlDescription = buildingBlock.getXML();
						try {
							InputSource source = new InputSource(new StringReader(xmlDescription));
							Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);
							Element element = document.getDocumentElement();
                            Operator operator = Operator.createFromXML(element);
							RapidMinerGUI.getMainFrame().getOperatorTree().insert(operator);
						} catch (Exception ex) {
							SwingTools.showSimpleErrorMessage("Cannot instantiate building block '" + buildingBlock.getName() + "'.", ex);
						}
					}
				} catch (Exception ex) {
					SwingTools.showSimpleErrorMessage("Cannot create building block:", ex);
				}
			}
		}
	}
}
