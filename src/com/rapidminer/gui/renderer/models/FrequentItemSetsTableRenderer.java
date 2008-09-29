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
package com.rapidminer.gui.renderer.models;

import java.awt.Component;

import javax.swing.table.TableModel;

import com.rapidminer.gui.renderer.AbstractTableModelTableRenderer;
import com.rapidminer.gui.viewer.FrequentItemSetVisualization;
import com.rapidminer.gui.viewer.FrequentItemSetsTableModel;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.learner.associations.FrequentItemSets;

/**
 * A renderer for the table view of frequent item sets.
 * 
 * @author Ingo Mierswa
 * @version $Id: FrequentItemSetsTableRenderer.java,v 1.2 2008/07/13 16:39:41 ingomierswa Exp $
 */
public class FrequentItemSetsTableRenderer extends AbstractTableModelTableRenderer {

	public TableModel getTableModel(Object renderable, IOContainer ioContainer) {
		FrequentItemSets frequentItemSets = (FrequentItemSets)renderable;
		frequentItemSets.sortSets();
        return new FrequentItemSetsTableModel(frequentItemSets);
	}
	
	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		FrequentItemSets frequentItemSets = (FrequentItemSets)renderable;
		return new FrequentItemSetVisualization(frequentItemSets);
	}
}
