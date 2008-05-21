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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;

import com.rapidminer.gui.tools.syntax.JEditTextArea;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterTypeText;


/**
 * A cell editor with a button that opens a {@link JEditTextArea}.
 * 
 * @author Ingo Mierswa
 * @version $Id: TextValueCellEditor.java,v 1.3 2008/05/09 19:22:46 ingomierswa Exp $
 */
public class TextValueCellEditor extends AbstractCellEditor implements PropertyValueCellEditor {

    private static final long serialVersionUID = -4429790999365057931L;

    private ParameterTypeText type;
    
    private JButton button = new JButton("Edit Text...");

    private String text = null;
    
    public TextValueCellEditor(ParameterTypeText type) {
        this.type = type;
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        button.setToolTipText(type.getDescription());
        setButtonText();
    }

    public void setOperator(final Operator operator) {
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TextPropertyDialog dialog = new TextPropertyDialog(type, text, operator);
                dialog.setVisible(true);
                if (dialog.isOk()) {
                    text = dialog.getText();
                    setButtonText();
                    fireEditingStopped();
                } else {
                    fireEditingCanceled();
                }
            }
        });
    }
    
    public Object getCellEditorValue() {
        return text;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
        this.text = (String)value;
        setButtonText();
        return button;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    public boolean useEditorAsRenderer() {
        return true;
    }

    private void setButtonText() {
        if ((text != null) && (text.length() > 0)) {
            button.setText("Edit Text (" + text.length() + " characters)...");
        } else {
            button.setText("Edit Text...");
        }
    }
}
