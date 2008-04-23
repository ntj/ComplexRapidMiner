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
package com.rapidminer.gui.graphs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections15.Factory;

import com.rapidminer.operator.learner.tree.Edge;
import com.rapidminer.operator.learner.tree.SplitCondition;
import com.rapidminer.operator.learner.tree.Tree;
import com.rapidminer.operator.learner.tree.TreeModel;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseTree;
import edu.uci.ics.jung.visualization.renderers.Renderer.EdgeLabel;
import edu.uci.ics.jung.visualization.renderers.Renderer.Vertex;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel;

/**
 * Creates a graph model for a learned tree model.
 *
 * @author Ingo Mierswa
 * @version $Id: TreeModelGraphCreator.java,v 1.10 2007/07/13 22:52:15 ingomierswa Exp $
 */
public class TreeModelGraphCreator extends GraphCreatorAdaptor {

	private Factory<String> edgeFactory = new Factory<String>() {
		int i = 0;
		public String create() {
			return "E" + i++;
		}
	};
	
	private Factory<String> vertexFactory = new Factory<String>() {
		int i = 0;
		public String create() {
			return "V" + i++;
		}
	};

	private TreeModel model;
	
	private Map<String,Tree> vertexMap = new HashMap<String, Tree>();

	private Map<String,SplitCondition> edgeMap = new HashMap<String, SplitCondition>();
	
	public TreeModelGraphCreator(TreeModel model) {
		this.model = model;	
	}
	
    public Tree getTree(String id) {
        return vertexMap.get(id);    
    }
    
	public String getVertexName(String object) {
		Tree node = vertexMap.get(object);
		String name = "";
		if (node != null) {
			if (node.isLeaf()) {
				name = node.getLabel();
			} else {
				Iterator<Edge> e = node.childIterator();
				while (e.hasNext()) {
					SplitCondition condition = e.next().getCondition();
					name = condition.getAttributeName();
					break;
				}
			}
		}
		return name;
	}
	
	public String getVertexToolTip(String object) {
		Tree tree = vertexMap.get(object);
		if (tree != null) {
			StringBuffer result = new StringBuffer();
			if (tree.isLeaf()) {
			    String labelString = tree.getLabel();
			    if (labelString != null) {
			        result.append("<html><b>Class:</b>&nbsp;" + labelString + "<br>");
			        result.append("<b>Size:</b>&nbsp;" + tree.getFrequencySum() + "<br>");
			        result.append("<b>Class Frequencies:</b>&nbsp;" + tree.getCounterMap().toString() + "</html>");
			    }
            } else {
                result.append("Inner Node");
            }
			return result.toString();
		} else {
			return null;
		}
	}
	
	public String getEdgeName(String object) {
		SplitCondition condition = edgeMap.get(object);
		if (condition != null) {
			return condition.getRelation() + " " + condition.getValueString();
		} else {
			return null;
		}
	}
	
	public boolean isLeaf(String object) {
		Tree tree = vertexMap.get(object);
		if (tree != null) {
			return tree.isLeaf();
		} else {
			return false;
		}
	}

	public Graph<String, String> createGraph() {
		SparseTree<String, String> treeGraph = new SparseTree<String, String>();
		Tree root = this.model.getRoot();
		treeGraph.addVertex("Root");
		vertexMap.put("Root", root);
		addTree(treeGraph, root, "Root");
		return treeGraph;
	}
	
	private void addTree(SparseTree<String, String> treeGraph, Tree node, String parentName) {
		Iterator<Edge> e = node.childIterator();
		while (e.hasNext()) {
			Edge edge = e.next();
			Tree child = edge.getChild();
			SplitCondition condition = edge.getCondition();
			String childName = vertexFactory.create();
			String edgeName = edgeFactory.create();
			vertexMap.put(childName, child);
			edgeMap.put(edgeName, condition);
	       	treeGraph.addEdge(edgeName, parentName, childName);
	       	addTree(treeGraph, child, childName);
		}
	}

	public Vertex<String, String> getVertexRenderer() {
		return new TreeModelNodeRenderer<String,String>(this);
	}

	public EdgeLabel<String, String> getEdgeLabelRenderer() {
		return new TreeModelEdgeLabelRenderer<String,String>();
	}

    public VertexLabel<String, String> getVertexLabelRenderer() {
        return new TreeModelNodeLabelRenderer<String, String>(this);
    }
    
    public boolean isEdgeLabelDecorating() {
        return true;
    }
    
    public int getMinLeafHeight() {
        return 26;
    }
    
    public int getMinLeafWidth() {
        return 40;
    }

    public boolean isBold(String id) {
        return isLeaf(id);
    }

    public boolean isRotatingEdgeLabels() {
        return false;
    }

    public Object getObject(String id) {
        return vertexMap.get(id);
    }

    /** Returns 0 (for other values the edge label painting will not work). */
    public int getLabelOffset() {
        return 0;
    }
}