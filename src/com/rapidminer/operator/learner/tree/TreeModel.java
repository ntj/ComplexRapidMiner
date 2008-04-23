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
package com.rapidminer.operator.learner.tree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.graphs.GraphViewer;
import com.rapidminer.gui.graphs.TreeModelGraphCreator;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.SimplePredictionModel;

/**
 * The tree model is the model created by all decision trees.
 * 
 * @author Sebastian Land
 * @version $Id: TreeModel.java,v 1.4 2007/07/13 22:52:14 ingomierswa Exp $
 */
public class TreeModel extends SimplePredictionModel {

    private static final long serialVersionUID = 4368631725370998591L;
    
    private Tree root;

	public TreeModel(ExampleSet exampleSet, Tree root) {
		super(exampleSet);
		this.root = root;
	}

	public Tree getRoot() {
		return this.root;
	}
	
	public double predict(Example example) throws OperatorException {
		return predict(example, root);
	}

	private double predict(Example example, Tree node) {
        if (node.isLeaf()) {
            Iterator<String> s = node.getCounterMap().keySet().iterator();
            int[] counts = new int[getLabel().getMapping().size()];
            int sum = 0;
            while (s.hasNext()) {
                String className = s.next();
                int count = node.getCount(className);
                int index = getLabel().getMapping().getIndex(className);
                counts[index] = count;
                sum += count;
            }
            for (int i = 0; i < counts.length; i++) {
                example.setConfidence(getLabel().getMapping().mapIndex(i), ((double) counts[i]) / sum);
            }
            return getLabel().getMapping().getIndex(node.getLabel());
        } else {
            Iterator<Edge> childIterator = node.childIterator();
            while (childIterator.hasNext()) {
                Edge edge = childIterator.next();
                SplitCondition condition = edge.getCondition();
                if (condition.test(example)) {
                    return (predict(example, edge.getChild()));
                }
            }
            
            // use majority class
            String majorityClass = null;
            int majorityCounter = -1;
            Iterator<String> s = node.getCounterMap().keySet().iterator();
            int[] counts = new int[getLabel().getMapping().size()];
            int sum = 0;
            while (s.hasNext()) {
                String className = s.next();
                int count = node.getCount(className);
                int index = getLabel().getMapping().getIndex(className);
                counts[index] = count;
                sum += count;
                if (count > majorityCounter) {
                    majorityCounter = count;
                    majorityClass = className;
                }
            }
            for (int i = 0; i < counts.length; i++) {
                example.setConfidence(getLabel().getMapping().mapIndex(i), ((double) counts[i]) / sum);
            }
            if (majorityClass != null)
                return getLabel().getMapping().getIndex(majorityClass);
            else
                return 0;
        }
	}
    
    public String toString() {
        return this.root.toString();
    }
    
	public Component getVisualizationComponent(IOContainer container) {
        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        final Component graphView = new GraphViewer<String,String>(new TreeModelGraphCreator(this));
		
        final JRadioButton graphViewButton = new JRadioButton("Graph View", true);
        graphViewButton.setToolTipText("Changes to a graphical view of this model.");
        graphViewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (graphViewButton.isSelected()) {
                    mainPanel.remove(1);
                    mainPanel.add(graphView, BorderLayout.CENTER);
                    mainPanel.repaint();
                }
            }
        });

        final Component textView = super.getVisualizationComponent(container);
        final JRadioButton textViewButton = new JRadioButton("Text View", true);
        textViewButton.setToolTipText("Changes to a textual view of this model.");
        textViewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (textViewButton.isSelected()) {
                    mainPanel.remove(1);
                    mainPanel.add(textView, BorderLayout.CENTER);
                    mainPanel.repaint();
                }
            }
        });
        
        ButtonGroup group = new ButtonGroup();
        group.add(textViewButton);
        group.add(graphViewButton);
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        togglePanel.add(textViewButton);
        togglePanel.add(graphViewButton);

        mainPanel.add(togglePanel, BorderLayout.NORTH);
        mainPanel.add(graphView, BorderLayout.CENTER);
        graphViewButton.setSelected(true);
        
        return mainPanel;
	}
}
