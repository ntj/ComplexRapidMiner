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
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;
import com.rapidminer.tools.math.MathFunctions;


/** The distribution plotter can be used to plot distributions (histograms) of
 *  the selected dimension.
 * 
 * @author Ingo Mierswa
 * @version $Id: DistributionPlotter.java,v 2.12 2006/03/27 13:21:58 ingomierswa
 *          Exp $
 */
public class HistogramPlotter extends PlotterAdapter {

	private static final long serialVersionUID = 5447178172542465015L;

    public static final int MIN_BIN_NUMBER = 2;
    
    public static final int MAX_BIN_NUMBER = 100;
    
    public static final int DEFAULT_BIN_NUMBER = 40;
    
	private static Icon[] RECTANGLE_STYLE_ICONS;
	
	static {
		RECTANGLE_STYLE_ICONS = new RectangleStyleIcon[RectangleStyle.NUMBER_OF_STYLES];
		for (int i = 0; i < RECTANGLE_STYLE_ICONS.length; i++) {
			RECTANGLE_STYLE_ICONS[i] = new RectangleStyleIcon(i);
		}
	}

	/**
	 * Defines the icon which is plotted before the attribute in the selection
	 * list (legend or key).
	 */
	private static class RectangleStyleIcon implements Icon {

		private RectangleStyle style;

		private RectangleStyleIcon(int index) {
			this.style = new RectangleStyle(index);
		}

		public int getIconWidth() {
			return 16;
		}

		public int getIconHeight() {
			return 16;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			style.set((Graphics2D) g);
			g.fillRect(x, y, 16, 16);
			g.setColor(Color.black);
			g.drawRect(x, y, 16, 16);
		}
	}


	private static final Font SCALED_LABEL_FONT = LABEL_FONT.deriveFont(AffineTransform.getScaleInstance(1, -1));

	private static final int LABEL_MARGIN_X = 50;

	private static final int LABEL_MARGIN_Y = 15;

	protected transient DataTable dataTable;

	protected double minX, maxX, minY, maxY, xTicSize, yTicSize;

	/** Indicates which columns will be plotted. */
	private boolean[] columns = new boolean[0];
	
	protected Map<Integer,Bins> allPlots = new HashMap<Integer,Bins>();

	protected int binNumber = DEFAULT_BIN_NUMBER;

	protected boolean drawLegend = true;
	
	private String key = null;
	
	protected int currentXPlotterColumn = -1;
	
	
	public HistogramPlotter() {
		super();
		setBackground(Color.white);
	}

	public HistogramPlotter(DataTable dataTable) {
		this();
		setDataTable(dataTable);
	}

	public void setDataTable(DataTable dataTable) {
		super.setDataTable(dataTable);
		this.dataTable = dataTable;
		this.columns = new boolean[this.dataTable.getNumberOfColumns()];
		repaint();
	}

	public Bins getBins(int plotColumn) {
		return allPlots.get(plotColumn);
	}

	public Icon getIcon(int index) {
		return RECTANGLE_STYLE_ICONS[index % RECTANGLE_STYLE_ICONS.length];
	}

    public JComponent getOptionsComponent(int index) {
        if (index == 0) {
        	JLabel label = new JLabel("Number of bins:");
            label.setToolTipText("Set the number of bins which should be displayed.");
            return label;
        } else if (index == 1) {
            final JSlider binNumberSlider = new JSlider(MIN_BIN_NUMBER, MAX_BIN_NUMBER, DEFAULT_BIN_NUMBER);
            binNumberSlider.setMajorTickSpacing(MAX_BIN_NUMBER - MIN_BIN_NUMBER);
            binNumberSlider.setMinorTickSpacing((MAX_BIN_NUMBER - MIN_BIN_NUMBER) / 10);
            binNumberSlider.setPaintTicks(true);
            binNumberSlider.setPaintLabels(true);
            binNumberSlider.setToolTipText("Set the number of bins which should be displayed.");
            binNumberSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (!binNumberSlider.getValueIsAdjusting())
                        setBinNumber(binNumberSlider.getValue());
                }
            });
            return binNumberSlider;
        } else {
            return null;
        }
    }
    
	/** Indicates how many bins should be used for the distribution plot. */
	public void setBinNumber(int binNumber) {
		this.binNumber = binNumber;
		repaint();
	}
	
	public void setPlotColumn(int index, boolean plot) {
		columns[index] = plot;
		repaint();
	}

	public boolean getPlotColumn(int index) {
		return columns[index];
	}
	
	public int getValuePlotSelectionType() {
		return MULTIPLE_SELECTION;
	}
	
	protected int getNumberOfPlots() {
		int counter = 0;
		for (int i = 0; i < columns.length; i++) {
			if (columns[i])
				counter++;
		}
		return counter;
	}
	
	public void setDrawLegend(boolean drawLegend) {
		this.drawLegend = drawLegend;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public void prepareData() {		
		this.minX = Double.POSITIVE_INFINITY;
		this.maxX = Double.NEGATIVE_INFINITY;
		this.minY = Double.POSITIVE_INFINITY;
		this.maxY = Double.NEGATIVE_INFINITY;

		allPlots.clear();
		
		if (getNumberOfPlots() == 0) {
			return;
		}

		this.currentXPlotterColumn = -1;
		synchronized (dataTable) {
			// init
			double[] minX = new double[dataTable.getNumberOfColumns()];
			double[] maxX = new double[dataTable.getNumberOfColumns()];
			for (int d = 0; d < minX.length; d++) {
				minX[d] = Double.POSITIVE_INFINITY;
				maxX[d] = Double.NEGATIVE_INFINITY;
			}
			// calculate min and max for all plots
			Iterator<DataTableRow> i = dataTable.iterator();
			while (i.hasNext()) {
				DataTableRow row = i.next();
				for (int d = 0; d < row.getNumberOfValues(); d++) {
					if (getPlotColumn(d)) {
						currentXPlotterColumn = d;
						double value = row.getValue(d);
						if (!Double.isNaN(value)) {
							minX[d] = MathFunctions.robustMin(value, minX[d]);
							this.minX = MathFunctions.robustMin(value, this.minX);
							maxX[d] = MathFunctions.robustMax(value, maxX[d]);
							this.maxX = MathFunctions.robustMax(value, this.maxX);
						}
					}
				}
			}
			// create bins and add to bin map
			for (int d = 0; d < minX.length; d++) {
				if (getPlotColumn(d)) {
					Bins bins = new Bins(d, minX[d], maxX[d], this.binNumber);
					allPlots.put(d, bins);
				}
			}
			// fill with data points
			i = dataTable.iterator();
			while (i.hasNext()) {
				DataTableRow row = i.next();
				for (int d = 0; d < row.getNumberOfValues(); d++) {
					if (getPlotColumn(d)) {
						Bins bins = allPlots.get(d); 
						bins.addPoint(row.getValue(d));
						this.maxY = Math.max(bins.getMaxCounter(), this.maxY);
					}
				}
			}
		}	
		this.minY = 0;
		
		if (dataTable.getNumberOfRows() == 0) {
			minX = 0;
			maxX = 1;
			minY = 0;
			maxY = 1;
		}

		if (minX == maxX) {
			minX -= 0.5;
			maxX += 0.5;
		}
		if (minY == maxY) {
			minY -= 0.5;
			maxY += 0.5;
		}
		
		xTicSize = getTicSize(dataTable, currentXPlotterColumn, minX, maxX);
		yTicSize = getNumericalTicSize(minY, maxY);
		minX = Math.floor(minX / xTicSize) * xTicSize;
		maxX = Math.ceil(maxX / xTicSize) * xTicSize;
		minY = Math.floor(minY / yTicSize) * yTicSize;
		maxY = Math.ceil(maxY / yTicSize) * yTicSize;
	}

	private void drawBins(Graphics2D g, double dx, double dy, double sx, double sy) {
		if (allPlots.size() == 0)
			return;

		Iterator<Bins> b = allPlots.values().iterator();
		int offset = 0;
		while (b.hasNext()) {
			Bins bins = b.next();
			if (bins.getMaxCounter() > 0) {
				Iterator i = bins.getIterator();
				while (i.hasNext()) {
					Bin bin = (Bin) i.next();
					Rectangle2D.Double rectangle = new Rectangle2D.Double((bin.getLeft() + dx) * sx + offset, dy * sy, (bin.getRight() - bin.getLeft()) * sx, bin.getCounter() * sy);
					bins.getRectangleStyle().set(g);
					g.fill(rectangle);
					g.setColor(Color.black);
					g.draw(rectangle);
				}
			}
			offset += 2;
		}
	}
	
	private void drawGrid(Graphics2D g, double dx, double dy, double sx, double sy) {
		DecimalFormat format = new DecimalFormat("0.00E0");
		g.setFont(SCALED_LABEL_FONT);
		int numberOfXTics = (int)Math.ceil((maxX - minX) / xTicSize) + 1;
		for (int i = 0; i < numberOfXTics; i++) {
			drawVerticalTic(g, i, format, dx, dy, sx, sy);
		}
		
		int numberOfYTics = (int)Math.ceil((maxY - minY) / yTicSize) + 1;
		for (int i = 0; i < numberOfYTics; i++) {
			drawHorizontalTic(g, i, format, dx, dy, sx, sy);
		}
	}

	private void drawVerticalTic(Graphics2D g, int ticNumber, DecimalFormat format, double dx, double dy, double sx, double sy) {
		double x = ticNumber * xTicSize + minX;
		g.setColor(GRID_COLOR);
		g.draw(new Line2D.Double((x + dx) * sx, (minY + dy) * sy, (x + dx) * sx, (maxY + dy) * sy));
		g.setColor(Color.black);
		String label = null;
		if ((getNumberOfPlots(dataTable) == 1) && (dataTable.isNominal(currentXPlotterColumn))) {
			int index = (int)Math.round(x);
			if ((index >= 0) && (index < dataTable.getNumberOfValues(currentXPlotterColumn)))
				label = dataTable.mapIndex(currentXPlotterColumn, index);
		} else {
			label = format.format(x);	
		}
		if (label != null) {
			Rectangle2D stringBounds = SCALED_LABEL_FONT.getStringBounds(label, g.getFontRenderContext());
			g.drawString(label, (float) ((x + dx) * sx - stringBounds.getWidth() / 2), (float) ((minY + dy) * sy + stringBounds.getHeight()));
		}
	}

	private void drawHorizontalTic(Graphics2D g, int ticNumber, DecimalFormat format, double dx, double dy, double sx, double sy) {
		double y = ticNumber * yTicSize + minY;
		g.setColor(GRID_COLOR);
		g.draw(new Line2D.Double((minX + dx) * sx, (y + dy) * sy, (maxX + dx) * sx, (y + dy) * sy));
		g.setColor(Color.black);
		String label = format.format(y) + " ";	
		Rectangle2D stringBounds = SCALED_LABEL_FONT.getStringBounds(label, g.getFontRenderContext());
		g.drawString(label, (float) ((minX + dx) * sx - stringBounds.getWidth()), (float) ((y + dy) * sy - stringBounds.getHeight() / 2 - stringBounds.getY()));
	}
	
	private void drawBins(Graphics2D g, int pixWidth, int pixHeight) {
		double sx = 0.0d;
		double sy = 0.0d;
		sx = ((double) pixWidth - LABEL_MARGIN_X) / (maxX - minX);
		sy = ((double) pixHeight - LABEL_MARGIN_Y) / (maxY - minY);

		Graphics2D coordinateSpace = (Graphics2D) g.create();
        if (drawLegend)
            coordinateSpace.translate(LABEL_MARGIN_X, LABEL_MARGIN_Y);
        else
            coordinateSpace.translate(LABEL_MARGIN_X / 2.0d, 3);

		if (Double.isNaN(sx) || Double.isNaN(sy)) {
			coordinateSpace.scale(1, -1);
			coordinateSpace.drawString("No data points available (yet).", 0, -20);
			coordinateSpace.drawString("Zooming out with a right click might help.", 0, 0);
		} else {
			if (drawLegend)
				drawGrid(coordinateSpace, -minX, -minY, sx, sy);
			drawBins(coordinateSpace, -minX, -minY, sx, sy);
		}
		coordinateSpace.dispose();
	}

	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
        int pixWidth = getWidth() - 2 * MARGIN;
        int pixHeight = getHeight() - 2 * MARGIN;
        Graphics2D translated = (Graphics2D) graphics.create();
        translated.translate(MARGIN, MARGIN);
		paintHistogram(translated, pixWidth, pixHeight);
	}

	public void paintHistogram(Graphics graphics, int pixWidth, int pixHeight) {
		Graphics2D g = (Graphics2D) graphics;
		
		if (key != null) {
			Rectangle2D stringBounds = LABEL_FONT.getStringBounds(key, g.getFontRenderContext());
			int xPos = (int)(pixWidth / 2.0d - stringBounds.getWidth() / 2.0d);
			int yPos = 16;
            g.setColor(Color.black);
			g.drawString(key, xPos, yPos);
		}
		
        Graphics2D scaled = (Graphics2D) g.create();
		scaled.translate(0, pixHeight + 1);

		prepareData();
		
		if (allPlots.size() == 0) {
			scaled.drawString("No plots selected.", 20, -20);
		} else {
			scaled.scale(1, -1);
			g.setColor(Color.black);
			drawBins(scaled, pixWidth, pixHeight);
		}
		scaled.dispose();
	}
}
