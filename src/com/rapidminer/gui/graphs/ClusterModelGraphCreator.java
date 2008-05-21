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
package com.rapidminer.gui.graphs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections15.Factory;

import com.rapidminer.operator.learner.clustering.ClusterNode;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.HierarchicalClusterModel;
import com.rapidminer.operator.learner.clustering.SimpleHierarchicalClusterModel;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseTree;

/**
 * The graph model creator for cluster models.
 * 
 * @author Ingo Mierswa
 * @version $Id: ClusterModelGraphCreator.java,v 1.8 2008/05/09 19:23:24 ingomierswa Exp $
 */
public class ClusterModelGraphCreator extends GraphCreatorAdaptor {

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
	
	
	private HierarchicalClusterModel clusterModel;
	
	private Map<String,ClusterNode> vertexMap = new HashMap<String, ClusterNode>();	
	
    private ClusterModelObjectViewer objectViewer = new ClusterModelObjectViewer();
    
    
	public ClusterModelGraphCreator(FlatClusterModel clusterModel) {
		this(new SimpleHierarchicalClusterModel(clusterModel));
	}

	public ClusterModelGraphCreator(HierarchicalClusterModel clusterModel) {
		this.clusterModel = clusterModel;
	}

	public Graph<String, String> createGraph() {
		SparseTree<String,String> graph = new SparseTree<String,String>();
		if (clusterModel.getRootNode() == null)
			return graph;
		
		ClusterNode root = clusterModel.getRootNode();
		graph.addVertex("Root");
		vertexMap.put("Root", root);
		
		Iterator<ClusterNode> it = clusterModel.getRootNode().getSubNodes();
		while (it.hasNext())
			createGraph(graph, "Root", it.next());

		return graph;
	}
	
	private void createGraph(Graph<String,String> graph, String parentName, ClusterNode node) {
		String childName = vertexFactory.create();
		vertexMap.put(childName, node);
		graph.addEdge(edgeFactory.create(), parentName, childName);
		Iterator it = node.getSubNodes();
		while (it.hasNext())
			createGraph(graph, childName, (ClusterNode) it.next());
	}

	public String getEdgeName(String id) {
		return null;
	}

	public String getVertexName(String id) {
		ClusterNode node = vertexMap.get(id);
		String name = "";
		if (node != null) {
			name = node.getId();
		}
		return name;
	}

	public String getVertexToolTip(String id) {
		ClusterNode node = vertexMap.get(id);
		String tip = "";
		if (node != null) {
			tip = "<html><b>Id:</b>&nbsp;" + node.getId() + "<br><b>Description:</b>&nbsp;" + node.getDescription();
		}
		return tip;
	}

    public Object getObject(String id) {
        return vertexMap.get(id);
    }

    public GraphObjectViewer getObjectViewer() {
        return objectViewer;
    }
}
