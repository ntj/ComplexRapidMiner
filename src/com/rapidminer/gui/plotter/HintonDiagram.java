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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.Icon;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.Tools;


/** Presents values by boxes more filled the higher the values are.
 * 
 *  @author Daniel Hakenjos, Ingo Mierswa
 *  @version $Id: HintonDiagram.java,v 1.3 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class HintonDiagram extends PlotterAdapter implements MouseListener {

	private static final long serialVersionUID = -1299407916734619185L;

	private double[] values;

	private double maxWeight;

	private String[] names;

	private int boxSize = 51;

	private int horizontalCount, verticalCount;

	private String currentToolTip;

	private double toolTipX, toolTipY;

    private int plotIndex = -1;
    
    private transient DataTable dataTable;
    
    public HintonDiagram() {
        super();
        setBackground(Color.white);
        addMouseListener(this);
    }
    
    public HintonDiagram(DataTable dataTable) {
        this();
        setDataTable(dataTable);
    }
    
    public void setDataTable(DataTable dataTable) {
        super.setDataTable(dataTable);
        this.dataTable = dataTable;
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
    
    public boolean canHandleZooming() {
        return true;
    }

    public Icon getIcon(int index) {
        return null;
    }

    public void setZooming(int amount) {
        if (amount % 2 == 0) {
            amount++;
        }
        boxSize = amount;
        repaint();
    }

    public int getInitialZoomFactor() {
        return boxSize;
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
    }
    
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
		int width = this.getWidth();
		int height = this.getHeight();

        prepareData();
        
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);

		// draw grid
		horizontalCount = (int) Math.floor(((double) (width + 1)) / ((double) (boxSize + 1)));
		verticalCount = (int) Math.floor(((double) (height + 1)) / ((double) (boxSize + 1)));

		if (horizontalCount * verticalCount < values.length) {
			while (horizontalCount * verticalCount < values.length) {
				verticalCount++;
			}
		} else if (horizontalCount * verticalCount > values.length) {
			while (horizontalCount * (verticalCount - 1) > values.length) {
				verticalCount--;
			}
		} else {
			// nothing to do here
		}
		this.setPreferredSize(new Dimension(horizontalCount * (boxSize + 1) + 1, verticalCount * (boxSize + 1) + 1));

		g.setColor(Color.BLACK);
		g.drawRect(0, 0, horizontalCount * (boxSize + 1), verticalCount * (boxSize + 1));

		for (int h = 1; h < horizontalCount; h++) {
			g.drawLine(boxSize * h + h, 0, boxSize * h + h, boxSize * verticalCount + verticalCount - 1);
		}
		for (int v = 1; v < verticalCount; v++) {
			g.drawLine(0, boxSize * v + v, boxSize * horizontalCount + horizontalCount - 1, boxSize * v + v);
		}

		int att = 0;
		int horiz = 1;
		int vert = 1;
		while (att < values.length) {
			if (values[att] < 0.0d) {
				g.setColor(SwingTools.LIGHT_BLUE);
			} else {
				g.setColor(SwingTools.LIGHT_YELLOW);
			}

			int breite = (int) (Math.abs(values[att]) / maxWeight * boxSize);
			int centerx = (horiz - 1) * (boxSize + 1) + (boxSize + 1) / 2;
			int centery = (vert - 1) * (boxSize + 1) + (boxSize + 1) / 2;
			g.fillRect(centerx - breite / 2, centery - breite / 2, breite, breite);

			horiz++;
			if (horiz > horizontalCount) {
				horiz = 1;
				vert++;
			}
			att++;
		}
		g.setColor(Color.WHITE);
		if (horiz <= horizontalCount) {
			g.fillRect((horiz == 1) ? 0 : (horiz - 1) * (boxSize + 1) + 1, (vert - 1) * (boxSize + 1) + 1, width, height);
		}
		vert++;
		if (vert <= verticalCount) {
			g.fillRect(0, (vert - 1) * (boxSize + 1) + 1, width, height);
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
	
	private String getAttributeName(int x, int y) {
		int horiz = x / (boxSize + 1) + ((x % (boxSize + 1) > 0) ? 1 : 0);
		horiz = Math.min(horiz, horizontalCount);

		int vert = y / (boxSize + 1) + ((y % (boxSize + 1) > 0) ? 1 : 0);
		vert = Math.min(vert, verticalCount);

		int index = (vert - 1) * horizontalCount + horiz;
		index = Math.min(index, values.length);
		index = Math.max(index, 0);

		return names[index - 1] + ": " + Tools.formatNumber(values[index - 1]);
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

