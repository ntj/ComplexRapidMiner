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
package com.rapidminer.parameter;

import java.util.List;

import com.rapidminer.Process;
import com.rapidminer.gui.renderer.Renderer;
import com.rapidminer.operator.Operator;

/**
 * This interface defines that instance are able to handle parameters. In RapidMiner,
 * this if for example true for the class {@link Operator} but also for the {@link Renderer}s.
 * 
 * @author Ingo Mierswa
 * @version $Id: ParameterHandler.java,v 1.3 2008/07/13 20:38:24 ingomierswa Exp $
 */
public interface ParameterHandler {

	/** Returns a collection of all parameters of this operator. */
	public Parameters getParameters();

    /** Sets all parameters of this operator. The given parameters are not allowed to be null and must 
     *  correspond to the parameter types defined by this operator. */
    public void setParameters(Parameters parameters);
    
	/**
	 * Sets the given single parameter to the Parameters object of this
	 * operator. For parameter list the method
	 * {@link #setListParameter(String, List)} should be used.
	 */
	public void setParameter(String key, String value);

	/**
	 * Sets the given parameter list to the Parameters object of this operator.
	 * For single parameters the method {@link #setParameter(String, String)}
	 * should be used.
	 */
	public void setListParameter(String key, List list);

	/**
	 * Returns a single parameter retrieved from the {@link Parameters} of this
	 * Operator.
	 */
	public Object getParameter(String key) throws UndefinedParameterError;

	/** Returns true iff the parameter with the given name is set. */
	public boolean isParameterSet(String key) throws UndefinedParameterError;

	/** Returns a single named parameter and casts it to String. */
	public String getParameterAsString(String key) throws UndefinedParameterError;

	/** Returns a single named parameter and casts it to int. */
	public int getParameterAsInt(String key) throws UndefinedParameterError;

	/** Returns a single named parameter and casts it to double. */
	public double getParameterAsDouble(String key) throws UndefinedParameterError;

	/**
	 * Returns a single named parameter and casts it to boolean. This method
	 * never throws an exception since there are no non-optional boolean
	 * parameters.
	 */
	public boolean getParameterAsBoolean(String key);

	/**
	 * Returns a single named parameter and casts it to List. The list returned
	 * by this method contains the user defined key-value pairs. Each element is
	 * an Object array of length 2. The first element is the key (String) the
	 * second the parameter value object, e.g. a Double object for
	 * ParameterTypeDouble. Since the definition of typed lists for arrays is not
	 * possible the caller have to perform the casts to the object arrays and from
	 * there to the actual types himself.
	 */
	public List getParameterList(String key) throws UndefinedParameterError;

	/** Returns a single named parameter and casts it to Color. */
	public java.awt.Color getParameterAsColor(String key) throws UndefinedParameterError;

	/**
	 * Returns a single named parameter and casts it to File. This file is
	 * already resolved against the process definition file. If the parameter name defines a
	 * non-optional parameter which is not set and has no default value, a
	 * UndefinedParameterError will be thrown. If the parameter is optional and
	 * was not set this method returns null. Operators should always use this
	 * method instead of directly using the method
	 * {@link Process#resolveFileName(String)}.
	 */
	public java.io.File getParameterAsFile(String key) throws UndefinedParameterError;

    /** Returns a single named parameter and casts it to a double matrix. */
    public double[][] getParameterAsMatrix(String key) throws UndefinedParameterError;
    
    /** Returns a list of all defined parameter types for this handler. */
    public List<ParameterType> getParameterTypes();
    
}
