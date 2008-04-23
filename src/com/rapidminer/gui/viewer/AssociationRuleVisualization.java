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
package com.rapidminer.gui.viewer;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.operator.learner.associations.AssociationRule;
import com.rapidminer.operator.learner.associations.AssociationRules;
import com.rapidminer.operator.learner.associations.Item;

/**
 * The viewer for association rule models.
 *
 * @author Ingo Mierswa
 * @version $Id: AssociationRuleVisualization.java,v 1.3 2007/06/24 12:05:49 ingomierswa Exp $
 */
public class AssociationRuleVisualization extends JPanel {

	private static final long serialVersionUID = 4589558372186371570L;

	private JTable table = new ExtendedJTable();
	
	public AssociationRuleVisualization(AssociationRules rules) {
		final AssociationRuleTableModel model = new AssociationRuleTableModel(rules);
		
		setLayout(new BorderLayout());
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
				
		// conclusion list
		{
			SortedSet<Item> conclusions = new TreeSet<Item>();
			for (AssociationRule rule : rules) {
				Iterator<Item> i = rule.getConclusionItems();
				while (i.hasNext()) {
					conclusions.add(i.next());
				}
			}

			final Item[] itemArray = new Item[conclusions.size()];
			conclusions.toArray(itemArray);
			final JList conclusionList = new JList(itemArray);
			conclusionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			conclusionList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						Item[] searchFilter = null;
						int[] selectedIndices = conclusionList.getSelectedIndices();
						if ((selectedIndices.length > 0) && (selectedIndices.length < itemArray.length)) {
							searchFilter = new Item[selectedIndices.length];
							int counter = 0;
							for (int s : selectedIndices) {
								searchFilter[counter++] = itemArray[s];
							}
						}
						model.setFilter(searchFilter);
						setColumnSizes();
					}
				}	
			});
			
			JPanel conclusionListPanel = new JPanel();
			GridBagLayout layout = new GridBagLayout();
			conclusionListPanel.setLayout(layout);
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 1;
			c.weighty = 0;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.insets = new Insets(4, 4, 4, 4);
			
			JLabel label = new JLabel("Conclusions:");
			layout.setConstraints(label, c);
			conclusionListPanel.add(label);
			
			ExtendedJScrollPane listPane = new ExtendedJScrollPane(conclusionList);
			c.weighty = 1;
			layout.setConstraints(listPane, c);
			conclusionListPanel.add(listPane);
			
			splitPane.add(conclusionListPanel, 0);
		}
		
		// main panel
		{
			JPanel mainPanel = new JPanel();
			GridBagLayout layout = new GridBagLayout();
			mainPanel.setLayout(layout);
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 1;
			c.weighty = 1;
			c.gridwidth = GridBagConstraints.REMAINDER;

			table.setModel(model);
			JScrollPane tablePane = new ExtendedJScrollPane(table);
			layout.setConstraints(tablePane, c);
			mainPanel.add(tablePane);

			setColumnSizes();
			
			splitPane.add(mainPanel, 1);
		}
		
		add(splitPane, BorderLayout.CENTER);
	}
	
	private void setColumnSizes() {
	    TableColumn col = table.getColumnModel().getColumn(0);
	    col.setPreferredWidth(600);
	    col = table.getColumnModel().getColumn(1);
	    col.setPreferredWidth(600);
	    col = table.getColumnModel().getColumn(2);
	    col.setPreferredWidth(100);
	    col = table.getColumnModel().getColumn(3);
	    col.setPreferredWidth(100);		
	}
}
