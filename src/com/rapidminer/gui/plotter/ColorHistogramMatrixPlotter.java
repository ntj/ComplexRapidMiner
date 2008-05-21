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

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.plotter.conditions.ColumnsPlotterCondition;
import com.rapidminer.gui.plotter.conditions.PlotterCondition;
import com.rapidminer.tools.LogService;


/**
 * A color histogram matrix plotter which uses the {@link ColorHistogramPlotter} for each of the plots.
 * 
 * @author Ingo Mierswa
 * @version $Id: ColorHistogramMatrixPlotter.java,v 1.4 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class ColorHistogramMatrixPlotter extends PlotterAdapter {

	private static final long serialVersionUID = 9049081889010883621L;

    static final int MAX_NUMBER_OF_COLUMNS = 100;
     
    static final int DEFAULT_BIN_NUMBER = 15;
    
	private ColorHistogramPlotter[] plotters = new ColorHistogramPlotter[0];

	private int plotterSize;

	private int colorIndex = -1;
	
    private double maxWeight = Double.NaN;
    
	private transient DataTable dataTable;
	
	
	public ColorHistogramMatrixPlotter() {
		setBackground(Color.white);
		String sizeProperty = System.getProperty(MainFrame.PROPERTY_RAPIDMINER_GUI_PLOTTER_MATRIXPLOT_SIZE);
        this.plotterSize = 200;
        try {
        	if (sizeProperty != null)
        		this.plotterSize = Integer.parseInt(sizeProperty);
        } catch (NumberFormatException e) {
            LogService.getGlobal().log("Color histogram matrix: cannot parse plotter size (was '" + sizeProperty + "'), using default (200).", LogService.WARNING);
        }
	}

	public ColorHistogramMatrixPlotter(DataTable dataTable) {
		this();
		setDataTable(dataTable);
	}

	public void setDataTable(DataTable dataTable) {
		super.setDataTable(dataTable);
		this.dataTable = dataTable;
		updateMatrix();
	}

    public PlotterCondition getPlotterCondition() {
        return new ColumnsPlotterCondition(MAX_NUMBER_OF_COLUMNS);
    }
    
	private void updateMatrix() {
		int numberOfPlotters = dataTable.getNumberOfColumns();
		if (colorIndex != -1)
			numberOfPlotters--;
		plotters = new ColorHistogramPlotter[numberOfPlotters];
		int x = 0;
		synchronized (dataTable) {
			for (int i = 0; i < dataTable.getNumberOfColumns(); i++) {
				if (i == colorIndex)
					continue;
				plotters[x] = new ColorHistogramPlotter(dataTable);
				plotters[x].getPlotter().setSize(new Dimension(plotterSize, plotterSize));
				plotters[x].setAxis(0, i);
				plotters[x].setPlotColumn(colorIndex, true);
				plotters[x].setDrawLegend(false);
				plotters[x].setKey(dataTable.getColumnName(i));
				x++;
			}
            
			this.maxWeight = getMaxWeight(dataTable);
		}
        setBinNumber(DEFAULT_BIN_NUMBER);
		repaint();		
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(getMaxNumberPerRow() * plotterSize + 2 * MARGIN, getMaxNumberPerRow() * plotterSize + 2 * MARGIN);
	}
	
	private int getMaxNumberPerRow() {
		return (int)Math.ceil(Math.sqrt(plotters.length));
	}
	
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		int xPos = 0;
		int yPos = 0;
		for (int x = 0; x < plotters.length; x++) {
			if (xPos >= getMaxNumberPerRow()) {
				yPos++;
				xPos = 0;
			}
			Graphics2D newSpace = (Graphics2D) graphics.create();
			newSpace.translate(MARGIN + xPos * plotterSize, MARGIN + yPos * plotterSize);
			drawWeightRectangle(newSpace, this.dataTable, x, this.maxWeight, plotterSize);
			newSpace.translate(WEIGHT_BORDER_WIDTH + 2, WEIGHT_BORDER_WIDTH + 2);
			plotters[x].paintHistogram(newSpace, plotterSize - (WEIGHT_BORDER_WIDTH + 8), plotterSize - (WEIGHT_BORDER_WIDTH + 8));
			newSpace.dispose();
			xPos++;
		}
		if (colorIndex != -1)
			drawLegend(graphics, dataTable, colorIndex, 0, RectangleStyle.ALPHA);
	}

	public String getPlotName() {
		return "Color";
	}
	
	public void setPlotColumn(int index, boolean plot) {
		colorIndex = index;
		updateMatrix();
	}

	public boolean getPlotColumn(int index) {
		return index == colorIndex;
	}
	
	public Icon getIcon(int index) {
		return null;
	}

    public JComponent getOptionsComponent(int index) {
        if (index == 0) {
            return new JLabel("Number of bins:");
        } else if (index == 1) {
            final JSlider binNumberSlider = new JSlider(HistogramPlotter.MIN_BIN_NUMBER, HistogramPlotter.MAX_BIN_NUMBER, DEFAULT_BIN_NUMBER);
            binNumberSlider.setMajorTickSpacing(HistogramPlotter.MAX_BIN_NUMBER - HistogramPlotter.MIN_BIN_NUMBER);
            binNumberSlider.setMinorTickSpacing((HistogramPlotter.MAX_BIN_NUMBER - HistogramPlotter.MIN_BIN_NUMBER) / 10);
            binNumberSlider.setPaintTicks(true);
            binNumberSlider.setPaintLabels(true);
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
    
	public void setBinNumber(int size) {
		for (int x = 0; x < plotters.length; x++)
			plotters[x].setBinNumber(size);
		repaint();
	}
}
