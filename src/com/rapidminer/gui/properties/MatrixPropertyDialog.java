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
package com.rapidminer.gui.properties;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterTypeMatrix;


/**
 * A Dialog displaying a {@link MatrixPropertyTable}. This can be used to add new
 * values to the parameter matrix or change current values. Removal of values is
 * also supported.
 * 
 * @see com.rapidminer.gui.properties.MatrixPropertyTable
 * @author Helge Homburg
 * @version $Id: MatrixPropertyDialog.java,v 1.3 2008/05/09 19:22:45 ingomierswa Exp $
 */
public class MatrixPropertyDialog extends JDialog {

	private static final long serialVersionUID = 0L;

	private boolean ok = false;
	
	private boolean isSquare = false;

	private MatrixPropertyTable matrixPropertyTable;	
	
	public MatrixPropertyDialog(final ParameterTypeMatrix type, double[][] matrix, Operator operator, Boolean isSquare) {
		super(RapidMinerGUI.getMainFrame(), "Parameter Matrix: " + type.getKey(), true);
							
		this.isSquare = isSquare;
		matrixPropertyTable = new MatrixPropertyTable(matrix, operator);

		getContentPane().setLayout(new BorderLayout());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		if (!this.isSquare) {
			JButton addRowButton = new JButton("Add row");
			addRowButton.addActionListener(new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					matrixPropertyTable.addRow();
				}
			});
			buttonPanel.add(addRowButton);
			
			JButton addColumnButton = new JButton("Add column");
			addColumnButton.addActionListener(new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					matrixPropertyTable.addColumn();
				}
			});
			buttonPanel.add(addColumnButton);
	
			JButton removeRowButton = new JButton("Remove row");
			removeRowButton.addActionListener(new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					matrixPropertyTable.removeSelectedRow();
				}
			});
			buttonPanel.add(removeRowButton);
			
			JButton removeColumnButton = new JButton("Remove column");
			removeColumnButton.addActionListener(new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					matrixPropertyTable.removeSelectedColumn();
				}
			});
			buttonPanel.add(removeColumnButton);
		
		} else {
			JButton addColumnButton = new JButton("Increase Size");
			addColumnButton.addActionListener(new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					matrixPropertyTable.addRow();
					matrixPropertyTable.addColumn();
					matrixPropertyTable.fillNewRowAndColumn();
				}
			});
			buttonPanel.add(addColumnButton);
	
			JButton removeRowButton = new JButton("Decrease Size");
			removeRowButton.addActionListener(new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					matrixPropertyTable.removeSelectedRowAndColumn();
					
				}
			});
			buttonPanel.add(removeRowButton);			
		}

		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});
		buttonPanel.add(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		buttonPanel.add(cancelButton);

		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JScrollPane scrollpane = new ExtendedJScrollPane(matrixPropertyTable);
		getContentPane().add(scrollpane, BorderLayout.CENTER);		
		
		setSize(RapidMinerGUI.getMainFrame().getWidth() / 2, RapidMinerGUI.getMainFrame().getHeight() / 2);
        setLocationRelativeTo(RapidMinerGUI.getMainFrame());
	}

	public double[][] getMatrix() {
		return matrixPropertyTable.getParameterMatrix();
	}
	
	private void ok() {
		ok = true;				
		dispose();
	}

	private void cancel() {
		ok = false;
		dispose();
	}

	public boolean isOk() {
		return ok;
	}
}
