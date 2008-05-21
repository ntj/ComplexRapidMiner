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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.gui.plotter.Plotter;
import com.rapidminer.gui.plotter.PlotterPanel;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.tools.Tableable;


/**
 * Can be used to display (parts of) the data by means of a JTable.
 * 
 * @author Ingo Mierswa
 * @version $Id: DataTableViewer.java,v 1.6 2008/05/09 19:23:01 ingomierswa Exp $
 */
public class DataTableViewer extends JPanel implements Tableable {
    
	public static final int TABLE_MODE = 0;
	public static final int PLOT_MODE  = 1;
	
    private static final long serialVersionUID = 6878549119308753961L;

    private JLabel generalInfo = new JLabel();
    
    private DataTableViewerTable dataTableViewerTable;
    
    private PlotterPanel plotterPanel;
	
    	
    public DataTableViewer(DataTable dataTable) {
    	this(dataTable, PlotterPanel.DATA_SET_PLOTTER_SELECTION, true, TABLE_MODE, false);
    }
    
    public DataTableViewer(DataTable dataTable, boolean showPlotter) {
    	this(dataTable, PlotterPanel.DATA_SET_PLOTTER_SELECTION, showPlotter, TABLE_MODE, false);
    }
    
    public DataTableViewer(DataTable dataTable, boolean showPlotter, int startMode) {
    	this(dataTable, PlotterPanel.DATA_SET_PLOTTER_SELECTION, showPlotter, startMode, false);
    }

    public DataTableViewer(DataTable dataTable, LinkedHashMap<String, Class<? extends Plotter>> availablePlotters) {
    	this(dataTable, availablePlotters, true, TABLE_MODE, false);
    }
    
    public DataTableViewer(DataTable dataTable, LinkedHashMap<String, Class<? extends Plotter>> availablePlotters, boolean showPlotter, int startMode, boolean autoResize) {
        super(new BorderLayout());
        
        // table view
        this.dataTableViewerTable = new DataTableViewerTable(autoResize);
        final JPanel tablePanel = new JPanel(new BorderLayout());
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(generalInfo);
        tablePanel.add(infoPanel, BorderLayout.NORTH);
        
        JScrollPane tableScrollPane = new ExtendedJScrollPane(dataTableViewerTable);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        add(tablePanel);
        
		// plotter panel
        if (showPlotter) {
        	this.plotterPanel = new PlotterPanel(dataTable, availablePlotters);

        	// toggle radio button for views
        	final JRadioButton tableButton = new JRadioButton("Table View", true);
        	tableButton.setToolTipText("Toggles to the table view of this model data.");
        	tableButton.addActionListener(new ActionListener() {
        		public void actionPerformed(ActionEvent e) {
        			if (tableButton.isSelected()) {
        				remove(plotterPanel);
        				add(tablePanel, BorderLayout.CENTER);
        				repaint();
        			}
        		}
        	});
        	final JRadioButton plotButton = new JRadioButton("Plot View", false);
        	plotButton.setToolTipText("Toggles to the plotter view of this model.");
        	plotButton.addActionListener(new ActionListener() {
        		public void actionPerformed(ActionEvent e) {
        			if (plotButton.isSelected()) {
        				remove(tablePanel);
        				add(plotterPanel, BorderLayout.CENTER);
        				repaint();
        			}
        		}
        	});
            
            ButtonGroup group = new ButtonGroup();
        	group.add(tableButton);
        	group.add(plotButton);
            JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        	togglePanel.add(tableButton);	
        	togglePanel.add(plotButton);

        	add(togglePanel, BorderLayout.NORTH);

        	// init correct mode
        	switch (startMode) {
        	case TABLE_MODE:
                add(tablePanel);
        		tableButton.setSelected(true);
        		break;
        	case PLOT_MODE:
                add(plotterPanel);
        		plotButton.setSelected(true);
        		break;
        	}
        } // end if (showPlotter)

        setDataTable(dataTable);
    }

    public PlotterPanel getPlotterPanel() {
    	return plotterPanel;
    }
    
    public DataTableViewerTable getTable() {
        return dataTableViewerTable;
    }
    
    public void setDataTable(DataTable dataTable) {
        generalInfo.setText(dataTable.getName() + " (" + dataTable.getNumberOfRows() + " rows, " + dataTable.getNumberOfColumns() + " columns)");
        dataTableViewerTable.setDataTable(dataTable);

        if (plotterPanel != null) {
        	plotterPanel.setDataTable(dataTable);
        }
    }

	public String getCell(int row, int column) {
		return dataTableViewerTable.getCell(row, column);
	}

	public int getColumnNumber() {
		return dataTableViewerTable.getColumnNumber();
	}

	public int getRowNumber() {
		return dataTableViewerTable.getRowNumber();
	}
}
