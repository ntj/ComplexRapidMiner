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

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.dialog.SearchDialog;
import com.rapidminer.gui.dialog.SearchableJTextComponent;
import com.rapidminer.gui.tools.actions.ClearMessageAction;
import com.rapidminer.gui.tools.actions.LoggingSearchAction;
import com.rapidminer.gui.tools.actions.SaveLogFileAction;
import com.rapidminer.operator.ProcessRootOperator;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * A text area displaying the log output. All kinds of streams can be redirected
 * to this viewer by using an instance of a special inner stream subclass. The
 * message viewer highlights some text which contains keywords like
 * &quot;error&quot; or &quot;warning&quot;. Since keeping all lines might
 * dramatically increase memory usage and slow down RapidMiner, only a maximum number
 * of lines is displayed.
 * 
 * @author Ingo Mierswa
 * @version $Id: LoggingViewer.java,v 1.8 2008/05/09 19:22:59 ingomierswa Exp $
 */
public class LoggingViewer extends ExtendedJScrollPane implements MouseListener {

	private static final long serialVersionUID = 551259537624386372L;

	/** A stream that can be used to print to this text area. */
	public transient LoggingViewerOutputStream outputStream;

	public transient final Action CLEAR_MESSAGE_VIEWER_ACTION_24 = new ClearMessageAction(this, IconSize.SMALL);
	public transient final Action CLEAR_MESSAGE_VIEWER_ACTION_32 = new ClearMessageAction(this, IconSize.MIDDLE);

	public transient final Action SAVE_LOGFILE_ACTION_24 = new SaveLogFileAction(this, IconSize.SMALL);
	public transient final Action SAVE_LOGFILE_ACTION_32 = new SaveLogFileAction(this, IconSize.MIDDLE);

	public transient final Action SEARCH_ACTION_24 = new LoggingSearchAction(this, IconSize.SMALL);
	public transient final Action SEARCH_ACTION_32 = new LoggingSearchAction(this, IconSize.MIDDLE);

	private JTextPane textArea;

	public LoggingViewer() {
		this(new JTextPane());
	}

	private LoggingViewer(JTextPane textArea) {
		super(textArea);
		this.textArea = textArea;
		this.textArea.setToolTipText("Displays logging messages according to the current log verbosity (parameter of root operator).");
		this.textArea.setEditable(false);
		this.textArea.addMouseListener(this);
        this.textArea.setFont(this.textArea.getFont().deriveFont(Font.PLAIN));
        this.outputStream = new LoggingViewerOutputStream(this.textArea);
	}

	protected Object readResolve() {
		this.outputStream = new LoggingViewerOutputStream(this.textArea);
		return this;
	}
	
	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mouseClicked(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {
		evaluatePopup(e);
	}

	public void mousePressed(MouseEvent e) {
		evaluatePopup(e);
	}

	private void evaluatePopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			createPopupMenu().show(textArea, e.getX(), e.getY());
		}
	}

	private JPopupMenu createPopupMenu() {
		JPopupMenu menu = new JPopupMenu();
		menu.add(CLEAR_MESSAGE_VIEWER_ACTION_24);
		menu.add(SAVE_LOGFILE_ACTION_24);
		menu.add(SEARCH_ACTION_24);
		return menu;
	}

	public String getLogMessage() {
		return textArea.getText();
	}

	public void clear() {
		outputStream.clear();
		textArea.setText("");
	}
	
	public void saveLog() {
		File file = new File("." + File.separator);
		String logFile = null;
		try {
			logFile = RapidMinerGUI.getMainFrame().getProcess().getRootOperator().getParameterAsString(ProcessRootOperator.PARAMETER_LOGFILE); 
		} catch (UndefinedParameterError ex) {
          // tries to use process file name for initialization
		} 
		if (logFile != null) {
			file = RapidMinerGUI.getMainFrame().getProcess().resolveFileName(logFile);
		} else {
			file = RapidMinerGUI.getMainFrame().getProcess().getProcessFile();
			if (file != null)
				file = file.getParentFile();
		}
		file = SwingTools.chooseFile(this, file, false, "log", "log file");
		if (file != null) {
			PrintWriter out = null;
			try {
				out = new PrintWriter(new FileWriter(file));
				out.println(textArea.getText());
			} catch (IOException ex) {
				SwingTools.showSimpleErrorMessage("Cannot write log file.", ex);
			} finally {
				if (out != null) {
					out.close();
				}
			}
		}
	}
	
	public void performSearch() {
		new SearchDialog(this, new SearchableJTextComponent(textArea)).setVisible(true);
	}
}
