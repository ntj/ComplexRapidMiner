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
package com.rapidminer.operator.similarity;

import java.awt.Component;

import javax.swing.Icon;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.viewer.SimilarityVisualization;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.IterationArrayList;


/**
 * Implements some common functionality for similarity measures.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: SimilarityAdapter.java,v 1.6 2008/05/09 19:22:52 ingomierswa Exp $
 */
public abstract class SimilarityAdapter extends ResultObjectAdapter implements SimilarityMeasure {

	private static final String RESULT_ICON_NAME = "graph_edged_curve.png";
	
	private static Icon resultIcon = null;
	
	static {
		resultIcon = SwingTools.createIcon("16/" + RESULT_ICON_NAME);
	}
	
	public String explainSimilarity(String x, String y) {
		return "no description or explaination available";
	}

	public String getName() {
		if (!isDistance())
			return "Similarity";
		else
			return "Distance";
	}

	public Component getVisualizationComponent(IOContainer container) {
		return new SimilarityVisualization(this);
	}
	
	public Icon getResultIcon() {
		return resultIcon;
	}

	public String toString() {
		if (!isDistance())
			return "A similarity measure defined on the following items... " + new IterationArrayList<String>(getIds());
		else
			return "A distance measure defined on the following items... " + new IterationArrayList<String>(getIds());
	}

	public String toResultString() {
		return toString();
	}

	public String getExtension() {
		return "sim";
	}

	public String getFileDescription() {
		return "similarity measure";
	}
}
