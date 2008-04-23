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
package com.rapidminer.operator.similarity;

import java.awt.Component;

import com.rapidminer.gui.viewer.SimilarityVisualization;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.IterationArrayList;


/**
 * Implements some common functionality for similarity measures.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: SimilarityAdapter.java,v 1.2 2007/06/17 15:48:56 ingomierswa Exp $
 */
public abstract class SimilarityAdapter extends ResultObjectAdapter implements SimilarityMeasure {

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
