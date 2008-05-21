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
package com.rapidminer.gui.viewer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;

import com.rapidminer.datatable.SimpleDataTable;
import com.rapidminer.datatable.SimpleDataTableRow;
import com.rapidminer.gui.plotter.mathplot.SticksPlot3D;
import com.rapidminer.tools.Tableable;

/** This viewer class can be used to display performance criteria based on a multi class confusion matrix. 
 *  The viewer consists of two parts, first a part containing the general performance info string and second
 *  a table with the complete confusion matrix.
 * 
 *  @author Ingo Mierswa
 *  @version $Id: ConfusionMatrixViewer.java,v 1.6 2008/05/09 19:22:59 ingomierswa Exp $
 */
public class ConfusionMatrixViewer extends JPanel implements Tableable{
	
	private static final long serialVersionUID = 3448880915145528006L;

	private ConfusionMatrixViewerTable table;
	public ConfusionMatrixViewer(String performance, String[] classNames, double[][] counter) {
		setLayout(new BorderLayout());
		
		final JPanel mainPanel = new JPanel();
		final CardLayout cardLayout = new CardLayout();
		mainPanel.setLayout(cardLayout);
		add(mainPanel, BorderLayout.CENTER);

		// *** table panel ***
		JPanel tablePanel = new JPanel(new BorderLayout());
		
		// info string
		JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JTextPane infoText = new JTextPane();
		infoText.setEditable(false);
		infoText.setBackground(infoPanel.getBackground());
		infoText.setFont(infoText.getFont().deriveFont(Font.BOLD));
		infoText.setText(performance);
		infoPanel.add(infoText);
		infoPanel.setBorder(BorderFactory.createEtchedBorder());
		tablePanel.add(infoPanel, BorderLayout.NORTH);
		
		// table
		table = new ConfusionMatrixViewerTable(classNames, counter);
		table.setBorder(BorderFactory.createEtchedBorder());
		tablePanel.add(table, BorderLayout.CENTER);
		
		// *** plot panel ***
		SimpleDataTable dataTable = new SimpleDataTable("Confusion Matrix", new String[] {"True Class", "Predicted Class", "Confusion Matrix (x: true class,  y: pred. class,  z: counters)" });		
		for (int row = 0; row < classNames.length; row++) {
			for (int column = 0; column < classNames.length; column++) {
				dataTable.add(new SimpleDataTableRow(new double[] { row, column, counter[row][column]} ));
			}
		}

		SticksPlot3D plotter = new SticksPlot3D(dataTable);
		plotter.setAxis(0, 0);
		plotter.setAxis(1, 1);
		plotter.setPlotColumn(2, true);
		
		mainPanel.add(tablePanel, "table");
		mainPanel.add(plotter, "plot");
		
		
		// toggle radio button for views
		final JRadioButton metaDataButton = new JRadioButton("Table View", true);
		metaDataButton.setToolTipText("Changes to a table showing the confusion matrix.");
		metaDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (metaDataButton.isSelected()) {
					cardLayout.show(mainPanel, "table");
				}
			}
		});

		final JRadioButton plotButton = new JRadioButton("Plot View", false);
		plotButton.setToolTipText("Changes to a plot view of the confusion matrix.");
		plotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (plotButton.isSelected()) {
					cardLayout.show(mainPanel, "plot");
				}
			}
		});
		
		ButtonGroup group = new ButtonGroup();
		group.add(metaDataButton);
		group.add(plotButton);
		JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		togglePanel.add(metaDataButton);
		togglePanel.add(plotButton);

		add(togglePanel, BorderLayout.NORTH);
	}
	public String getCell(int row, int column) {
		return table.getCell(row, column);
	}
	public int getColumnNumber() {
		return table.getColumnNumber();
	}
	public int getRowNumber() {
		return table.getRowNumber();
	}
}
