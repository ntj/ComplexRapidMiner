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

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.Tools;

/**
 * A tree is a node in a tree model containing several edges to other
 * trees (children) combined with conditions at these edges.
 * 
 * Leafs contain the class label which should be predicted.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: Tree.java,v 1.9 2008/05/09 19:22:53 ingomierswa Exp $
 */
public class Tree implements Serializable {
	
    private static final long serialVersionUID = -5930873649086170840L;

	private String label = null;
    
    private List<Edge> children = new LinkedList<Edge>();
    
    private Map<String,Integer> counterMap = new LinkedHashMap<String, Integer>();
    
    private transient ExampleSet trainingSet = null;
    
    public Tree(ExampleSet trainingSet) {
        this.trainingSet = trainingSet;
    }
    
    public ExampleSet getTrainingSet() {
        return this.trainingSet;
    }
    
    public void addCount(String className, int count) {
        counterMap.put(className, count);
    }
    
    public int getCount(String className) {
        Integer count = counterMap.get(className);
        if (count == null)
            return 0;
        else
            return count;
    }
    
    public int getFrequencySum() {
        int sum = 0;
        for (Integer i : counterMap.values()) {
            sum += i;
        }
        return sum;
    }
    
    public Map<String, Integer> getCounterMap() {
    	return counterMap;
    }
    
    public void setLeaf(String label) {
        this.label = label;
    }
    
    public void addChild(Tree child, SplitCondition condition) {
        this.children.add(new Edge(child, condition));
    }
    
    public void removeChildren() {
        this.children.clear();
    }
    
    public boolean isLeaf() {
        return children.size() == 0;
    }
    
    public String getLabel() { 
        return this.label;
    }
    
    public Iterator<Edge> childIterator() {
        return children.iterator();
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        toString(null, this, "", buffer);
        return buffer.toString();
    }
    
    private void toString(SplitCondition condition, Tree tree, String indent, StringBuffer buffer) {
        if (condition != null) {
            buffer.append(condition.toString());
        }
        if (!tree.isLeaf()) {
            Iterator<Edge> childIterator = tree.childIterator();
            while (childIterator.hasNext()) {
                buffer.append(Tools.getLineSeparator());
                buffer.append(indent);
                Edge edge = childIterator.next();
                toString(edge.getCondition(), edge.getChild(), indent + "|   ", buffer);
            }
        } else {
            buffer.append(": ");
            buffer.append(tree.getLabel());
            buffer.append(" " + tree.counterMap.toString());
        }
    }
}
