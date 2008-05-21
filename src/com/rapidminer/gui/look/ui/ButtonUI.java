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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.plaf.basic.BasicButtonUI;

import com.rapidminer.gui.look.ButtonListener;
import com.rapidminer.gui.look.RapidLookTools;
import com.rapidminer.gui.look.painters.CashedPainter;

/**
 * The UI for the basic button.
 *
 * @author Ingo Mierswa
 * @version $Id: ButtonUI.java,v 1.2 2008/05/09 19:22:42 ingomierswa Exp $
 */
public class ButtonUI extends BasicButtonUI {

	private final static ButtonUI BUTTON_UI = new ButtonUI();
	
	
	public static ComponentUI createUI(JComponent c) {
		return BUTTON_UI;
	}

	@Override
	protected void installDefaults(AbstractButton b) {
		super.installDefaults(b);
		b.setRolloverEnabled(true);
	}

	@Override
	protected void uninstallDefaults(AbstractButton b) {
		super.uninstallDefaults(b);
	}

	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
	}

	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
	}

	@Override
	protected BasicButtonListener createButtonListener(AbstractButton b) {
		return new ButtonListener(b);
	}

	@Override
	protected void paintText(Graphics g, AbstractButton c, Rectangle textRect, String text) {
		super.paintText(g, c, textRect, text);
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		AbstractButton b = (AbstractButton) c;
		if (b.isContentAreaFilled()) {
			if (b.getParent() instanceof JToolBar) {
				RapidLookTools.drawToolbarButton(g, c);
			} else {
				CashedPainter.drawButton(c, g);
			}
		}
		CashedPainter.drawButtonBorder(c, g, getPropertyPrefix());
		super.paint(g, c);
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		if (c.getParent() instanceof JToolBar) {
			return new Dimension((int) super.getPreferredSize(c).getWidth() + 6, (int) super.getPreferredSize(c).getHeight() + 6);
		} else {
			return new Dimension((int) super.getPreferredSize(c).getWidth() + 10, (int) super.getPreferredSize(c).getHeight() + 6);
		}
	}

	@Override
	protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {
		CashedPainter.drawButtonBorder(b, g, getPropertyPrefix());
	}

	@Override
	protected void paintButtonPressed(Graphics g, AbstractButton b) {
		setTextShiftOffset();
	}
}
