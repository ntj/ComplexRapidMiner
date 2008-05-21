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
package com.rapidminer.gui.templates;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.BuildingBlockService;
import com.rapidminer.tools.LogService;


/** This dialog can be used to add a new building block to the process setup. 
 *  
 *  @author Ingo Mierswa
 *  @version $Id: NewBuildingBlockDialog.java,v 1.4 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class NewBuildingBlockDialog extends JDialog {
		
	private static final long serialVersionUID = 4234757981716378086L;

	private boolean ok = false;
	
	private JList buildingBlockList = new JList();
	
	public NewBuildingBlockDialog(MainFrame mainFrame) {
		super(mainFrame, "Insert Building Block", true);
		
		JPanel rootPanel = new JPanel(new BorderLayout());
		rootPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagLayout layout = new GridBagLayout();
		JPanel mainPanel = new JPanel(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0d;
		c.weighty = 0.0d;
		
		JPanel textPanel = 
			SwingTools.createTextPanel("Add Building Block...", "Please select the building block which should be added.");
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(textPanel, c);
		mainPanel.add(textPanel);
		
		Component sep = Box.createVerticalStrut(10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(sep, c);
		mainPanel.add(sep);
		
		// add components to main panel
		List<BuildingBlock> buildingBlocks = BuildingBlockService.getBuildingBlocks();
		Iterator<BuildingBlock> i = buildingBlocks.iterator();
		while (i.hasNext()) {
			BuildingBlock currentBB = i.next();
			if (!NewBuildingBlockMenu.checkBuildingBlock(currentBB)) {
				i.remove();
                LogService.getGlobal().log("Cannot initialize building block '" + currentBB.getName(), LogService.WARNING);
        	}
        }
		BuildingBlock[] buildingBlockArray = new BuildingBlock[buildingBlocks.size()];
		buildingBlocks.toArray(buildingBlockArray);
		buildingBlockList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		buildingBlockList.setListData(buildingBlockArray);
        
		JScrollPane listPane = new ExtendedJScrollPane(buildingBlockList);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 1.0d;
		layout.setConstraints(listPane, c);
		mainPanel.add(listPane);
		c.weighty = 0.0d;
		
		// buttons
		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});
		buttonPanel.add(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		buttonPanel.add(cancelButton);
		
		rootPanel.add(mainPanel, BorderLayout.CENTER);
		rootPanel.add(buttonPanel, BorderLayout.SOUTH);
		getContentPane().add(rootPanel);
		
		pack();
		setSize(250, 400);
		setLocationRelativeTo(mainFrame);
	}
	
	private void ok() {
		this.ok = true;
		dispose();
	}
	
	private void cancel() {
		this.ok = false;
		dispose();
	}
	
	public boolean isOk() {
		return ok;
	}
	
	public BuildingBlock getSelectedBuildingBlock() {
		return (BuildingBlock)buildingBlockList.getSelectedValue();
	}
}
