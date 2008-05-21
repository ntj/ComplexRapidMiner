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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.operator.learner.associations.AssociationRule;
import com.rapidminer.operator.learner.associations.AssociationRules;
import com.rapidminer.operator.learner.associations.Item;

/**
 * This is a gui component which can be used to define filter conditions for association rules.
 * 
 * @author Ingo Mierswa
 * @version $Id: AssociationRuleFilter.java,v 1.3 2008/05/09 19:22:59 ingomierswa Exp $
 */
public class AssociationRuleFilter extends JPanel {

	private static final long serialVersionUID = 5619543957729778883L;

	private static final int MAX_CONFIDENCE = 10000;
	
    private JSlider confidenceSlider = new JSlider(SwingConstants.HORIZONTAL, 0, MAX_CONFIDENCE, (int)(MAX_CONFIDENCE / 10.0d)) {

		private static final long serialVersionUID = 1210754337620619916L;
		
		public Dimension getMinimumSize() {
            return new Dimension(40, (int)super.getMinimumSize().getHeight());
        }
        public Dimension getPreferredSize() {
            return new Dimension(40, (int)super.getPreferredSize().getHeight());
        }
        public Dimension getMaximumSize() {
            return new Dimension(40, (int)super.getMaximumSize().getHeight());
        }
    };
    
	private JList conclusionList = null;
	
	private JComboBox conjunctionBox = new JComboBox(AssociationRuleFilterListener.CONJUNCTION_NAMES);

	private Item[] itemArray;
	
	private List<AssociationRuleFilterListener> listeners = new LinkedList<AssociationRuleFilterListener>();
	
	private AssociationRules rules;
	
	public AssociationRuleFilter(AssociationRules rules) {
		this.rules = rules;
		this.itemArray = rules.getAllConclusionItems();

		// layout
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(4, 4, 4, 4);

		// conjunction mode
		conjunctionBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				adjustFilter();
			}
		});

		JLabel label = new JLabel("Conjunction Type:");
		layout.setConstraints(label, c);
		add(label);
		
		layout.setConstraints(conjunctionBox, c);
		add(conjunctionBox);

		// conclusion list
		this.conclusionList = new JList(itemArray);
		this.conclusionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		conclusionList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					adjustFilter();
				}
			}	
		});

		label = new JLabel("Conclusions:");
		layout.setConstraints(label, c);
		add(label);
		
		ExtendedJScrollPane listPane = new ExtendedJScrollPane(conclusionList);
		c.weighty = 1;
		layout.setConstraints(listPane, c);
		add(listPane);
		
		c.weighty = 0;
		label = new JLabel("Min. Confidence:");
		layout.setConstraints(label, c);
		add(label);
		
		confidenceSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!confidenceSlider.getValueIsAdjusting()) {
					adjustFilter();
				}
			}
		});
		layout.setConstraints(confidenceSlider, c);
		add(confidenceSlider);
	}
	
	private void adjustFilter() {
		int conjunctionMode = conjunctionBox.getSelectedIndex();
		Item[] searchFilter = null;
		int[] selectedIndices = conclusionList.getSelectedIndices();
		if ((selectedIndices.length > 0) && (selectedIndices.length <= itemArray.length)) {
			searchFilter = new Item[selectedIndices.length];
			int counter = 0;
			for (int s : selectedIndices) {
				searchFilter[counter++] = itemArray[s];
			}
		}
		double minConfidence = confidenceSlider.getValue() / (double)MAX_CONFIDENCE;
		fireFilteringEvent(searchFilter, conjunctionMode, minConfidence);
	}
	
	public void addAssociationRuleFilterListener(AssociationRuleFilterListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeAssociationRuleFilterListener(AssociationRuleFilterListener listener) {
		this.listeners.remove(listener);
	}
	
	private void fireFilteringEvent(Item[] searchFilter, int conjunctionMode, double minConfidence) {
		boolean[] filter = getFilter(rules, searchFilter, conjunctionMode, minConfidence);
		for (AssociationRuleFilterListener listener : listeners) {
			listener.setFilter(filter);
		}
	}
	
	private boolean[] getFilter(AssociationRules rules, Item[] filter, int conjunctionMode, double minConfidence) {				
		boolean[] mapping = new boolean[rules.getNumberOfRules()];
		int counter = 0;
		for (AssociationRule rule : rules) {
			if (rule.getConfidence() >= minConfidence) {
				if (checkForItem(filter, rule, conjunctionMode)) {
					mapping[counter] = true;
				} else {
					mapping[counter] = false;
				}
			} else {
				mapping[counter] = false;
			}
			counter++;
		}

		return mapping;
	}
	
	private boolean checkForItem(Item[] filter, AssociationRule rule, int conjunctionMode) {
		if (filter == null)
			return true;
		if (conjunctionMode == AssociationRuleFilterListener.CONJUNCTION_OR) {
			boolean found = false;
			for (Item filterItem : filter) {
				Iterator<Item> c = rule.getConclusionItems();
				while (c.hasNext()) {
					Item conclusionItem = c.next();
					if (filterItem.equals(conclusionItem)) {
						found = true;
						break;
					}
				}	
				if (found)
					break;
			}
			return found;
		} else {
			boolean found = true;
			for (Item filterItem : filter) {
				Iterator<Item> c = rule.getConclusionItems();
				while (c.hasNext()) {
					Item conclusionItem = c.next();
					if (!filterItem.equals(conclusionItem)) {
						found = false;
						break;
					}
				}	
				if (!found)
					break;
			}
			return found;
		}
	}
}
