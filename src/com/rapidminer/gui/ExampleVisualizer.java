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
package com.rapidminer.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.rapidminer.ObjectVisualizer;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJTable;


/**
 * A visualizer which shows the attribute values of an example. This is the most
 * simple visualizer which should work in all cases.
 * 
 * @author Ingo Mierswa
 * @version $Id: ExampleVisualizer.java,v 2.11 2006/03/21 15:35:40 ingomierswa
 *          Exp $
 */
public class ExampleVisualizer implements ObjectVisualizer {

	private ExampleSet exampleSet;

	private Attribute idAttribute;
	
	public ExampleVisualizer(ExampleSet exampleSet) {
		this.exampleSet = exampleSet;
		this.exampleSet.remapIds();
		this.idAttribute = exampleSet.getAttributes().getId();
	}

	public void startVisualization(String objId) {
		final JDialog dialog = new JDialog(RapidMinerGUI.getMainFrame(), "Example: " + objId, false);
		dialog.getContentPane().setLayout(new BorderLayout());
		double idValue = idAttribute.isNominal() ? idAttribute.getMapping().mapString(objId) : Double.parseDouble(objId);
		Example example = exampleSet.getExampleFromId(idValue);

        if (example != null) {
            String[] columnNames = new String[] { "Attribute", "Value" };
            String[][] data = new String[example.getAttributes().allSize()][2];
            Iterator<Attribute> a = example.getAttributes().allAttributes();
            int counter = 0;
            while (a.hasNext()) {
                Attribute attribute = a.next();
                data[counter][0] = attribute.getName();
                data[counter][1] = example.getValueAsString(attribute);
                counter++;
            }            
            JTable table = new ExtendedJTable();
            table.setDefaultEditor(Object.class, null);
            TableModel tableModel = new DefaultTableModel(data, columnNames);
            table.setModel(tableModel);
            JScrollPane scrollPane = new ExtendedJScrollPane(table);
            dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
        } else {
            JLabel noInfoLabel = new JLabel("No information available for object '" + objId + "'.");
            dialog.getContentPane().add(noInfoLabel, BorderLayout.CENTER);
        }

		JPanel buttons = new JPanel(new FlowLayout());
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    dialog.dispose();
			}
		});
		buttons.add(okButton);
		dialog.getContentPane().add(buttons, BorderLayout.SOUTH);
		dialog.pack();
		dialog.setLocationRelativeTo(RapidMinerGUI.getMainFrame());
		dialog.setVisible(true);
	}
    
    /** Does nothing. */
	public void stopVisualization(String objId) {}

	public String getTitle(String objId) {
		return objId;
	}

	public boolean isCapableToVisualize(String id) {
		double idValue = idAttribute.isNominal() ? idAttribute.getMapping().mapString(id) : Double.parseDouble(id);
		return exampleSet.getExampleFromId(idValue) != null;
	}
}
