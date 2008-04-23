/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.learner;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.rapidminer.gui.plotter.PlotterAdapter;
import com.rapidminer.operator.learner.bayes.DistributionModel;


/**
 * This plotter can be used in order to plot a distribution model
 * like the one which can be delivered by NaiveBayes.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: DistributionPlotter.java,v 1.1 2007/05/27 22:03:29 ingomierswa Exp $
 */
public class DistributionPlotter extends PlotterAdapter {

	private static final long serialVersionUID = 2923008541302883925L;

	private transient DistributionModel model;

	public DistributionPlotter(DistributionModel model) {
		this.model = model;
	}

	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		int pixWidth = getWidth() - 2 * MARGIN;
		int pixHeight = getHeight() - 2 * MARGIN;
		Graphics2D translated = (Graphics2D) graphics.create();
		translated.translate(MARGIN, MARGIN);
		paintDistributions(translated, pixWidth, pixHeight);
	}

	private void paintDistributions(Graphics2D g, int pixWidth, int pixHeight) {
		int classIndex = model.getClasses().next();
		int attributeIndex = model.getAttributes(classIndex).next();
		g.setColor(Color.BLACK);
		g.draw(new Rectangle2D.Double(0, 0, 300, 100));
		paintDistribution(g, model.getDistribution(classIndex, attributeIndex), Color.RED, 300, 100, 60, 110, 4);
	}

	/**
	 * Paints the given distribution with given width and height on coordinates (0,0).
	 * Caller has to ensure that (0,0) is right location.
	 * @param g is the Graphics2D object to paint in
	 * @param width defines the width of the graph
	 * @param height defines the height of the graph
	 * @param start defines the value, the distributiongraph starts with
	 * @param end defines the value where the distributiongraph ends
	 */
	private void paintDistribution(Graphics2D g, Distribution distribution, Color color, int width, int height, double start, double end,
			int zoomFactor) {
		double valueInterval = (end - start) / width;
		double currentValue = start;
		g.setColor(color);
		int lastX = 0;
		double currentProbability = distribution.getProbability(currentValue);
		int lastY = height - (int) (currentProbability * height);
		for (int x = 1; x < width; x++) {
			currentProbability = distribution.getProbability(currentValue);
			int y = height - (int) (currentProbability * (height * zoomFactor));
			g.drawLine(lastX, lastY, x, y);
			currentValue += valueInterval;
			lastX = x;
			lastY = y;
		}
	}
}
