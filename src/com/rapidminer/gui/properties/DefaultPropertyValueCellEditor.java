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

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypePassword;
import com.rapidminer.parameter.ParameterTypeStringCategory;


/**
 * Editor for parameter values string, int, double, category, and boolean. This
 * can be used in all {@link PropertyTable}s to show or editing the properties /
 * parameters. For more special parameter types other solutions exist.
 * 
 * @see FileValueCellEditor
 * @see ListValueCellEditor
 * @see ColorValueCellEditor
 * @see OperatorValueValueCellEditor
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: DefaultPropertyValueCellEditor.java,v 2.17 2006/03/21 15:35:40
 *          ingomierswa Exp $
 */
public class DefaultPropertyValueCellEditor extends DefaultCellEditor implements PropertyValueCellEditor {

	private static final long serialVersionUID = 3594466409311826645L;

	private boolean useEditorAsRenderer = false;

	public DefaultPropertyValueCellEditor(ParameterTypeCategory type) {
		super(new JComboBox(type.getValues()));
		editorComponent.setBackground(javax.swing.UIManager.getColor("Table.cellBackground"));
		useEditorAsRenderer = true;
		((JComboBox) editorComponent).removeItemListener(this.delegate);
		this.delegate = new EditorDelegate() {

			private static final long serialVersionUID = -2104662561680969750L;

			public void setValue(Object x) {
				super.setValue(x);
				((JComboBox) editorComponent).setSelectedIndex(((Integer) x).intValue());
			}

			public Object getCellEditorValue() {
				return Integer.valueOf(((JComboBox) editorComponent).getSelectedIndex());
			}
		};
		
		((JComboBox) editorComponent).addItemListener(delegate);
	}

	public DefaultPropertyValueCellEditor(ParameterTypeStringCategory type) {
		super(new JComboBox(type.getValues()));
		editorComponent.setBackground(javax.swing.UIManager.getColor("Table.cellBackground"));
		useEditorAsRenderer = true;
		((JComboBox) editorComponent).removeItemListener(this.delegate);
		((JComboBox) editorComponent).setEditable(true);
		this.delegate = new EditorDelegate() {

			private static final long serialVersionUID = -5592150438626222295L;

			public void setValue(Object x) {
				super.setValue(x);
				((JComboBox) editorComponent).setSelectedItem(x);
			}

			public Object getCellEditorValue() {
				String selected = (String) ((JComboBox) editorComponent).getSelectedItem();
				if ((selected != null) && (selected.trim().length() == 0))
					selected = null;
				return selected;
			}
		};
	}

	public DefaultPropertyValueCellEditor(ParameterTypeBoolean type) {
		super(new JCheckBox());
		((JCheckBox) editorComponent).setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		editorComponent.setBackground(javax.swing.UIManager.getColor("Table.cellBackground"));
		useEditorAsRenderer = true;
	}
    
    public DefaultPropertyValueCellEditor(final ParameterTypeInt type) {
        super(new JTextField());
        ((JTextField) editorComponent).setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ((JTextField) editorComponent).setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        this.delegate = new EditorDelegate() {

            private static final long serialVersionUID = 152467444047540403L;

            public void setValue(Object x) {
                super.setValue(x);
                if (x != null) {
                    if ((x instanceof Integer) || (x instanceof String))
                        ((JTextField) editorComponent).setText(x.toString());
                    else
                        throw new IllegalArgumentException("Illegal value class for integer parameter: " + x.getClass().getName());
                }
            }

            public Object getCellEditorValue() {
                try {
                    int i = Integer.parseInt(((JTextField) editorComponent).getText());
                    if (i < type.getMinValue())
                        i = (int) type.getMinValue();
                    if (i > type.getMaxValue())
                        i = (int) type.getMaxValue();
                    return Integer.valueOf(i);
                } catch (NumberFormatException e) {
                    return type.getDefaultValue();
                }
            }
        };
        
        editorComponent.setToolTipText(type.getDescription() + " (" + type.getRange() + ")");
        useEditorAsRenderer = true;
    }

    public DefaultPropertyValueCellEditor(final ParameterTypeDouble type) {
        super(new JTextField());
        ((JTextField) editorComponent).setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ((JTextField) editorComponent).setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        this.delegate = new EditorDelegate() {

            private static final long serialVersionUID = 5764937097891322370L;

            public void setValue(Object x) {
                super.setValue(x);
                if (x != null) {
                    if ((x instanceof Double) || (x instanceof String))
                        ((JTextField) editorComponent).setText(x.toString());
                    else
                        throw new IllegalArgumentException("Illegal value class for double parameter: " + x.getClass().getName());
                }
            }

            public Object getCellEditorValue() {
                try {
                    double d = Double.parseDouble(((JTextField) editorComponent).getText());
                    if (d < type.getMinValue())
                        d = type.getMinValue();
                    if (d > type.getMaxValue())
                        d = type.getMaxValue();
                    return Double.valueOf(d);
                } catch (NumberFormatException e) {
                    return type.getDefaultValue();
                }
            }
        };
        
        editorComponent.setToolTipText(type.getDescription() + " (" + type.getRange() + ")");
        useEditorAsRenderer = true;
    }
    
	public DefaultPropertyValueCellEditor(final ParameterTypePassword type) {
		super(new JPasswordField());
		((JPasswordField) editorComponent).setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		this.delegate = new EditorDelegate() {

			private static final long serialVersionUID = -2736861014783898296L;

			public void setValue(Object x) {
				super.setValue(x);
				if (x != null) {
					((JTextField) editorComponent).setText(x.toString());
				}
			}
			
			public Object getCellEditorValue() {
				String text = ((JTextField) editorComponent).getText();
				if ((text == null) || (text.length() == 0))
					return type.getDefaultValue();
				else
					return text;
			}
		};
		useEditorAsRenderer = true;
	}
    
	public DefaultPropertyValueCellEditor(final ParameterType type) {
		super(new JTextField());
		((JTextField) editorComponent).setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		this.delegate = new EditorDelegate() {

			private static final long serialVersionUID = -2868203350553070093L;

			public void setValue(Object x) {
				super.setValue(x);
				if (x != null) {
					((JTextField) editorComponent).setText(x.toString());
				}
			}
			
			public Object getCellEditorValue() {
				String text = ((JTextField) editorComponent).getText();
				if ((text == null) || (text.length() == 0))
					return type.getDefaultValue();
				else
					return text;
			}
		};
		useEditorAsRenderer = true;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
		if (isSelected)
			c.setBackground(SwingTools.LIGHTEST_BLUE);
		else
			c.setBackground(Color.WHITE);
		return c;
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component c = getTableCellEditorComponent(table, value, isSelected, row, column);
		if (isSelected)
			c.setBackground(SwingTools.LIGHTEST_BLUE);
		else
			c.setBackground(Color.WHITE);
		return c;
	}

	public boolean useEditorAsRenderer() {
		return useEditorAsRenderer;
	}

    /** Does nothing. */
    public void setOperator(Operator operator) {}
}
