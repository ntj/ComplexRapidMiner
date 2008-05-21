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
package com.rapidminer.operator.visualization;

import java.awt.Component;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableExampleSetAdapter;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.plotter.Plotter;
import com.rapidminer.gui.plotter.PlotterPanel;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ResultObjectAdapter;


/**
 * This class provides an operator for the visualization of arbitrary models with
 * help of the dimensionality reduction via a SOM of both the data set and the 
 * given model.
 * 
 * @author Sebastian Land
 * @version $Id: SOMModelVisualization.java,v 1.5 2008/05/09 19:23:15 ingomierswa Exp $
 */
public class SOMModelVisualization extends Operator {
	
	private static class ModelVisualization extends ResultObjectAdapter {
        
		private static final long serialVersionUID = -6250201023324000922L;
		
		private ExampleSet exampleSet;
		private Model model;
        
		public ModelVisualization(ExampleSet exampleSet, Model model) {
			this.exampleSet = exampleSet;
			this.model = model;
		}
        
		public Component getVisualizationComponent(IOContainer container) {
			DataTable table = new DataTableExampleSetAdapter(exampleSet, null);
			PlotterPanel panel = new PlotterPanel(table, PlotterPanel.MODEL_PLOTTER_SELECTION);
			Plotter plotter = panel.getSelectedPlotter();
			plotter.setDataTable(table);
			((SOMModelPlotter)plotter).setExampleSet(exampleSet);
			((SOMModelPlotter)plotter).setModel(model);
			return panel;
		}
        
		public String getName() {
			return "ModelVisualization";
		}
        
        public boolean isSavable() { return false; }
        
        public String getExtension() { return "mvs"; }
        
        public String getFileDescription() { return "model visualization"; }
	}
    
    
	public SOMModelVisualization(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		Model model = getInput(Model.class);
		return new IOObject[] { exampleSet, model, new ModelVisualization(exampleSet, model) };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class, Model.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class, Model.class };
	}	
}
