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
package com.rapidminer.gui.renderer.models;

import java.awt.Component;

import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.gui.renderer.NonGraphicalRenderer;
import com.rapidminer.gui.viewer.ContainerModelViewer;
import com.rapidminer.operator.GroupedModel;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

/**
 * A renderer for container models.
 * 
 * @author Ingo Mierswa
 * @version $Id: ContainerModelRenderer.java,v 1.4 2008/07/19 16:31:17 ingomierswa Exp $
 */
public class ContainerModelRenderer extends NonGraphicalRenderer {

	public String getName() {
		return "Grouped Model";
	}

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		GroupedModel cm = (GroupedModel) renderable;
		return new ContainerModelViewer(cm, ioContainer);
	}

	public Reportable createReportable(Object renderable, IOContainer ioContainer) {
		return new DefaultReadable(renderable.toString());
	}
}
