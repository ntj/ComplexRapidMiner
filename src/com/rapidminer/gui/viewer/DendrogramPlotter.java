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
package com.rapidminer.gui.viewer;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;

import javax.swing.JPanel;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.learner.clustering.ClusterNode;
import com.rapidminer.operator.learner.clustering.HierarchicalClusterModel;

/**
 * Plots a dendrogram of a given cluster model. The nodes in the model must have different, non-NaN values for 
 * this operator to work.
 * 
 * @author Michael Wurst
 * @version $Id: DendrogramPlotter.java,v 1.3 2008/05/09 19:22:59 ingomierswa Exp $
 *
 */
public class DendrogramPlotter extends JPanel {

	private static final long serialVersionUID = 2892192060246909733L;

	private static final int MARGIN = 10;
	
	private HierarchicalClusterModel hcm;
	
	private int numObjects;
	private double maxWeight;
	private double minWeight;
	
	private int maxX;
	private int maxY;

	private int count;
	
	private Color color = SwingTools.DARKEST_BLUE;
	

	public DendrogramPlotter(HierarchicalClusterModel hcm) {
		this.hcm = hcm;
	
		numObjects = hcm.getRootNode().getNumberOfObjectsInSubtree();
		minWeight = Double.POSITIVE_INFINITY;
		maxWeight = Double.NEGATIVE_INFINITY;
		findMinMaxWeight(hcm.getRootNode());
	}

	
	private void findMinMaxWeight(ClusterNode cn) {
		if(maxWeight < cn.getWeight())
			maxWeight = cn.getWeight();
		
		if(minWeight > cn.getWeight())
			minWeight = cn.getWeight();
		
		for(Iterator<ClusterNode> it = cn.getSubNodes(); it.hasNext();)
			findMinMaxWeight(it.next());
	}
	
	private void drawLine(int x1, int y1, int x2, int y2, Graphics g) {
		g.setColor(color);
		g.drawLine(x1, y1, x2, y2);
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if((minWeight == maxWeight)||(Double.isNaN(minWeight))||(Double.isInfinite(minWeight))||
				(Double.isNaN(maxWeight))||(Double.isInfinite(maxWeight))) {
			g.drawString("Dendrogram not available for this cluster model. Use an agglomerative clusterer.", MARGIN, MARGIN + 15);
			return;
		}
				
		this.maxX = getWidth() - 2 * MARGIN;
		this.maxY = getHeight() - 2 * MARGIN;
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		Graphics translated = g.create();
		translated.translate(MARGIN, MARGIN);
		
		count = 0;
		maxWeight = maxWeight + maxWeight * maxWeight;
		
		paintRecursively(hcm.getRootNode(), hcm.getRootNode().getWeight(), translated);
	}

	private int weightToYPos(double weight) {
		return (int) Math.round(maxY * (((weight) - minWeight) / ((maxWeight - minWeight))));
	}
	
	private int countToXPos(int count) {
		return (int) Math.round((((double) count) /((double) numObjects)) * ((double) maxX));
	}
	
	private int paintRecursively(ClusterNode cn, double baseWeight, Graphics g) {
		int leftPos = -1;
		int rightPos = -1;

		for(Iterator<ClusterNode> it= cn.getSubNodes(); it.hasNext();) {
			
			ClusterNode subNode = it.next();
			
			if((subNode.getNumberOfSubNodes() > 0)||(subNode.getNumberOfObjects() > 1)) { 
				int currentPos = paintRecursively(subNode,  cn.getWeight(), g);		
				if(leftPos == -1)
					leftPos = currentPos;
				rightPos = currentPos;
			}
		}
	
		for(Iterator<ClusterNode> it= cn.getSubNodes(); it.hasNext();) {
			ClusterNode subNode = it.next();
			if((subNode.getNumberOfObjects() == 1)&&(subNode.getNumberOfSubNodes() == 0)) {
				drawLine(countToXPos(count), weightToYPos(cn.getWeight()), countToXPos(count), weightToYPos(maxWeight),g);
				int currentPos = countToXPos(count);	
				if(leftPos == -1)
					leftPos = currentPos;
				rightPos = currentPos;
				count++;
			}
		}
	
		for(Iterator<String> it = cn.getObjects(); it.hasNext();) {
			drawLine(countToXPos(count), weightToYPos(cn.getWeight()), countToXPos(count), weightToYPos(maxWeight),g);
			int currentPos = countToXPos(count);
			if(leftPos == -1)
				leftPos = currentPos;
			rightPos = currentPos;
			count++;
			it.next();
		}
		
	
		int middlePos = (rightPos + leftPos)/2;
		
		drawLine(middlePos, weightToYPos(baseWeight) ,middlePos, weightToYPos(cn.getWeight()), g);
		drawLine(leftPos, weightToYPos(cn.getWeight()) ,rightPos, weightToYPos(cn.getWeight()), g);
		
		return middlePos;
	}
}
