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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.parameter.ParameterTypeDirectory;
import com.rapidminer.parameter.ParameterTypeFile;


/**
 * Cell editor consiting of a text field and a small button for opening a file
 * chooser. Should be used for parameters / properties which are files (but no
 * special files like attribute description files). If the desired property is a
 * directory, the button automatically opens a file choser for directories.
 * 
 * @see com.rapidminer.gui.properties.AttributeFileCellEditor
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: FileValueCellEditor.java,v 1.4 2008/05/09 19:22:45 ingomierswa Exp $
 */
public abstract class FileValueCellEditor extends AbstractCellEditor implements PropertyValueCellEditor {

	private JPanel panel = new JPanel();

	private JTextField textField = new JTextField(12);

	private ParameterTypeFile type;

	private GridBagLayout gridBagLayout = new GridBagLayout();

	public FileValueCellEditor(ParameterTypeFile type) {
		this.type = type;
		panel.setLayout(gridBagLayout);
		panel.setToolTipText(type.getDescription());
		textField.setToolTipText(type.getDescription());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		gridBagLayout.setConstraints(textField, c);
		panel.add(textField);
	}

	protected JButton createFileChooserButton() {
		JButton button = new JButton(" ... ");
		button.setMargin(new Insets(0, 0, 0, 0));
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				buttonPressed();
			}
		});
		button.setToolTipText("Select file (" + type.getDescription() + ")");
		return button;
	}

	protected void addButton(JButton button, int gridwidth) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = gridwidth;
		c.weightx = 0;
		c.fill = GridBagConstraints.BOTH;
		gridBagLayout.setConstraints(button, c);
		panel.add(button);
	}

	private void buttonPressed() {
		String value = (String) getCellEditorValue();
		File file = (value == null || value.length() == 0) ? null : RapidMinerGUI.getMainFrame().getProcess().resolveFileName(value);
		File selectedFile = SwingTools.chooseFile(RapidMinerGUI.getMainFrame(), file, true, type instanceof ParameterTypeDirectory, type.getExtension(), type.getKey());
		if ((selectedFile != null)) {
			setText(selectedFile);
			fireEditingStopped();
		} else {
			fireEditingCanceled();
		}
	}

	protected void setText(File file) {
		if (file == null)
			textField.setText("");
		else
			textField.setText(file.getPath());
	}

	public Object getCellEditorValue() {
		return (textField.getText().trim().length() == 0) ? null : textField.getText().trim();
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
		textField.setText((value == null) ? "" : value.toString());
		return panel;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		return getTableCellEditorComponent(table, value, isSelected, row, column);
	}

	public boolean useEditorAsRenderer() {
		return true;
	}

}
