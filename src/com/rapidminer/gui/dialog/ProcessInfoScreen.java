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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;


/**
 * This dialog is shown after loading a process definition if the root operator has a
 * user comment (description tag). The text of this description is shown in this
 * dialog.
 * 
 * @author Ingo Mierswa
 * @version $Id: ProcessInfoScreen.java,v 1.3 2008/05/09 19:23:20 ingomierswa Exp $
 */
public class ProcessInfoScreen extends JDialog {

	private static final long serialVersionUID = 7687035897010730802L;

	public ProcessInfoScreen(Frame owner, String file, String text) {
		super(owner, "Process Info", true);

		JPanel rootPanel = new JPanel(new BorderLayout());
		rootPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagLayout layout = new GridBagLayout();
		JPanel mainPanel = new JPanel(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.ipadx = 10;
		c.ipady = 10;
		c.weightx = 1.0d;
		c.weighty = 0.0d;

		JLabel label = new JLabel("Process file:");
		label.setToolTipText("The name of the process definition file.");
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		label = new JLabel(file);
		label.setToolTipText("The name of the process definition file.");
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		c.weighty = 1.0d;
		JEditorPane description = new JEditorPane("text/html", SwingTools.text2DisplayHtml(text));
		description.setToolTipText("A short description of this process");
		description.setEditable(false);
		description.setBackground(this.getBackground());
		JScrollPane textScrollPane = new ExtendedJScrollPane(description);
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(textScrollPane, c);
		mainPanel.add(textScrollPane);
		c.weighty = 0.0d;

		rootPanel.add(mainPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});
		buttonPanel.add(okButton);
		rootPanel.add(buttonPanel, BorderLayout.SOUTH);

		getContentPane().add(rootPanel);

		description.setCaretPosition(0);
		
		pack();
		setSize(600, 450);
		setLocationRelativeTo(owner);
	}

	private void ok() {
		dispose();
	}
}
