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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel for delayed calculations.
 * 
 * @author Sebastian Land
 * @version $Id: JDelayedCalculationPanel.java,v 1.3 2008/05/09 19:23:23 ingomierswa Exp $
 */
public class JDelayedCalculationPanel extends JPanel {

	private static final long serialVersionUID = -6010071394984207389L;
	
	private final GridBagLayout layout = new GridBagLayout();
	private final GridBagConstraints c = new GridBagConstraints();
	private JButton startButton = new JButton("Start calculation");
	private JLabel calculationLabel = new JLabel("Calculation started...");
	private Thread calculationThread = null;
	
	public JDelayedCalculationPanel() {
		this.setLayout(layout);
		this.c.fill = GridBagConstraints.BOTH;
		this.c.weightx = 1;
		this.c.weighty = 1;
		this.c.gridwidth = GridBagConstraints.REMAINDER;
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(startButton);
		layout.setConstraints(buttonPanel, c);
		this.add(buttonPanel);

		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// removing current Content
				removeAll();
				add(calculationLabel);
				revalidate();
				repaint();

				// performing thread
				getCalculationThread().start();
			}
		});
	}
	public Thread getCalculationThread() {
		return calculationThread;
	}
	
	public Component add(Component comp) {
		layout.setConstraints(comp, c);
		return super.add(comp);
	}
	
	public void setDelayThread(Thread delayThread) {
		this.calculationThread = delayThread;
	}
}
