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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterTypeList;


/**
 * A Dialog displaying a {@link ListPropertyTable}. This can be used to add new
 * values to the parameter list or change current values. Removal of values is
 * also supported.
 * 
 * @see com.rapidminer.gui.properties.ListPropertyTable
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ListPropertyDialog.java,v 2.9 2006/04/05 08:57:23 ingomierswa
 *          Exp $
 */
public class ListPropertyDialog extends JDialog {

	private static final long serialVersionUID = 1876607848416333390L;

	private boolean ok = false;

	private ListPropertyTable listPropertyTable;

	private List<Object[]> parameterList;

	public ListPropertyDialog(final ParameterTypeList type, List<Object[]> parameterList, Operator operator) {
		super(RapidMinerGUI.getMainFrame(), "Parameter List: " + type.getKey(), true);
		this.parameterList = parameterList;
		listPropertyTable = new ListPropertyTable(type, parameterList, operator);

		getContentPane().setLayout(new BorderLayout());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				listPropertyTable.addRow();
			}
		});
		buttonPanel.add(addButton);

		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				listPropertyTable.removeSelected();
			}
		});
		buttonPanel.add(removeButton);

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

		JScrollPane scrollpane = new ExtendedJScrollPane(listPropertyTable);
		getContentPane().add(scrollpane, BorderLayout.CENTER);
        
		setSize(RapidMinerGUI.getMainFrame().getWidth() / 2, RapidMinerGUI.getMainFrame().getHeight() / 2);
        setLocationRelativeTo(RapidMinerGUI.getMainFrame());
	}

	private void ok() {
		ok = true;
		listPropertyTable.getParameterList(parameterList);
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
