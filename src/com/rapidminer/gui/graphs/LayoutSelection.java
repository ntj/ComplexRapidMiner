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

import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComboBox;

import com.rapidminer.tools.LogService;

import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Tree;

/**
 * The layout selection for the {@link GraphViewer}.
 *
 * @author Ingo Mierswa
 * @version $Id: LayoutSelection.java,v 1.5 2008/05/09 19:23:24 ingomierswa Exp $
 */
public class LayoutSelection<V,E> extends JComboBox {

    private static final long serialVersionUID = 8924517975475876102L;

    private GraphViewer<V, E> graphViewer;

    private transient Graph graph;

    private Map<String, Class> layoutMap = null;

    public LayoutSelection(GraphViewer<V, E> graphViewer, Graph graph) {
        super();
        this.graphViewer = graphViewer;
        this.graph = graph;

        layoutMap = new java.util.LinkedHashMap<String, Class>();

        if (graph instanceof Tree) {
            layoutMap.put("Tree", TreeLayout.class);
            layoutMap.put("Balloon", BalloonLayout.class);
        }

        layoutMap.put("KKLayout", KKLayout.class);
        layoutMap.put("FRLayout", FRLayout2.class);
        layoutMap.put("ISOM", ISOMLayout.class);
        layoutMap.put("Circle", CircleLayout.class);
        layoutMap.put("Spring", SpringLayout2.class);

        Iterator<String> it = layoutMap.keySet().iterator();
        while (it.hasNext())
            addItem(it.next());
        
        addActionListener(this);
    }
    
    public void actionPerformed(ActionEvent arg0) {
        setLayout();
    }
    
    @SuppressWarnings("unchecked")
    public void setLayout() {
        String layoutName = (String) getSelectedItem();
        Class<?> layoutClass = null;
        try {
            layoutClass = layoutMap.get(layoutName);
        } catch (Exception e) {
            LogService.getGlobal().logError("Layout could not be intialized: " + e.getMessage());
        }
        
        if (layoutClass != null) {
            try {
                Constructor constructor = null;
                if ((layoutClass == TreeLayout.class) || (layoutClass == BalloonLayout.class)) {
                    constructor = layoutClass.getConstructor(new Class[] { Forest.class });
                } else {
                    constructor = layoutClass.getConstructor(new Class[] { Graph.class });
                }

                Object o = constructor.newInstance(new Object[] { graph });
                Layout layout = (Layout) o;
                graphViewer.changeLayout(layout);
            } catch (Exception e) {
                e.printStackTrace();
            } 
        }
    }
}
