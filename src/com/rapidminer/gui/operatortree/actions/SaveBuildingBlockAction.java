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
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Icon;

import com.rapidminer.gui.ConditionalAction;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.operatortree.OperatorTree;
import com.rapidminer.gui.templates.BuildingBlock;
import com.rapidminer.gui.templates.SaveAsBuildingBlockDialog;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.tools.ParameterService;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: SaveBuildingBlockAction.java,v 1.4 2008/05/09 19:23:18 ingomierswa Exp $
 */
public class SaveBuildingBlockAction extends ConditionalAction {

	private static final long serialVersionUID = 2238740826770976483L;

	private static final String ICON_NAME = "box_add.png";
	
	private static final Icon[] ICONS = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			ICONS[counter++] = SwingTools.createIcon(size.getSize() + "/" + ICON_NAME);
		}
	}

	private OperatorTree operatorTree;
	
	public SaveBuildingBlockAction(OperatorTree operatorTree, IconSize size) {
		super("Save as Building Block...", ICONS[size.ordinal()]);
		putValue(SHORT_DESCRIPTION, "Save the selected operator as a new building block");
		putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_S));
		setCondition(OPERATOR_SELECTED, MANDATORY);
		setCondition(ROOT_SELECTED, DISALLOWED);
		this.operatorTree = operatorTree;
	}

	public void actionPerformed(ActionEvent e) {
		Operator selectedOperator = this.operatorTree.getSelectedOperator();
		if (selectedOperator != null) {
			SaveAsBuildingBlockDialog dialog = new SaveAsBuildingBlockDialog(RapidMinerGUI.getMainFrame(), selectedOperator);
			dialog.setVisible(true);
			if (dialog.isOk()) {
				BuildingBlock buildingBlock = dialog.getBuildingBlock(selectedOperator);
				String name = buildingBlock.getName();
				try {
					File buildingBlockFile = ParameterService.getUserConfigFile(name + ".buildingblock");
					buildingBlock.save(buildingBlockFile);
				} catch (IOException ioe) {
					SwingTools.showSimpleErrorMessage("Cannot write building block file:", ioe);
				}
			}
		}
	}
}
