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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.Icon;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.Tools;


/**
 * A bound diagram used for displaying attribute weights.
 * 
 * @author Daniel Hakenjos, Ingo Mierswa
 * @version $Id: BoundDiagram.java,v 1.3 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class BoundDiagram extends PlotterAdapter implements MouseListener {

	private static final long serialVersionUID = 3155061651939372589L;

	private double[] values;

	private double maxWeight;

	private String[] names;

	private int radius;

	private String currentToolTip;

	private double toolTipX, toolTipY;

	private double[] angles;

	private double[] attributeVectorX, attributeVectorY;
    
    private int plotIndex = -1;
    
    private transient DataTable dataTable;
    
    
    public BoundDiagram() {
        super();
        setBackground(Color.white);
        addMouseListener(this);
    }
    
    public BoundDiagram(DataTable dataTable) {
        this();
        setDataTable(dataTable);
    }
    
    public void setDataTable(DataTable dataTable) {
        super.setDataTable(dataTable);
        this.dataTable = dataTable;
        repaint();
    }

	/** Calculates the angles. */
	public void calculateAngles() {
		float anglegesamt = 360.0f;
		float delta = anglegesamt / values.length;

		float angle = 0.0f;

		angles = new double[values.length];

		for (int i = 0; i < angles.length; i++) {
			angles[i] = angle;
			angle += delta;
		}
	}

	/** Calculate the attribute vectors. */
	public void calculateAttributeVectors() {
		attributeVectorX = new double[values.length];
		attributeVectorY = new double[values.length];

		double angle;
		double radius = 1.0f;

		int angleInt;

		double x = 0.0f, y = 0.0f;
		for (int dimindex = 0; dimindex < values.length; dimindex++) {
			angleInt = (int) angles[dimindex];
			angle = angles[dimindex];

			if (angleInt / 90 == 0) {
				x = sin(angle) * radius;
				y = sin(90.0f - angle) * radius;
			} else if (angleInt / 90 == 1) {
				angle = angle - 90.0f;
				x = sin(90.0f - angle) * radius;
				y = sin(angle) * radius;
				y = -y;
			} else if (angleInt / 90 == 2) {
				angle = angle - 180.0f;
				x = sin(angle) * radius;
				y = sin(90.0f - angle) * radius;
				x = -x;
				y = -y;
			} else if (angleInt / 90 == 3) {
				angle = angle - 270.0f;
				x = sin(90.0f - angle) * radius;
				y = sin(angle) * radius;
				x = -x;
			}

			attributeVectorX[dimindex] = x;
			attributeVectorY[dimindex] = y;
		}

	}

	/**
	 * Gets the sinus of the angle.
	 * 
	 * @param angle
	 */
	private double sin(double angle) {
		while (angle >= 180.0f) {
			angle -= 180.0;
		}
		double value = angle / 180.0f * Math.PI;
		return Math.sin(value);
	}

    private void prepareData() {
        if (plotIndex < 0) {
            this.values = new double[0];
            this.names  = new String[0];
        } else {
            int size = dataTable.getNumberOfRows();
            this.values = new double[size];
            this.names = new String[size];
            
            Iterator<DataTableRow> i = dataTable.iterator();
            int counter = 0;
            this.maxWeight = Double.NEGATIVE_INFINITY;
            while (i.hasNext()) {
                DataTableRow row = i.next();
                this.values[counter] = row.getValue(plotIndex);
                this.maxWeight = Math.max(maxWeight, Math.abs(this.values[counter]));
                String id = row.getId();
                if (id == null)
                    id = this.values[counter] + "";
                this.names[counter] = id;
                counter++;
            }
        }      
        calculateAngles();
        calculateAttributeVectors();   
    }
    
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
        
        prepareData();

		int width = this.getWidth();
		int height = this.getHeight();

		radius = Math.min(width, height);
		radius = (radius - 20) / 2;

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);

		int centerX = radius + 10;
		int centerY = radius + 10;

		g.setColor(Color.BLACK);
		double[] r = new double[] { 1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1 };
		for (int i = 0; i < r.length; i++) {
			g.drawOval((int) (centerX - radius * r[i]), (int) (centerY - radius * r[i]), (int) (radius * r[i] * 2), (int) (radius * r[i] * 2));
		}

		Polygon polygon;
		double ratio;
		int x, y;
		for (int att = 0; att < values.length; att++) {
			polygon = new Polygon();
			polygon.addPoint(centerX, centerY);

			ratio = Math.abs(values[att]) / maxWeight;
			x = centerX + ((int) (ratio * radius * attributeVectorX[att]));
			y = centerY - ((int) (ratio * radius * attributeVectorY[att]));
			polygon.addPoint(x, y);

			x = centerX + ((int) (ratio * radius * attributeVectorX[(att + 1) % values.length]));
			y = centerY - ((int) (ratio * radius * attributeVectorY[(att + 1) % values.length]));
			polygon.addPoint(x, y);

			polygon.addPoint(centerX, centerY);

			if (values[att] < 0.0d) {
				g.setColor(SwingTools.LIGHT_BLUE);
				g.fillPolygon(polygon);
				g.setColor(Color.DARK_GRAY);
				g.drawPolygon(polygon);
			} else {
				g.setColor(SwingTools.LIGHT_YELLOW);
				g.fillPolygon(polygon);
				g.setColor(Color.DARK_GRAY);
				g.drawPolygon(polygon);
			}

		}

		drawToolTip((Graphics2D) g);
	}

	private void drawToolTip(Graphics2D g) {
		if (currentToolTip != null) {
			g.setFont(LABEL_FONT);
			Rectangle2D stringBounds = LABEL_FONT.getStringBounds(currentToolTip, g.getFontRenderContext());
			g.setColor(TOOLTIP_COLOR);
			Rectangle2D bg = new Rectangle2D.Double(toolTipX - stringBounds.getWidth() / 2 - 4, toolTipY - stringBounds.getHeight() / 2 - 2, stringBounds.getWidth() + 5, Math.abs(stringBounds.getHeight()) + 3);
			g.fill(bg);
			g.setColor(Color.black);
			g.draw(bg);
			g.drawString(currentToolTip, (float) (toolTipX - stringBounds.getWidth() / 2) - 2, (float) (toolTipY + 3));
		}
	}

	private void setToolTip(String toolTip, double x, double y) {
		this.currentToolTip = toolTip;
		this.toolTipX = x;
		this.toolTipY = y;
		repaint();
	}
	
    public String getPlotName() {
        return "Plot";
    }
    
    public void setPlotColumn(int index, boolean plot) {
        plotIndex = index;
        repaint();
    }

    public boolean getPlotColumn(int index) {
        return index == plotIndex;
    }
    
	public Icon getIcon(int index) {
		return null;
	}

	private String getAttributeName(int x, int y) {
		// get the closest attribute vector
		double distanceX, distanceY, distance;
		double minDistance = Double.POSITIVE_INFINITY;
		double minDistance2 = Double.POSITIVE_INFINITY;
		int minIndex = 0, minIndex2 = 0;

		for (int att = 0; att < values.length; att++) {
			distanceX = radius + 10.0d + attributeVectorX[att] * radius - x;
			distanceY = radius + 10.0d - attributeVectorY[att] * radius - y;
			distanceX = distanceX * distanceX;
			distanceY = distanceY * distanceY;

			distance = Math.sqrt(distanceX + distanceY);

			if (distance < minDistance) {
				minIndex2 = minIndex;
				minIndex = att;
				minDistance2 = minDistance;
				minDistance = distance;
			} else if (distance < minDistance2) {
				minIndex2 = att;
				minDistance2 = distance;
			}
		}

		if ((minIndex2 + 1) % values.length == minIndex) {
			return names[minIndex2] + ": " + Tools.formatNumber(values[minIndex2]);
		}
		return names[minIndex] + ": " + Tools.formatNumber(values[minIndex]);
	}

	public void mouseClicked(MouseEvent event) {
		String name = getAttributeName(event.getX(), event.getY());
		setToolTip(name, event.getX(), event.getY());
	}

	public void mouseReleased(MouseEvent event) {
		currentToolTip = null;
	}

	public void mouseEntered(MouseEvent arg0) {}

	public void mouseExited(MouseEvent arg0) {}

	public void mousePressed(MouseEvent arg0) {}

	public void mouseDragged(MouseEvent arg0) {}

	public void mouseMoved(MouseEvent event) {}
}
