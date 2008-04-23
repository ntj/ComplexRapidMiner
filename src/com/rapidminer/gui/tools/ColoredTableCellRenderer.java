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
package com.rapidminer.gui.tools;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import com.rapidminer.tools.Tools;


/**
 * The default table cell renderer for all viewer tables. Provides the correct border and colors.
 * Numbers will be formatted with the generic number of fraction digits.
 * 
 * @author Ingo Mierswa
 * @version $Id: ColoredTableCellRenderer.java,v 1.1 2007/05/27 21:59:31 ingomierswa Exp $
 */
public class ColoredTableCellRenderer implements TableCellRenderer {

	private static final Color SELECTED_COLOR = UIManager.getColor("Tree.selectionBackground");

	private static final Color TEXT_SELECTED_COLOR = UIManager.getColor("Tree.selectionForeground");

	private static final Color TEXT_NON_SELECTED_COLOR = UIManager.getColor("Table.textForeground");
	
	private JTextField renderer = new JTextField();

	public ColoredTableCellRenderer() {
		renderer.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));	
	}

	public void setColor(Color color) {
		renderer.setBackground(color);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		String text = null;
		if (value instanceof Number) {
			Number number = (Number)value;
			double numberValue = number.doubleValue();
			text = Tools.formatIntegerIfPossible(numberValue);
		} else {
			if (value != null)
				text = value.toString();
			else
				text = "?";
		}
		renderer.setText(text);
		
		if (isSelected) {
			renderer.setBackground(SELECTED_COLOR);
			renderer.setForeground(TEXT_SELECTED_COLOR);
		} else {
			renderer.setForeground(TEXT_NON_SELECTED_COLOR);
		}
		
		return renderer;
	}	
}
