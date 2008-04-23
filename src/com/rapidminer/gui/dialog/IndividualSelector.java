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
package com.rapidminer.gui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.viewer.MetaDataViewer;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.Population;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.tools.Tools;


/**
 * This dialog can be used to select an individual from a population.
 * 
 * @author Ingo Mierswa
 * @version $Id: IndividualSelector.java,v 2.3 2006/03/21 15:35:40 ingomierswa
 *          Exp $
 */
public class IndividualSelector extends JDialog {

	private static final long serialVersionUID = -6512675217777454316L;

	private transient Population population;

	private JList selectionList;

	private JLabel performanceLabel = new JLabel();

	private MetaDataViewer metaDataViewer = null;

	public IndividualSelector(Population population) {
		this(population, true);
	}

	public IndividualSelector(Population population, boolean modal) {
		this(RapidMinerGUI.getMainFrame(), population, -1, -1, modal);
	}

	public IndividualSelector(Frame owner, Population population, int width, int height, boolean modal) {
		super(owner, "Result Individual Selection", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.population = population;

		// selection list
		String[] listItems = new String[population.getNumberOfIndividuals()];
		for (int i = 0; i < listItems.length; i++) {
			listItems[i] = i + "";
		}
		this.selectionList = new JList(listItems);
		selectionList.setBorder(BorderFactory.createLoweredBevelBorder());
		selectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectionList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				setSelectedIndividual(selectionList.getSelectedIndex());
			}
		});
		JScrollPane listScrollPane = new ExtendedJScrollPane(selectionList);
		listScrollPane.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		getContentPane().add(listScrollPane, BorderLayout.WEST);

		// center panel
		JPanel centerPanel = new JPanel();
		GridBagLayout gridBag = new GridBagLayout();
		centerPanel.setLayout(gridBag);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(2, 2, 2, 2);
		c.weightx = 0;
		c.weighty = 0;

		// performanceLabel
		gridBag.setConstraints(performanceLabel, c);
		centerPanel.add(performanceLabel);

		// meta data viewer
		this.metaDataViewer = new MetaDataViewer(population.get(0).getExampleSet());
		JScrollPane metaDataPane = new ExtendedJScrollPane(metaDataViewer);
		c.weightx = 1;
		c.weighty = 1;
		gridBag.setConstraints(metaDataPane, c);
		centerPanel.add(metaDataPane);
		selectionList.setSelectedIndex(0);

		getContentPane().add(centerPanel, BorderLayout.CENTER);

		// button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});
		buttonPanel.add(okButton);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		// size and location
		if ((width < 0) || (height < 0))
			setSize(800, 600);
		else
			setSize(width, height);

		setLocationRelativeTo(owner);
	}

	private void ok() {
		dispose();
	}

	public void setSelectedIndividual(int index) {
		Individual selected = population.get(index);
		PerformanceVector performance = selected.getPerformance();
		StringBuffer performanceString = new StringBuffer();
		for (int i = 0; i < performance.getSize(); i++) {
			if (i != 0)
				performanceString.append(", ");
			PerformanceCriterion pc = performance.getCriterion(i);
			performanceString.append(pc.getName() + " = " + Tools.formatNumber(pc.getAverage()) + " (fitness: " + Tools.formatNumber(pc.getFitness()) + ")");
		}
		performanceLabel.setText(performanceString.toString());
		performanceLabel.repaint();

		metaDataViewer.setExampleSet(selected.getExampleSet());
	}

	public Individual getSelectedIndividual() {
		int index = selectionList.getSelectedIndex();
		if ((index >= 0) && (index < population.getNumberOfIndividuals()))
			return population.get(index);
		else
			return null;
	}
}
