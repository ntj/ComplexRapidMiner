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
package com.rapidminer.operator;

import java.awt.Component;
import java.util.List;

import javax.swing.Icon;

import com.rapidminer.gui.tools.ExtendedJScrollPane;


/**
 * This interface extends IOObject and is hence an object which can be handled
 * by operators. Additionally this object is a result and can be of interest for
 * a user. ResultWriters can write the results in a result file.
 * 
 * @see com.rapidminer.operator.io.ResultWriter
 * @author Ingo Mierswa
 * @version $Id: ResultObject.java,v 1.4 2008/05/09 19:23:19 ingomierswa Exp $
 */
public interface ResultObject extends IOObject {

	/** Defines the name of this result object. */
	public abstract String getName();

	/** Result string will be displayed in result files written with a ResultWriter operator. */
	public abstract String toResultString();

	/** Returns a component that can visualize this result in the GUI. Please note that
     *  the delivered visualization component is _not_ enclosed by a JScrollPane and implementing
     *  classes must ensure themself that the component is scrollable if desired. 
     *  It is suggested that an instance of the class {@link ExtendedJScrollPane} is used
     *  for that purpose.
     *  The given IOContainer can usually be ignored but can be used in order to allow a sort
     *  of combined visualization of two or more results, e.g. a ExampleSet visualization containing
     *  also the weights for the attributes (AttributeWeights). Please note that the given container 
     *  might be null which must not lead to a NullPointerExcepton! */
	public abstract Component getVisualizationComponent(IOContainer container);
	
	/** Returns an icon used for displaying the results. May return null. */
	public Icon getResultIcon();
	
	/**
	 * Returns a list of actions (e.g. "save") that is displayed below (or near
	 * to) the visualisation component.
	 */
	public abstract List getActions();

}
