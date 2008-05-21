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
package com.rapidminer.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import com.rapidminer.InputHandler;


/**
 * An input handler which uses GUI components. Currently only used for passwords
 * (e.g. for database access).
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: GUIInputHandler.java,v 1.3 2008/05/09 19:23:23 ingomierswa Exp $
 */
public class GUIInputHandler implements InputHandler {

	public String inputPassword(String messageText) {
		final JDialog dialog = new JDialog(RapidMinerGUI.getMainFrame(), "Authentication", true);
		JPanel contentPanel = new JPanel();

		contentPanel.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));
		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(new JLabel(messageText), BorderLayout.NORTH);
		JPasswordField passwordField = new JPasswordField();
        passwordField.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {}
            public void keyTyped(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    dialog.dispose();
                }
            }
        });
		contentPanel.add(passwordField, BorderLayout.CENTER);
		JButton close = new JButton("Ok");
		close.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(close);
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialog.getContentPane().add(contentPanel);

		dialog.pack();
		dialog.setLocationRelativeTo(RapidMinerGUI.getMainFrame());
		dialog.setVisible(true);
		return new String(passwordField.getPassword());
	}

}
