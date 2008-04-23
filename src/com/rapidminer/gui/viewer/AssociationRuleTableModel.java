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
package com.rapidminer.gui.viewer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.rapidminer.operator.learner.associations.AssociationRule;
import com.rapidminer.operator.learner.associations.AssociationRules;
import com.rapidminer.operator.learner.associations.Item;

/**
 * The table model for the association rules visualization.
 *
 * @author Ingo Mierswa
 * @version $Id: AssociationRuleTableModel.java,v 1.2 2007/06/22 22:14:16 ingomierswa Exp $
 */
public class AssociationRuleTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -4323147898914632476L;

	private static final String[] COLUMN_NAMES = {
		"Premises",
		"Conclusion",
		"Support",
		"Confidence"
	};
	
	private static final int COLUMN_PREMISES   = 0;
	private static final int COLUMN_CONCLUSION = 1;
	private static final int COLUMN_SUPPORT    = 2;
	private static final int COLUMN_CONFIDENCE = 3;
	
	private AssociationRules rules;
	
	private int[] mapping = null;
	
	public AssociationRuleTableModel(AssociationRules rules) {
		this.rules = rules;
		createCompleteMapping();
	}

	public Class<?> getColumnClass(int column) {
		if ((column == COLUMN_CONFIDENCE) || (column == COLUMN_SUPPORT)) {
			return Double.class;
		} else {
			return String.class;
		}
	}
	
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}
	
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	public int getRowCount() {
		return this.mapping.length;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		AssociationRule rule = rules.getRule(this.mapping[rowIndex]);
		switch (columnIndex) {
		case COLUMN_PREMISES:
			return getItemString(rule.getPremiseItems());
		case COLUMN_CONCLUSION:
			return getItemString(rule.getConclusionItems());
		case COLUMN_SUPPORT:
			return Double.valueOf(rule.getTotalSupport());
		case COLUMN_CONFIDENCE:
			return Double.valueOf(rule.getConfidence());
		default:
			// cannot happen
			return "?";
		}
	}
	
	private String getItemString(Iterator<Item> iterator) {
		StringBuffer result = new StringBuffer();
		boolean first = true;
		while (iterator.hasNext()) {
			if (!first)
				result.append(", ");
			Item item = iterator.next();
			result.append(item.toString());
			first = false;
		}		
		return result.toString();
	}
	
	public void setFilter(Item[] filter) {
		if (filter == null) {
			createCompleteMapping();
		} else {
			List<Integer> indices = new LinkedList<Integer>();
			int counter = 0;
			for (AssociationRule rule : this.rules) {
				boolean found = false;
				for (Item filterItem : filter) {
					Iterator<Item> c = rule.getConclusionItems();
					while (c.hasNext()) {
						Item conclusionItem =  c.next();
						if (filterItem.equals(conclusionItem)) {
							indices.add(counter);
							found = true;
							break;
						}
					}
					if (found)
						break;
				}
				counter++;
			}
			this.mapping = new int[indices.size()];
			Iterator<Integer> k = indices.iterator();
			counter = 0;
			while (k.hasNext()) {
				this.mapping[counter++] = k.next();
			}
		}
		fireTableStructureChanged();
	}
	
	private void createCompleteMapping() {
		this.mapping = new int[this.rules.getNumberOfRules()];
		for (int i = 0; i < this.mapping.length; i++)
			this.mapping[i] = i;
	}
}
