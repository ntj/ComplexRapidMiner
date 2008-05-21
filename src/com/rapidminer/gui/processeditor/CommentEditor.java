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
package com.rapidminer.gui.processeditor;

import java.awt.event.KeyEvent;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.syntax.HTMLTokenMarker;
import com.rapidminer.gui.tools.syntax.JEditTextArea;
import com.rapidminer.operator.Operator;


/**
 * This editor is used to edit the description (comment) for the currently selected
 * operator.
 * 
 * @author Ingo Mierswa
 * @version $Id: CommentEditor.java,v 1.2 2008/05/09 19:23:16 ingomierswa Exp $
 */
public class CommentEditor extends JEditTextArea implements ProcessEditor {
	
	private static final long serialVersionUID = -2661346182983330754L;
	
	private transient Operator currentOperator;
	
	public CommentEditor() {				
		super(SwingTools.getTextAreaDefaults());
		
		// set token marker (HTML in this case)
		setTokenMarker(new HTMLTokenMarker());
	}
	
	public void setCurrentOperator(Operator operator) {
		this.currentOperator = operator;
		if (this.currentOperator != null) {
			String description = this.currentOperator.getUserDescription();
			if (description != null) {
				String text = SwingTools.text2SimpleHtml(description);
				setText(text);
			} else {
				setText(null);
			}
		} else {
			setText(null);
		}
	}

	public void processChanged(Operator operator) {}

	public void validateProcess() throws Exception {
		saveComment();
	}
	
	public void saveComment() {
		String oldExp = RapidMinerGUI.getMainFrame().getProcess().getRootOperator().getXML("");
		if (this.currentOperator != null)
			this.currentOperator.setUserDescription(SwingTools.html2RapidMinerText(getText()));
		String newExp = RapidMinerGUI.getMainFrame().getProcess().getRootOperator().getXML("");
		
		if (!newExp.equals(oldExp)) {
			RapidMinerGUI.getMainFrame().processChanged();
		}		
	}

	/**
	 * Overwrites the super method in order to save the typed text.
	 */
	public void processKeyEvent(KeyEvent evt) {
		super.processKeyEvent(evt);
		switch (evt.getID()) {
		case KeyEvent.KEY_RELEASED:
			saveComment();
			break;
		}
	}
}
