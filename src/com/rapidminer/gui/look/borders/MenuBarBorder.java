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
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.UIResource;

/**
 * The UIResource for menu bar borders.
 *
 * @author Ingo Mierswa
 * @version $Id: MenuBarBorder.java,v 1.2 2008/05/09 19:22:44 ingomierswa Exp $
 */
public class MenuBarBorder extends AbstractBorder implements UIResource {

	private static final long serialVersionUID = -2583591973991105007L;

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
		g.translate(x, y);
		g.setColor(new ColorUIResource(220, 220, 220));
		g.drawLine(0, 0, w, 0);
		g.drawLine(0, h - 1, w, h - 1);
		g.setColor(new ColorUIResource(200, 200, 200));
		g.drawLine(0, 1, w, 1);
		g.drawLine(0, h - 2, w, h - 2);
		g.translate(-x, -y);
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(2, 5, 2, 0);
	}

	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		insets.top = 2;
		insets.bottom = 2;
		insets.left = 5;
		insets.right = 0;
		return insets;
	}
}
