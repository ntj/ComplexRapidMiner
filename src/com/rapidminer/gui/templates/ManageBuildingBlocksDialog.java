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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.BuildingBlockService;
import com.rapidminer.tools.LogService;


/**
 * The manage building blocks dialog assists the user in managing the saved building blocks.
 * Building Blocks are saved in the local &quot;.rapidminer&quot;
 * directory of the user. In this dialog the user can delete his templates.
 * 
 * @author Ingo Mierswa
 * @version $Id: ManageBuildingBlocksDialog.java,v 1.5 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class ManageBuildingBlocksDialog extends JDialog {

	private static final long serialVersionUID = -2146505003821251075L;

	private JList buildingBlockList = new JList();

	private Map<String, BuildingBlock> buildingBlockMap = new HashMap<String, BuildingBlock>();

	public ManageBuildingBlocksDialog(MainFrame mainFrame) {
		super(mainFrame, "Manage Building Blocks", true);

		JPanel rootPanel = new JPanel(new BorderLayout());
		rootPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagLayout layout = new GridBagLayout();
		JPanel mainPanel = new JPanel(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0d;
		c.weighty = 0.0d;

		JPanel textPanel = 
			SwingTools.createTextPanel(
					"Manage Building Blocks...", 
					"Please select building blocks to delete them. Only user defined building blocks can be removed.");
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(textPanel, c);
		mainPanel.add(textPanel);

		Component sep = Box.createVerticalStrut(10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(sep, c);
		mainPanel.add(sep);

		// add components to main panel
		Collection<BuildingBlock> buildingBlocks = BuildingBlockService.getUserBuildingBlocks();
		Iterator<BuildingBlock> i = buildingBlocks.iterator();
		while (i.hasNext()) {
			BuildingBlock buildingBlock = i.next();
			buildingBlockMap.put(buildingBlock.getName(), buildingBlock);
		}

		JScrollPane listPane = new ExtendedJScrollPane(buildingBlockList);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 1.0d;
		layout.setConstraints(listPane, c);
		mainPanel.add(listPane);
		c.weighty = 0.0d;

		// buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				delete();
			}
		});
		buttonPanel.add(deleteButton);
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});
		buttonPanel.add(okButton);

		rootPanel.add(mainPanel, BorderLayout.CENTER);
		rootPanel.add(buttonPanel, BorderLayout.SOUTH);
		getContentPane().add(rootPanel);

		update();
		pack();
		setSize(250, 400);
		setLocationRelativeTo(mainFrame);
	}

	private void update() {
		Vector<String> data = new Vector<String>();
		Iterator<BuildingBlock> i = buildingBlockMap.values().iterator();
		while (i.hasNext()) {
			BuildingBlock buildingBlock = i.next();
			data.add(buildingBlock.getName());
		}
		buildingBlockList.setListData(data);
		repaint();
	}

	private void ok() {
		dispose();
	}

	private void delete() {
		Object[] selection = buildingBlockList.getSelectedValues();
		for (int i = 0; i < selection.length; i++) {
			String name = (String) selection[i];
			BuildingBlock buildingBlock = buildingBlockMap.remove(name);
			File buildingBlockFile = buildingBlock.getFile();
			if (buildingBlockFile != null) {
				boolean result = buildingBlockFile.delete();
				if (!result)
					LogService.getGlobal().logWarning("Unable to delete building block file: " + buildingBlockFile);
			}
		}
		update();
	}
}
