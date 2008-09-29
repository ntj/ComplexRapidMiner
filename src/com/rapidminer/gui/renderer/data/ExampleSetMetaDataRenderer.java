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
package com.rapidminer.gui.renderer.data;

import java.awt.Component;

import javax.swing.table.TableModel;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.renderer.AbstractTableModelTableRenderer;
import com.rapidminer.gui.viewer.MetaDataViewer;
import com.rapidminer.gui.viewer.MetaDataViewerTableModel;
import com.rapidminer.operator.IOContainer;

/**
 * A renderer for the meta data view of example sets.
 * 
 * @author Ingo Mierswa
 * @version $Id: ExampleSetMetaDataRenderer.java,v 1.3 2008/07/19 16:31:17 ingomierswa Exp $
 */
public class ExampleSetMetaDataRenderer extends AbstractTableModelTableRenderer {

	public String getName() {
		return "Meta Data View";
	}
	
	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		ExampleSet exampleSet = (ExampleSet)renderable;
		return new MetaDataViewer(exampleSet, true);
	}

	public TableModel getTableModel(Object renderable, IOContainer ioContainer) {
		ExampleSet exampleSet = (ExampleSet)renderable;
		return new MetaDataViewerTableModel(exampleSet);
	}
}
