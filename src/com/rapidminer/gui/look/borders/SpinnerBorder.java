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

import com.rapidminer.gui.look.Colors;
import com.rapidminer.gui.look.RapidLookTools;

/**
 * The UIResource for empty splinner borders.
 *
 * @author Ingo Mierswa
 * @version $Id: SpinnerBorder.java,v 1.2 2008/05/09 19:22:44 ingomierswa Exp $
 */
public class SpinnerBorder extends AbstractBorder implements UIResource {

	private static final long serialVersionUID = -3165427531529058453L;

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
		g.translate(x, y);
		g.setColor(Colors.getWhite());
		g.drawLine(1, 6, 1, h - 7);
		g.drawLine(2, 4, 2, h - 5);
		g.drawLine(3, 3, 3, h - 4);
		g.drawLine(4, 2, 4, h - 3);
		g.drawLine(5, 2, 5, h - 3);

		g.setColor(RapidLookTools.getColors().getSpinnerColors()[8]);
		g.drawLine(w - 2, 6, w - 2, h - 7);
		g.translate(-x, -y);
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(1, 6, 1, 2);
	}

	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		insets.top = 1;
		insets.bottom = 1;
		insets.left = 6;
		insets.right = 2;
		return insets;
	}
}
