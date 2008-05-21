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
package com.rapidminer.operator.learner.clustering;

import java.awt.Component;
import java.awt.Graphics;
import java.util.Iterator;

import javax.swing.Icon;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.viewer.ClusterModelVisualization;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.Renderable;
import com.rapidminer.tools.StringProperties;
import com.rapidminer.tools.Tools;


/**
 * An abstract implementation of the most basic cluster model features.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: AbstractClusterModel.java,v 1.10 2008/05/09 19:23:10 ingomierswa Exp $
 */
public abstract class AbstractClusterModel extends ResultObjectAdapter implements ClusterModel, Renderable {

	private static final String RESULT_ICON_NAME = "lightbulb_on.png";
	
	private static Icon resultIcon = null;
	
	private Renderable renderer;
	
	static {
		resultIcon = SwingTools.createIcon("16/" + RESULT_ICON_NAME);
	}
	
	private StringProperties properties;

	public AbstractClusterModel() {
		properties = new StringProperties();
	}

	public AbstractClusterModel(ClusterModel cm) {
		properties = new StringProperties();
		Iterator it = cm.getAllPropertyKeys();
		while (it.hasNext()) {
			String key = (String) it.next();
			properties.set(key, cm.getProperty(key));
		}
	}

	public String getExtension() {
		return "clm";
	}

	public String getFileDescription() {
		return "cluster model";
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}

	public void setProperty(String key, Object val) {
		properties.set(key, val);
	}

	public Iterator getAllPropertyKeys() {
		return properties.getKeys();
	}

	public String getName() {
		return "ClusterModel";
	}

	public String toString() {
		StringBuffer s = new StringBuffer("A cluster model with the following properties:");
		s.append(Tools.getLineSeparator());
		if (properties.getKeys().hasNext())
			s.append(Tools.getLineSeparator() + properties.toString());
		return s.toString();
	}

	public Component getVisualizationComponent(IOContainer container) {
		ClusterModelVisualization clusterModelVisualization = new ClusterModelVisualization(this, super.getVisualizationComponent(container));
		renderer = clusterModelVisualization;
		return clusterModelVisualization;
	}
	
	public Icon getResultIcon() {
		return resultIcon;
	}
	
	public int getRenderWidth(int preferredWidth) {
		return renderer.getRenderWidth(preferredWidth);
	}
	
	public int getRenderHeight(int preferredHeight) {
		return renderer.getRenderHeight(preferredHeight);
	}
	
	public void render(Graphics graphic, int width, int height) {
		renderer.render(graphic, width, height);
	}
	
}
