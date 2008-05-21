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

import javax.swing.JComponent;

import com.rapidminer.ObjectVisualizer;
import com.rapidminer.tools.ObjectVisualizerService;

/**
 * The graph object viewer for nodes in a similarity graph.
 *  
 * @author Ingo Mierswa
 * @version $Id: SimilarityObjectViewer.java,v 1.3 2008/05/09 19:23:24 ingomierswa Exp $
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
