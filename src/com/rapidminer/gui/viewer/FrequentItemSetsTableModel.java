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
package com.rapidminer.gui.viewer;

import java.util.LinkedList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.rapidminer.operator.learner.associations.FrequentItemSet;
import com.rapidminer.operator.learner.associations.FrequentItemSets;
import com.rapidminer.operator.learner.associations.Item;
import com.rapidminer.tools.Tools;

/** 
 * This table model can be used as visualization for a set of frequent
 * item sets.
 * 
 * @author Ingo Mierswa
 * @version $Id: FrequentItemSetsTableModel.java,v 1.4 2008/05/09 19:22:59 ingomierswa Exp $
 */
public class FrequentItemSetsTableModel extends AbstractTableModel {
        
    private static final long serialVersionUID = 1497336028647445690L;
    
    private static final int COLUMN_SIZE    = 0;
    private static final int COLUMN_SUPPORT = 1;
    
    private FrequentItemSets frequentSets;
 
    private int[] mapping = null;
    
    private int maxItemSetSize = 0;
    
    public FrequentItemSetsTableModel(FrequentItemSets frequentSets) {
        this.frequentSets = frequentSets;
        updateFilter(0, Integer.MAX_VALUE, null);
    }
    
    public String getColumnName(int column) {
        if (column == COLUMN_SIZE) {
            return "Size";
        } else if (column == COLUMN_SUPPORT) {
            return "Support";
        } else {
            return "Item " + (column - 1);
        }
    }
    
    public int getColumnCount() {
        return maxItemSetSize + 2;
    }

    public int getRowCount() {
        return mapping.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        FrequentItemSet itemSet = frequentSets.getItemSet(this.mapping[rowIndex]);
        if (columnIndex == COLUMN_SIZE) {
            return itemSet.getItems().size();
        } else if (columnIndex == COLUMN_SUPPORT) {
            return Tools.formatNumber((double)itemSet.getFrequency() / (double)frequentSets.getNumberOfTransactions());
        } else {
            int actualIndex = columnIndex - 2;
            if (actualIndex < itemSet.getNumberOfItems()) {
                return itemSet.getItem(actualIndex).toString();
            } else {
                return "";
            }
        }
    }
    
    public void updateFilter(int min, int max, String itemName) {
        List<Integer> indices = new LinkedList<Integer>();
        int index = 0;
        this.maxItemSetSize = 0;
        String[] itemNames = null;
        if (itemName != null) {
            itemNames = itemName.split(",");
        }
        for (FrequentItemSet itemSet : frequentSets) {
            if (acceptItemSet(itemSet, min, max, itemNames)) {
                indices.add(index);
                this.maxItemSetSize = Math.max(this.maxItemSetSize, itemSet.getNumberOfItems());
            }
            index++;
        }
        
        this.mapping = new int[indices.size()];
        int counter = 0;
        for (int c : indices) {
            this.mapping[counter++] = c;
        }
        fireTableStructureChanged();
    }
    
    private boolean acceptItemSet(FrequentItemSet itemSet, int min, int max, String[] itemNames) {
        int size = itemSet.getNumberOfItems();
        if (size < min)
            return false;
        if (size > max)
            return false;
        
        if ((itemNames == null) || (itemNames.length == 0))
            return true;
        
        for (Item item : itemSet.getItems()) {
            for (String itemName : itemNames)
                if (item.toString().indexOf(itemName.trim()) >= 0)
                    return true;
        }
        return false;
    }
}
