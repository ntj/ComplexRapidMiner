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
import java.awt.Paint;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;


/**
 * Renderer class for operator chains.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ChainRenderer.java,v 1.4 2008/05/09 19:23:02 ingomierswa Exp $
 */
public abstract class ChainRenderer extends OperatorRenderer {

	private static final int BORDER = 10;

	public abstract Paint getBackground(Dimension d);

	public void drawOperator(Operator op, Graphics2D g) {
		Dimension d = getSize(op, g);
		g.setPaint(getBackground(d));
		g.fillRect(0, 0, (int) d.getWidth(), (int) d.getHeight());
		g.setPaint(Color.black);
		g.setStroke(new BasicStroke(3));
		g.drawRect(0, 0, (int) d.getWidth(), (int) d.getHeight());
		Graphics2D g2 = (Graphics2D) g.create();
		g2.translate(BORDER, BORDER);
		drawName(op, g2);
		g2.dispose();

		double width = d.getWidth();
		double y = 2 * BORDER + getNameSize(op, g).getHeight();

		OperatorChain chain = (OperatorChain) op;
		for (int i = 0; i < chain.getNumberOfOperators(); i++) {
			Operator innerOp = chain.getOperator(i);
			OperatorRenderer renderer = ProcessRenderer.getRenderer(innerOp);

			Dimension innerSize = renderer.getSize(innerOp, g);
			Graphics2D gn = (Graphics2D) g.create((int) (width / 2 - innerSize.getWidth() / 2), (int) y, (int) innerSize.getWidth(), (int) innerSize.getHeight());
			renderer.drawOperator(innerOp, gn);
			gn.dispose();
			y += innerSize.getHeight() + BORDER;
		}

	}

	public Dimension getSize(Operator operator, Graphics2D g) {
		Dimension d = getNameSize(operator, g);

		OperatorChain chain = (OperatorChain) operator;
		double width = d.getWidth();
		double height = d.getHeight() + BORDER;
		for (int i = 0; i < chain.getNumberOfOperators(); i++) {
			Operator innerOp = chain.getOperator(i);
			OperatorRenderer renderer = ProcessRenderer.getRenderer(innerOp);
			Dimension innerSize = renderer.getSize(innerOp, g);
			width = Math.max(width, innerSize.getWidth());
			height += innerSize.getHeight() + BORDER;
		}

		return new Dimension((int) width + 2 * BORDER, (int) height + BORDER);
	}

}
