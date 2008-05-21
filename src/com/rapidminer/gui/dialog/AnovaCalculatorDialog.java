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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.math.AnovaCalculator;
import com.rapidminer.tools.math.SignificanceCalculationException;
import com.rapidminer.tools.math.SignificanceTestResult;


/**
 * The ANOVA calculator dialog is a small tool which can be used to perform an
 * ANalysis Of VAriances in order to determine if a given set of mean values is
 * probably actually different.
 * 
 * @author Ingo Mierswa
 * @version $Id: AnovaCalculatorDialog.java,v 2.2 2006/03/21 15:35:40
 *          ingomierswa Exp $
 */
public class AnovaCalculatorDialog extends JDialog {

	private static final long serialVersionUID = 3023267244921354296L;

	private static class AnovaTableModel extends DefaultTableModel {

		private static final long serialVersionUID = -2904775003271582149L;

		public AnovaTableModel() {
			super(new String[] { "Mean", "Variance", "Number" }, 0);
		}

		public Class<?> getColumnClass(int c) {
			if (c == 2) {
				return Integer.class;
			} else {
				return Double.class;
			}
		}
		
		public boolean isCellEditable(int row, int column) {
			return true;
		}
	}

	private transient AnovaCalculator calculator = new AnovaCalculator();

	private JTextField alphaField = new JTextField("0.05");
	
	private AnovaTableModel tableModel;

	public AnovaCalculatorDialog(Frame owner) {
		super(owner, "Anova Calculator", false);
		this.calculator = new AnovaCalculator();
		getContentPane().setLayout(new BorderLayout());

		// data table
		this.tableModel = new AnovaTableModel();
		JTable dataTable = new ExtendedJTable(tableModel, false);
		getContentPane().add(new ExtendedJScrollPane(dataTable), BorderLayout.CENTER);

		// input panel
		JPanel inputPanel = new JPanel(new FlowLayout());
		JLabel label = new JLabel("Mean:");
		inputPanel.add(label);
		final JTextField meanField = new JTextField(8);
		inputPanel.add(meanField);
		label = new JLabel("Variance:");
		inputPanel.add(label);
		final JTextField varianceField = new JTextField(8);
		inputPanel.add(varianceField);
		label = new JLabel("Number:");
		inputPanel.add(label);
		final JTextField numberField = new JTextField(8);
		inputPanel.add(numberField);

		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// mean
				double mean = Double.NaN;
				String meanString = meanField.getText();
				if ((meanString != null) && (meanString.trim().length() > 0)) {
					try {
						mean = Double.parseDouble(meanString);
					} catch (NumberFormatException ex) {
						SwingTools.showVerySimpleErrorMessage("The field 'mean' must contain a real-valued number.");
						return;
					}
				} else {
					SwingTools.showVerySimpleErrorMessage("The field 'mean' must contain a real-valued number.");
					return;
				}

				// variance
				double variance = Double.NaN;
				String varianceString = varianceField.getText();
				if ((varianceString != null) && (varianceString.trim().length() > 0)) {
					try {
						variance = Double.parseDouble(varianceString);
					} catch (NumberFormatException ex) {
						SwingTools.showVerySimpleErrorMessage("The field 'variance' must contain a real-valued number.");
						return;
					}
				} else {
					SwingTools.showVerySimpleErrorMessage("The field 'variance' must contain a real-valued number.");
					return;
				}

				// number
				int number = -1;
				String numberString = numberField.getText();
				if ((numberString != null) && (numberString.trim().length() > 0)) {
					try {
						number = Integer.parseInt(numberString);
					} catch (NumberFormatException ex) {
						SwingTools.showVerySimpleErrorMessage("The field 'number' must contain a positive integer number > 1.");
						return;
					}
				} else {
					SwingTools.showVerySimpleErrorMessage("The field 'number' must contain a positive integer number > 1.");
					return;
				}

				if ((!Double.isNaN(mean)) && (!Double.isNaN(variance)) && (number > 1)) {
					tableModel.addRow(new Object[] { mean, variance, number });
				}
			}
		});
		inputPanel.add(addButton);

		getContentPane().add(inputPanel, BorderLayout.NORTH);

		// button panel
		Box buttonPanel = new Box(BoxLayout.X_AXIS);
		JLabel alphaLabel = new JLabel("Significance Level: ");
		buttonPanel.add(alphaLabel);
		buttonPanel.add(alphaField);
		buttonPanel.add(Box.createHorizontalGlue());
		JButton calculateButton = new JButton("Calculate...");
		calculateButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					calculateANOVA();
				} catch (SignificanceCalculationException e1) {
					SwingTools.showSimpleErrorMessage("Cannot calculate ANOVA: ", e1);
				}
			}
		});
		buttonPanel.add(calculateButton);
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				clearData();
			}
		});
		buttonPanel.add(clearButton);
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		buttonPanel.add(closeButton);

		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		setSize(600, 400);
		setLocationRelativeTo(owner);
	}

	protected Object readResolve() {
		this.calculator = new AnovaCalculator();
		return this;
	}
	
	private void close() {
		dispose();
	}

	private void clearData() {
		while (tableModel.getRowCount() > 0)
			tableModel.removeRow(0);
	}

	private void calculateANOVA() throws SignificanceCalculationException {
		double alpha = -1;
		String alphaString = alphaField.getText();
		try {
			alpha = Double.parseDouble(alphaString);
		} catch (NumberFormatException e) {
			SwingTools.showVerySimpleErrorMessage("Significance level must be a number between 0 and 1.");
		}
		
		if ((alpha < 0) || (alpha > 1)) {
			SwingTools.showVerySimpleErrorMessage("Significance level must be a number between 0 and 1.");			
		} else {
			this.calculator.clearGroups();
			this.calculator.setAlpha(alpha);
			for (int i = 0; i < tableModel.getRowCount(); i++) {
				int number = ((Integer) tableModel.getValueAt(i, 2)).intValue();
				double mean = ((Double) tableModel.getValueAt(i, 0)).doubleValue();
				double variance = ((Double) tableModel.getValueAt(i, 1)).doubleValue();
				calculator.addGroup(number, mean, variance);
			}
			if (tableModel.getRowCount() < 2) {
				SwingTools.showVerySimpleErrorMessage("You need to add at least two rows in order to calculate an ANOVA test.");
				return;
			}

			SignificanceTestResult result = calculator.performSignificanceTest();
			JOptionPane.showMessageDialog(this, result.getVisualizationComponent(null), "ANOVA result", JOptionPane.PLAIN_MESSAGE);
		}
	}
}
