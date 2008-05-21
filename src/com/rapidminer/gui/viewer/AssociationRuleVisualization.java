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
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import com.rapidminer.gui.JRadioSelectionPanel;
import com.rapidminer.gui.graphs.AssociationRulesGraphCreator;
import com.rapidminer.gui.graphs.GraphViewer;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.operator.learner.associations.AssociationRules;

/**
 * The viewer for association rule models.
 *
 * @author Ingo Mierswa
 * @version $Id: AssociationRuleVisualization.java,v 1.10 2008/05/09 19:22:59 ingomierswa Exp $
 */
public class AssociationRuleVisualization extends JPanel implements AssociationRuleFilterListener {

	private static final long serialVersionUID = 4589558372186371570L;

	private JTable table = new ExtendedJTable();

	private AssociationRuleTableModel model = null;
		
	public AssociationRuleVisualization(AssociationRules rules, Component textView) {
		if ((rules != null) && (rules.getNumberOfRules() > 0)) {
			this.model = new AssociationRuleTableModel(rules);
			final JRadioSelectionPanel selectionPanel = new JRadioSelectionPanel();
			setLayout(new BorderLayout());
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			
			// conclusion list
			{
				AssociationRuleFilter filter = new AssociationRuleFilter(rules);
				filter.addAssociationRuleFilterListener(this);
				splitPane.add(filter, 0);			
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

			GraphViewer graphViewer = new GraphViewer<String,String>(new AssociationRulesGraphCreator(rules));
			selectionPanel.addComponent("Table View", splitPane, "Changes to a table view of the association rules.");
			selectionPanel.addComponent("Graph View", graphViewer, "Changes to a graphical view of the association rules.");
			selectionPanel.addComponent("Text View", textView, "Changes to a textual view of the association rules.");
			
			add(selectionPanel, BorderLayout.CENTER);
		} else {
			add(new JLabel("no rules found"), BorderLayout.CENTER);
		}
	}
	
	private void setColumnSizes() {
	    TableColumn col = table.getColumnModel().getColumn(0);
	    col.setPreferredWidth(50);
	    col = table.getColumnModel().getColumn(1);
	    col.setPreferredWidth(600);
	    col = table.getColumnModel().getColumn(2);
	    col.setPreferredWidth(600);
	    col = table.getColumnModel().getColumn(3);
	    col.setPreferredWidth(100);		
	    col = table.getColumnModel().getColumn(4);
	    col.setPreferredWidth(100);		
	}
	
	public void setFilter(boolean[] filter) {
		this.model.setFilter(filter);
		setColumnSizes();
	}
}
