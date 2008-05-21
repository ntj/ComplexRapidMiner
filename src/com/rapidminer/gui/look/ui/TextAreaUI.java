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
package com.rapidminer.gui.look.ui;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextAreaUI;

import com.rapidminer.gui.look.ClipboardActionsPopup;

/**
 * The UI for text areas.
 *
 * @author Ingo Mierswa
 * @version $Id: TextAreaUI.java,v 1.2 2008/05/09 19:22:42 ingomierswa Exp $
 */
public class TextAreaUI extends BasicTextAreaUI {

	private class TextAreaPopupListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			evaluateClick(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			evaluateClick(e);
		}

		private void evaluateClick(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e.getPoint());
			}
		}
	}
	
	
	private ClipboardActionsPopup popup = null;

	private TextAreaPopupListener tfp = new TextAreaPopupListener();
	
	
	public static ComponentUI createUI(JComponent c) {
		return new TextAreaUI();
	}

	@Override
	protected void installDefaults() {
		super.installDefaults();
		getComponent().addMouseListener(this.tfp);
	}

	@Override
	protected void uninstallDefaults() {
		super.installDefaults();
		getComponent().removeMouseListener(this.tfp);
		this.popup = null;
	}

	@Override
	public void update(Graphics g, JComponent c) {
		super.update(g, c);
	}

	private void showPopup(Point p) {
		if (!getComponent().isEnabled()) {
			return;
		}
		if (this.popup == null) {
			this.popup = new ClipboardActionsPopup(getComponent());
		}
		getComponent().requestFocus();
		this.popup.show(getComponent(), (int) p.getX(), (int) p.getY());
	}
}
