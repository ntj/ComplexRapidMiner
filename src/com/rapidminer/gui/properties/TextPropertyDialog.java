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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.HTMLEditor;
import com.rapidminer.gui.tools.JavaEditor;
import com.rapidminer.gui.tools.PlainTextEditor;
import com.rapidminer.gui.tools.SQLEditor;
import com.rapidminer.gui.tools.XMLEditor;
import com.rapidminer.gui.tools.syntax.JEditTextArea;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterTypeText;
import com.rapidminer.parameter.TextType;


/**
 * A Dialog displaying a {@link JEditTextArea}. This can be used to type some lengthy
 * text instead of the short text fields usually used for ParameterTypeStrings. This
 * dialog is used by the {@link TextValueCellEditor}.
 * 
 * @author Ingo Mierswa
 * @version $Id: TextPropertyDialog.java,v 1.4 2008/05/09 19:22:46 ingomierswa Exp $
 */
public class TextPropertyDialog extends JDialog {

    private static final long serialVersionUID = 8574310060170861505L;

    private String text = null;
    
    private boolean ok = false;

    private JEditTextArea textArea = null;
    
    
    public TextPropertyDialog(final ParameterTypeText type, String text, Operator operator) {
        super(RapidMinerGUI.getMainFrame(), "Text Editor for '" + type.getKey() + "'", true);
        this.text = text;
        
        getContentPane().setLayout(new BorderLayout());

        // buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });
        buttonPanel.add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
        buttonPanel.add(cancelButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        
        // text area
        this.textArea = createTextArea(type.getTextType());
        this.textArea.setText(this.text);
        getContentPane().add(this.textArea, BorderLayout.CENTER);
        
        setSize((int)(0.8 * RapidMinerGUI.getMainFrame().getWidth()), (int)(0.8 * RapidMinerGUI.getMainFrame().getHeight()));
        setLocationRelativeTo(RapidMinerGUI.getMainFrame());
    }

    private void ok() {
        ok = true;
        this.text = this.textArea.getText();
        dispose();
    }

    private void cancel() {
        ok = false;
        dispose();
    }

    public boolean isOk() {
        return ok;
    }
    
    public String getText() {
        return this.text;
    }
    
    private  JEditTextArea createTextArea(TextType type) {
        switch (type) {
        	case PLAIN: return new PlainTextEditor();
            case XML: return new XMLEditor();
            case HTML: return new HTMLEditor();
            case SQL: return new SQLEditor();
            case JAVA: return new JavaEditor();
        }
        return new PlainTextEditor();
    }
}
