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
package com.rapidminer.gui.tools;

import java.awt.Color;
import java.awt.Component;
import java.util.Date;

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
 * @version $Id: ColoredTableCellRenderer.java,v 1.4 2008/05/25 12:08:46 ingomierswa Exp $
 */
public class ColoredTableCellRenderer implements TableCellRenderer {

	private static final Color SELECTED_COLOR = UIManager.getColor("Tree.selectionBackground");

	private static final Color TEXT_SELECTED_COLOR = UIManager.getColor("Tree.selectionForeground");

	private static final Color TEXT_NON_SELECTED_COLOR = UIManager.getColor("Table.textForeground");
	
	private JTextField renderer = new JTextField();

	private int dateFormat = ExtendedJTable.NO_DATE_FORMAT;
	
	public ColoredTableCellRenderer() {
		renderer.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));	
	}

	public void setColor(Color color) {
		renderer.setBackground(color);
	}

	public void setDateFormat(int dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		String text = null;
		if (value instanceof Number) {
			Number number = (Number)value;
			double numberValue = number.doubleValue();
			text = Tools.formatIntegerIfPossible(numberValue);
		} else {
			if (value != null) {
				if (value instanceof Date) {
					switch (dateFormat) {
					case ExtendedJTable.DATE_FORMAT: text = Tools.formatDate((Date)value); break;
					case ExtendedJTable.TIME_FORMAT: text = Tools.formatTime((Date)value); break;
					case ExtendedJTable.DATE_TIME_FORMAT: text = Tools.formatDateTime((Date)value); break;
					default: text = value.toString(); break;
					}
				} else {
					text = value.toString();
				}
			} else {
				text = "?";
			}
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
