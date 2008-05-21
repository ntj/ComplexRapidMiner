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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.UIManager;

import com.rapidminer.gui.RapidMinerGUI;

/**
 * The dialog asks the user if he wants to save the current process. It also 
 * has an option &quot;Don't ask again&quot; which allows to turn off this
 * possibly annoying dialog.
 * 
 * @author Ingo Mierswa 
 * @version $Id: RequestSaveDialog.java,v 1.4 2008/05/09 19:23:21 ingomierswa Exp $
 */
public class RequestSaveDialog extends JDialog {

	private static final long serialVersionUID = 2334018675086892610L;

	private boolean ok = false;
	
	private boolean doNotAskAgain = false;
	
	public RequestSaveDialog(String title, String text) {
		super(RapidMinerGUI.getMainFrame(), title, true);
		setTitle(title);
		
		// from JOptionPane.java
		if (JDialog.isDefaultLookAndFeelDecorated()) {
			if (UIManager.getLookAndFeel().getSupportsWindowDecorations()) {
				setUndecorated(true);
				getRootPane().setWindowDecorationStyle(JRootPane.QUESTION_DIALOG);
			}
		}
		
		getContentPane().setLayout(new BorderLayout());

		final JPanel mainPanel = new JPanel();
		final GridBagLayout gbl = new GridBagLayout();
		mainPanel.setLayout(gbl);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(3, 3, 3, 3);
		c.gridwidth = GridBagConstraints.RELATIVE;

		// icon
		Icon icon = UIManager.getIcon("OptionPane.questionIcon");
		JLabel iconLabel = new JLabel(icon);
		iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 7));
		gbl.setConstraints(iconLabel, c);
		mainPanel.add(iconLabel);
		
		// text
		JLabel textLabel = new JLabel(text);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		mainPanel.add(textLabel);
		
		add(mainPanel, BorderLayout.CENTER);
		
		
		// buttons
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints innerC = new GridBagConstraints();
		innerC.fill = GridBagConstraints.BOTH;
		innerC.weightx = 0;
		innerC.weighty = 0;
		innerC.insets = new Insets(4,4,4,4);
		JPanel buttonPanel = new JPanel(layout);
		
		JCheckBox dontAskAgainBox = new JCheckBox("Do not ask again", doNotAskAgain);
		dontAskAgainBox.setToolTipText("Deactivates this dialog before process starts. Can be re-activated in the settings dialog of RapidMiner.");
		dontAskAgainBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doNotAskAgain = !doNotAskAgain;
			}
		});
		dontAskAgainBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
		layout.setConstraints(dontAskAgainBox, innerC);
		buttonPanel.add(dontAskAgainBox);
		
		
		JPanel fillPanel = new JPanel();
		innerC.weightx = 1.0;
		layout.setConstraints(fillPanel, innerC);
		buttonPanel.add(fillPanel);
		
		JButton yesButton = new JButton("Yes");
		yesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = true;
				dispose();
			}
		});
		yesButton.setPreferredSize(new Dimension(60, (int)yesButton.getPreferredSize().getHeight()));
		innerC.weightx = 0;
		layout.setConstraints(yesButton, innerC);
		buttonPanel.add(yesButton);
		
		JButton noButton = new JButton("No");
		noButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = false;
				dispose();
			}
		});
		noButton.setPreferredSize(new Dimension(60, (int)noButton.getPreferredSize().getHeight()));
		layout.setConstraints(noButton, innerC);
		buttonPanel.add(noButton);
		
		add(buttonPanel, BorderLayout.SOUTH);

		pack();
		setResizable(false);
		setLocationRelativeTo(RapidMinerGUI.getMainFrame());
		noButton.requestFocusInWindow();
	}
	
	public boolean isOk() {
		return this.ok;
	}
	
	public boolean shouldNotAskAgain() {
		return doNotAskAgain;
	}
}
