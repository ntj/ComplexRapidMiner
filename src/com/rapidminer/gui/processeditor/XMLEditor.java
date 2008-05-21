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

import java.awt.BorderLayout;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.swing.JPanel;

import com.rapidminer.Process;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.syntax.JEditTextArea;
import com.rapidminer.operator.Operator;


/**
 * A text area for editing the process as XML. This editor is the second
 * possible way to edit or create RapidMiner processes. All changes are reflected by
 * the process. However, it should be more convenient to use the tree view
 * for process design. <br/>
 * 
 * This XML editor support very simple syntax highlighting based on keyword
 * parsing.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: XMLEditor.java,v 1.4 2008/05/09 19:23:16 ingomierswa Exp $
 */
public class XMLEditor extends JPanel implements ProcessEditor {

	private static final long serialVersionUID = 4172143138689034659L;
		
	private JEditTextArea editor;
	
	private MainFrame mainFrame;
	
	public XMLEditor(MainFrame mainFrame) {
		super(new BorderLayout());
		this.mainFrame = mainFrame;
		
		// create text area
		this.editor = new com.rapidminer.gui.tools.XMLEditor();
		
		// add popup menu for right click
		add(editor, BorderLayout.CENTER);
	}
	
	public void setText(String text) {
		this.editor.setText(text);		
	}
	
	public void processChanged(Operator op) {
		setText(op.getXML(""));
	}

	/** Just jumps to the position of the currently selected operator. */
    public void setCurrentOperator(Operator currentOperator) {
		if (currentOperator != null) {
			this.editor.requestFocus();
			this.editor.setCaretPosition(0);
			String name = currentOperator.getName();
			String text = this.editor.getText();
			int result = text.indexOf("\"" + name + "\"");
			if (result >= 0) {
				this.editor.select(result + 1, result + name.length() + 1);
			}
		}
	}
	
	public void validateProcess() throws Exception {
		InputStream in = new ByteArrayInputStream(editor.getText().getBytes());
		Process newExp = new Process(in);
		in.close();

		if (!newExp.getRootOperator().getXML("").equals(RapidMinerGUI.getMainFrame().getProcess().getRootOperator().getXML(""))) {
			newExp.setProcessFile(RapidMinerGUI.getMainFrame().getProcess().getProcessFile());
			RapidMinerGUI.getMainFrame().setProcess(newExp, true);
			mainFrame.processChanged();
		}
	}
}
