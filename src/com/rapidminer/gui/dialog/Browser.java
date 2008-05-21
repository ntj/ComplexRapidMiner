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
package com.rapidminer.gui.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;


/**
 * A simple HTML browser which can be used for displaying simple contents, e.g.
 * the GUI manual.
 * 
 * @author Ingo Mierswa
 * @version $Id: Browser.java,v 1.4 2008/05/09 19:23:20 ingomierswa Exp $
 */
public class Browser extends JEditorPane implements HyperlinkListener {

	private static final long serialVersionUID = -2342332990027338104L;

	public Browser() {
		setEditable(false);
		setMargin(new java.awt.Insets(5, 5, 5, 5));
		addHyperlinkListener(this);
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			if (e instanceof HTMLFrameHyperlinkEvent) {
				HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
				HTMLDocument doc = (HTMLDocument) this.getDocument();
				doc.processHTMLFrameHyperlinkEvent(evt);
			} else {
				try {
					setPage(e.getURL());
				} catch (Throwable t) {
					SwingTools.showSimpleErrorMessage("While following link:", t);
				}
			}
		}
	}

	private JMenuItem createItem(String name, final URL url) {
		if (url != null) {
			JMenuItem item = new JMenuItem(name);
			item.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					try {
						Browser.this.setPage(url);
					} catch (IOException t) {
						SwingTools.showSimpleErrorMessage("Cannot find '" + url + "'", t);
					}
				}
			});
			return item;
		} else {
			return null;
		}
	}

	public static void showDialog(URL url) {
		Browser browser = new Browser();
		JDialog dialog = new JDialog();
		dialog.setTitle("RapidMiner Browser");

		JMenuBar menuBar = new JMenuBar();
		JMenu visitMenu = new JMenu("Browse");
		JMenuItem item = browser.createItem("RapidMiner GUI Manual", Tools.getResource("manual/RapidMinerGUIManual.html"));
		if (item != null)
			visitMenu.add(item);
		else
			LogService.getGlobal().log("Cannot show GUI manual: resource 'manual/RapidMinerGUIManual.html' not found...", LogService.WARNING);
		
		try {
			item = browser.createItem("RapidMiner@WWW", new URL("http://www.rapidminer.com"));
			if (item != null)
				visitMenu.add(item);
			else
                LogService.getGlobal().log("Cannot show RapidMiner web site: URL 'http://www.rapidminer.com' not found...", LogService.WARNING);
		} catch (MalformedURLException e) {
            LogService.getGlobal().log("Cannot show RapidMiner web site: " + e.getMessage(), LogService.WARNING);
		}
		item = browser.createItem("License", Tools.getResource("LICENSE.html"));
		if (item != null)
			visitMenu.add(item);
		else
            LogService.getGlobal().log("Cannot show license: resource 'LICENSE.html' not found...", LogService.WARNING);
				
		menuBar.add(visitMenu);
		dialog.setJMenuBar(menuBar);

		JScrollPane editorScrollPane = new ExtendedJScrollPane(browser);
		editorScrollPane.setPreferredSize(new java.awt.Dimension(600, 500));
		dialog.getContentPane().add(editorScrollPane);
		dialog.pack();
		dialog.setVisible(true);
		try {
			browser.setPage(url);
		} catch (Throwable e) {
			SwingTools.showSimpleErrorMessage("Cannot find '" + url + "'!", e);
		}
	}
}
