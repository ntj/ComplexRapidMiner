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
package com.rapidminer.operator.learner.functions.kernel;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableKernelModelAdapter;
import com.rapidminer.datatable.SimpleDataTable;
import com.rapidminer.datatable.SimpleDataTableRow;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.JRadioSelectionPanel;
import com.rapidminer.gui.plotter.PlotterPanel;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.viewer.DataTableViewerTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.Renderable;
import com.rapidminer.tools.Tableable;
import com.rapidminer.tools.Tools;


/** This is the abstract model class for all kernel models. This class actually only provide 
 *  a common interface for plotting SVM and other kernel method models.
 * 
 *  @author Ingo Mierswa
 *  @version $Id: KernelModel.java,v 1.14 2008/05/10 18:29:35 stiefelolm Exp $ 
 */
public abstract class KernelModel extends PredictionModel implements Renderable, Tableable {

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
	
	public Renderable renderer; 
	
	public Tableable table;
	
	/** The default implementation returns the classname without package. */
	public String getName() {
		return "Kernel Model";
	}
	
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
	
	private DataTable createWeightsTable() {
		SimpleDataTable weightTable = new SimpleDataTable("Kernel Model Weights", new String[] { "Attribute", "Weight" } );
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
					int nameIndex = weightTable.mapString(0, attributeNames[j]);
					weightTable.add(new SimpleDataTableRow(new double[] { nameIndex, w[j]}));
				}
				return weightTable;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	/** Returns a html label with a table view or a plotter for statistic view. */
	public Component getVisualizationComponent(IOContainer container) {
		final JRadioSelectionPanel mainPanel = new JRadioSelectionPanel();

		// text view
		Component textView = super.getVisualizationComponent(container);
		mainPanel.addComponent("Text View", textView, "Changes to a textual view of this model.");
		
		// weight table
		DataTable weightDataTable = createWeightsTable();
		if (weightDataTable != null) {
			DataTableViewerTable weightTableViewer = new DataTableViewerTable(true);
			table = weightTableViewer;
			weightTableViewer.setDataTable(weightDataTable);
			Component weightTableView = new ExtendedJScrollPane(weightTableViewer);
			mainPanel.addComponent("Weight Table View", weightTableView, "Changes to a weight table view of this model.");
		} else {
			mainPanel.addComponent("Weight Table View", new ExtendedJScrollPane(new JLabel("Calculation of a weight table only possible for regression or binominal classification tasks.")), "Changes to a weight table view of this model.");
		}

		// support vector table
		DataTable supportVectorDataTable = new DataTableKernelModelAdapter(this);
	    DataTableViewerTable supportVectorTableViewer = new DataTableViewerTable(false);
	    if (this.table == null)
	    	this.table = supportVectorTableViewer;
	    supportVectorTableViewer.setDataTable(supportVectorDataTable);
		final Component supportVectorTableView = new ExtendedJScrollPane(supportVectorTableViewer);
		mainPanel.addComponent("Support Vector Table View", supportVectorTableView, "Changes to a support vector table view of this model.");
		
		// plot
		PlotterPanel panel = new PlotterPanel(supportVectorDataTable, PlotterPanel.DATA_SET_PLOTTER_SELECTION);
		renderer = panel;
		final JScrollPane graphView = new ExtendedJScrollPane(panel);
		mainPanel.addComponent("Plot View", graphView, "Changes to a plot view of this model.");

		return mainPanel;
	}
	
	public void render(Graphics graphics, int width, int height) {
		renderer.render(graphics, width, height);
	}

	public int getRenderWidth(int preferredWidth) {
		return renderer.getRenderWidth(preferredWidth);
	}

	public int getRenderHeight(int preferredHeight) {
		return renderer.getRenderHeight(preferredHeight);
	}
	
	public int getRowNumber() {
		return table.getRowNumber();
	}
	
	public int getColumnNumber() {
		return table.getColumnNumber();
	}
	
	public String getCell(int row, int column) {
		return table.getCell(row, column);
	}
}
