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
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.rapidminer.Process;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.Value;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeValue;


/**
 * Parameter editor for {@link ParameterTypeValue}, i.e. the parameter type for
 * values which are provided by operators.
 * 
 * @see com.rapidminer.gui.properties.DefaultPropertyValueCellEditor
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: OperatorValueValueCellEditor.java,v 1.4 2008/05/09 19:22:46 ingomierswa Exp $
 */
public class OperatorValueValueCellEditor extends AbstractCellEditor implements PropertyValueCellEditor {

	private static final long serialVersionUID = 2332956173516489926L;

	private JPanel panel = new JPanel();

	private JComboBox operatorCombo;

	private JComboBox typeCombo = new JComboBox(new String[] { "value", "parameter" });

	private JComboBox valueCombo = new JComboBox();

	private transient Process process;

	public OperatorValueValueCellEditor(ParameterTypeValue type) {
	}

    public void setOperator(Operator operator) {
        this.process = operator.getProcess();
        operatorCombo = createOperatorCombo();
        typeCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                fillValueCombo();
                fireEditingStopped();
            }
        });
        valueCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                fireEditingStopped();
            }
        });

        fillValueCombo();

        panel.setLayout(new GridLayout(1, 3));

        panel.add(operatorCombo);
        panel.add(typeCombo);
        panel.add(valueCombo);
    }
    
	private JComboBox createOperatorCombo() {
		Vector<String> allOps = new Vector<String>(process.getAllOperatorNames());
		Collections.sort(allOps);
		JComboBox combo = new JComboBox(allOps);
		combo.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				fillValueCombo();
				fireEditingStopped();
			}
		});
		return combo;
	}

	private void fillValueCombo() {
		valueCombo.removeAllItems();
		Operator operator = process.getOperator((String) operatorCombo.getSelectedItem());
		switch (typeCombo.getSelectedIndex()) {
			case 0:
				Iterator i = operator.getValues().iterator();
				while (i.hasNext()) {
					valueCombo.addItem(((Value) i.next()).getKey());
				}
				if (valueCombo.getItemCount() == 0)
					valueCombo.addItem("no values");
				break;
			case 1:
				i = operator.getParameterTypes().iterator();
				while (i.hasNext()) {
					valueCombo.addItem(((ParameterType) i.next()).getKey());
				}
				if (valueCombo.getItemCount() == 0)
					valueCombo.addItem("no params");
				break;
		}
		valueCombo.setSelectedIndex(0);
	}

	public Object getCellEditorValue() {
		return "operator." + operatorCombo.getSelectedItem() + "." + ((typeCombo.getSelectedIndex() == 0) ? "value" : "parameter") + "." + valueCombo.getSelectedItem();
	}

	public void setValue(String valueName) {
		if (valueName != null) {
			String[] components = valueName.split("\\.");
			if (components.length == 4) {
				String operator = components[1];
				int type = components[2].equals("parameter") ? 1 : 0;
				String name = components[3];
				operatorCombo.setSelectedItem(operator);
				typeCombo.setSelectedIndex(type);
				valueCombo.setSelectedItem(name);
			} else {
				operatorCombo.setSelectedIndex(0);
				typeCombo.setSelectedIndex(0);
				valueCombo.setSelectedIndex(0);
			}
		} else {
			operatorCombo.setSelectedIndex(0);
			typeCombo.setSelectedIndex(0);
			valueCombo.setSelectedIndex(0);
		}
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
		setValue((String) value);
		return panel;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		return getTableCellEditorComponent(table, value, isSelected, row, column);
	}

	public boolean useEditorAsRenderer() {
		return true;
	}

}
