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
package com.rapidminer.operator.learner.associations;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.Icon;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.viewer.AssociationRuleVisualization;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.Tableable;
import com.rapidminer.tools.Tools;

/**
 * A set of {@link AssociationRule}s which can be constructed from frequent item sets.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: AssociationRules.java,v 1.10 2008/05/09 19:23:21 ingomierswa Exp $
 */
public class AssociationRules extends ResultObjectAdapter implements Iterable<AssociationRule>, Tableable {
	
	private static final long serialVersionUID = 3734387908954857589L;
	
	private static final int MAXIMUM_NUMBER_OF_RULES_IN_OUTPUT = 100;
	
	private static final String RESULT_ICON_NAME = "lightbulb_on.png";
	
	private static Icon resultIcon = null;
	
	static {
		resultIcon = SwingTools.createIcon("16/" + RESULT_ICON_NAME);
	}
	
	private List<AssociationRule> associationRules = new ArrayList<AssociationRule>();

	public void addItemRule(AssociationRule rule) {
		associationRules.add(rule);
	}

	public int getNumberOfRules() {
		return associationRules.size();
	}
	
	public AssociationRule getRule(int index) {
		return associationRules.get(index);
	}
	
	public String getExtension() {
		return "asr";
	}

	public String getFileDescription() {
		return "Association Rules";
	}

	public String toResultString() {
		return toString(-1);
	}
	
	public String toString() {
		return toString(MAXIMUM_NUMBER_OF_RULES_IN_OUTPUT);
	}
	
	public String toString(int maxNumber) {
		Collections.sort(associationRules);
		StringBuffer buffer = new StringBuffer("Association Rules" + Tools.getLineSeparator());
		int counter = 0;
		for (AssociationRule rule : associationRules) {
			if ((maxNumber >= 0) && (counter > maxNumber)) {
				buffer.append("... " + (associationRules.size() - maxNumber) + " other rules ...");
				break;
			}
			buffer.append(rule.toString());
			buffer.append(Tools.getLineSeparator());
			counter++;
		}
		return buffer.toString();
	}
	
    /** Returns the visualization component. */
    public Component getVisualizationComponent(IOContainer container) {
        return new AssociationRuleVisualization(this, super.getVisualizationComponent(container));
    }

    public Icon getResultIcon() {
    	return resultIcon;
    }
    
	public Iterator<AssociationRule> iterator() {
		return associationRules.iterator();
	}
	
	public Item[] getAllConclusionItems() {
		SortedSet<Item> conclusions = new TreeSet<Item>();
		for (AssociationRule rule : this) {
			Iterator<Item> i = rule.getConclusionItems();
			while (i.hasNext()) {
				conclusions.add(i.next());
			}
		}
		Item[] itemArray = new Item[conclusions.size()];
		conclusions.toArray(itemArray);
		return itemArray;
	}

	public String getCell(int row, int column) {
		if (column == 0) {
			StringBuffer buffer = new StringBuffer();
			Iterator<Item> iterator = associationRules.get(row).getPremiseItems();
			while (iterator.hasNext()) {
				buffer.append(iterator.next().toString());
				if (iterator.hasNext())
					buffer.append(", ");
			}
			return buffer.toString();
		} else {
			StringBuffer buffer = new StringBuffer();
			Iterator<Item> iterator = associationRules.get(row).getPremiseItems();
			while (iterator.hasNext()) {
				buffer.append(iterator.next().toString());
				if (iterator.hasNext())
					buffer.append(", ");
			}
			return buffer.toString();
		}
	}

	public int getColumnNumber() {
		return 2;
	}

	public int getRowNumber() {
		return getNumberOfRules();
	}
}
