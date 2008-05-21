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

/**
 * This object viewer can be used by the {@link GraphViewer}. A new object will be shown after
 * clicking on a node.
 * 
 * @author Ingo Mierswa
 * @version $Id: GraphObjectViewer.java,v 1.3 2008/05/09 19:23:24 ingomierswa Exp $
 */
public interface GraphObjectViewer {
    
    /** Returns the component which should be used for showing information about 
     *  the currently selected object. */
    public JComponent getViewerComponent();
   
    public void showObject(Object object);
    
}
