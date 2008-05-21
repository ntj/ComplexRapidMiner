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
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JTable;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterTypeColor;


/**
 * Cell editor consisting of a colored button which opens a color chooser as
 * action. Currently only used for property setting, not for parameters.
 * 
 * @author Ingo Mierswa
 * @version $Id: ColorValueCellEditor.java,v 1.4 2008/05/09 19:22:46 ingomierswa Exp $
 */
public class ColorValueCellEditor extends AbstractCellEditor implements PropertyValueCellEditor {

	private static final long serialVersionUID = -7069543356398085334L;

	private static class ColorIcon implements Icon {

		private Color color;
		
		private ColorIcon(Color color) {
			this.color = color;
		}

		public Color getColor() {
			return this.color;
		}
		
		public int getIconWidth() {
			return 20;
		}

		public int getIconHeight() {
			return 8;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(color);
			g.fillRoundRect(x, y, getIconWidth(), getIconHeight(), 2, 2);
			g.setColor(Color.black);
			g.drawRoundRect(x, y, getIconWidth(), getIconHeight(), 2, 2);
		}
	}
	
	private transient ParameterTypeColor type;
    
	private JButton button;
	
	public ColorValueCellEditor(final ParameterTypeColor type) {
		this.type = type;
		button = new JButton("Choose Color...");
        button.setToolTipText(type.getDescription());
        button.setIconTextGap(6);
        button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(RapidMinerGUI.getMainFrame(),
	                                                      "Choose Color for " + type.getKey(),
	                                                      ((ColorIcon)button.getIcon()).getColor());
                if (newColor != null)
                    setEditorColor(newColor);
				fireEditingStopped();
			}
        });
	}

    /** Does nothing. */
    public void setOperator(Operator operator) {}
    
	public void setEditorColor(Color color) {
		button.setIcon(new ColorIcon(color));
	}
	
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
		if (value == null) {
			setEditorColor((transformString2Color((String) type.getDefaultValue())));
		} else {
			setEditorColor(transformString2Color((String) value));
		}
		return button;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		return getTableCellEditorComponent(table, value, isSelected, row, column);
	}
	
	public Object getCellEditorValue() {
		Color color = ((ColorIcon)button.getIcon()).getColor();
		return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
	}

	public boolean useEditorAsRenderer() {
		return true;
	}

	private Color transformString2Color(String value) {
		String[] colors = value.split(",");
		Color color = new Color(Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2]));
		return color;
	}
}
