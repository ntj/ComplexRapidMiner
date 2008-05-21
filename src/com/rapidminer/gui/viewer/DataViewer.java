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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionCreationException;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.tools.Tableable;


/**
 * Can be used to display (parts of) the data by means of a JTable.
 * 
 * @author Ingo Mierswa
 * @version $Id: DataViewer.java,v 1.6 2008/05/09 19:23:01 ingomierswa Exp $
 */
public class DataViewer extends JPanel implements Tableable {

    private static final long serialVersionUID = -8114228636932871865L;

    private JLabel generalInfo = new JLabel();
    
    private DataViewerTable dataTable = new DataViewerTable();
    
    /** Filter counter display. */
    private JLabel filterCounter = new JLabel();
    
    private transient ExampleSet originalExampleSet;
    
    public DataViewer(ExampleSet exampleSet, boolean providedFilter) {
        super(new BorderLayout());
        this.originalExampleSet = exampleSet;
        
        JPanel infoPanel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        infoPanel.setLayout(layout);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5,5,5,5);
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.gridwidth = GridBagConstraints.RELATIVE;
        
        StringBuffer infoText = new StringBuffer("ExampleSet (");
        int noExamples = originalExampleSet.size();
        infoText.append(noExamples);
        infoText.append(noExamples == 1 ? " example, " : " examples, ");
        int noSpecial = originalExampleSet.getAttributes().specialSize();
        infoText.append(noSpecial);
        infoText.append(noSpecial == 1 ? " special attribute, " : " special attributes, ");
        int noRegular = originalExampleSet.getAttributes().size();
        infoText.append(noRegular);
        infoText.append(noRegular == 1 ? " regular attribute)" : " regular attributes)");
        generalInfo.setText(infoText.toString());
        layout.setConstraints(generalInfo, c);
        infoPanel.add(generalInfo);
        
        // filter
        if (providedFilter) {
        	JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        	filterPanel.add(new JLabel("View Filter "));
        	updateFilterCounter(originalExampleSet);
        	filterPanel.add(filterCounter);
        	List<String> applicableFilterNames = new LinkedList<String>();
        	for (int i = 0; i < ConditionedExampleSet.KNOWN_CONDITION_NAMES.length; i++) {
        		String conditionName = ConditionedExampleSet.KNOWN_CONDITION_NAMES[i];
        		try {
        			ConditionedExampleSet.createCondition(conditionName, exampleSet, null);
        			applicableFilterNames.add(conditionName);
        		} catch (ConditionCreationException ex) {} // Do nothing
        	}
        	String[] applicableConditions = new String[applicableFilterNames.size()];
        	applicableFilterNames.toArray(applicableConditions);
        	final JComboBox filterSelector = new JComboBox(applicableConditions);
        	filterSelector.setToolTipText("These filters can be used to skip examples in the view fulfilling the filter condition.");
        	filterSelector.addItemListener(new ItemListener() {
        		public void itemStateChanged(ItemEvent e) {
        			updateFilter((String)filterSelector.getSelectedItem());
        		}
        	});
        	filterPanel.add(filterSelector);

        	c.weightx = 0.0;
        	c.gridwidth = GridBagConstraints.REMAINDER;
        	layout.setConstraints(filterPanel, c);
        	infoPanel.add(filterPanel);
        }
        
        add(infoPanel, BorderLayout.NORTH);
        
        JScrollPane tableScrollPane = new ExtendedJScrollPane(dataTable);
        add(tableScrollPane, BorderLayout.CENTER);
        
        setExampleSet(exampleSet);
    }

    public void setExampleSet(ExampleSet exampleSet) {
        dataTable.setExampleSet(exampleSet);
    }
    
    private void updateFilter(String conditionName) {
        ExampleSet filteredExampleSet = originalExampleSet;
        try {
            Condition condition = ConditionedExampleSet.createCondition(conditionName, originalExampleSet, null);
            filteredExampleSet = new ConditionedExampleSet(originalExampleSet, condition);
        } catch (ConditionCreationException ex) {
            originalExampleSet.getLog().logError("Cannot create condition '" + conditionName + "' for filtered data view: " + ex.getMessage() + ". Using original data set view...");
            filteredExampleSet = originalExampleSet;
        }
        updateFilterCounter(filteredExampleSet);
        setExampleSet(filteredExampleSet);
    }
    
    private void updateFilterCounter(ExampleSet filteredExampleSet) {
        filterCounter.setText("(" + filteredExampleSet.size() + " / " + originalExampleSet.size() + "): ");        
    }

	public String getCell(int row, int column) {
		return dataTable.getCell(row, column);
	}

	public int getColumnNumber() {
		return dataTable.getColumnNumber();
	}

	public int getRowNumber() {
		return dataTable.getRowNumber();
	}
}
