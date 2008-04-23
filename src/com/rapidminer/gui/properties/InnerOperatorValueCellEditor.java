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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

import com.rapidminer.operator.AddListener;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;


/**
 * Parameter editor for {@link com.rapidminer.parameter.ParameterTypeInnerOperator}.
 * 
 * @author Ingo Mierswa
 * @version $Id: InnerOperatorValueCellEditor.java,v 1.1 2007/05/27 21:59:26 ingomierswa Exp $
 */
public class InnerOperatorValueCellEditor extends AbstractCellEditor implements PropertyValueCellEditor, AddListener {

    private static final long serialVersionUID = -2559892872774108384L;

    private JComboBox operatorCombo = new JComboBox();

    private transient OperatorChain parentOperator;

    public InnerOperatorValueCellEditor(InnerOperatorValueCellEditor editor) {
    }
    
    public void setOperator(Operator parentOperator) {
        this.parentOperator = (OperatorChain)parentOperator;
        this.parentOperator.addAddListener(this);
        this.operatorCombo = new JComboBox();
        updateOperatorCombo();        
    }
    
    public void operatorAdded(Operator child) {
        updateOperatorCombo();
    }
    
    private void updateOperatorCombo() {
        Object selectedItem = this.operatorCombo.getSelectedItem();
        this.operatorCombo.removeAllItems();
        List<Operator> allInnerOps = parentOperator.getAllInnerOperators();
        Vector<String> allOpNames = new Vector<String>();
        Iterator<Operator> i = allInnerOps.iterator();
        while (i.hasNext())
            allOpNames.add(i.next().getName());
        Collections.sort(allOpNames);
        Iterator<String> s = allOpNames.iterator();
        while (s.hasNext()) {
            this.operatorCombo.addItem(s.next());
        }
        this.operatorCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                fireEditingStopped();
            }
        });
        if (this.operatorCombo.getItemCount() == 0)
            this.operatorCombo.addItem("add inner operators");
        this.operatorCombo.setSelectedItem(selectedItem);
    }

    public Object getCellEditorValue() {
        return operatorCombo.getSelectedItem();
    }

    public void setValue(String valueName) {
        if (valueName != null) {
            operatorCombo.setSelectedItem(valueName);
        } else {
            operatorCombo.setSelectedIndex(0);
        }
    }
    
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
        setValue((String) value);
        return operatorCombo;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return getTableCellEditorComponent(table, value, isSelected, row, column);
    }
    
    public boolean useEditorAsRenderer() { return true; }
}
