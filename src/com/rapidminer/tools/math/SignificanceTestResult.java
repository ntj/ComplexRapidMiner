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
package com.rapidminer.tools.math;

import javax.swing.Icon;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.ResultObjectAdapter;

/**
 * This class encapsulates the result of a statistical significance test.
 * Subclasses may also want to override the method getVisualizationComponent to
 * provide a nicer graphical version for the GUI.
 * 
 * @author Ingo Mierswa
 * @version $Id: SignificanceTestResult.java,v 1.2 2006/03/21 15:35:53
 *          ingomierswa Exp $
 */
public abstract class SignificanceTestResult extends ResultObjectAdapter {

	private static final String RESULT_ICON_NAME = "percent.png";
	
	private static Icon resultIcon = null;
	
	static {
		resultIcon = SwingTools.createIcon("16/" + RESULT_ICON_NAME);
	}
	
	/** Returns the name of the test. */
	public abstract String getName();

	/** Returns a string describing the test result. */
	public abstract String toString();
	
	/** Returns the calculated probability value. */
    public abstract double getProbability();
    
    public String getExtension() { return "sgf"; }
    
    public String getFileDescription() { return "significance test"; }
    
    public Icon getResultIcon() {
    	return resultIcon;
    }
}
