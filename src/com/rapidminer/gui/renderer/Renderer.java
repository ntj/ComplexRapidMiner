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

import com.rapidminer.operator.IOContainer;
import com.rapidminer.parameter.ParameterHandler;
import com.rapidminer.parameter.Parameters;
import com.rapidminer.report.Reportable;

/**
 * This is the renderer interface. A renderer is a visualization component
 * for all types of objects. In addition, it should also deliver an object
 * of the interface {@link Reportable} in order to support automatic reporting 
 * actions.
 * 
 * @author Ingo Mierswa
 * @version $Id: Renderer.java,v 1.6 2008/07/18 15:50:45 ingomierswa Exp $
 */
public interface Renderer extends ParameterHandler {

	public String getName();
	
	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer);
	
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight);
	
	public Parameters getParameters();
	
	public String toString();
}
