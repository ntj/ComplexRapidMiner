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
package com.rapidminer.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * The stop dialog can be used by operators which allow to abort an iteration.
 * Instead of using a listener concept the operator should ask this dialog if it
 * should still perform its operation. Using this operator might be useful in
 * cases like evolutionary optimizing.
 * 
 * @author Ingo Mierswa
 * @version $Id: StopDialog.java,v 1.1 2007/05/27 22:02:45 ingomierswa Exp $
 */
public class StopDialog extends JDialog {

	private static final long serialVersionUID = -7090498773341030469L;

	private boolean stillRunning = true;

	public StopDialog(String title, String text) {
		super((Frame) null, title, false);
		getContentPane().setLayout(new BorderLayout());
		JLabel label = new JLabel(text);
		label.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		getContentPane().add(label, BorderLayout.CENTER);

		Icon informationIcon = UIManager.getIcon("OptionPane.informationIcon");

		if (informationIcon != null) {
			JLabel informationIconLabel = new JLabel(informationIcon);
			informationIconLabel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
			getContentPane().add(informationIconLabel, BorderLayout.WEST);
		}

		JPanel buttonPanel = new JPanel();
		JButton stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				stillRunning = false;
			}
		});
		buttonPanel.add(stopButton);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(null);
	}

	public boolean isStillRunning() {
		return stillRunning;
	}
}
