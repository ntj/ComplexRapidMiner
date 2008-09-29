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
package com.rapidminer.gui.renderer.models;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidminer.gui.plotter.DistributionPlotter;
import com.rapidminer.gui.plotter.Plotter;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.learner.bayes.DistributionModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.report.Reportable;

/**
 * A renderer for the plot view of a distribution model.
 * 
 * @author Ingo Mierswa
 * @version $Id: DistributionModelPlotRenderer.java,v 1.3 2008/07/18 15:50:44 ingomierswa Exp $
 */
public class DistributionModelPlotRenderer extends AbstractRenderer {

	public static final String PARAMETER_ATTRIBUTE_NAME = "attribute_name";
	
	public String getName() {
		return "Plot View";
	}
	
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int width, int height) {
		DistributionModel distributionModel = (DistributionModel) renderable;
		String attributeName = null;
		try {
			attributeName = getParameterAsString(PARAMETER_ATTRIBUTE_NAME);
		} catch (UndefinedParameterError e) {
			// do nothing
		}
		
		Plotter plotter = new DistributionPlotter(distributionModel);
		String[] attributeNames = distributionModel.getAttributeNames();
		for (int i = 0; i < attributeNames.length; i++) {
			if (attributeNames[i].equals(attributeName)) {
				plotter.setPlotColumn(i, true);
				break;
			}
		}
		
		plotter.getPlotter().setSize(width, height);
		return plotter;
	}

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		DistributionModel distributionModel = (DistributionModel) renderable;
		
		JPanel graphPanel = new JPanel(new BorderLayout());
		final Plotter plotter = new DistributionPlotter(distributionModel);
		graphPanel.add(plotter.getPlotter(), BorderLayout.CENTER);
		
		final JComboBox combo = new JComboBox(distributionModel.getAttributeNames());
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.0d;
		c.weightx = 1.0d;
		c.insets = new Insets(4,4,4,4);
		c.gridwidth = GridBagConstraints.REMAINDER;
		
		JPanel boxPanel = new JPanel(layout);
		JLabel label = new JLabel("Attribute:");
		layout.setConstraints(label, c);
		boxPanel.add(label);
		
		layout.setConstraints(combo, c);
		boxPanel.add(combo);
		
		c.weighty = 1.0d;
		JPanel fillPanel = new JPanel();
		layout.setConstraints(fillPanel, c);
		boxPanel.add(fillPanel);
		
		graphPanel.add(boxPanel, BorderLayout.WEST);
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				plotter.setPlotColumn(combo.getSelectedIndex(), true);
			}
		});
		
		combo.setSelectedIndex(0);
	
		return graphPanel;
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_ATTRIBUTE_NAME, "Indicates for which attribute the distribution should be plotted.", false));
		return types;
	}
}
