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
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.plotter.conditions.ColumnsPlotterCondition;
import com.rapidminer.gui.plotter.conditions.PlotterCondition;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.LogService;


/**
 * A scatter plot matrix which uses the {@link ScatterPlotter} for each of the plots.
 * 
 * @author Ingo Mierswa
 * @version $Id: ScatterMatrixPlotter.java,v 1.6 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class ScatterMatrixPlotter extends PlotterAdapter {
    
	private static final long serialVersionUID = 9049081889010883621L;

	static final int MAX_NUMBER_OF_COLUMNS = 11;
     
	private ScatterPlotter[][] plotters = new ScatterPlotter[0][0];

	private int plotDimension = -1;

	private transient DataTable dataTable;

	private int plotterSize;

	public ScatterMatrixPlotter() {
		setBackground(Color.white);
		String sizeProperty = System.getProperty(MainFrame.PROPERTY_RAPIDMINER_GUI_PLOTTER_MATRIXPLOT_SIZE);
        this.plotterSize = 200;
        try {
        	if (sizeProperty != null)
        		this.plotterSize = Integer.parseInt(sizeProperty);
        } catch (NumberFormatException e) {
            LogService.getGlobal().log("Scatter matrix: cannot parse plotter size (was '" + sizeProperty + "'), using default size (200).", LogService.WARNING);
        }
	}

	public ScatterMatrixPlotter(DataTable dataTable) {
		this();
		setDataTable(dataTable);
	}

	public void setDataTable(DataTable dataTable) {
		super.setDataTable(dataTable);
		this.dataTable = dataTable;
		//synchronized (this.dataTable) {
			plotters = new ScatterPlotter[dataTable.getNumberOfColumns() - 1][dataTable.getNumberOfColumns() - 1];
			for (int x = 0; x < plotters.length; x++) {
				for (int y = 0; y < plotters[x].length; y++) {
					plotters[x][y] = new ScatterPlotter(dataTable);
					plotters[x][y].setDrawLegend(false);
					plotters[x][y].setDrawAxes(false);
					plotters[x][y].getPlotter().setSize(new Dimension(plotterSize, plotterSize));
				}
			}
		//}
	}

    public PlotterCondition getPlotterCondition() {
        return new ColumnsPlotterCondition(MAX_NUMBER_OF_COLUMNS);
    }
    
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		for (int x = 0; x < plotters.length; x++) {
			for (int y = 0; y < plotters[x].length; y++) {
				Graphics2D newSpace = (Graphics2D) graphics.create();
				newSpace.translate(x * plotterSize, y * plotterSize + MARGIN);
				plotters[x][y].paint2DPlots(newSpace);
			}
		}
		
		// key or legend
		if (plotDimension != -1) {
			drawLegend(graphics, dataTable, plotDimension);
		}
	}

	private void updatePlotters() {
		if (plotDimension >= 0) {
			int firstIndex = 0;
			for (int x = 0; x < dataTable.getNumberOfColumns(); x++) {
				if (x != plotDimension) {
					int secondIndex = 0;
					for (int y = 0; y < dataTable.getNumberOfColumns(); y++) {
						if (y != plotDimension) {
							if (firstIndex == secondIndex) {
								plotters[firstIndex][secondIndex].setAxis(ScatterPlotter.X_AXIS, -1);
								plotters[firstIndex][secondIndex].setAxis(ScatterPlotter.Y_AXIS, -1);
								plotters[firstIndex][secondIndex].clearPlotColumns();
							} else {
								plotters[firstIndex][secondIndex].setAxis(ScatterPlotter.X_AXIS, x);
								plotters[firstIndex][secondIndex].setAxis(ScatterPlotter.Y_AXIS, y);
								plotters[firstIndex][secondIndex].clearPlotColumns();
								plotters[firstIndex][secondIndex].setPlotColumn(plotDimension, true);
							}
							secondIndex++;
						}
					}
					firstIndex++;
				}
			}
		} else {
			for (int x = 0; x < plotters.length; x++) {
				for (int y = 0; y < plotters[x].length; y++) {
					plotters[x][y].setAxis(ScatterPlotter.X_AXIS, -1);
					plotters[x][y].setAxis(ScatterPlotter.Y_AXIS, -1);
					plotters[x][y].clearPlotColumns();
				}
			}
		}
        repaint();
	}

	public Dimension getPreferredSize() {
		return new Dimension(plotters.length * plotterSize  + 2 * MARGIN, plotters.length * plotterSize + 2 * MARGIN);
	}

	public String getAxisName(int index) {
		return "none";
	}

	public Icon getIcon(int index) {
		return null;
	}
	
	public boolean isSaveable() {
		return true;
	}

	public void save() {
		JFileChooser chooser = SwingTools.createFileChooser(null, false, new FileFilter[0]);
		if (chooser.showSaveDialog(ScatterMatrixPlotter.this) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
            PrintWriter out = null;
			try {
				out = new PrintWriter(new FileWriter(file));
				dataTable.write(out);
				out.close();
			} catch (Exception ex) {
				SwingTools.showSimpleErrorMessage("Cannot write to file '" + file + "'", ex);
			} finally {
			    if (out != null)
                    out.close();
            }
		}
	}

	public boolean canHandleJitter() {
		return true;
	}
	
	public void setJitter(int jitter) {
		for (int x = 0; x < plotters.length; x++)
			for (int y = 0; y < plotters[x].length; y++)
				plotters[x][y].setJitter(jitter);
		updatePlotters();		
	}
	
	public void setPlotColumn(int index, boolean plot) {
		if (plot)
			this.plotDimension = index;
		updatePlotters();
	}

	public boolean getPlotColumn(int index) {
		return this.plotDimension == index;
	}
}
