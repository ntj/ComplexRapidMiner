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
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.Icon;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.viewer.FrequentItemSetVisualization;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.Tableable;
import com.rapidminer.tools.Tools;

/**
 * Contains a collection of {@link FrequentItemSet}s.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: FrequentItemSets.java,v 1.12 2008/05/09 19:23:21 ingomierswa Exp $
 */
public class FrequentItemSets extends ResultObjectAdapter implements Iterable<FrequentItemSet>, Tableable {

	private static final long serialVersionUID = -6195363961857170621L;

    private static final int MAX_NUMBER_OF_ITEMSETS = 100;

	private static final String RESULT_ICON_NAME = "lightbulb_on.png";
	
	private static Icon resultIcon = null;
	
	static {
		resultIcon = SwingTools.createIcon("16/" + RESULT_ICON_NAME);
	}
	
	private int numberOfTransactions;

    private int maximumSetSize = 0;
    
	private ArrayList<FrequentItemSet> frequentSets;

	public FrequentItemSets(int numberOfTransactions) {
		this.numberOfTransactions = numberOfTransactions;
		this.frequentSets = new ArrayList<FrequentItemSet>();
	}

	/**
	 * Adds a frequent item set to this container. ConditionalItems and frequentItems are merged.
	 * 
	 * @param itemSet
	 *            the frequent set
	 */
	public void addFrequentSet(FrequentItemSet itemSet) {
		frequentSets.add(itemSet);
        maximumSetSize = Math.max(itemSet.getNumberOfItems(), maximumSetSize);
	}

	public String getExtension() {
		return "frq";
	}

	public String getFileDescription() {
		return "frequent item set";
	}

    public int getMaximumSetSize() {
        return this.maximumSetSize;
    }
    
	public Iterator<FrequentItemSet> iterator() {
		return frequentSets.iterator();
	}
	
    public FrequentItemSet getItemSet(int index) {
        return frequentSets.get(index);
    }
    
	public void sortSets() {
		Collections.sort(frequentSets);
	}

	public void sortSets(Comparator<FrequentItemSet> comparator) {
		Collections.sort(frequentSets, comparator);
	}
	
    public int size() {
        return frequentSets.size();
    }
    
    public int getNumberOfTransactions() {
        return this.numberOfTransactions;
    }
    
    public String toResultString() {
        return toString(-1);
    }

    /** This method generates the a string representatio of this object. */
    public String toString() {
        return toString(MAX_NUMBER_OF_ITEMSETS);
    }
    
    /** This method generates the a string representatio of this object. */
    public String toString(int maxNumber) {
        StringBuffer output = new StringBuffer("Frequent Item Sets (" + size() + "):" + Tools.getLineSeparator());
        if (frequentSets.size() == 0) {
            output.append("no itemsets found");
        } else {
            int counter = 0;
            for (FrequentItemSet set : frequentSets) {
                counter++;
                if ((maxNumber > 0) && (counter > maxNumber)) {
                    output.append("... " + (size() - maxNumber) + " additional item sets ...");
                    break;
                } else {
                    output.append(set.getItemsAsString());
                    output.append(" / ");
                    output.append(Tools.formatNumber((double)set.getFrequency() / (double)numberOfTransactions));
                    output.append(Tools.getLineSeparator());
                }
                
            }
        }
        return output.toString();
    }
    
    /** Returns the visualization component. */
    public Component getVisualizationComponent(IOContainer container) {
        return new FrequentItemSetVisualization(this);
    }
    
    public Icon getResultIcon() {
    	return resultIcon;
    }

	public String getCell(int row, int column) {
		FrequentItemSet set = frequentSets.get(row);
		if (set.getNumberOfItems() > column)
			return set.getItem(column).toString();
		else
			return "";
	}

	public int getColumnNumber() {
		return getMaximumSetSize();
	}

	public int getRowNumber() {
		return size();
	}
}
