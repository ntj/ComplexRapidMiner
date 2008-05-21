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
import java.util.Iterator;
import java.util.Random;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.SimpleDataTable;
import com.rapidminer.datatable.SimpleDataTableRow;
import com.rapidminer.gui.graphs.GraphViewer;
import com.rapidminer.gui.graphs.SimilarityGraphCreator;
import com.rapidminer.gui.plotter.HistogramPlotter;
import com.rapidminer.operator.similarity.DistanceSimilarityConverter;
import com.rapidminer.operator.similarity.SimilarityMeasure;


/**
 * Visualizes a similarity measure interactively.
 * 
 * @author Ingo Mierswa
 * @version $Id: SimilarityVisualization.java,v 1.10 2008/05/09 19:23:01 ingomierswa Exp $
 */
public class SimilarityVisualization extends JPanel {

	private static final long serialVersionUID = 1976956148942768107L;

	public SimilarityVisualization(SimilarityMeasure sim) {
		super();
		setLayout(new BorderLayout());

		ButtonGroup group = new ButtonGroup();
		JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		// similarity table
		final JComponent tableView = new SimilarityTable(sim);
		final JRadioButton tableButton = new JRadioButton("Table View", true);
		tableButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tableButton.isSelected()) {
					remove(1);
					add(tableView, BorderLayout.CENTER);
					repaint();
				}
			}
		});
		group.add(tableButton);
		togglePanel.add(tableButton);

		// graph view
		final JComponent graphView = new GraphViewer<String,String>(new SimilarityGraphCreator(sim));
		final JRadioButton graphButton = new JRadioButton("Graph View", false);
		graphButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (graphButton.isSelected()) {
					remove(1);
					add(graphView, BorderLayout.CENTER);
					repaint();
				}
			}
		});
		group.add(graphButton);
		togglePanel.add(graphButton);

		// histogram view
		DataTable dataTable = new SimpleDataTable("Histogram", new String[] { "Histogram" });
        double sampleRatio = Math.min(1.0d, 500.0d / sim.getNumberOfIds());
        
        Random random = new Random();
        Iterator<String> i = sim.getIds();
        while (i.hasNext()) {
            String idX = i.next();
            Iterator<String> j = sim.getIds();
            if (random.nextDouble() < sampleRatio) {
                while (j.hasNext()) {
                    String idY = j.next();
                    if (!(idX.equals(idY)) && (random.nextDouble() < sampleRatio)) {
                        double simValue = sim.similarity(idX, idY);
                        dataTable.add(new SimpleDataTableRow(new double[] { simValue }));
                    }
                }
            }
        }

		final HistogramPlotter histogramView = new HistogramPlotter();
		histogramView.setDataTable(dataTable);
		histogramView.setPlotColumn(0, true);
		histogramView.setBinNumber(100);
		
		final JRadioButton histogramButton = new JRadioButton("Histogram View", false);
		histogramButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (histogramButton.isSelected()) {
					remove(1);
					add(histogramView, BorderLayout.CENTER);
					repaint();
				}
			}
		});
		group.add(histogramButton);
		togglePanel.add(histogramButton);

		// K distance view
		//if (sim.isDistance()) {
			final SimilarityKDistanceVisualization kDistancePlotter = 
				new SimilarityKDistanceVisualization((sim.isDistance() ? sim : new DistanceSimilarityConverter(sim)), this);
			final JRadioButton kdistanceButton = new JRadioButton("k-Distance View", false);
			kdistanceButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (kdistanceButton.isSelected()) {
						remove(1);
						add(kDistancePlotter, BorderLayout.CENTER);
						repaint();
					}
				}
			});
			group.add(kdistanceButton);
            togglePanel.add(kdistanceButton);
		//}

		add(togglePanel, BorderLayout.NORTH);
		add(tableView, BorderLayout.CENTER);
	}
}
