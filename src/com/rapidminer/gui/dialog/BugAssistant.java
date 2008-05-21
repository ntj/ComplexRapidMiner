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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.text.JTextComponent;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.BugReport;
import com.rapidminer.tools.Tools;


/**
 * This dialog is shown in cases where a non-user error occured and the user
 * decided to send a bugreport. Collects all necessary data for bug fixing and
 * creates a zip file from the data.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: BugAssistant.java,v 1.4 2008/05/09 19:23:20 ingomierswa Exp $
 */
public class BugAssistant extends JDialog {

	private static final long serialVersionUID = 8379605320787188372L;

	private static final String INSTRUCTIONS = "<html><h4>Send Bug Report</h4>"
			+ "This dialog will help you to file a bug report. It will automatically collect most of the data necessary to examine the problem. The assistant will create a zip file containing your process setup (xml file), parameter settings, log file, and information about the runtime environment. RapidMiner does <i>not</i> automatically add data files or any other files since these may contain confidential data and may be large. You can attach them using the \"Add file...\" button. When you are done, save the bug report to disc and send it by email to</p><center><b><tt>request@rapid-i.com</tt></b></center>";

	private static final String TEXT_INSTRUCTIONS = "Enter a brief description of what happened into this text field. Please also describe the purpose of your process definition because this cannot always be concluded trivially from the process setup.";

	private JTextArea message = new JTextArea(TEXT_INSTRUCTIONS, 5, 20);

	private JTextComponent name = new JTextField(15);

	private JList attachments = new JList(new DefaultListModel());

	public BugAssistant(final Throwable exception) {
		super(RapidMinerGUI.getMainFrame(), "Bug Report Assistant", true);
		GridBagLayout gbl = new GridBagLayout();
		getContentPane().setLayout(gbl);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		c.insets = new Insets(5, 5, 5, 5);

		c.gridwidth = GridBagConstraints.REMAINDER;
		JLabel instructions = new JLabel(INSTRUCTIONS);
		instructions.setFont(instructions.getFont().deriveFont(Font.PLAIN));
		gbl.setConstraints(instructions, c);
		getContentPane().add(instructions);

		JLabel nameLabel = new JLabel("Your email address");
		c.gridwidth = 1;
		gbl.setConstraints(nameLabel, c);
		getContentPane().add(nameLabel);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(name, c);
		getContentPane().add(name);

		message.setLineWrap(true);
		message.setWrapStyleWord(true);
		JScrollPane messagePane = new ExtendedJScrollPane(message);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 5;
		gbl.setConstraints(messagePane, c);
		getContentPane().add(messagePane);

		c.weighty = 0;
		c.gridwidth = 1;
		c.gridheight = 2;
		attachments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane attachmentPane = new JScrollPane(attachments);
		gbl.setConstraints(attachmentPane, c);
		getContentPane().add(attachmentPane);

		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 1;
		c.gridy = 3;
		JButton add = new JButton("Add file...");
		add.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				File file = SwingTools.chooseFile(null, null, true, null, null);
				if (file != null)
					((DefaultListModel) attachments.getModel()).addElement(file);
			}
		});
		gbl.setConstraints(add, c);
		getContentPane().add(add);
		c.gridx = 1;
		c.gridy = 4;
		JButton remove = new JButton("Remove file");
		remove.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (attachments.getSelectedIndex() >= 0) {
					((DefaultListModel) attachments.getModel()).remove(attachments.getSelectedIndex());
				}
			}
		});
		gbl.setConstraints(remove, c);
		getContentPane().add(remove);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.gridx = 2;
		c.gridy = 3;
		JButton save = new JButton("Save Report...");
		save.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String email = name.getText().trim();
				if (email.length() == 0) {
					JOptionPane.showMessageDialog(RapidMinerGUI.getMainFrame(), "Please enter an email address.", "Form incomplete", JOptionPane.ERROR_MESSAGE);
					return;
				}
				File file = SwingTools.chooseFile(null, new File("bugreport.zip"), false, false, ".zip", "zip archives");
				if (file != null) {
					try {
						ListModel model = attachments.getModel();
						File[] attachments = new File[model.getSize()];
						for (int i = 0; i < attachments.length; i++) {
							attachments[i] = (File) model.getElementAt(i);
						}
						BugReport.createBugReport(file, exception, "From: " + name.getText() + Tools.getLineSeparator() + "Date: " + new java.util.Date() + Tools.getLineSeparator() + Tools.getLineSeparator() + message.getText(), RapidMinerGUI.getMainFrame().getProcess(), RapidMinerGUI.getMainFrame().getMessageViewer().getLogMessage(), attachments);
						dispose();
					} catch (Throwable t) {
						SwingTools.showSimpleErrorMessage("Cannot create report file!", t);
					}
				}
			}
		});
		gbl.setConstraints(save, c);
		getContentPane().add(save);
		c.gridx = 2;
		c.gridy = 4;
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		gbl.setConstraints(cancel, c);
		getContentPane().add(cancel);

		pack();

		message.setSelectionStart(0);
		message.setSelectionEnd(TEXT_INSTRUCTIONS.length() - 1);

		setLocationRelativeTo(RapidMinerGUI.getMainFrame());
	}

	public Dimension getPreferredSize() {
		return new Dimension(RapidMinerGUI.getMainFrame().getWidth() * 3 / 4, RapidMinerGUI.getMainFrame().getHeight() * 3 / 4);
	}
}
