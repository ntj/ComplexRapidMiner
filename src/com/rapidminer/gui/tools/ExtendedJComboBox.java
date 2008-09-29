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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * A combo box which can use a predefined preferred size. Can also show the full value as
 * tool tip in cases where the strings were too short.
 * 
 * @author Ingo Mierswa
 * @version $Id: ExtendedJComboBox.java,v 1.1 2008/08/21 13:17:07 ingomierswa Exp $
 */
public class ExtendedJComboBox extends JComboBox {

	private static final long serialVersionUID = 8320969518243948543L;
	
	private static class ExtendedComboBoxRenderer extends BasicComboBoxRenderer {

		private static final long serialVersionUID = -6192190927539294311L;

		public Component getListCellRendererComponent(JList list, Object value,	int index, boolean isSelected, boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
				if (index >= 0) {
					list.setToolTipText((value == null) ? null : value.toString());
				}
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setFont(list.getFont());
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}
	  
	private int preferredWidth = -1;
	
	public ExtendedJComboBox() {
		this(-1);
	}
	
	public ExtendedJComboBox(int preferredWidth) {
		this.preferredWidth = preferredWidth;
		
		setRenderer(new ExtendedComboBoxRenderer());
	}
	
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		if (this.preferredWidth != -1) {
			if (preferredWidth < dim.getWidth()) {
				return new Dimension(preferredWidth, (int)dim.getHeight());
			} else {
				return dim;
			}
		} else {
			return dim;
		}
	}
}
