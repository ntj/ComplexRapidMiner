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
import java.io.OutputStream;
import java.util.LinkedList;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;


/**
 * A special output stream that appends all its incoming characters and perform necessary formatting.
 * 
 * @author Ingo Mierswa
 * @version $Id: LoggingViewerOutputStream.java,v 1.5 2008/05/09 19:22:59 ingomierswa Exp $
 */
public class LoggingViewerOutputStream extends OutputStream {
    
    private JTextPane textArea;
    
    private int currentLineLength = 0;
    
    private char lastChar = (char)-1;

    private SimpleAttributeSet attributeSet;
    
    private LinkedList<Integer> lineLengths = new LinkedList<Integer>();

    private StringBuffer lastStyledText = new StringBuffer();
    
    public LoggingViewerOutputStream(JTextPane textArea) {
        this.attributeSet = new SimpleAttributeSet();
        this.textArea = textArea;
    }
    
    private void addTextToBuffer(String text) {
        lastStyledText.append(text);
    }
    
    private synchronized void addTextToViewer() {
        Document doc = textArea.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), lastStyledText.toString(), attributeSet);
        } catch (BadLocationException e) {
            // do nothing
        }
        String maxRowsString = System.getProperty(MainFrame.PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_ROWLIMIT);
        int maxRows = 1000;
        try {
        	if (maxRowsString != null)
        		maxRows = Integer.parseInt(maxRowsString);
        } catch (NumberFormatException e) {
        	LogService.getGlobal().log("Bad integer format for property '', using default number of maximum rows for logging (1000).", LogService.WARNING);
        }
        if (maxRows >= 0) {
            int removeLength = 0;
            while (lineLengths.size() > maxRows) {
                removeLength += lineLengths.removeFirst();
            }
            try {
                doc.remove(0, removeLength);
            } catch (BadLocationException e) {
                SwingTools.showSimpleErrorMessage("Error during logging: ", e);
            }                
        }
        textArea.setCaretPosition(textArea.getDocument().getLength());
        lastStyledText = new StringBuffer();
    }
    
    public void write(int b) {
        char c = (char) b;
        switch (b) {
            case 0x000d: // carriage return \r (on Windows \r\n for line breaks)
            case 0x000a: // linefeed \n
                if (lastChar != (char)-1) { // add only a line break if not performed a line break as last
                    addTextToBuffer(Tools.getLineSeparator());
                    lineLengths.add(currentLineLength + Tools.getLineSeparator().length());
                    addTextToViewer();
                }
                lastChar = (char)-1;
                currentLineLength = 0;                
                break;
            case '\t':
                addTextToBuffer("    ");
                currentLineLength += "    ".length();
                break;
            default:
                if (lastChar == '$') {
                    addTextToViewer();
                    evaluateCommand("$" + c);
                } else if (lastChar == '^') {
                    addTextToViewer();
                    evaluateCommand("^" + c);
                } else {
                    if ((c != '$') && (c != '^')) {
                        lastStyledText.append(c);
                        currentLineLength++;
                    }
                }
                lastChar = c;
                break;
        }
    }
    
    private void evaluateCommand(String style) {
        if (style.equals("$b")) {
            StyleConstants.setBold(attributeSet, true);                
        } else if (style.equals("^b")) {
            StyleConstants.setBold(attributeSet, false);
        } else if (style.equals("$i")) {
            StyleConstants.setItalic(attributeSet, true);                
        } else if (style.equals("^i")) {
            StyleConstants.setItalic(attributeSet, false);        
        } else if (style.equals("$m")) {
            StyleConstants.setFontFamily(attributeSet, "monospaced");                
        } else if (style.equals("^m")) {
            StyleConstants.setFontFamily(attributeSet, "sansserif");
        } else if (style.equals("$e")) {
            StyleConstants.setForeground(attributeSet, getHighlightingColor("rapidminer.gui.messageviewer.highlight.errors", new Color(255, 51, 204)));
        } else if (style.equals("^e")) {
            StyleConstants.setForeground(attributeSet, Color.BLACK);
        } else if (style.equals("$w")) {
            StyleConstants.setForeground(attributeSet, getHighlightingColor("rapidminer.gui.messageviewer.highlight.warnings", new Color(51, 51, 255)));
        } else if (style.equals("^w")) {
            StyleConstants.setForeground(attributeSet, Color.BLACK);
        } else if (style.equals("$n")) {
            StyleConstants.setForeground(attributeSet, getHighlightingColor("rapidminer.gui.messageviewer.highlight.notes", new Color(51, 151, 51)));
        } else if (style.equals("^n")) {
            StyleConstants.setForeground(attributeSet, Color.BLACK);
        } else if (style.equals("$g")) {
            StyleConstants.setForeground(attributeSet, getHighlightingColor("rapidminer.gui.messageviewer.highlight.logservice", new Color(184, 184, 184)));
        } else if (style.equals("^g")) {
            StyleConstants.setForeground(attributeSet, Color.BLACK);
        }
    }

    public void clear() {
        lastChar = (char)-1;
        currentLineLength = 0;
        lineLengths.clear();
    }
    
    private Color getHighlightingColor(String propertyName, Color errorColor) {
        String propertyString = System.getProperty(propertyName);
        if (propertyString != null) {
        	String[] colors = propertyString.split(",");
        	Color color = new Color(Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2]));
        	return color;
        } else {
        	return errorColor;
        }
    }
}
