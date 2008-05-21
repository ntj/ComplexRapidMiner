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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicFormattedTextFieldUI;

import com.rapidminer.gui.look.ClipboardActionsPopup;

/**
 * The UI for formatted text fields.
 *
 * @author Ingo Mierswa
 * @version $Id: FormattedTextFieldUI.java,v 1.3 2008/05/09 19:22:42 ingomierswa Exp $
 */
public class FormattedTextFieldUI extends BasicFormattedTextFieldUI {

	private static class FormattedTextFieldFocusListener implements FocusListener {

		public void focusGained(FocusEvent e) {
			((JComponent) e.getSource()).repaint();
		}

		public void focusLost(FocusEvent e) {
			((JComponent) e.getSource()).repaint();
		}
	}

	private class FormattedTextFieldPopupListener extends MouseAdapter {

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

	private FormattedTextFieldPopupListener popupListener = new FormattedTextFieldPopupListener();

	private FormattedTextFieldFocusListener focusListener = new FormattedTextFieldFocusListener();
	
	
	public static ComponentUI createUI(JComponent c) {
		return new FormattedTextFieldUI();
	}

	@Override
	protected void installDefaults() {
		super.installDefaults();
		getComponent().addFocusListener(this.focusListener);
		getComponent().addMouseListener(this.popupListener);
	}

	@Override
	protected void uninstallDefaults() {
		super.installDefaults();
		getComponent().removeFocusListener(this.focusListener);
		getComponent().removeMouseListener(this.popupListener);
		this.popup = null;
	}

	@Override
	public void update(Graphics g, JComponent c) {
		super.update(g, c);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
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
