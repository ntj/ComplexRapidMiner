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
import java.util.LinkedHashMap;
import java.util.List;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.gui.plotter.Plotter;
import com.rapidminer.gui.plotter.PlotterPanel;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualStringCondition;
import com.rapidminer.report.Reportable;

/**
 * This is the abstract renderer superclass for all renderers which
 * should be a plotter based on a given {@link DataTable}.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractDataTablePlotterRenderer.java,v 1.6 2008/07/19 16:31:17 ingomierswa Exp $
 */
public abstract class AbstractDataTablePlotterRenderer extends AbstractRenderer {

	public static final String PARAMETER_PLOTTER = "plotter";
	
	public abstract DataTable getDataTable(Object renderable, IOContainer ioContainer);
	
	public String getName() {
		return "Plot View";
	}
	
	public LinkedHashMap<String,Class<? extends Plotter>> getPlotterSelection() {
		return PlotterPanel.COMPLETE_PLOTTER_SELECTION;
	}
	
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int width, int height) {
		DataTable dataTable = getDataTable(renderable, ioContainer);
		PlotterPanel plotterPanel = new PlotterPanel(dataTable);
		String plotterName = null;
		try {
			plotterName = getParameterAsString(PARAMETER_PLOTTER);
		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
		
		if (plotterName != null) {
			plotterPanel.setSelectedPlotter(plotterName);
			Plotter plotter = plotterPanel.getSelectedPlotter();
			plotter.stopUpdates(true);
			
			List<ParameterType> plotterParameters = plotter.getParameterTypes();
			if (plotterParameters != null) {
				for (ParameterType type : plotterParameters) {
					String key = type.getKey();
					Object value;
					try {
						value = getParameter(key);
						if (value != null)
							plotter.setParameter(dataTable, key, value);
					} catch (UndefinedParameterError e) {
						// only set defined parameters
					}
				}
			}
			plotter.getPlotter().setSize(width, height);
			plotter.stopUpdates(false);
		}
		
		return plotterPanel;
	}

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		return new PlotterPanel(getDataTable(renderable, ioContainer), getPlotterSelection());
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		
		String[] availablePlotterNames = getPlotterSelection().keySet().toArray(new String[getPlotterSelection().size()]);
		ParameterTypeStringCategory plotterType = null;
		if (availablePlotterNames.length == 0)
			plotterType = new ParameterTypeStringCategory(PARAMETER_PLOTTER, "Indicates the type of the plotter which should be used.", availablePlotterNames, "dummy");
		else
			plotterType = new ParameterTypeStringCategory(PARAMETER_PLOTTER, "Indicates the type of the plotter which should be used.", availablePlotterNames, availablePlotterNames[0]);
		plotterType.setEditable(false);
		types.add(plotterType);
		
		for (String plotterName : getPlotterSelection().keySet()) {
			Class clazz = getPlotterSelection().get(plotterName);
			try {
				Plotter plotter = (Plotter)clazz.newInstance();
				
				List<ParameterType> plotterParameters = plotter.getParameterTypes();
				if (plotterParameters != null) {
					for (ParameterType type : plotterParameters) {
						type.registerDependencyCondition(new EqualStringCondition(this, PARAMETER_PLOTTER, false, plotterName));
						type.setHidden(false);
						type.setExpert(false);
						types.add(type);
					}
				}
			} catch (InstantiationException e) {
				// do nothing
			} catch (IllegalAccessException e) {
				// do nothing
			}
		}
		
		return types;
	}
}
