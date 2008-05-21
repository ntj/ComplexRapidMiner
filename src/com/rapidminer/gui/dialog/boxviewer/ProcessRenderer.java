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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import com.rapidminer.operator.ProcessRootOperator;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.SimpleOperatorChain;


/**
 * A pure renderer that cannot (yet) edit the process definition. Can be used for
 * printing. This renderer generates the box view of a process definition.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: ProcessRenderer.java,v 2.8 2006/03/21 15:35:40 ingomierswa
 *          Exp $
 */
public class ProcessRenderer extends JPanel {

	private static final long serialVersionUID = 2111854024857950879L;

	private static OperatorRenderer leafRenderer = new LeafRenderer();

	private static OperatorRenderer chainRenderer = new SimpleChainRenderer();

	private static OperatorRenderer wrapperRenderer = new WrapperRenderer();

	private boolean resized = false;
	
	private transient Operator operator;
	
	public ProcessRenderer() {}

	public void setOperator(Operator operator) {
		this.operator = operator;
        this.resized = false;
		repaint();
	}
    
	public void paint(Graphics g) {
		if (operator == null) {
			return;
		}

		g.clearRect(0, 0, getWidth(), getHeight());

		Graphics2D g2d = (Graphics2D) g;

		Dimension size = getRenderer(operator).getSize(operator, g2d);
		Dimension dimension = new Dimension((int) size.getWidth() + 20, (int) size.getHeight() + 20);
		setPreferredSize(dimension);
		g2d.translate(getWidth() / 2.0d - size.getWidth() / 2.0d, 10);
		getRenderer(operator).drawOperator(operator, g2d);
        
        // necessary for activating scroll bars in the box viewer dialog
        if (!resized) {
            revalidate();
            repaint();
            resized = true;
        }
	}

	public void print(Graphics g) {
		if (operator == null) {
			return;
		}
		Graphics2D g2d = (Graphics2D) g;
		getRenderer(operator).drawOperator(operator, g2d);
	}

	public static OperatorRenderer getRenderer(Operator op) {
		if (op instanceof OperatorChain) {
			if ((op instanceof SimpleOperatorChain) || (op instanceof ProcessRootOperator))
				return chainRenderer;
			else
				return wrapperRenderer;
		} else
			return leafRenderer;
	}
}
