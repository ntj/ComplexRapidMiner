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
package com.rapidminer.tools.math.matrix;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.rapidminer.gui.graphs.GraphViewer;
import com.rapidminer.gui.viewer.MatrixGraphCreator;

/**
 * This class can be used to visualize matrices.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: MatrixVisualizer.java,v 1.5 2008/05/09 19:23:23 ingomierswa Exp $
 * 
 * @param <Ex>
 * @param <Ey>
 */
public class MatrixVisualizer<Ex, Ey> extends JPanel {

	private static final long serialVersionUID = -4906730566941207026L;

	public MatrixVisualizer(Matrix<Ex, Ey> matrix) {
		super();
		setLayout(new BorderLayout());

		ButtonGroup group = new ButtonGroup();
		JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		// Matrix Table
		final JComponent tableView = new MatrixComboBoxVisualizer<Ex, Ey>(matrix);
		final JRadioButton tableButton = new JRadioButton("Table View", true);
		tableButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tableButton.isSelected()) {
					remove(1);
					add(tableView, BorderLayout.CENTER);
					repaint();
				}
			}
		});
		group.add(tableButton);
		togglePanel.add(tableButton);

		// graph view
		final JComponent graphView = new GraphViewer<String,String>(new MatrixGraphCreator<Ex,Ey>(matrix));
		final JRadioButton graphButton = new JRadioButton("Graph View", false);
		graphButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (graphButton.isSelected()) {
					remove(1);
					add(graphView, BorderLayout.CENTER);
					repaint();
				}
			}
		});
		group.add(graphButton);
		togglePanel.add(graphButton);
		
		add(togglePanel, BorderLayout.NORTH);
		add(tableView, BorderLayout.CENTER);
	}
	
	
	
}
