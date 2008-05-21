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
package com.rapidminer.tools;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.ObjectVisualizer;
import com.rapidminer.gui.DummyObjectVisualizer;


/** 
 * This class provides the management of {@link ObjectVisualizer}s. 
 * 
 * @author Ingo Mierswa
 * @version $Id: ObjectVisualizerService.java,v 1.3 2008/05/09 19:22:55 ingomierswa Exp $
 */
public class ObjectVisualizerService {

	private static final DummyObjectVisualizer DUMMY_VISUALIZER = new DummyObjectVisualizer();
	
	private static List<ObjectVisualizer> objectVisualizers = new LinkedList<ObjectVisualizer>();
	
	public static void addObjectVisualizer(ObjectVisualizer visualizer) {
		objectVisualizers.add(visualizer);
	}

	public static void removeObjectVisualizer(ObjectVisualizer visualizer) {
		objectVisualizers.remove(visualizer);
	}

	/**
	 * Returns the last object visualizer which is capable to visualize the
	 * object with the given id.
	 */
	public static ObjectVisualizer getVisualizerForObject(String id) {
		Iterator i = objectVisualizers.iterator();
		ObjectVisualizer capableVisualizer = DUMMY_VISUALIZER;
		while (i.hasNext()) {
			ObjectVisualizer visualizer = (ObjectVisualizer) i.next();
			if (visualizer.isCapableToVisualize(id))
				capableVisualizer = visualizer;
		}
		return capableVisualizer;
	}
    
    public static void clearVisualizers() {
        objectVisualizers.clear();
    }
}
