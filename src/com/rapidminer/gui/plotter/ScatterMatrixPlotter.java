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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
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
 * @version $Id: ScatterMatrixPlotter.java,v 1.8 2008/07/19 16:31:17 ingomierswa Exp $
 */
public class ScatterMatrixPlotter extends PlotterAdapter {
    
	private static final long serialVersionUID = 9049081889010883621L;

	static final int MAX_NUMBER_OF_COLUMNS = 50;
     
	private ScatterPlotter plotter = new ScatterPlotter();

	private BufferedImage[][] images = new BufferedImage[0][0];
		
	private int plotDimension = -1;

	private transient DataTable dataTable;

	private int plotterSize;

	private JProgressBar progressBar = new JProgressBar();
	
	private Thread calculationThread = null;
	
	private boolean stopUpdates = false;
	
	
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
        
        plotter.setDrawLegend(false);
		plotter.setDrawAxes(false);
		plotter.getPlotter().setSize(new Dimension(plotterSize, plotterSize));
		
        progressBar.setToolTipText("Shows the progress of the Scatter Matrix calculation.");
	}

	public ScatterMatrixPlotter(DataTable dataTable) {
		this();
		setDataTable(dataTable);
	}

	public void forcePlotGeneration() {
		updatePlotters();
	}
	
	/** Indicates if the plotter is currently under a process of value adjustments. Might give 
	 *  implementing plotters a hint that graphical updates should not be performed until all
	 *  settings are made. */
	public void stopUpdates(boolean value) {
		this.stopUpdates = value;
	}
	
	public void setDataTable(DataTable dataTable) {
		super.setDataTable(dataTable);
		this.dataTable = dataTable;
		if (!stopUpdates)
			updatePlottersInThread();
	}

    public PlotterCondition getPlotterCondition() {
        return new ColumnsPlotterCondition(MAX_NUMBER_OF_COLUMNS);
    }
    
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		if (calculationThread == null) {
			for (int x = 0; x < images.length; x++) {
				for (int y = 0; y < images[x].length; y++) {
					Graphics2D newSpace = (Graphics2D) graphics.create();
					newSpace.translate(x * plotterSize, y * plotterSize + MARGIN);
					newSpace.drawImage(images[x][y], null, 0, 0);
				}
			}
		}
		
		// key or legend
		if (plotDimension != -1) {
			drawLegend(graphics, dataTable, plotDimension);
		}
	}

	private synchronized void updatePlotters() {
		if (plotDimension >= 0) {
			images = new BufferedImage[dataTable.getNumberOfColumns()][dataTable.getNumberOfColumns()];
			int counter = 0;
			int firstIndex = 0;
			for (int x = 0; x < dataTable.getNumberOfColumns(); x++) {
				if (x != plotDimension) {
					int secondIndex = 0;
					for (int y = 0; y < dataTable.getNumberOfColumns(); y++) {
						if (y != plotDimension) {
							if (firstIndex == secondIndex) {
								images[firstIndex][secondIndex] = new BufferedImage(plotterSize, plotterSize, BufferedImage.TYPE_INT_ARGB);
								Graphics2D graphics = images[firstIndex][secondIndex].createGraphics();
								graphics.setColor(Color.WHITE);
								graphics.fillRect(0, 0, plotterSize, plotterSize);
							} else {
								plotter.setDataTable(dataTable);
								plotter.setAxis(ScatterPlotter.X_AXIS, x);
								plotter.setAxis(ScatterPlotter.Y_AXIS, y);
								plotter.setPlotColumn(plotDimension, true);

								images[firstIndex][secondIndex] = new BufferedImage(plotterSize, plotterSize, BufferedImage.TYPE_INT_ARGB);
								Graphics2D graphics = images[firstIndex][secondIndex].createGraphics();
								plotter.paint2DPlots(graphics);
							}
							secondIndex++;
						}
						progressBar.setValue(++counter);
					}
					firstIndex++;
				}
			}
			progressBar.setValue(0);
			revalidate();
			repaint();
		} else {
			images = new BufferedImage[0][0];
			revalidate();
			repaint();
		}
	}
	
	private void updatePlottersInThread() {
		if (plotDimension >= 0) {
			if (calculationThread == null) {
				progressBar.setMinimum(0);
				progressBar.setMaximum(images.length * images.length);
				progressBar.setValue(0);

				this.calculationThread = new Thread() {
					public void run() {
						updatePlotters();
						calculationFinished();
					}
				};
				this.calculationThread.start();
			}
		} else {
			images = new BufferedImage[0][0];
			revalidate();
			repaint();
		}
	}

	private void calculationFinished() {
		calculationThread = null;
	}
	
	public Dimension getPreferredSize() {
		if (images.length > 0)
			return new Dimension((images.length - 1) * plotterSize  + 2 * MARGIN, (images.length - 1) * plotterSize + 2 * MARGIN);
		else
			return new Dimension(2 * MARGIN, 2 * MARGIN);
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
	
	public boolean canHandleContinousJittering() { 
		return false; 
	}
	
	public void setJitter(int jitter) {
		this.plotter.setJitter(jitter);
		if (!stopUpdates)
			updatePlottersInThread();		
	}
	
	public void setPlotColumn(int index, boolean plot) {
		if (plot)
			this.plotDimension = index;
		else
			this.plotDimension = -1;
		if (!stopUpdates)
			updatePlottersInThread();
	}

	public boolean getPlotColumn(int index) {
		return this.plotDimension == index;
	}
	
	public JComponent getOptionsComponent(int index) {
		switch (index) {
			case 0:
				return progressBar;
			default:
				return null;
		}
	}

}
