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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.rapidminer.ObjectVisualizer;
import com.rapidminer.datatable.DataTable;
import com.rapidminer.gui.RapidMinerGUI;


/**
 * This dialog can be used to create a simple plot dialog from a given {@link DataTable}.
 * This might be useful if an operator should display some data or results. This
 * class supports some of the main options of plotting like axis selection or
 * draw range selection.
 * 
 * @author Ingo Mierswa
 * @version $Id: SimplePlotterDialog.java,v 2.17 2006/04/05 09:41:59 ingomierswa
 *          Exp $
 */
public class SimplePlotterDialog extends JDialog implements MouseMotionListener, MouseListener {

	private static final long serialVersionUID = 3024071214549165747L;

	private ScatterPlotter plotter;

	private List<ObjectVisualizer> visualizers = new LinkedList<ObjectVisualizer>();

	private JButton createOtherPlottersButton = null;
	
	
	public SimplePlotterDialog(DataTable dataTable) {
		this(dataTable, true);
	}

	public SimplePlotterDialog(DataTable dataTable, boolean modal) {
		this(RapidMinerGUI.getMainFrame(), dataTable, -1, -1, false, modal);
	}

	public SimplePlotterDialog(Frame owner, final DataTable dataTable, int width, int height, boolean createPlotterPanelButton, boolean modal) {
		super(owner, dataTable.getName(), modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.plotter = new ScatterPlotter(dataTable);
		plotter.addMouseMotionListener(this);
		plotter.addMouseListener(this);
		JComponent plotterComponent = plotter.getPlotter();
		plotterComponent.setBorder(BorderFactory.createEtchedBorder());
		getContentPane().add(plotterComponent, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		if (createPlotterPanelButton) {
			JButton createOtherPlottersButton = new JButton("Create other plotters...");
			createOtherPlottersButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					SimplePlotterPanelDialog plotterPanelDialog = new SimplePlotterPanelDialog(dataTable);
					plotterPanelDialog.setVisible(true);
				}
			});
			buttonPanel.add(createOtherPlottersButton);
		}
		
		if (plotter.isSaveable()) {
			JButton saveButton = new JButton("Save...");
			saveButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					plotter.save();
				}
			});
			buttonPanel.add(saveButton);
		}
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});
		buttonPanel.add(okButton);

		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		if ((width < 0) || (height < 0))
			setSize(600, 400);
		else
			setSize(width, height);
		setLocationRelativeTo(owner);
	}

	public void setCreateOtherPlottersEnabled(boolean enabled) {
		if (this.createOtherPlottersButton != null)
			this.createOtherPlottersButton.setEnabled(enabled);
	}
	
	public void setDrawRange(double minX, double maxX, double minY, double maxY) {
		plotter.setDrawRange(minX, maxX, minY, maxY);
	}

	public void setXAxis(int index) {
		plotter.setAxis(ScatterPlotter.X_AXIS, index);
	}

	public void setYAxis(int index) {
		plotter.setAxis(ScatterPlotter.Y_AXIS, index);
	}

	public void plotColumn(int index, boolean plot) {
		plotter.setPlotColumn(index, plot);
	}

    public void setPointType(int pointType) {
    	plotter.setPointType(pointType);
    }
    
    public void setDrawLabel(boolean v) {
        plotter.setDrawLabel(v);
    }
    
	public void setKey(String key) {
		plotter.setKey(key);
	}

	private void ok() {
		dispose();
	}

	// ================================================================================
	// Mouse Events
	// ================================================================================

	public void addObjectVisualizer(ObjectVisualizer visualizer) {
		visualizers.add(visualizer);
	}

	public void removeObjectVisualizer(ObjectVisualizer visualizer) {
		visualizers.remove(visualizer);
	}

	private void fireVisualizationEvent(String id) {
		Iterator i = visualizers.iterator();
		while (i.hasNext()) {
			ObjectVisualizer visualizer = (ObjectVisualizer) i.next();
			if (visualizer.isCapableToVisualize(id)) {
				visualizer.startVisualization(id);
			}
		}
	}

	public void mouseMoved(MouseEvent e) {
		plotter.setMousePosInDataSpace(e.getX(), e.getY());
	}

	public void mouseDragged(MouseEvent e) {}

	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (e.getClickCount() > 1) {
				String id = plotter.getIdForPos(e.getX(), e.getY());
				if (id != null) {
					fireVisualizationEvent(id);
				}
			}
		}
	}

	public void mouseReleased(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}
}
