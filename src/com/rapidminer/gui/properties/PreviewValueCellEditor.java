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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;

import com.rapidminer.gui.wizards.PreviewCreator;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterTypePreview;


/**
 * Cell editor consisting of a simple button which opens a preview for 
 * the corresponding operator.
 * 
 * @author Ingo Mierswa
 * @version $Id: PreviewValueCellEditor.java,v 1.1 2007/05/27 21:59:26 ingomierswa Exp $
 */
public class PreviewValueCellEditor extends AbstractCellEditor implements PropertyValueCellEditor {

    private static final long serialVersionUID = -7163760967040772736L;

    private transient ParameterTypePreview type;

    private JButton button;
    
    public PreviewValueCellEditor(ParameterTypePreview type) {
        this.type = type;
        button = new JButton("Show Preview...");
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
    	PreviewCreator creator = type.getPreviewCreator();
    	if (creator != null)
    		creator.createPreview(type.getPreviewListener());
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
