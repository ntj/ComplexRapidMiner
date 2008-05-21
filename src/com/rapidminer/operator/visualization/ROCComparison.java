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
import java.awt.Graphics;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidminer.gui.viewer.ROCChartPlotter;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.Renderable;
import com.rapidminer.tools.math.ROCData;

/**
 * This object can usually not be passed to other operators but can simply be
 * used for the inline visualization of a ROC comparison plot (without a dialog).
 * 
 * @author Ingo Mierswa
 * @version $Id: ROCComparison.java,v 1.2 2008/05/09 19:23:14 ingomierswa Exp $
 */
public class ROCComparison extends ResultObjectAdapter implements Renderable {

	private static final long serialVersionUID = 9181453276271041294L;
	
	private Map<String, List<ROCData>> rocData;
	
    private ROCChartPlotter plotter;
    
	public ROCComparison(Map<String, List<ROCData>> rocData) {
		this.rocData = rocData;
	}
	
	public String getExtension() {
		return "roc";
	}

	public String getFileDescription() {
		return "ROC comparison files";
	}

	public Component getVisualizationComponent(IOContainer container) {
		plotter = new ROCChartPlotter();
        Iterator<Map.Entry<String, List<ROCData>>> e = rocData.entrySet().iterator();
        while (e.hasNext()) {
        	Map.Entry<String, List<ROCData>> entry = e.next();
        	plotter.addROCData(entry.getKey(), entry.getValue());	
        }
        return plotter;
	}

	public int getRenderHeight(int preferredHeight) {
		return plotter.getRenderHeight(preferredHeight);
	}

	public int getRenderWidth(int preferredWidth) {
		return plotter.getRenderWidth(preferredWidth);
	}

	public void render(Graphics graphics, int width, int height) {
		plotter.render(graphics, width, height);
	}
}
