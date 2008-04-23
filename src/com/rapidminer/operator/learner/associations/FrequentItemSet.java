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
package com.rapidminer.operator.learner.associations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.rapidminer.tools.Tools;

/**
 * A frequent item set contains a set of frequent {@link Item}s. 
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: FrequentItemSet.java,v 1.3 2007/06/20 22:13:13 ingomierswa Exp $
 */
public class FrequentItemSet implements Comparable<FrequentItemSet>, Cloneable {

	private ArrayList<Item> items;

	private int frequency;

	public FrequentItemSet() {
		this.items = new ArrayList<Item>();
	}

	public FrequentItemSet(ArrayList<Item> items, int frequency) {
		this.items = items;
		Collections.sort(this.items);
		this.frequency = frequency;
	}

	public void addItem(Item item, int frequency) {
		items.add(item);
		Collections.sort(this.items);
		this.frequency = frequency;
	}

	public Collection<Item> getItems() {
		return items;
	}

    public Item getItem(int index) {
        return items.get(index);
    }
    
    public int getNumberOfItems() {
        return items.size();
    }
    
	public int getFrequency() {
		return frequency;
	}

	/**
	 * This method compares FrequentItemSets. It first compares the length of items sets, then the items itself. If they are the same, the Sets are
	 * equal.
	 */
	public int compareTo(FrequentItemSet o) {
		// compare size
		Collection<Item> hisItems = o.getItems();
		if (items.size() < hisItems.size()) {
			return -1;
		} else if (items.size() > hisItems.size()) {
			return 1;
		} else {
			// compare items
			Iterator<Item> iterator = hisItems.iterator();
			for (Item myCurrentItem : this.items) {
				int relation = myCurrentItem.compareTo(iterator.next());
				if (relation != 0) {
					return relation;
				}
			}
			// equal sets
			return 0;
		}
	}

	/**
	 * this method returns true if the frequent Items set are equal in size and items.
	 */
	public boolean equals(Object o) {
		if (o instanceof FrequentItemSet) {
			return (this.compareTo((FrequentItemSet) o) == 0);
		}
		return false;
	}

	public int hashCode() {
		return items.hashCode();
	}

	/**
	 * This method returns a representation of the items
	 */
	public String getItemsAsString() {
		StringBuffer buffer = new StringBuffer();
		Iterator<Item> iterator = items.iterator();
		while (iterator.hasNext()) {
			buffer.append(iterator.next().toString());
			if (iterator.hasNext()) {
				buffer.append(", ");
			}
		}
		return buffer.toString();
	}

	/**
	 * This method should return a proper String representation of this frequent Item Set
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		Iterator<Item> iterator = items.iterator();
		while (iterator.hasNext()) {
			buffer.append(iterator.next().toString());
			if (iterator.hasNext()) {
				buffer.append(", ");
			}
		}
		buffer.append(", frequency: ");
		buffer.append(Tools.formatNumber(frequency));
		return buffer.toString();
	}

	public Object clone() {
		return new FrequentItemSet(new ArrayList<Item>(items), frequency);
	}
}
