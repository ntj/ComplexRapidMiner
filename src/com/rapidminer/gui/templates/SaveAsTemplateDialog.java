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
package com.rapidminer.gui.templates;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.Tools;


/**
 * The save as template dialog assists the user in creating a new process
 * template. Template processes are saved in the local .rapidminer directory of the
 * user. The name, description and additional parameters to set can be specified
 * by the user.
 * 
 * @author Ingo Mierswa
 * @version $Id: SaveAsTemplateDialog.java,v 2.11 2006/04/05 08:57:23
 *          ingomierswa Exp $
 */
public class SaveAsTemplateDialog extends JDialog {

	private static final long serialVersionUID = -4892200177390173103L;

	/** A helper class for pairs of operators and their parameters. */
	private static class OperatorParameterPair implements Comparable<OperatorParameterPair> {

		private String[] pair;

		public OperatorParameterPair(String[] pair) {
			this.pair = pair;
		}

		public int compareTo(OperatorParameterPair opp) {
			return (this.pair[0] + "." + this.pair[1]).compareTo(opp.pair[0] + "." + opp.pair[1]);
		}

		public boolean equals(Object o) {
			if (!(o instanceof OperatorParameterPair)) {
				return false;
			} else {
				return pair == ((OperatorParameterPair)o).pair;
			}
		}
		
		public int hashCode() {
			return pair.hashCode();
		}
		
		public String toString() {
			return (this.pair[0] + "." + this.pair[1]);
		}

		public String[] getStringPair() {
			return pair;
		}
	}

	/** A table model for operator - parameter pairs. */
	private static class TemplateParameterTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 394789495217375600L;

		/** 0: operator, 1: parameter key */
		private transient OperatorParameterPair[] parameters;

		private boolean[] selection;

		public TemplateParameterTableModel(OperatorParameterPair[] parameters) {
			this.parameters = parameters;
			this.selection = new boolean[parameters.length];
		}

		public String getColumnName(int column) {
			if (column == 0)
				return "Parameter name";
			else
				return "Selected for Template";
		}

		public Object getValueAt(int row, int column) {
			if (column == 0)
				return parameters[row].toString();
			else
				return Boolean.valueOf(selection[row]);
		}

		public void setValueAt(Object o, int row, int column) {
			if (column == 1) {
				selection[row] = ((Boolean) o).booleanValue();
				fireTableCellUpdated(row, column);
			}
		}

		public boolean isCellEditable(int row, int column) {
			return column == 1;
		}

		public int getRowCount() {
			return parameters.length;
		}

		public int getColumnCount() {
			return 2;
		}

		/** Makes sure that a checkbox is used for the boolean values. */
		public Class<?> getColumnClass(int column) {
			return getValueAt(0, column).getClass();
		}

		public List<String[]> getSelectedParameters() {
			List<String[]> selected = new LinkedList<String[]>();
			for (int i = 0; i < parameters.length; i++) {
				if (selection[i])
					selected.add(parameters[i].getStringPair());
			}
			return selected;
		}
	}

	private boolean ok = false;

	private JTextField nameField = new JTextField();

	private JTextField descriptionField = new JTextField();

	private TemplateParameterTableModel tableModel = null;

	/** Creates a new save as template dialog. */
	public SaveAsTemplateDialog(MainFrame mainFrame, Operator operator) {
		super(mainFrame, "Save as Template", true);

		JPanel rootPanel = new JPanel(new BorderLayout());
		rootPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagLayout layout = new GridBagLayout();
		JPanel mainPanel = new JPanel(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0d;

		JPanel textPanel = SwingTools.createTextPanel("Save this process setup as Template...", "Please specify a name and short description of this template. " + "The selected parameters can be determined by the user in addition " + "to the mandatory parameters.");
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(textPanel, c);
		mainPanel.add(textPanel);

		Component sep = Box.createVerticalStrut(10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(sep, c);
		mainPanel.add(sep);

		// add components to main panel
		JLabel label = new JLabel("Name");
		label.setToolTipText("The name of the template.");
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		nameField.setToolTipText("The name of the template.");
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(nameField, c);
		mainPanel.add(nameField);

		label = new JLabel("Description");
		label.setToolTipText("A short description of this template.");
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		descriptionField.setToolTipText("A short description of this template.");
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(descriptionField, c);
		mainPanel.add(descriptionField);

		sep = Box.createVerticalStrut(10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(sep, c);
		mainPanel.add(sep);

		tableModel = new TemplateParameterTableModel(getParameters(operator));
		JTable table = new ExtendedJTable(tableModel, false);
		JScrollPane tablePane = new ExtendedJScrollPane(table);
		table.setPreferredScrollableViewportSize(new java.awt.Dimension(600, 200));
		table.getTableHeader().setToolTipText("Select the parameters which could be defined by the user.");
		table.setRowHeight(table.getRowHeight() + SwingTools.TABLE_WITH_COMPONENTS_ROW_EXTRA_HEIGHT);
		table.getTableHeader().setReorderingAllowed(false);

		c.weighty = 1.0d;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(tablePane, c);
		mainPanel.add(tablePane);

		// buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
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

		rootPanel.add(mainPanel, BorderLayout.CENTER);
		rootPanel.add(buttonPanel, BorderLayout.SOUTH);
		getContentPane().add(rootPanel);
		pack();
		setSize(700, 500);
		setLocationRelativeTo(mainFrame);
	}

	private OperatorParameterPair[] getParameters(Operator operator) {
		List<OperatorParameterPair> parameters = new LinkedList<OperatorParameterPair>();
		addParameters(operator, parameters);
		Collections.sort(parameters);
		OperatorParameterPair[] result = new OperatorParameterPair[parameters.size()];
		parameters.toArray(result);
		return result;
	}

	private void addParameters(Operator operator, List<OperatorParameterPair> parameters) {
		Iterator<ParameterType> i = operator.getParameterTypes().iterator();
		while (i.hasNext()) {
			ParameterType type = i.next();
			if (type.isOptional())
				parameters.add(new OperatorParameterPair(new String[] { operator.getName(), type.getKey() }));
		}
		if (operator instanceof OperatorChain) {
			OperatorChain chain = (OperatorChain) operator;
			for (int n = 0; n < chain.getNumberOfOperators(); n++) {
				addParameters(chain.getOperator(n), parameters);
			}
		}
	}

	private void addMandatoryParameters(Operator operator, List<String[]> parameters) {
		Iterator<ParameterType> i = operator.getParameterTypes().iterator();
		while (i.hasNext()) {
			ParameterType type = i.next();
			if (!type.isOptional())
				parameters.add(new String[] { operator.getName(), type.getKey() });
		}
		if (operator instanceof OperatorChain) {
			OperatorChain chain = (OperatorChain) operator;
			for (int n = 0; n < chain.getNumberOfOperators(); n++) {
				addMandatoryParameters(chain.getOperator(n), parameters);
			}
		}
	}

	public boolean isOk() {
		return ok;
	}

	public Template getTemplate(Operator operator) {
		String name = nameField.getText();
		List<String[]> selectedOptional = tableModel.getSelectedParameters();
		addMandatoryParameters(operator, selectedOptional);
		return new Template(name, descriptionField.getText(), name + ".xml", selectedOptional);
	}

	private boolean checkIfNameOk() {
		String name = nameField.getText();
		if ((name == null) || (name.length() == 0)) {
			SwingTools.showVerySimpleErrorMessage("Please specify a name for this template!");
			return false;
		}

		File[] preDefinedTemplateFiles = ParameterService.getConfigFile("templates").listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(".template");
			}
		});
		File[] userDefinedTemplateFiles = ParameterService.getUserRapidMinerDir().listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(".template");
			}
		});

		int numberOfPredinedTemplates = preDefinedTemplateFiles != null ? preDefinedTemplateFiles.length : 0;
		int numberOfUserTemplates     = userDefinedTemplateFiles != null ? userDefinedTemplateFiles.length : 0;
		File[] templateFiles = new File[numberOfPredinedTemplates + numberOfUserTemplates];
		if (preDefinedTemplateFiles != null)
			System.arraycopy(preDefinedTemplateFiles, 0, templateFiles, 0, numberOfPredinedTemplates);
		if (userDefinedTemplateFiles != null)
			System.arraycopy(userDefinedTemplateFiles, 0, templateFiles, numberOfPredinedTemplates, numberOfUserTemplates);

		for (int i = 0; i < templateFiles.length; i++) {
			String tempName = templateFiles[i].getName().substring(0, templateFiles[i].getName().lastIndexOf("."));
			if (tempName.equals(name)) {
				SwingTools.showVerySimpleErrorMessage("Name '" + name + "' is already used." + Tools.getLineSeparator() + "Please change name or delete the old template!");
				return false;
			}
		}
		return true;
	}

	private void ok() {
		if (checkIfNameOk()) {
			ok = true;
			dispose();
		}
	}

	private void cancel() {
		ok = false;
		dispose();
	}
}
