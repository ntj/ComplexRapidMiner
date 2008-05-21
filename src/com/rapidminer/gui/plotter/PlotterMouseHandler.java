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
package com.rapidminer.gui.plotter;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;

import com.rapidminer.ObjectVisualizer;
import com.rapidminer.tools.ObjectVisualizerService;

/**
 * The mouse handler for plotters. This might be useful in cases where mouse handling
 * is necessary without a plotter panel.
 * 
 * @author Ingo Mierswa
 * @version $Id: PlotterMouseHandler.java,v 1.3 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class PlotterMouseHandler implements MouseListener, MouseMotionListener {

	private Plotter plotter;
	
	private CoordinatesHandler coordinatesHandler;
	
	/** The point at which a mouse pressing started. */
	private Point pressStart = null;
	
	public PlotterMouseHandler(Plotter plotter, CoordinatesHandler coordinatesHandler) {
		this.plotter = plotter;
		this.coordinatesHandler = coordinatesHandler;
	}
	
	public void mouseMoved(MouseEvent e) {
		Point2D p = plotter.getPositionInDataSpace(e.getPoint());
		if (coordinatesHandler != null) {
			if (p != null) {
				DecimalFormat format = new DecimalFormat(" 0.000E0;-0.000E0");
				coordinatesHandler.updateCoordinates(format.format(p.getX()) + " , " + format.format(p.getY()));
			}
		}
		plotter.setMousePosInDataSpace(e.getX(), e.getY());
	}

	public void mouseDragged(MouseEvent e) {
		if ((pressStart != null) && (Math.abs(e.getX() - pressStart.getX()) > 5) && (Math.abs(e.getY() - pressStart.getY()) > 5)) {
			plotter.setDragBounds((int) Math.min(pressStart.getX(), e.getX()), (int) Math.min(pressStart.getY(), e.getY()), (int) Math.abs(pressStart.getX() - e.getX()), (int) Math.abs(pressStart.getY() - e.getY()));
		} else {
			plotter.setDragBounds(-1, -1, -1, -1);
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (e.getClickCount() > 1) {
				String id = plotter.getIdForPos(e.getX(), e.getY());
				if (id != null) {
					ObjectVisualizer visualizer = ObjectVisualizerService.getVisualizerForObject(id);
					visualizer.startVisualization(id);
				}
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			plotter.setDrawRange(-1, -1, -1, -1);
			pressStart = null;
			plotter.setDragBounds(-1, -1, -1, -1);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (pressStart != null) {
				if ((Math.abs(e.getX() - pressStart.getX()) > 5) && (Math.abs(e.getY() - pressStart.getY()) > 5)) {
					Point2D pressPoint = plotter.getPositionInDataSpace(pressStart);
					Point2D releasePoint = plotter.getPositionInDataSpace(e.getPoint());
					if (pressPoint != null) {
						plotter.setDrawRange(Math.min(pressPoint.getX(), releasePoint.getX()), 
								             Math.max(pressPoint.getX(), releasePoint.getX()), 
								             Math.min(pressPoint.getY(), releasePoint.getY()), 
								             Math.max(pressPoint.getY(), releasePoint.getY()));
					}
				}
			}
		}
		plotter.setDragBounds(-1, -1, -1, -1);
		pressStart = null;
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			pressStart = e.getPoint();
		}
	}

	public void mouseExited(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
}
