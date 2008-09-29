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
package com.rapidminer.gui.renderer;

import java.awt.Component;
import java.util.List;

import javax.swing.JLabel;

import com.rapidminer.gui.graphs.GraphCreator;
import com.rapidminer.gui.graphs.GraphViewer;
import com.rapidminer.gui.graphs.LayoutSelection;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.report.Reportable;

/**
 * This is the abstract renderer superclass for all renderers which
 * should be a graph based on a given {@link GraphCreator}.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractGraphRenderer.java,v 1.3 2008/07/18 15:50:46 ingomierswa Exp $
 */
public abstract class AbstractGraphRenderer extends AbstractRenderer {
	
	public static final String PARAMETER_LAYOUT           = "layout";
	
	public static final String PARAMETER_SHOW_NODE_LABELS = "show_node_labels";
	
	public static final String PARAMETER_SHOW_EDGE_LABELS = "show_edge_labels";
	
	public static final String[] LAYOUTS = {
		"ISOM",
		"KKLayout",
		"FRLayout",
		"Circle",
		"Spring",
		"Tree",
		"Balloon"
	};
	
	public abstract GraphCreator<String, String> getGraphCreator(Object renderable, IOContainer ioContainer);
	
	public String getName() {
		return "Graph View";
	}
	
	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		GraphCreator<String, String> graphCreator = getGraphCreator(renderable, ioContainer);
		if (graphCreator != null) {
			return new GraphViewer<String,String>(graphCreator);
		} else {
			return new JLabel("no graph visualization supported.");
		}
	}

	public Reportable createReportable(Object renderable, IOContainer ioContainer, int width, int height) {
		GraphCreator<String, String> graphCreator = getGraphCreator(renderable, ioContainer);
		if (graphCreator != null) {
			GraphViewer<String, String> viewer = new GraphViewer<String,String>(graphCreator);
			
			LayoutSelection<String, String> layoutSelection = viewer.getLayoutSelection();
			try {
				layoutSelection.setSelectedItem(getParameter(PARAMETER_LAYOUT));
			} catch (UndefinedParameterError e) {
				// do nothing
			}
			
			
			try {
				// necessary to give layout (thread!) time to finish its work before reporting
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// do nothing
			}
			
			viewer.setPaintEdgeLabels(getParameterAsBoolean(PARAMETER_SHOW_EDGE_LABELS));
			viewer.setPaintVertexLabels(getParameterAsBoolean(PARAMETER_SHOW_NODE_LABELS));
			
			return viewer;
		} else {
			return null;
		}
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		ParameterTypeStringCategory layoutType = new ParameterTypeStringCategory(PARAMETER_LAYOUT, "Indicates which layout should be used for graph rendering.", LAYOUTS, LAYOUTS[0]);
		layoutType.setEditable(false);
		types.add(layoutType);
		
		types.add(new ParameterTypeBoolean(PARAMETER_SHOW_NODE_LABELS, "Indicates if the labels of the node should be visualized.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_SHOW_EDGE_LABELS, "Indicates if the labels of the edges should be visualized.", true));
		
		return types;
	}
}
