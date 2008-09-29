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
package com.rapidminer.operator.features;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.rapidminer.ObjectVisualizer;
import com.rapidminer.datatable.SimpleDataTable;
import com.rapidminer.datatable.SimpleDataTableRow;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.plotter.ScatterPlotter;
import com.rapidminer.gui.plotter.SimplePlotterDialog;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.operator.features.selection.NonDominatedSortingSelection;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.tools.Tools;


/**
 * Plots all individuals in performance space, i.e. the dimensions of the plot
 * (color for the third dimension) corresponds to performance criteria.
 * 
 * @author Ingo Mierswa
 * @version $Id: PopulationPlotter.java,v 2.7 2006/04/14 07:47:16 ingomierswa
 *          Exp $
 */
public class PopulationPlotter implements PopulationOperator, ObjectVisualizer {

	/** Indicates in which generations the plot should be updated. */
	private int plotGenerations = 1;

	/** The plotter. */
	private SimplePlotterDialog plotter = null;

	/** The data table containing the individuals criteria data. */
	private SimpleDataTable criteriaDataTable = null;

	/** Indicates if the draw range should be set. */
	private boolean setDrawRange = false;

	/** Indicates if dominated points should also be drawn. */
	private boolean drawDominated = true;

	/** The last population. */
	//private Population lastPopulation;

	/** Contains a copy of the individuals of the last generation. */
	private Map<String, ExampleSet> lastPopulation = new HashMap<String, ExampleSet>();
	
	
	/** Creates plotter panel which is repainted every generation. */
	public PopulationPlotter() {
		this(1, false, true);
	}

	/**
	 * Creates plotter panel which is repainted each plotGenerations
	 * generations.
	 */
	public PopulationPlotter(int plotGenerations, boolean setDrawRange, boolean drawDominated) {
		this.plotGenerations = plotGenerations;
		this.setDrawRange = setDrawRange;
		this.drawDominated = drawDominated;
	}

	/**
	 * Returns true if the current generation modulo the plotGenerations
	 * parameter is zero.
	 */
	public boolean performOperation(int generation) {
		return ((generation % plotGenerations) == 0);
	}

	public void operate(Population pop) {
		if (pop.getNumberOfIndividuals() == 0)
			return;
		if ((pop.getGeneration() % plotGenerations) != 0)
			return;

		// init data table
		if (criteriaDataTable == null) {
			this.criteriaDataTable = createDataTable(pop);
		}

		// fill table
		int numberOfCriteria = fillDataTable(this.criteriaDataTable, this.lastPopulation, pop, drawDominated);

		// create plotter
		if (plotter == null) {
			plotter = new SimplePlotterDialog(criteriaDataTable, false);
			if (numberOfCriteria == 1) {
				plotter.setXAxis(0);
				plotter.plotColumn(0, true);
			} else if (numberOfCriteria == 2) {
				plotter.setXAxis(0);
				plotter.plotColumn(1, true);
			} else if (numberOfCriteria > 2) {
				plotter.setXAxis(0);
				plotter.setYAxis(1);
				plotter.plotColumn(2, true);
			}
			plotter.setPointType(ScatterPlotter.POINTS);
			plotter.setVisible(true);
			plotter.addObjectVisualizer(this);
		}

		// change some plotter paras
		if (setDrawRange)
			plotter.setDrawRange(0.0d, 1.0d, 0.0d, 1.0d);
		plotter.setKey("Generation " + pop.getGeneration());
	}

	public static SimpleDataTable createDataTable(Population pop) {
		PerformanceVector prototype = pop.get(0).getPerformance();
		SimpleDataTable dataTable = new SimpleDataTable("Population", prototype.getCriteriaNames());
		return dataTable;
	}

	public static int fillDataTable(SimpleDataTable dataTable, Map<String,ExampleSet> lastPopulation, Population pop, boolean drawDominated) {
		lastPopulation.clear();
		dataTable.clear();
		int numberOfCriteria = 0;
		for (int i = 0; i < pop.getNumberOfIndividuals(); i++) {
			boolean dominated = false;
			if (!drawDominated) {
				for (int j = 0; j < pop.getNumberOfIndividuals(); j++) {
					if (i == j)
						continue;
					if (NonDominatedSortingSelection.isDominated(pop.get(i), pop.get(j))) {
						dominated = true;
						break;
					}
				}
			}

			if (drawDominated || (!dominated)) {
				StringBuffer id = new StringBuffer(i + " (");
				PerformanceVector current = pop.get(i).getPerformance();
				numberOfCriteria = Math.max(numberOfCriteria, current.getSize());
				double[] data = new double[current.getSize()];
				for (int d = 0; d < data.length; d++) {
					data[d] = current.getCriterion(d).getFitness();
					if (d != 0)
						id.append(", ");
					id.append(Tools.formatNumber(data[d]));
				}
				id.append(")");
				dataTable.add(new SimpleDataTableRow(data, id.toString()));
				lastPopulation.put(id.toString(), (ExampleSet)pop.get(i).getExampleSet().clone());
			}
		}
		return numberOfCriteria;
	}

	// ================================================================================

	public boolean isCapableToVisualize(String id) {
		return this.lastPopulation.get(id) != null;
	}

	public String getTitle(String id) {
		return id;
	}

	public void stopVisualization(String id) {}

	public void startVisualization(String id) {
		ExampleSet es = lastPopulation.get(id);
		
		// MetaDataViewer mdViewer = new MetaDataViewer(es, 50);
		Component visualizationComponent = es.getVisualizationComponent(null);
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		// frame.getContentPane().add(new ExtendedJScrollPane(mdViewer),
		// BorderLayout.CENTER);
		frame.getContentPane().add(new ExtendedJScrollPane(visualizationComponent), BorderLayout.CENTER);
		frame.setSize(600, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
