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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;


/**
 * A box view renderer for a leaf of the operator tree. A leaf should be an
 * simple operator which cannot have any children.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: LeafRenderer.java,v 1.3 2008/05/09 19:23:02 ingomierswa Exp $
 */
public class LeafRenderer extends OperatorRenderer {

	private static final int BORDER = 10;

	public void drawOperator(Operator op, Graphics2D g) {
		Dimension d = getSize(op, g);

		g.setPaint(SwingTools.makeBluePaint(d.getWidth(), d.getHeight()));
		g.fillRect(0, 0, (int) d.getWidth(), (int) d.getHeight());
		g.setPaint(Color.black);
		g.setStroke(new BasicStroke(3));
		g.drawRect(0, 0, (int) d.getWidth(), (int) d.getHeight());
		Graphics2D g2 = (Graphics2D) g.create();
		g2.translate(BORDER, BORDER);
		drawName(op, g2);
		g2.dispose();
	}

	public Dimension getSize(Operator operator, Graphics2D g) {
		Dimension d = getNameSize(operator, g);
		return new Dimension((int) (d.getWidth() + 2 * BORDER), (int) (d.getHeight() + 2 * BORDER));
	}

}
