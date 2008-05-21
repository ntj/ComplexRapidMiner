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

import com.rapidminer.gui.wizards.ConfigurationWizardCreator;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterTypeConfiguration;


/**
 * Cell editor consisting of a simple button which opens a configuration wizard for 
 * the corresponding operator.
 * 
 * @author Ingo Mierswa
 * @version $Id: ConfigurationWizardValueCellEditor.java,v 1.3 2008/05/09 19:22:46 ingomierswa Exp $
 */
public class ConfigurationWizardValueCellEditor extends AbstractCellEditor implements PropertyValueCellEditor {

    private static final long serialVersionUID = -7163760967040772736L;

    private transient ParameterTypeConfiguration type;

    private JButton button;
    
    public ConfigurationWizardValueCellEditor(ParameterTypeConfiguration type) {
        this.type = type;
        button = new JButton("Start Configuration Wizard...");
        button.setToolTipText(type.getDescription());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonPressed();
            }
        });
    }

    /** Does nothing. */
    public void setOperator(Operator operator) {}
    
    private void buttonPressed() {
    	ConfigurationWizardCreator creator = type.getWizardCreator();
    	if (creator != null)
    		creator.createConfigurationWizard(type.getWizardListener());
    }

    public Object getCellEditorValue() {
        return null;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
        return button;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    public boolean useEditorAsRenderer() {
        return true;
    }
}
