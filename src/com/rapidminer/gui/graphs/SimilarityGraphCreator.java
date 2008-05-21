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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.Factory;

import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.tools.Tools;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * The graph model creator for similarity measurements.
 *
 * @author Ingo Mierswa
 * @version $Id: SimilarityGraphCreator.java,v 1.14 2008/05/09 19:23:24 ingomierswa Exp $
 */
public class SimilarityGraphCreator extends GraphCreatorAdaptor {

    private static class SortableEdge implements Comparable<SortableEdge> {
        
        public static final int DIRECTION_DISTANCE   = 1;
        public static final int DIRECTION_SIMILARITY = -1;
        
        private String vertex1;
        private String vertex2;
        private double similarity;
        private int direction;
        
        public SortableEdge(String v1, String v2, double sim, int direction) {
            this.vertex1 = v1;
            this.vertex2 = v2;
            this.similarity = sim;
            this.direction = direction;
        }
        
        public int hashCode() { return Double.valueOf(this.similarity).hashCode(); }
        
        public boolean equals(Object o) {
        	if (!(o instanceof SortableEdge)) 
        		return false;
        	return ((SortableEdge)o).similarity == this.similarity;
        }
        
        public int compareTo(SortableEdge o) {
            return direction * Double.compare(this.similarity, o.similarity);
        }
        
        public double getSimilarity() { return this.similarity; }
        public String getFirstVertex() { return this.vertex1; }
        public String getSecondVertex() { return this.vertex2; }
        
    }
    
	private Factory<String> edgeFactory = new Factory<String>() {
		int i = 0;
		public String create() {
			return "E" + i++;
		}
	};
	
    private JSlider distanceSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 1000, 100) {
        private static final long serialVersionUID = -6931545310805789589L;
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
    
    private Graph<String,String> graph;
    
    private List<String> idList = new ArrayList<String>();
    
	private SimilarityMeasure sim;
    
    private Map<String, String> edgeLabelMap = new HashMap<String, String>();

    private SimilarityObjectViewer objectViewer = new SimilarityObjectViewer();
    
    
	public SimilarityGraphCreator(SimilarityMeasure sim) {
		this.sim = sim;
	}

	public Graph<String,String> createGraph() {
		graph = new UndirectedSparseGraph<String, String>();
			
		Iterator<String> it = sim.getIds();
		while (it.hasNext()) {
			idList.add(it.next());
		}

		for (int i = 0; i < idList.size(); i++) {
			String id = idList.get(i);
			graph.addVertex(id);
		}

		addEdges();
        
		return graph;
	}
    
	public String getEdgeName(String id) {
		return edgeLabelMap.get(id);
	}

	public String getVertexName(String id) {
		return id;
	}

	public String getVertexToolTip(String id) {
		return id;
	}
    
    /** Returns the label offset. In most case, using -1 is just fine (default offset). 
     *  Some tree like graphs might prefer to use 0 since they manage the offset themself. */
    public int getLabelOffset() {
        return -1;
    }
    
    public int getNumberOfOptionComponents() { 
        return 2;
    }
    
    public JComponent getOptionComponent(final GraphViewer viewer, int index) {
        if (index == 0) {
            return new JLabel("Number of Edges:");
        } else if (index == 1) {
            this.distanceSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (!distanceSlider.getValueIsAdjusting()) {
                        addEdges();
                        viewer.updateLayout();
                    }
                }
            });
            return distanceSlider;            
        } else {
            return null;
        }
    }
    
    private void addEdges() {
        Iterator<String> e = edgeLabelMap.keySet().iterator();
        while (e.hasNext()) {
            graph.removeEdge(e.next());
        }
        edgeLabelMap.clear();
    
        int direction = sim.isDistance() ? SortableEdge.DIRECTION_DISTANCE : SortableEdge.DIRECTION_SIMILARITY;
        List<SortableEdge> sortableEdges = new LinkedList<SortableEdge>();
        for (int i = 0; i < idList.size(); i++) {
            String x = idList.get(i);
            for (int j = i + 1; j < idList.size(); j++) {
                String y = idList.get(j);
                if (sim.isSimilarityDefined(x, y)) {
                    double simV = sim.similarity(x, y);
                    sortableEdges.add(new SortableEdge(x, y, simV, direction));
                }
            }
        }
        
        Collections.sort(sortableEdges);
        
        int numberOfEdges = distanceSlider.getValue();
        int counter = 0;
        for (SortableEdge sortableEdge : sortableEdges) {
            if (counter > numberOfEdges)
                break;
            String id = edgeFactory.create();
            graph.addEdge(id, sortableEdge.getFirstVertex(), sortableEdge.getSecondVertex(), EdgeType.UNDIRECTED);
            edgeLabelMap.put(id, Tools.formatIntegerIfPossible(sortableEdge.getSimilarity()));
            counter++;
        }
    }
    
    /** Returns false. */
    public boolean showEdgeLabelsDefault() {
        return false;
    }
    
    /** Returns false. */
    public boolean showVertexLabelsDefault() {
        return false;
    }
    
    public Object getObject(String id) {
        return id;
    }
    
    public GraphObjectViewer getObjectViewer() {
        return objectViewer;
    }
}

