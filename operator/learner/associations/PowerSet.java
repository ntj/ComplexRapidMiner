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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * The power set of a collection of items.
 * 
 * @author Sebastian Land
 * @version $Id: PowerSet.java,v 1.5 2008/05/09 19:23:21 ingomierswa Exp $
 * 
 * @param <ItemT> the item type
 */
public class PowerSet<ItemT> implements Iterable<Collection<ItemT>>, Iterator<Collection<ItemT>>{

    private ArrayList<ItemT> set;
    
	private boolean[] subSetIndicator;
	
    
	public PowerSet(Collection<ItemT> collection) {
		set = new ArrayList<ItemT>(collection);
	}

	public Iterator<Collection<ItemT>> iterator() {
		return new PowerSet<ItemT>(set);
	}
	
	public Collection<ItemT> getComplement(Collection<ItemT> collection) {
		Collection<ItemT> complement = new ArrayList<ItemT>(set.size() - collection.size());
		Iterator<ItemT> iterator = set.iterator();
		for (ItemT item: collection) {
			while(iterator.hasNext()) {
				ItemT currentSetItem = iterator.next();
				if (currentSetItem == item) {
					break;
				} else {
					complement.add(currentSetItem);
				}
			}
		}
		while(iterator.hasNext()) {
			complement.add(iterator.next());
		}
		return complement;
	}

	public boolean hasNext() {
		if (subSetIndicator == null) {
			return true;
		} else {
			for (int i = 0; i < subSetIndicator.length; i++) {
				if (!subSetIndicator[i]) {
					return true;
				}
			}
			return false;
		}
	}

	public Collection<ItemT> next() {
		// do initialising
		if (subSetIndicator == null) {
			subSetIndicator = new boolean[set.size()];
		} else {
			// increase binary counter
			for (int i = 0; i < subSetIndicator.length; i++) {
				if (subSetIndicator[i]) {
					// flipping 1 from right to left
					subSetIndicator[i] = false;
				} else {
					// found zero: flip to 1
					subSetIndicator[i] = true;
					break;
				}
			}
		}
		// generating subset
		ArrayList<ItemT> subset = new ArrayList<ItemT>();
		for (int i = 0; i < subSetIndicator.length; i++) {
			if (subSetIndicator[i]) {
				subset.add(set.get(i));
			}
		}
		return subset;
	}

    /** Throws an {@link UnsupportedOperationException} since removal is not supported. */
    public void remove() {
        throw new UnsupportedOperationException("The 'remove' operation is not supported by power sets!");
    }
}
