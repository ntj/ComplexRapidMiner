/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.gui.properties;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.rapidminer.Process;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeParameterValue;
import com.rapidminer.parameter.ParameterTypeValue;


/**
 * Parameter editor for a {@link ParameterTypeValue}, i.e. a parameter name and value of a
 * single operator. This can for example be used by parameter optimization operators. 
 * 
 * @author Ingo Mierswa
 * @version $Id: ParameterValueKeyCellEditor.java,v 1.5 2007/06/08 21:29:44 ingomierswa Exp $
 */
public class ParameterValueKeyCellEditor extends AbstractCellEditor implements PropertyKeyCellEditor {

    private static final long serialVersionUID = -2559892872774108384L;

    private JPanel panel = new JPanel();

    private JComboBox operatorCombo = new JComboBox();

    private JComboBox parameterCombo = new JComboBox();

    private transient OperatorChain parentOperator;

    private transient Process process;

    private transient ParameterChangeListener listener = null;
    
    private int row;
    
    private boolean fireEvent = true;
    
    public ParameterValueKeyCellEditor(ParameterTypeParameterValue type) {
    }
    
    protected Object readResolve() {
    	this.process = this.parentOperator.getProcess();
    	return this;
    }
    
    public void setOperator(Operator operator) {
        this.parentOperator = (OperatorChain)operator;
        this.process = parentOperator.getProcess();
        operatorCombo = createOperatorCombo();
        fillParameterCombo();

        panel.setLayout(new GridLayout(1, 2));

        panel.add(operatorCombo);
        panel.add(parameterCombo);
    }
    
    private JComboBox createOperatorCombo() {
        List<Operator> allInnerOps = parentOperator.getAllInnerOperators();
        Vector<String> allOpNames = new Vector<String>();
        Iterator<Operator> i = allInnerOps.iterator();
        while (i.hasNext())
            allOpNames.add(i.next().getName());
        Collections.sort(allOpNames);
        JComboBox combo = new JComboBox(allOpNames);
        combo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                fillParameterCombo();
                fireEditingStopped();
                fireParameterChangedEvent();
            }
        });
        if (combo.getItemCount() == 0)
            combo.addItem("add inner operators");
        else
            combo.setSelectedIndex(0);
        return combo;
    }

    private void fillParameterCombo() {
        parameterCombo.removeAllItems();
        Operator operator = process.getOperator((String) operatorCombo.getSelectedItem());
        if (operator != null) {
            Iterator i = operator.getParameterTypes().iterator();
            while (i.hasNext()) {
                parameterCombo.addItem(((ParameterType) i.next()).getKey());
            }
        }
        if (parameterCombo.getItemCount() == 0)
            parameterCombo.addItem("no parameters");
        parameterCombo.setSelectedIndex(0);
        parameterCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                fireEditingStopped();
                fireParameterChangedEvent();
            }
        });
    }

    public Object getCellEditorValue() {
        String result = operatorCombo.getSelectedItem() + "." + parameterCombo.getSelectedItem();
        return result;
    }

    public void setValue(String valueName) {
    	this.fireEvent = false;
        if (valueName != null) {
            String[] components = valueName.split("\\.");
            if (components.length == 2) {
                String operator = components[0];
                String parameterName = components[1];
                operatorCombo.setSelectedItem(operator);
                parameterCombo.setSelectedItem(parameterName);
            } else {
                operatorCombo.setSelectedIndex(0);
                parameterCombo.setSelectedIndex(0);
            }
        } else {
            operatorCombo.setSelectedIndex(0);
            parameterCombo.setSelectedIndex(0);
        }
        this.fireEvent = true;
    }
    
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
        setValue((String) value);
        return panel;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return getTableCellEditorComponent(table, value, isSelected, row, column);
    }
    
    public void setParameterChangeListener(ParameterChangeListener listener, int row) {
    	this.listener = listener;
    	this.row = row;
    }
    
    public void fireParameterChangedEvent() {
    	if (fireEvent) {
    		if (listener != null) {
    			listener.parameterSelectionChanged((String)operatorCombo.getSelectedItem(), (String)parameterCombo.getSelectedItem(), row);
    		}
    	}
    }
}
