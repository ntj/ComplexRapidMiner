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
package com.rapidminer.gui.renderer.similarity;

import javax.swing.table.TableModel;

import com.rapidminer.gui.renderer.AbstractTableModelTableRenderer;
import com.rapidminer.gui.viewer.SimilarityTableModel;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.similarity.SimilarityMeasure;

/**
 * A renderer for the table view of a similarity measure.
 * 
 * @author Ingo Mierswa
 * @version $Id: SimilarityTableRenderer.java,v 1.2 2008/07/13 16:39:41 ingomierswa Exp $
 */
public class SimilarityTableRenderer extends AbstractTableModelTableRenderer {

	public TableModel getTableModel(Object renderable, IOContainer ioContainer) {
		SimilarityMeasure similarity = (SimilarityMeasure) renderable;
		return new SimilarityTableModel(similarity);
	}
}
