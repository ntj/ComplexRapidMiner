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
package com.rapidminer.gui.tools;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

/**
 * This is a helper component consisting of a a set of radio buttons together
 * with a main panel with a card layout. If more than one component is added, 
 * the viewable component can be selected via the radio buttons. Hence, this
 * component works similar to a tabbed pane but with radio buttons instead.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: RadioCardPanel.java,v 1.2 2008/07/13 16:39:42 ingomierswa Exp $
 */
public class RadioCardPanel extends JPanel {

	private static final long serialVersionUID = 2929637220390538982L;

	private CardLayout layout = new CardLayout();
	
	private JPanel mainPanel = new JPanel(layout);

	private JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	
	private ButtonGroup buttonGroup = new ButtonGroup();
	
	private int counter = 0;
	
	public RadioCardPanel() {
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
	}
	
	public void addCard(final String name, final Component component) {
		counter++;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainPanel.add(component, name);
				JRadioButton viewButton = new JRadioButton(name);
				viewButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						showCard(name);
					}
				});
				buttonPanel.add(viewButton);
				buttonGroup.add(viewButton);
				if (buttonGroup.getButtonCount() <= 1) {
					viewButton.setSelected(true);
				}
				
				if (counter >= 2) {
					add(buttonPanel, BorderLayout.NORTH);
				}
			}
		});
	}
	
	private void showCard(String name) {
		layout.show(mainPanel, name);
	}
}

