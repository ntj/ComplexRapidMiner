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

import javax.swing.JComponent;

import com.rapidminer.ObjectVisualizer;
import com.rapidminer.tools.ObjectVisualizerService;

/**
 * The graph object viewer for nodes in a similarity graph.
 *  
 * @author Ingo Mierswa
 * @version $Id: SimilarityObjectViewer.java,v 1.1 2007/07/06 21:15:16 ingomierswa Exp $
 */
public class SimilarityObjectViewer implements GraphObjectViewer {  
            
    public JComponent getViewerComponent() {
    	return null;
    }

    public void showObject(Object object) {
        if (object != null) {
            String id = (String)object;
            ObjectVisualizer visualizer = ObjectVisualizerService.getVisualizerForObject(id);
            visualizer.startVisualization(id);
        }
    }
}
