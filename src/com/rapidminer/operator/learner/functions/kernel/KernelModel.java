/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.learner.functions.kernel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableKernelModelAdapter;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.plotter.PlotterPanel;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.viewer.DataTableViewerTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.Tools;


/** This is the abstract model class for all kernel models. This class actually only provide 
 *  a common interface for plotting SVM and other kernel method models.
 * 
 *  @author Ingo Mierswa
 *  @version $Id: KernelModel.java,v 1.5 2007/07/14 12:31:38 ingomierswa Exp $ 
 */
public abstract class KernelModel extends PredictionModel {

	private String[] attributeNames;
	
	public KernelModel(ExampleSet exampleSet) {
		super(exampleSet);
		this.attributeNames = com.rapidminer.example.Tools.getRegularAttributeNames(exampleSet);
	}
	
	public abstract double getBias();
	
	public abstract double getAlpha(int index);
	
	public abstract double getFunctionValue(int index);
	
	public abstract boolean isClassificationModel();
	
	public abstract String getClassificationLabel(int index);

	public abstract double getRegressionLabel(int index);
	
	public abstract String getId(int index);
	
	public abstract SupportVector getSupportVector(int index);
	
	public abstract int getNumberOfSupportVectors();
	
	public abstract int getNumberOfAttributes();
	
	public abstract double getAttributeValue(int exampleIndex, int attributeIndex);
	
	/** Returns a string representation of this model. */
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("Total number of Support Vectors: " + getNumberOfSupportVectors() + Tools.getLineSeparator());
		result.append("Bias (offset): " + Tools.formatNumber(getBias()) + Tools.getLineSeparators(2));
		if ((!getLabel().isNominal()) || (getLabel().getMapping().size() == 2)) {
			double[] w = new double[getNumberOfAttributes()];
			boolean showWeights = true;
			for (int i = 0; i < getNumberOfSupportVectors(); i++) {
				SupportVector sv = getSupportVector(i);
				if (sv != null) {
					double[] x = sv.getX();
					double alpha = sv.getAlpha();
					double y = sv.getY();
					for (int j = 0; j < w.length; j++) {
						w[j] += y * alpha * x[j];
					}
				} else {
					showWeights = false;
				}
			}
			if (showWeights) {
				for (int j = 0; j < w.length; j++) {
					result.append("w(" + attributeNames[j] + ") = " + Tools.formatNumber(w[j]) + Tools.getLineSeparator());
				}
			}
		} else {
			result.append("Feature weight calculation not possible for more than two classes."+Tools.getLineSeparator()+"Please use the operator SVMWeighting instead." + Tools.getLineSeparator());
		}
		return result.toString();
	}
	
	/** Returns a html label with a table view or a plotter for statistic view. */
	public Component getVisualizationComponent(IOContainer container) {
		final JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		// text
		final Component textView = super.getVisualizationComponent(container);
		
		// table
		DataTable dataTable = new DataTableKernelModelAdapter(this);
	    DataTableViewerTable dataTableViewerTable = new DataTableViewerTable();
		dataTableViewerTable.setDataTable(dataTable);
		final Component tableView = new ExtendedJScrollPane(dataTableViewerTable);
		
		// plot
		final JScrollPane graphView = new ExtendedJScrollPane(new PlotterPanel(dataTable, PlotterPanel.DATA_SET_PLOTTER_SELECTION));
		
		final JRadioButton textViewButton = new JRadioButton("Text View", true);
		textViewButton.setToolTipText("Changes to a textual view of this model.");
		textViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (textViewButton.isSelected()) {
					mainPanel.remove(1);
					mainPanel.add(textView, BorderLayout.CENTER);
					mainPanel.repaint();
				}
			}
		});
		
		final JRadioButton tableViewButton = new JRadioButton("Table View", true);
		tableViewButton.setToolTipText("Changes to a table view of this model.");
		tableViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tableViewButton.isSelected()) {
					mainPanel.remove(1);
					mainPanel.add(tableView, BorderLayout.CENTER);
					mainPanel.repaint();
				}
			}
		});
		
		final JRadioButton graphViewButton = new JRadioButton("Plot View", true);
		graphViewButton.setToolTipText("Changes to a plot view of this model.");
		graphViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (graphViewButton.isSelected()) {
					mainPanel.remove(1);
					mainPanel.add(graphView, BorderLayout.CENTER);
					mainPanel.repaint();
				}
			}
		});

		ButtonGroup group = new ButtonGroup();
		group.add(textViewButton);
		group.add(tableViewButton);
		group.add(graphViewButton);
		JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		togglePanel.add(textViewButton);
		togglePanel.add(tableViewButton);
		togglePanel.add(graphViewButton);

		mainPanel.add(togglePanel, BorderLayout.NORTH);
		mainPanel.add(textView, BorderLayout.CENTER);
		textViewButton.setSelected(true);

		return mainPanel;
	}
}
