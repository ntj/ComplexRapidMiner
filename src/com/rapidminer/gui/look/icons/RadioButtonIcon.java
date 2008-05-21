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
package com.rapidminer.gui.look.icons;

import java.awt.Component;
import java.awt.Graphics;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.plaf.UIResource;

import com.rapidminer.gui.look.painters.CashedPainter;

/**
 * The radio button icon.
 *
 * @author Ingo Mierswa
 * @version $Id: RadioButtonIcon.java,v 1.2 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class RadioButtonIcon implements Icon, UIResource, Serializable {
	
	private static final long serialVersionUID = -2576744883403903818L;

	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.translate(x, y);
		CashedPainter.drawRadioButton(c, g);
		g.translate(-x, -y);
	}

	public int getIconWidth() {
		return 16;
	}

	public int getIconHeight() {
		return 16;
	}
}
