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
package com.rapidminer.gui.look.borders;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;

import com.rapidminer.gui.look.RapidLookTools;

/**
 * The UIResource for internal frame borders.
 *
 * @author Ingo Mierswa
 * @version $Id: InternalFrameBorder.java,v 1.2 2008/05/09 19:22:44 ingomierswa Exp $
 */
public class InternalFrameBorder extends AbstractBorder implements UIResource {

	private static final long serialVersionUID = -7249038472856067993L;

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
		g.setColor(RapidLookTools.getColors().getBorderColors()[0][1]);
		g.drawLine(1, 23, 1, h - 2);
		g.drawLine(w - 2, 23, w - 2, h - 2);
		g.drawLine(1, h - 2, w - 2, h - 2);

		g.setColor(RapidLookTools.getColors().getBorderColors()[0][0]);
		g.drawLine(0, 23, 0, h - 1);
		g.drawLine(2, 23, 2, h - 3);
		g.drawLine(w - 1, 23, w - 1, h - 1);
		g.drawLine(w - 3, 23, w - 3, h - 3);
		g.drawLine(0, h - 1, w - 1, h - 1);
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return getBorderInsets(c, new Insets(0, 3, 2, 3));
	}

	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		insets.top = 0;
		insets.left = 3;
		insets.right = 3;
		insets.bottom = 2;
		return insets;
	}

	@Override
	public boolean isBorderOpaque() {
		return true;
	}
}
