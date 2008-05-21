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
package com.rapidminer.operator.learner.tree;

import java.awt.Component;
import java.awt.Graphics;
import java.util.Iterator;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.JRadioSelectionPanel;
import com.rapidminer.gui.graphs.GraphViewer;
import com.rapidminer.gui.graphs.TreeModelGraphCreator;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.SimplePredictionModel;
import com.rapidminer.tools.Renderable;

/**
 * The tree model is the model created by all decision trees.
 * 
 * @author Sebastian Land
 * @version $Id: TreeModel.java,v 1.8 2008/05/09 19:22:53 ingomierswa Exp $
 */
public class TreeModel extends SimplePredictionModel implements Renderable{

    private static final long serialVersionUID = 4368631725370998591L;
    
    private Tree root;

    private Renderable renderer;
    
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
        final JRadioSelectionPanel mainPanel = new JRadioSelectionPanel();
        
        GraphViewer graphViewer = new GraphViewer<String,String>(new TreeModelGraphCreator(this));
        renderer = graphViewer;
        final Component textView = super.getVisualizationComponent(container);
        mainPanel.addComponent("Graph View", graphViewer, "Changes to a graphical view of this model.");
        mainPanel.addComponent("Text View", textView, "Changes to a textual view of this model.");
        return mainPanel;
	}

	public int getRenderHeight(int preferredHeight) {
		return renderer.getRenderHeight(preferredHeight);
	}

	public int getRenderWidth(int preferredWidth) {
		return renderer.getRenderWidth(preferredWidth);
	}

	public void render(Graphics graphics, int width, int height) {
		renderer.render(graphics, width, height);
	}
}
