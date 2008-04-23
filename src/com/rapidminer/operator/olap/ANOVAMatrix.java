/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.olap;

import java.awt.Component;
import java.util.List;

import com.rapidminer.gui.viewer.ANOVAMatrixViewer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ResultObjectAdapter;


/**
 * Displays the result of an ANOVA matrix calculation.
 * 
 * @author Ingo Mierswa
 * @version $Id: ANOVAMatrix.java,v 1.1 2007/05/27 22:02:02 ingomierswa Exp $
 */
public class ANOVAMatrix extends ResultObjectAdapter {

	private static final long serialVersionUID = 6245314851143584397L;

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
