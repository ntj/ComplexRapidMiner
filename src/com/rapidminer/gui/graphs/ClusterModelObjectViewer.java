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

import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.rapidminer.ObjectVisualizer;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.operator.learner.clustering.ClusterNode;
import com.rapidminer.tools.ObjectVisualizerService;

/**
 * The graph object viewer for cluster nodes in a cluster model.
 *  
 * @author Ingo Mierswa
 * @version $Id: ClusterModelObjectViewer.java,v 1.2 2007/06/19 22:23:12 ingomierswa Exp $
 */
public class ClusterModelObjectViewer implements GraphObjectViewer, ListSelectionListener {

    private static final long serialVersionUID = -1849689229737482745L;
    
    private DefaultListModel model = new DefaultListModel();
            
    public JComponent getViewerComponent() {
        JList listComponent = new JList(this.model);
        listComponent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listComponent.addListSelectionListener(this);
        listComponent.setVisibleRowCount(-1);
        return new ExtendedJScrollPane(listComponent);
    }

    public void showObject(Object object) {
        this.model.removeAllElements();
        if (object != null) {
            ClusterNode node = (ClusterNode)object;
            Iterator<String> i = node.getObjectsInSubtree();
            while (i.hasNext())
                this.model.addElement(i.next());
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int index = e.getFirstIndex();
            if (index >= 0) {
                String id = (String)model.get(index);
                if (id != null) {
                    ObjectVisualizer visualizer = ObjectVisualizerService.getVisualizerForObject(id);
                    visualizer.startVisualization(id);
                }
            }
        }
    }
}
