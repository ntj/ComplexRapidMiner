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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.operator.ContainerModel;
import com.rapidminer.operator.IOContainer;


/**
 * Can be used to display the models of a ContainerModel.
 * 
 * @author Ingo Mierswa
 * @version $Id: ContainerModelViewer.java,v 1.4 2006/03/27 13:21:58 ingomierswa
 *          Exp $
 */
public class ContainerModelViewer extends JPanel {

	private static final long serialVersionUID = -322963469866592863L;

	/** The currently used visualization component. */
	private Component current;

	public ContainerModelViewer(final ContainerModel model, final IOContainer container) {
		this.current = null;

		final GridBagLayout gridBag = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();
		setLayout(gridBag);
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(5, 5, 5, 5);

		// selection list
		List<String> modelNameList = new LinkedList<String>();
		for (int i = 0; i < model.getNumberOfModels(); i++) {
			modelNameList.add(model.getModel(i).getName());
		}
		String[] modelNames = new String[modelNameList.size()];
		modelNameList.toArray(modelNames);
		final JList modelList = new JList(modelNames);
		modelList.setBorder(BorderFactory.createTitledBorder("Model Selector"));
		modelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		modelList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (current != null) {
					remove(current);
				}
				current = model.getModel(modelList.getSelectedIndex()).getVisualizationComponent(container);
				c.gridwidth = GridBagConstraints.REMAINDER;
				c.weightx = 1;
				c.weighty = 1;
				gridBag.setConstraints(current, c);
				add(current);
			}
		});

		JScrollPane listScrollPane = new ExtendedJScrollPane(modelList);

		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 0.1;
		c.weighty = 0;
		gridBag.setConstraints(listScrollPane, c);
		add(listScrollPane);

		// select first model
		modelList.setSelectedIndices(new int[] { 0 });
	}
}
