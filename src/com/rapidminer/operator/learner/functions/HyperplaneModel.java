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
package com.rapidminer.operator.learner.functions;

import java.awt.Component;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.SimpleDataTable;
import com.rapidminer.datatable.SimpleDataTableRow;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.JRadioSelectionPanel;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.viewer.DataTableViewerTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.SimplePredictionModel;
import com.rapidminer.operator.learner.functions.kernel.functions.DotKernel;
import com.rapidminer.operator.learner.functions.kernel.functions.Kernel;
import com.rapidminer.tools.Tools;

/**
 * This model is a separating hyperplane for two classes.
 * 
 * @author Sebastian Land
 * @version $Id: HyperplaneModel.java,v 1.8 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class HyperplaneModel extends SimplePredictionModel {

	private static final long serialVersionUID = -4990692589416639697L;

	private String[] coefficientNames;
	
	private double[] coefficients;

	private double intercept;

	private String classNegative;

	private String classPositive;

	private Kernel kernel;
	
	
	protected HyperplaneModel(ExampleSet exampleSet) {
		super(exampleSet);
	}

	protected HyperplaneModel(ExampleSet exampleSet, String classNegative, String classPositive) {
		this(exampleSet, classNegative, classPositive, new DotKernel());
	}
	
	protected HyperplaneModel(ExampleSet exampleSet, String classNegative, String classPositive, Kernel kernel) {
		super(exampleSet);
		this.coefficientNames = com.rapidminer.example.Tools.getRegularAttributeNames(exampleSet);
		this.classNegative = classNegative;
		this.classPositive = classPositive;
		this.kernel = kernel;
	}
	
	public double predict(Example example) throws OperatorException {
		int i = 0;
		double distance = intercept;
		// using kernel for distance calculation
		double[] values = new double[example.getAttributes().size()];
		for (Attribute currentAttribute : example.getAttributes()) {
			values[i] = example.getValue(currentAttribute);
			i++;
		}
		distance += kernel.calculateDistance(values, coefficients);
		int positiveMapping = getLabel().getMapping().mapString(classPositive);
		int negativeMapping = getLabel().getMapping().mapString(classNegative);
		boolean isApplying = example.getAttributes().getPredictedLabel() != null; 
		if (isApplying) {
			example.setConfidence(classPositive, 1.0d / (1.0d + java.lang.Math.exp(-distance)));
			example.setConfidence(classNegative, 1.0d / (1.0d + java.lang.Math.exp(distance)));
		}
		if (distance < 0) {
			return negativeMapping;
		} else {
			return positiveMapping;
		}
	}
	
	public void init(double[] coefficients, double intercept) {
		this.coefficients = coefficients;
		this.intercept = intercept;
	}

	public double[] getCoefficients() {
		return coefficients;
	}

	public double getIntercept() {
		return intercept;
	}

	public void setCoefficients(double[] coefficients) {
		this.coefficients = coefficients;
	}

	public void setIntercept(double intercept) {
		this.intercept = intercept;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Hyperplane seperating " + classPositive + " and " + classNegative + Tools.getLineSeparator());
		buffer.append("Intercept: ");
		buffer.append(Double.toString(intercept));
		buffer.append(Tools.getLineSeparator());
		buffer.append("Coefficients: " + Tools.getLineSeparator());
		int counter = 0;
		for (double value : coefficients) {
			buffer.append("w(" + coefficientNames[counter] + ") = " + Double.toString(value) + Tools.getLineSeparator());
			counter++;
		}
		buffer.append(Tools.getLineSeparator());
		return buffer.toString();
	}
	
	private DataTable createWeightsTable() {
		SimpleDataTable weightTable = new SimpleDataTable("Hyperplane Model Weights", new String[] { "Attribute", "Weight" } );
		for (int j = 0; j < this.coefficientNames.length; j++) {
			int nameIndex = weightTable.mapString(0, this.coefficientNames[j]);
			weightTable.add(new SimpleDataTableRow(new double[] { nameIndex, this.coefficients[j]}));
		}
		return weightTable;
	}
	
	/** Returns a html label with a table view or a plotter for statistic view. */
	public Component getVisualizationComponent(IOContainer container) {
		final JRadioSelectionPanel mainPanel = new JRadioSelectionPanel();

		// text view
		Component textView = super.getVisualizationComponent(container);
		mainPanel.addComponent("Text View", textView, "Changes to a textual view of this model.");
		
		// weight table
		DataTable weightDataTable = createWeightsTable();
		DataTableViewerTable weightTableViewer = new DataTableViewerTable(true);
		weightTableViewer.setDataTable(weightDataTable);
		Component weightTableView = new ExtendedJScrollPane(weightTableViewer);
		mainPanel.addComponent("Weight Table View", weightTableView, "Changes to a weight table view of this model.");

		return mainPanel;
	}
}
