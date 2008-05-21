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
import java.io.FileFilter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.Tools;


/**
 * The save as building block dialog assists the user in creating a new process
 * building block. Building blocks are saved in the local .rapidminer directory of the
 * user. The name and description can be specified by the user.
 * 
 * @author Ingo Mierswa
 * @version $Id: SaveAsBuildingBlockDialog.java,v 1.4 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class SaveAsBuildingBlockDialog extends JDialog {

	private static final long serialVersionUID = 7662184237558085856L;

	private boolean ok = false;

	private JTextField nameField = new JTextField();

	private JTextField descriptionField = new JTextField();

	/** Creates a new save as building block dialog. */
	public SaveAsBuildingBlockDialog(MainFrame mainFrame, Operator operator) {
		super(mainFrame, "Save as Building Block", true);

		JPanel rootPanel = new JPanel(new BorderLayout());
		rootPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagLayout layout = new GridBagLayout();
		JPanel mainPanel = new JPanel(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0d;

		JPanel textPanel = 
			SwingTools.createTextPanel(
					"Save this operator as building block",
					"Save this operator, its parameters and children as a building block which can ease future process setup." + 
					"Please specify a name and short description for this building block.");
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(textPanel, c);
		mainPanel.add(textPanel);

		Component sep = Box.createVerticalStrut(10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(sep, c);
		mainPanel.add(sep);

		// add components to main panel
		JLabel label = new JLabel("Name");
		label.setToolTipText("The name of the template.");
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		nameField.setToolTipText("The name of the template.");
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(nameField, c);
		mainPanel.add(nameField);

		label = new JLabel("Description");
		label.setToolTipText("A short description of this template.");
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		descriptionField.setToolTipText("A short description of this template.");
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(descriptionField, c);
		mainPanel.add(descriptionField);

		// buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
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
		setSize(600, 400);
		setLocationRelativeTo(mainFrame);
	}

	public boolean isOk() {
		return ok;
	}

	public BuildingBlock getBuildingBlock(Operator operator) {
		String name = nameField.getText();
		return new BuildingBlock(name, descriptionField.getText(), operator.getOperatorDescription().getIconPath(), operator.getXML(""));
	}

	private boolean checkIfNameOk() {
		String name = nameField.getText();
		if ((name == null) || (name.length() == 0)) {
			SwingTools.showVerySimpleErrorMessage("Please specify a name for this template!");
			return false;
		}

		File[] preDefinedBuildingBlockFiles = ParameterService.getConfigFile("buildingblocks").listFiles(new FileFilter() {

			public boolean accept(File file) {
				return file.getName().endsWith(".buildingblock");
			}
		});
		File[] userDefinedBuildingBlockFiles = ParameterService.getUserRapidMinerDir().listFiles(new FileFilter() {

			public boolean accept(File file) {
				return file.getName().endsWith(".buildingblock");
			}
		});

		File[] buildingBlockFiles = new File[preDefinedBuildingBlockFiles.length + userDefinedBuildingBlockFiles.length];
		System.arraycopy(preDefinedBuildingBlockFiles, 0, buildingBlockFiles, 0, preDefinedBuildingBlockFiles.length);
		System.arraycopy(userDefinedBuildingBlockFiles, 0, buildingBlockFiles, preDefinedBuildingBlockFiles.length, userDefinedBuildingBlockFiles.length);

		for (int i = 0; i < buildingBlockFiles.length; i++) {
			String tempName = buildingBlockFiles[i].getName().substring(0, buildingBlockFiles[i].getName().lastIndexOf("."));
			if (tempName.equals(name)) {
				SwingTools.showVerySimpleErrorMessage("Name '" + name + "' is already used." + Tools.getLineSeparator() + "Please change name or delete the old building block before!");
				return false;
			}
		}
		return true;
	}

	private void ok() {
		if (checkIfNameOk()) {
			ok = true;
			dispose();
		}
	}

	private void cancel() {
		ok = false;
		dispose();
	}
}
