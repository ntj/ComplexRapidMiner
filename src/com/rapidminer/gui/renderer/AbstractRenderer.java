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

import java.awt.Color;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.Parameters;
import com.rapidminer.parameter.UndefinedParameterError;

/**
 * This is the abstract renderer superclass for all renderers which
 * provide some basic methods for parameter handling.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractRenderer.java,v 1.3 2008/07/13 16:39:41 ingomierswa Exp $
 */
public abstract class AbstractRenderer implements Renderer {

	private Parameters parameters;
	
	public AbstractRenderer() {}
	
	public List<ParameterType> getParameterTypes() {
		return new LinkedList<ParameterType>();
	}
	
	public Object getParameter(String key) throws UndefinedParameterError {
		return getParameters().getParameter(key);
	}
	
	public String toString() {
		return getName();
	}

	public boolean getParameterAsBoolean(String key) {
		try {
			return (Boolean)getParameter(key);
		} catch (UndefinedParameterError e) {
			return false;
		}
	}

	public Color getParameterAsColor(String key) throws UndefinedParameterError {
		return (Color)getParameter(key);
	}

	public double getParameterAsDouble(String key) throws UndefinedParameterError {
		return (Double)getParameter(key);
	}

	public File getParameterAsFile(String key) throws UndefinedParameterError {
		return (File)getParameter(key);
	}

	public int getParameterAsInt(String key) throws UndefinedParameterError {
		return (Integer)getParameter(key);
	}

	public double[][] getParameterAsMatrix(String key) throws UndefinedParameterError {
		return (double[][])getParameter(key);
	}

	public String getParameterAsString(String key) throws UndefinedParameterError {
		return (String)getParameter(key);
	}

	public List getParameterList(String key) throws UndefinedParameterError {
		return (List)getParameter(key);
	}

	public boolean isParameterSet(String key) throws UndefinedParameterError {
		return getParameter(key) != null;
	}

	public void setListParameter(String key, List list) {
		this.parameters.setParameter(key, list);
	}

	public void setParameter(String key, String value) {
		this.parameters.setParameter(key, value);
	}

	/** Do nothing. */
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
	
	/** Returns null. */
	public Parameters getParameters() {
		if (this.parameters == null) {
			this.parameters = new Parameters(getParameterTypes());
		}
		return this.parameters;
	}
}
