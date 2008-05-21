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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.attributeeditor.AttributeEditorDialog;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterTypeAttributeFile;


/**
 * This is an extension of the FileValueCellEditor which also supports the opening of
 * an AttributeEditor. This editor should be used if an attribute description
 * file is desired instead of a normal file.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: AttributeFileCellEditor.java,v 2.10 2006/03/21 15:35:40
 *          ingomierswa Exp $
 */
public class AttributeFileCellEditor extends FileValueCellEditor {

	private static final long serialVersionUID = 99319694250830796L;

	private transient Operator exampleSource;

	public AttributeFileCellEditor(ParameterTypeAttributeFile type) {
		super(type);
		JButton button = new JButton("Edit");
		button.setMargin(new Insets(0, 0, 0, 0));
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				buttonPressed();
			}
		});
		button.setToolTipText("Edit or create attribute description files and data (XML).");
		addButton(button, GridBagConstraints.RELATIVE);

		addButton(createFileChooserButton(), GridBagConstraints.REMAINDER);
	}

    public void setOperator(Operator exampleSource) {
        this.exampleSource = exampleSource;
    }
    
	private void buttonPressed() {
		Object value = getCellEditorValue();
		File file = value == null ? null : RapidMinerGUI.getMainFrame().getProcess().resolveFileName(value.toString());
		AttributeEditorDialog dialog = new AttributeEditorDialog(RapidMinerGUI.getMainFrame(), exampleSource, file);
		dialog.setVisible(true);
		setText(dialog.getFile());
		fireEditingStopped();
	}
}
