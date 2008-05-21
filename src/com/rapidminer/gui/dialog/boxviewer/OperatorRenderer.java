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
package com.rapidminer.gui.dialog.boxviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;

import com.rapidminer.operator.Operator;


/**
 * Superclass for renderers that can display operators. Chains, wrappers and
 * simple operators must be treated differently. The rendered image can be used
 * in box view or in the wizard dialog.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: OperatorRenderer.java,v 1.4 2008/05/09 19:23:02 ingomierswa Exp $
 */
public abstract class OperatorRenderer {

	public static final Font NAME_FONT = new Font("LucidaSans", Font.BOLD, 12);

	public static final Font TYPE_FONT = new Font("LucidaSans", Font.PLAIN, 10);

	public static final int ICON_GAP = 4;

	public void drawName(Operator op, Graphics2D g) {
		ImageIcon icon = op.getOperatorDescription().getIcon();
		int x = 0;
		int y = 0;
		if (icon != null) {
			Image image = icon.getImage();
			g.drawImage(image, 0, 0, null);
			x += image.getWidth(null) + ICON_GAP;
		}
		g.setPaint(Color.black);
		g.setFont(NAME_FONT);
		Rectangle2D rect = NAME_FONT.getStringBounds(op.getName(), g.getFontRenderContext());
		g.drawString(op.getName(), (int) (x - rect.getX()), (int) (y - rect.getY()));
		y += rect.getHeight();

		g.setFont(TYPE_FONT);
		rect = TYPE_FONT.getStringBounds(op.getName(), g.getFontRenderContext());
		g.drawString(op.getOperatorDescription().getName(), (int) (x - rect.getX()), (int) (y - rect.getY()));
	}

	public Dimension getNameSize(Operator op, Graphics2D g) {
		Rectangle2D rect1 = NAME_FONT.getStringBounds(op.getName(), g.getFontRenderContext());
		Rectangle2D rect2 = TYPE_FONT.getStringBounds(op.getOperatorDescription().getName(), g.getFontRenderContext());
		double h = rect1.getHeight() + rect2.getHeight();
		double w = Math.max(rect1.getWidth(), rect2.getWidth());
		ImageIcon icon = op.getOperatorDescription().getIcon();
		if (icon != null) {
			Image image = icon.getImage();
			h = Math.max(image.getHeight(null), h);
			w += image.getWidth(null) + ICON_GAP;
		}
		return new Dimension((int) w, (int) h);
	}

	public abstract void drawOperator(Operator op, Graphics2D g);

	public abstract Dimension getSize(Operator op, Graphics2D g);
}
