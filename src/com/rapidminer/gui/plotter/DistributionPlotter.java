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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableExampleSetAdapter;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.bayes.Distribution;
import com.rapidminer.operator.learner.bayes.DistributionModel;
import com.rapidminer.operator.learner.bayes.NaiveBayes;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;

/**
 * This plotter can be used in order to plot a distribution model
 * like the one which can be delivered by NaiveBayes.
 * 
 * @author Sebastian Land, Ingo Mierswa, Tobias Malbrecht
 * @version $Id: DistributionPlotter.java,v 1.7 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class DistributionPlotter extends PlotterAdapter {

	private static final long serialVersionUID = 2923008541302883925L;
	
	private static final int NUMBER_OF_STEPS = 300;
	
	private boolean plot = true;
	
	private int plotColumn = -1;
	
	private int groupColumn = -1;
	
	private transient DistributionModel model;

	private transient DataTable dataTable;
	
	private boolean createFromDataTable = false;
	
	private JSlider kernelSlider = new JSlider(1,200,1);
	
	public DistributionPlotter() {
		kernelSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!kernelSlider.getValueIsAdjusting()) {
					repaint();
				}
			}
		});
	}
	
	public DistributionPlotter(DistributionModel model) {
		this();
		this.model = model;
		createFromDataTable = false;
		this.plotColumn = 0;
	}

	public DistributionPlotter(DataTable dataTable) {
		this();
		setDataTable(dataTable);
	}

	public void setDataTable(DataTable dataTable) {
		super.setDataTable(dataTable);
		this.dataTable = dataTable;
		this.createFromDataTable = true;
	}

	public void preparePlots() {
		if (createFromDataTable) {
			plot = false;
			if ((groupColumn >= 0) && (plotColumn >= 0) && (groupColumn != plotColumn)) {
				ExampleSet wrappedExampleSet = DataTableExampleSetAdapter.createExampleSetFromDataTable(this.dataTable);
				Attribute[] attributes = Tools.createRegularAttributeArray(wrappedExampleSet);
				Attribute label = attributes[groupColumn];
				if (label.isNominal()) {
					wrappedExampleSet.getAttributes().setLabel(label);
					try {
						NaiveBayes modelLearner = (NaiveBayes)OperatorService.createOperator(NaiveBayes.class);
						modelLearner.setParameter(NaiveBayes.PARAMETER_USE_KERNEL, "true");
						modelLearner.setParameter(NaiveBayes.PARAMETER_NUMBER_OF_KERNELS, kernelSlider.getValue() + "");
						this.model = (DistributionModel)modelLearner.learn(wrappedExampleSet);
					} catch (OperatorCreationException e) {
						LogService.getGlobal().logWarning("Cannot create distribution model generator. Skip plot...");
					} catch (MissingIOObjectException e) {
						LogService.getGlobal().logWarning("No distribution model was created from data. Skip plot...");
					} catch (OperatorException e) {
						LogService.getGlobal().logWarning("Error during creation of distribution model. Skip plot...");
					}
					plot = true;
				}
			}
		}
	}
	
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		paintComponent(graphics, getWidth(), getHeight());
	}

	public void paintComponent(Graphics graphics, int width, int height) {
		preparePlots();
		if (plot) {
			JFreeChart chart = null;
			try {
				if (!Double.isNaN(model.getUpperBound(plotColumn))) {
					chart = createNumericalChart();
				} else {
					chart = createNominalChart();
				}
			} catch (Exception e) {
				// do nothing - just do not draw the chart
			}
			
			if (chart != null) {
				// set the background color for the chart...
				chart.setBackgroundPaint(Color.white);

				// legend settings
				LegendTitle legend = chart.getLegend();
				if (legend != null) {
					legend.setPosition(RectangleEdge.TOP);
					legend.setFrame(BlockBorder.NONE);
					legend.setHorizontalAlignment(HorizontalAlignment.LEFT);
				}
				Rectangle2D drawRect = new Rectangle2D.Double(0, 0, width, height);
				chart.draw((Graphics2D) graphics, drawRect);
			}
		}
	}

	private XYDataset createNumericalDataSet() {
		//NormalDistribution normal = new NormalDistribution(1.464d, 0.174d);
		//normal.getProbability(1.5d);

		XYSeriesCollection dataSet = new XYSeriesCollection();
		double start = model.getLowerBound(plotColumn);
		double end = model.getUpperBound(plotColumn);
		double stepSize = (end - start) / (NUMBER_OF_STEPS - 1);
		for (int classIndex : model.getClasses()) {
			XYSeries series = new XYSeries(model.getLabelName(classIndex));
			for (double currentValue = start; currentValue <= end; currentValue += stepSize) {
				series.add(currentValue, model.getProbabilityForAttribute(
						classIndex, plotColumn, currentValue));
			}
			dataSet.addSeries(series);
		}
		return dataSet;
	}

	private JFreeChart createNumericalChart() {
		JFreeChart chart;
		XYDataset dataset = createNumericalDataSet();
		// create the chart...
		chart = ChartFactory.createXYLineChart(null, // chart title
				"value", // x axis label
				"density", // y axis label
				dataset, // data
				PlotOrientation.VERTICAL, 
				true, // include legend
				true, // tooltips
				false // urls
				);

		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

		DeviationRenderer renderer = new DeviationRenderer(true, false);
		Stroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		if (dataset.getSeriesCount() == 1) {
			renderer.setSeriesStroke(0, stroke);
			renderer.setSeriesPaint(0, Color.RED);
			renderer.setSeriesFillPaint(0, Color.RED);
		} else {
			for (int i = 0; i < dataset.getSeriesCount(); i++) {
				renderer.setSeriesStroke(i, stroke);
				Color color = getPointColor((double) i
						/ (double) (dataset.getSeriesCount() - 1));
				renderer.setSeriesPaint(i, color);
				renderer.setSeriesFillPaint(i, color);
			}
		}
		renderer.setAlpha(0.12f);

		plot.setRenderer(renderer);

		NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		return chart;

	}

	private JFreeChart createNominalChart() {
		JFreeChart chart;
		CategoryDataset dataset = createNominalDataSet();
		// create the chart...
		chart = ChartFactory.createBarChart(null, // chart title
				"value", // x axis label
				"density", // y axis label
				dataset, // data
				PlotOrientation.VERTICAL, 
				true, // include legend
				true, // tooltips
				false // urls
				);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		BarRenderer renderer = new BarRenderer();
		if (dataset.getRowCount() == 1) {
			renderer.setSeriesPaint(0, Color.RED);
			renderer.setSeriesFillPaint(0, Color.RED);
		} else {
			for (int i = 0; i < dataset.getRowCount(); i++) {
				Color color = getPointColor((double) i
						/ (double) (dataset.getRowCount() - 1));
				renderer.setSeriesPaint(i, color);
				renderer.setSeriesFillPaint(i, color);
			}
		}
		plot.setRenderer(renderer);
		
		return chart;
	}

	private CategoryDataset createNominalDataSet() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (Integer classIndex : model.getClasses()) {
			Distribution distribution = model.getDistribution(classIndex, plotColumn);
			String labelName = model.getLabelName(classIndex);
			for (Double value : model.getValues(plotColumn)) {
				String valueName;
				if (Double.isNaN(value))
					valueName = "unkown";
				else
					valueName = distribution.mapValue(value);
				dataset.addValue(model.getProbabilityForAttribute(classIndex,
						plotColumn, value), labelName, valueName);
			}
		}
		return dataset;
	}

	public void setPlotColumn(int column, boolean plot) {
		this.plotColumn = column;
		this.plot = plot;
		repaint();
	}

	public boolean getPlotColumn(int column) {
		return (column == this.plotColumn && plot);
	}
	
	public String getPlotName() {
		return "Plot Column:";
	}
	
	public int getNumberOfAxes() {
		return 1;
	}

	public void setAxis(int index, int dimension) {
		if (groupColumn != dimension) {
			groupColumn = dimension;
			repaint();
		}
	}

	public int getAxis(int index) {
		return groupColumn;
	}
	
	public String getAxisName(int axis) {
		return "Class Column:";
	}
	
	public JComponent getOptionsComponent(int index) {
		switch (index) {
			case 0:
				JLabel label = new JLabel("Number of Kernels:");
                label.setToolTipText("Select the number of kernels used for the estimation of the distribution of numerical attributes.");
                return label;
			case 1:
				return kernelSlider;
			default:
				return null;
		}
	}
}
