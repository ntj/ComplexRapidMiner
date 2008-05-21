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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.freehep.util.export.ExportDialog;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.plotter.charts.BarChart2DPlotter;
import com.rapidminer.gui.plotter.charts.BarChart3DPlotter;
import com.rapidminer.gui.plotter.charts.BubbleChartPlotter;
import com.rapidminer.gui.plotter.charts.DeviationChartPlotter;
import com.rapidminer.gui.plotter.charts.PieChart2DPlotter;
import com.rapidminer.gui.plotter.charts.PieChart3DPlotter;
import com.rapidminer.gui.plotter.charts.RingChartPlotter;
import com.rapidminer.gui.plotter.charts.SeriesChartPlotter;
import com.rapidminer.gui.plotter.conditions.PlotterCondition;
import com.rapidminer.gui.plotter.mathplot.BoxPlot2D;
import com.rapidminer.gui.plotter.mathplot.BoxPlot3D;
import com.rapidminer.gui.plotter.mathplot.ScatterPlot3D;
import com.rapidminer.gui.plotter.mathplot.ScatterPlot3DColor;
import com.rapidminer.gui.plotter.mathplot.SticksPlot2D;
import com.rapidminer.gui.plotter.mathplot.SticksPlot3D;
import com.rapidminer.gui.plotter.mathplot.SurfacePlot3D;
import com.rapidminer.gui.plotter.som.SOMPlotter;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.visualization.SOMModelPlotter;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Renderable;


/**
 * This is the main component for all data or statistics plotters. Depending on
 * the selected plotter type the options panel part is created or adapted. The
 * option panel usually contains selectors for up to three axis and other
 * options depending on the plotter like a plot amount slider or option buttons.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: PlotterPanel.java,v 1.15 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class PlotterPanel extends JPanel implements ItemListener, Runnable, Renderable, CoordinatesHandler {
    
	private static final long serialVersionUID = -8724351470349745191L;

    public static final int DEFAULT_MAX_NUMBER_OF_DATA_POINTS = 1000;
    	
    public final static LinkedHashMap<String,Class<? extends Plotter>> WEIGHT_PLOTTER_SELECTION = new LinkedHashMap<String,Class<? extends Plotter>>();
    
    public final static LinkedHashMap<String,Class<? extends Plotter>> DATA_SET_PLOTTER_SELECTION = new LinkedHashMap<String, Class<? extends Plotter>>();
    
    public final static LinkedHashMap<String,Class<? extends Plotter>> MODEL_PLOTTER_SELECTION = new LinkedHashMap<String, Class<? extends Plotter>>();
    
    static { 
        WEIGHT_PLOTTER_SELECTION.put("Lines", ScatterPlotter.class);
        WEIGHT_PLOTTER_SELECTION.put("Histogram", HistogramPlotter.class);
        WEIGHT_PLOTTER_SELECTION.put("Hinton", HintonDiagram.class);
        WEIGHT_PLOTTER_SELECTION.put("Bound", BoundDiagram.class);
        WEIGHT_PLOTTER_SELECTION.put("Pie", PieChart2DPlotter.class);
        WEIGHT_PLOTTER_SELECTION.put("Pie 3D", PieChart3DPlotter.class);
        WEIGHT_PLOTTER_SELECTION.put("Ring", RingChartPlotter.class);
        WEIGHT_PLOTTER_SELECTION.put("Bars", BarChart2DPlotter.class);
        WEIGHT_PLOTTER_SELECTION.put("Bars 3D", BarChart3DPlotter.class);
        

        DATA_SET_PLOTTER_SELECTION.put("Scatter", ScatterPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Scatter Matrix", ScatterMatrixPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Scatter 3D", ScatterPlot3D.class);
        DATA_SET_PLOTTER_SELECTION.put("Scatter 3D Color", ScatterPlot3DColor.class);
        DATA_SET_PLOTTER_SELECTION.put("Bubble", BubbleChartPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Parallel", ParallelPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Deviation", DeviationChartPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Series", SeriesChartPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Survey", SurveyPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("SOM", SOMPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Density", DensityPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Pie", PieChart2DPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Pie 3D", PieChart3DPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Ring", RingChartPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Bars", BarChart2DPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Bars 3D", BarChart3DPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Andrews Curves", AndrewsCurves.class);
        DATA_SET_PLOTTER_SELECTION.put("Distribution", DistributionPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Histogram", HistogramPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Histogram Color", ColorHistogramPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Histogram Matrix", HistogramMatrixPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Histogram Color Matrix", ColorHistogramMatrixPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Quartile", QuartilePlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Quartile Color", ColorQuartilePlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Quartile Color Matrix", ColorQuartileMatrixPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Sticks", SticksPlot2D.class);
        DATA_SET_PLOTTER_SELECTION.put("Sticks 3D", SticksPlot3D.class);
        DATA_SET_PLOTTER_SELECTION.put("Box", BoxPlot2D.class);
        DATA_SET_PLOTTER_SELECTION.put("Box 3D", BoxPlot3D.class);
        DATA_SET_PLOTTER_SELECTION.put("RadViz", RadVizPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("GridViz", GridVizPlotter.class);
        DATA_SET_PLOTTER_SELECTION.put("Surface 3D", SurfacePlot3D.class);
        

        MODEL_PLOTTER_SELECTION.put("SOM", SOMModelPlotter.class);
    }

	/** The line style rendered for the legend (or key). */
	private static class LineStyleCellRenderer extends JLabel implements ListCellRenderer {

		private static final long serialVersionUID = -7039142638209143602L;

		Plotter plotter;

		public LineStyleCellRenderer(Plotter plotter) {
			this.plotter = plotter;
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			String s = value.toString();
			setText(s);
			Icon icon = plotter.getIcon(index);
			if (icon != null)
				setIcon(icon);
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}
	}

	/** The currently selected plotter object. */
	private Plotter plotter = null;

	/** The current data table object which backs up the plotting. */
	private transient DataTable dataTable;

	/**
	 * The label which shows the coordinates in data space. Not supported by all
	 * plotters.
	 */
	private JLabel coordinatesLabel = new JLabel("                      ");

	/** The selected plotter type. */
	private String selectedPlotter = null;

    /** The main panel containing the axes selection panel and the actual plotter component. */
    private JPanel mainPanel = new JPanel(new BorderLayout());
    
    /** The plotter selection combo box. */
	private JComboBox plotterCombo = new JComboBox();
	
    /** Indicates if the plotter will be available. */
    private LinkedHashMap<String,Class<? extends Plotter>> availablePlotters = new LinkedHashMap<String,Class<? extends Plotter>>();

    /**
     * Creates a new plotter panel based on the given {@link DataTable} object.
     */
    public PlotterPanel(DataTable dataTable) {
        this(dataTable, DATA_SET_PLOTTER_SELECTION);
    }
    
	/**
	 * Creates a new plotter panel based on the given {@link DataTable} object.
	 */
	public PlotterPanel(DataTable dataTable, LinkedHashMap<String, Class<? extends Plotter>> availablePlotters) {
		super(new BorderLayout());
        
        // set available plotters and select first
        this.availablePlotters = availablePlotters;
        if ((this.availablePlotters == null) || (this.availablePlotters.size() == 0))
            throw new IllegalArgumentException("The list of available plotters must not be null or empty!");
        Iterator<String> i = availablePlotters.keySet().iterator();
        this.selectedPlotter = i.next();

        JScrollPane plotterScrollPane = new ExtendedJScrollPane(mainPanel);
        add(plotterScrollPane, BorderLayout.CENTER);
        
        setDataTable(dataTable);
	}

    public Plotter getSelectedPlotter() {
        return this.plotter;
    }
    
    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
        
        // check for missing values
        if (dataTable.containsMissingValues()) {
            LogService.getGlobal().log("Plotter: the given data contains missing values. Probably most plotters will not be able to produce proper visualizations. Please replace missing values beforehand if possible.", LogService.ERROR);
        }
        
        // perform sampling
        int maxRowNumber = DEFAULT_MAX_NUMBER_OF_DATA_POINTS;
        String maxRowNumberString = System.getProperty(MainFrame.PROPERTY_RAPIDMINER_GUI_PLOTTER_ROWS_MAXIMUM);
        if ((maxRowNumberString != null) && (maxRowNumberString.trim().length() > 0)) {
            try {
                int newMaxRows = Integer.parseInt(maxRowNumberString);
                maxRowNumber = newMaxRows;
            } catch (NumberFormatException e) {
                LogService.getGlobal().log("Plotter: cannot read maximum number of plotter points (was '" + maxRowNumberString + "').", LogService.WARNING);
            }
        }
        
        if (this.dataTable.getNumberOfRows() > maxRowNumber) {
            this.dataTable.sample(maxRowNumber);
            LogService.getGlobal().log("Cannot plot all data points, using only a sample of " + maxRowNumber + " rows.", LogService.WARNING);
        }

        update(true);
    }
    
	private void setSelectedPlotter(String name) {
		this.selectedPlotter = name;
		update(false);
	}
 
	private void update(boolean fillPlotterList) {
		int[] axis = null;
		List<Integer> valuesList = new LinkedList<Integer>();
		if (plotter != null) {
			axis = new int[plotter.getNumberOfAxes()];
			for (int i = 0; i < axis.length; i++)
				axis[i] = plotter.getAxis(i);
			for (int i = 0; i < dataTable.getNumberOfColumns(); i++)
				if (plotter.getPlotColumn(i))
					valuesList.add(i);
		}
		int[] selectedIndices = new int[valuesList.size()];
		int k = 0;
		Iterator v = valuesList.iterator();
		while (v.hasNext())
			selectedIndices[k++] = ((Integer) v.next()).intValue();

		try {
			Class plotterClass = availablePlotters.get(this.selectedPlotter);
			if (plotterClass != null) {
				this.plotter = availablePlotters.get(this.selectedPlotter).newInstance();
				this.plotter.setDataTable(dataTable);
			}
		} catch (Exception e) {
			SwingTools.showSimpleErrorMessage("Cannot instantiate plotter '" + this.selectedPlotter + "':", e);
			return;
		}
		mainPanel.removeAll();
        
		PlotterMouseHandler mouseHandler = new PlotterMouseHandler(plotter, this);  
		plotter.addMouseMotionListener(mouseHandler);
		plotter.addMouseListener(mouseHandler);

		JComponent plotterComponent = plotter.getPlotter();
		plotterComponent.setBorder(BorderFactory.createEtchedBorder());
		mainPanel.add(plotterComponent, BorderLayout.CENTER);
		
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(2, 2, 2, 2);

		
		// axis selection panel (main)
		JPanel axesSelectionPanel = new JPanel(gridBag);

		// plotter list
		String toolTip = "The plotter which should be used for displaying data.";
		JLabel label = new JLabel("Plotter");
		label.setToolTipText(toolTip);
		gridBag.setConstraints(label, c);
		axesSelectionPanel.add(label);
		
		if (fillPlotterList) {
			Thread thread = new Thread(this);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}
		
		gridBag.setConstraints(plotterCombo, c);
		axesSelectionPanel.add(plotterCombo);

		// axis selections
		for (int i = 0; i < plotter.getNumberOfAxes(); i++) {
			toolTip = "Select a column for " + plotter.getAxisName(i);
			label = new JLabel(plotter.getAxisName(i));
			label.setToolTipText(toolTip);
			gridBag.setConstraints(label, c);
			axesSelectionPanel.add(label);

			final JComboBox axisCombo = new JComboBox();
			axisCombo.setToolTipText(toolTip);
			axisCombo.addItem("None");
			for (int j = 0; j < dataTable.getNumberOfColumns(); j++) {
				axisCombo.addItem(dataTable.getColumnName(j));
			}
			final int index = i;
			axisCombo.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					plotter.setAxis(index, axisCombo.getSelectedIndex() - 1);
				}
			});
			if ((axis != null) && (axis.length > i))
				axisCombo.setSelectedIndex(Math.max(0, axis[i] + 1));
			gridBag.setConstraints(axisCombo, c);
			axesSelectionPanel.add(axisCombo);
			
			if (plotter.isSupportingLogScale(i)) {
				final JCheckBox logScaleBox = new JCheckBox("Log Scale", false);
				logScaleBox.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						plotter.setLogScale(index, logScaleBox.isSelected());
					}
				});
				gridBag.setConstraints(logScaleBox, c);
				axesSelectionPanel.add(logScaleBox);
			}
		}
		
		// plots (colors, values, etc.)
		if (plotter.getValuePlotSelectionType() != Plotter.NO_SELECTION) {
			JLabel plotLabel;
			if (plotter.getPlotName() == null) {
				plotLabel = new JLabel("Plots");
				toolTip = "Select the column which should be plotted.";
			} else {
				plotLabel = new JLabel(plotter.getPlotName());
				toolTip = "Select a column for " + plotter.getPlotName();
			}
			plotLabel.setToolTipText(toolTip);
			gridBag.setConstraints(plotLabel, c);
			axesSelectionPanel.add(plotLabel);
		}
		switch (plotter.getValuePlotSelectionType()) {
			case Plotter.MULTIPLE_SELECTION:
				final JList plotList = new JList(dataTable.getColumnNames());
				plotList.setToolTipText(toolTip);
				plotList.setBorder(BorderFactory.createLoweredBevelBorder());
				plotList.setCellRenderer(new LineStyleCellRenderer(plotter));
				plotList.addListSelectionListener(new ListSelectionListener() {

					public void valueChanged(ListSelectionEvent e) {
						for (int i = 0; i < plotList.getModel().getSize(); i++)
							plotter.setPlotColumn(i, plotList.isSelectedIndex(i));
					}
				});
				plotList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				plotList.setSelectedIndices(selectedIndices);
				JScrollPane listScrollPane = new ExtendedJScrollPane(plotList);
				c.weighty = 1.0;
				gridBag.setConstraints(listScrollPane, c);
				axesSelectionPanel.add(listScrollPane);
				c.weighty = 0.0;
				break;
			case Plotter.SINGLE_SELECTION:
				final JComboBox plotCombo = new JComboBox();
				plotCombo.setToolTipText(toolTip);
				plotCombo.addItem("None");
				for (int j = 0; j < dataTable.getNumberOfColumns(); j++) {
					plotCombo.addItem(dataTable.getColumnName(j));
				}
				plotCombo.addItemListener(new ItemListener() {

					public void itemStateChanged(ItemEvent e) {
						plotter.setPlotColumn(plotCombo.getSelectedIndex() - 1, true);
					}
				});
				if (selectedIndices.length > 0)
					plotCombo.setSelectedIndex(selectedIndices[0] + 1);
				gridBag.setConstraints(plotCombo, c);
				axesSelectionPanel.add(plotCombo);
				break;
			case Plotter.NO_SELECTION:
				// do nothing
				break;
		}
        
		// zooming
		if (plotter.canHandleZooming()) {
			label = new JLabel("Zooming");
			toolTip = "Set a new zooming factor.";
			label.setToolTipText(toolTip);
			gridBag.setConstraints(label, c);
			axesSelectionPanel.add(label);
			final JSlider zoomingSlider = new JSlider(1, 100, plotter.getInitialZoomFactor());
			zoomingSlider.setToolTipText(toolTip);
			gridBag.setConstraints(zoomingSlider, c);
			axesSelectionPanel.add(zoomingSlider);
			zoomingSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					plotter.setZooming(zoomingSlider.getValue());
				}
			});
		}
		
		// jitter
		if (plotter.canHandleJitter()) {
			label = new JLabel("Jitter");
			toolTip = "Select the amount of jittering (small perturbation of data points).";
			label.setToolTipText(toolTip);
			gridBag.setConstraints(label, c);
			axesSelectionPanel.add(label);
			final JSlider jitterSlider = new JSlider(0, 100, 0);
			jitterSlider.setToolTipText(toolTip);
			gridBag.setConstraints(jitterSlider, c);
			axesSelectionPanel.add(jitterSlider);
			jitterSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
                    //if (!jitterSlider.getValueIsAdjusting())
					plotter.setJitter(jitterSlider.getValue());
				}
			});
		}
		
		// option dialog
		if (plotter.hasOptionsDialog()) {
			toolTip = "Opens a dialog with further options for this plotter.";
			JButton optionsButton = new JButton("Options");
			optionsButton.setToolTipText(toolTip);
			gridBag.setConstraints(optionsButton, c);
			axesSelectionPanel.add(optionsButton);
			optionsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					plotter.showOptionsDialog();
				}
			});
		}

		// Add the plotter options components for user interaction, if provided
		int componentCounter = 0;
		while (plotter.getOptionsComponent(componentCounter) != null) {
			Component options = plotter.getOptionsComponent(componentCounter);			
			gridBag.setConstraints(options, c);
			axesSelectionPanel.add(options);
			componentCounter++;
		}
		
		// Save image button for the plotter
		if (!plotter.hasSaveImageButton()) {
			toolTip = "Saves an image of the current plot.";
			JButton imageButton = new JButton("Save Image...");
			imageButton.setToolTipText(toolTip);
			imageButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Component tosave = plotter.getPlotter();
					ExportDialog exportDialog = new ExportDialog("RapidMiner");
					exportDialog.showExportDialog(getPanel(), "Save Image...", tosave, "plot");
				}
			});
			gridBag.setConstraints(imageButton, c);
			axesSelectionPanel.add(imageButton);
		}
		
		// check if savable (for data)
		if (plotter.isSaveable()) {
			toolTip = "Saves the data underlying this plot into a file.";
			JButton saveButton = new JButton("Save...");
			saveButton.setToolTipText(toolTip);
			saveButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					plotter.save();
				}
			});
			gridBag.setConstraints(saveButton, c);
			axesSelectionPanel.add(saveButton);
		}

		// coordinates
		if (plotter.isProvidingCoordinates()) {
			coordinatesLabel = new JLabel("                      ");
			toolTip = "The current coordinates of the mouese cursor with respect to the data dimensions.";
			coordinatesLabel.setToolTipText(toolTip);
			coordinatesLabel.setBorder(BorderFactory.createEtchedBorder());
			coordinatesLabel.setFont(new Font("Monospaced", Font.PLAIN, coordinatesLabel.getFont().getSize()));
			gridBag.setConstraints(coordinatesLabel, c);
			axesSelectionPanel.add(coordinatesLabel);
		}
		
		// add fill component if necessary (glue)
		if (plotter.getValuePlotSelectionType() != Plotter.MULTIPLE_SELECTION) {
			c.weighty = 1.0;
			JPanel fillPanel = new JPanel();
			gridBag.setConstraints(fillPanel, c);
			axesSelectionPanel.add(fillPanel);
			c.weighty = 0.0;
		}

		axesSelectionPanel.setAlignmentX(LEFT_ALIGNMENT);
		mainPanel.add(axesSelectionPanel, BorderLayout.WEST);

		revalidate();
		repaint();
	}

	private Component getPanel() {
		return this;
	}

    public void itemStateChanged(ItemEvent e) {
        setSelectedPlotter((String)plotterCombo.getSelectedItem());
    }
    
	public void run() {
		synchronized (plotterCombo) {
            plotterCombo.removeItemListener(this);
			plotterCombo.removeAllItems();
			Iterator<String> n = availablePlotters.keySet().iterator();
			while (n.hasNext()) {
				String plotterName = n.next();
				try {
					Class<? extends Plotter> plotterClass = availablePlotters.get(plotterName);
					if (plotterClass != null) {
						Plotter plotter = plotterClass.newInstance();
						PlotterCondition condition = plotter.getPlotterCondition();
						if (condition.acceptDataTable(this.dataTable)) {
							plotterCombo.addItem(plotterName);
						} else {
							LogService.getGlobal().log("Cannot use plotter '" + plotterName + "': " + condition.getRejectionReason(this.dataTable), LogService.NOTE);
						}
					}
				} catch (InstantiationException e) {
                    LogService.getGlobal().log("Plotter panel: cannot instantiate plotter '" + plotterName + "'. Skipping...", LogService.WARNING);
				} catch (IllegalAccessException e) {
                    LogService.getGlobal().log("Plotter panel: cannot acess plotter '" + plotterName + "'. Skipping...", LogService.WARNING);
				}
			}
	        plotterCombo.setToolTipText("The plotter which should be used for displaying data.");
			plotterCombo.addItemListener(this);
		}
	}

	public int getRenderHeight(int preferredHeight) {
		return plotter.getRenderHeight(preferredHeight);
	}

	public int getRenderWidth(int preferredWidth) {
		return plotter.getRenderWidth(preferredWidth);
	}

	public void render(Graphics graphics, int width, int height) {
		plotter.render(graphics, width, height);
	}

	public void updateCoordinates(String coordinateInfo) {
		this.coordinatesLabel.setText(coordinateInfo);
	}
}
