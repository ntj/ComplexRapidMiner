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
package com.rapidminer.gui.dialog;

import com.rapidminer.gui.tools.syntax.JEditTextArea;

/**
 * This class wraps a JEditTextArea for searching purposes.
 * 
 * @author Ingo Mierswa
 * @version $Id: SearchableJEditTextArea.java,v 1.1 2007/05/27 22:02:45 ingomierswa Exp $
 */
public class SearchableJEditTextArea implements SearchableTextComponent {
	
	private JEditTextArea component;

	public SearchableJEditTextArea(JEditTextArea component) {
		this.component = component;
	}
	
	public void select(int start, int end) {
		this.component.select(start, end);
	}

	public int getCaretPosition() {
		return this.component.getCaretPosition();
	}

	public String getText() {
		return this.component.getText();
	}

	public void replaceSelection(String newString) {
		this.component.setSelectedText(newString);
	}

	public void requestFocus() {
		this.component.requestFocus();
	}

	public void setCaretPosition(int pos) {
		this.component.setCaretPosition(pos);
	}
    
    public boolean canHandleCarriageReturn() {
        return true;
    }
}
