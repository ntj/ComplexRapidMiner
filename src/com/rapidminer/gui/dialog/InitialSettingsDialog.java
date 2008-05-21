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
package com.rapidminer.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileSystemView;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.SwingTools;

/** This dialog can be used in order to define an initial workspace (for example
 *  after the installation of a new version). It is also used for the definition 
 *  of other important initialization options.
 *   
 *  @author Ingo Mierswa
 *  @version $Id: InitialSettingsDialog.java,v 1.8 2008/05/09 19:23:20 ingomierswa Exp $
 */
public class InitialSettingsDialog extends JDialog {
	
    private static final long serialVersionUID = 784141258768877739L;

	private static final String ICON_NAME = "folder.png";
	
	private static Icon openIcon = null;
	
	static {
		openIcon = SwingTools.createIcon("24/" + ICON_NAME);
	}
	
	private JTextField workspaceField = new JTextField();
	
	private JComboBox lookAndFeelBox = new JComboBox(RapidMinerGUI.LOOK_AND_FEELS);
	
	public InitialSettingsDialog(Frame owner, File oldWorkspace, String defaultWorkspaceName, String workspaceText, int defaultLookAndFeel, boolean showLookAndFeelSelection) {
		super(owner);
		setTitle("Select Workspace");
		SwingTools.setDialogIcon(this);
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		setLayout(new BorderLayout());
		
		Box mainPanel = new Box(BoxLayout.Y_AXIS);	
		
		
		// WORKSPACE		
		JPanel workspacePanel = new JPanel();
		workspacePanel.setBorder(BorderFactory.createEmptyBorder(7,7,7,7));
		GridBagLayout layout = new GridBagLayout();
		workspacePanel.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(4,4,4,4);
		
		String usedWorkspaceText = workspaceText;
		if (usedWorkspaceText == null) {
			usedWorkspaceText = "Please select a workspace directory. You should ensure that you can write into this directory. Please note that a set of initial sample process setups will be copied into this workspace directory. Hence, an existing directory named 'sample' in your workspace directory will be overwritten."; 	
		}
		JTextArea introText = new JTextArea(usedWorkspaceText, 4, 40);
		introText.setLineWrap(true);
		introText.setWrapStyleWord(true);
		introText.setEditable(false);
		introText.setBackground(workspacePanel.getBackground());
		
		c.weightx = 0;
		c.weighty = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(introText, c);
		workspacePanel.add(introText);

		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		if (oldWorkspace != null) {
			workspaceField.setText(oldWorkspace.getAbsolutePath());
		} else {
			FileSystemView fsv = FileSystemView.getFileSystemView();
			File workspaceProposal = new File(fsv.getDefaultDirectory(), defaultWorkspaceName);
			workspaceField.setText(workspaceProposal.getAbsolutePath());
		}
		layout.setConstraints(workspaceField, c);
		workspacePanel.add(workspaceField);
		
		JButton fileButton = null;
		if (openIcon != null) {
			fileButton = new JButton(openIcon);
		} else {
			fileButton = new JButton("...");
		}
		fileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File currentFile = null;
				String currentFilePath = workspaceField.getText();
				if ((currentFilePath != null) && (currentFilePath.length() > 0))
					currentFile = new File(currentFilePath);
				File file = SwingTools.chooseFile(null, currentFile, true, true, null, null);
				if (file != null) {
					workspaceField.setText(file.getAbsolutePath());
				}
			}
		});
		c.weightx = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(fileButton, c);
		workspacePanel.add(fileButton);
		
		mainPanel.add(workspacePanel);
		

		// LOOK AND FEEL
		if (showLookAndFeelSelection) {
			if (defaultLookAndFeel >= 0)
				lookAndFeelBox.setSelectedIndex(defaultLookAndFeel);
			
			JPanel lookAndFeelPanel = new JPanel();
			lookAndFeelPanel.setBorder(BorderFactory.createEmptyBorder(7,7,7,7));
			GridBagLayout plafLayout = new GridBagLayout();
			lookAndFeelPanel.setLayout(plafLayout);
			GridBagConstraints plafC = new GridBagConstraints();
			plafC.fill = GridBagConstraints.BOTH;
			plafC.insets = new Insets(4,4,4,4);

			JTextArea lookAndFeelText = new JTextArea("Select the look of RapidMiner. We recommend the more comfortable 'modern' look for most users.", 2, 40);
			lookAndFeelText.setLineWrap(true);
			lookAndFeelText.setWrapStyleWord(true);
			lookAndFeelText.setEditable(false);
			lookAndFeelText.setBackground(lookAndFeelPanel.getBackground());

			plafC.weightx = 0;
			plafC.weighty = 0;
			plafC.gridwidth = GridBagConstraints.REMAINDER;
			plafLayout.setConstraints(lookAndFeelText, plafC);
			lookAndFeelPanel.add(lookAndFeelText);

			JLabel lookAndFeelLabel = new JLabel("Preferred Look: ");
			plafC.gridwidth = GridBagConstraints.RELATIVE;
			plafLayout.setConstraints(lookAndFeelLabel, plafC);
			lookAndFeelPanel.add(lookAndFeelLabel);

			plafC.weightx = 1.0;
			plafC.gridwidth = GridBagConstraints.REMAINDER;
			plafLayout.setConstraints(lookAndFeelBox, plafC);
			lookAndFeelPanel.add(lookAndFeelBox);
			
			mainPanel.add(Box.createVerticalStrut(15));
			mainPanel.add(lookAndFeelPanel);
		}
		
		// MAIN COMPONENTS
		add(mainPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 7, 7, 7));
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String path = workspaceField.getText();
				if ((path != null) && (path.length() > 0)) {
					ok();
				} else {
					JOptionPane.showMessageDialog(null, "Please specify a valid workspace directory.", "Empty Workspace", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		okButton.setPreferredSize(new Dimension(60, (int)okButton.getPreferredSize().getHeight()));
		buttonPanel.add(okButton);
		add(buttonPanel, BorderLayout.SOUTH);
		
		pack();
		setLocationRelativeTo(null);
	}

	public String getWorkspacePath() {
		return workspaceField.getText();
	}
	
	public int getSelectedLookAndFeel() {
		return lookAndFeelBox.getSelectedIndex();
	}
	
	private void ok() {
		// write settings
		/*
		int dataManagementValue = DataRowFactory.TYPE_DOUBLE_ARRAY;
		if (dataManagement.getSelectedIndex() == 1)
			dataManagementValue = DataRowFactory.TYPE_FLOAT_ARRAY;
		System.setProperty(RapidMiner.PROPERTY_RAPIDMINER_DEFAULT_DATA_REPRESENTATION, dataManagementValue + "");
		ParameterService.writePropertyIntoMainUserConfigFile(RapidMiner.PROPERTY_RAPIDMINER_DEFAULT_DATA_REPRESENTATION, dataManagementValue + "");
		*/
		dispose();
	}
}
