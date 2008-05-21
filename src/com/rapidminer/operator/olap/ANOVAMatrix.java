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
package com.rapidminer.operator.olap;

import java.awt.Component;
import java.util.List;

import javax.swing.Icon;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.viewer.ANOVAMatrixViewer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ResultObjectAdapter;


/**
 * Displays the result of an ANOVA matrix calculation.
 * 
 * @author Ingo Mierswa
 * @version $Id: ANOVAMatrix.java,v 1.5 2008/05/09 19:23:24 ingomierswa Exp $
 */
public class ANOVAMatrix extends ResultObjectAdapter {

	private static final long serialVersionUID = 6245314851143584397L;

	private static final String RESULT_ICON_NAME = "table.png";
	
	private static Icon resultIcon = null;
	
	static {
		resultIcon = SwingTools.createIcon("16/" + RESULT_ICON_NAME);
	}
	
	private double[][] probabilities;
	
	private List<String> anovaAttributeNames;
	
	private List<String> groupNames; 
	
	private double significanceLevel;
	
	public ANOVAMatrix(double[][] probabilities,
			           List<String> anovaAttributeNames,
			           List<String> groupNames,
			           double significanceLevel) {
		this.probabilities = probabilities;
		this.anovaAttributeNames = anovaAttributeNames;
		this.groupNames = groupNames;
		this.significanceLevel = significanceLevel;
	}
	
	public double[][] getProbabilities() { 
		return probabilities; 
	}
	
	public List<String> getAnovaAttributeNames() {
		return anovaAttributeNames;
	}
	
	public List<String> getGroupingAttributeNames() {
		return groupNames;
	}
	
	public double getSignificanceLevel() {
		return significanceLevel;
	}
	
	public Component getVisualizationComponent(IOContainer container) {
		return new ANOVAMatrixViewer(this);
	}
	
	public Icon getResultIcon() {
		return resultIcon;
	}
	
	public String toResultString() {
		StringBuffer result = new StringBuffer();
		
		return result.toString();
	}
	
	public String toString() {
		return 
		"ANOVA matrix indicating which attributes provide significant differences " + 
		"between groups defined by other (nominal) attributes.";
	}
	
	public String getExtension() {
		return "ano";
	}

	public String getFileDescription() {
		return "anova matrix";
	}
}
