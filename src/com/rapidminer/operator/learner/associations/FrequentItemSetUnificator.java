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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.Tupel;
/**
 * This operator compares a number of FrequentItemSet sets and removes every
 * not unique FrequentItemSet. 
 * 
 * @author Sebastian Land
 * @version $Id: FrequentItemSetUnificator.java,v 1.4 2008/05/09 19:23:21 ingomierswa Exp $
 */
public class FrequentItemSetUnificator extends Operator {

//	private static final String PARAMETER_MINIMAL_DIFFERENCE = "minimal_support_difference";
//	private static final String PARAMETER_MINIMAL_SUPPORT = "minimal_support";

	private class FrequencyIgnoringSetComparator implements Comparator<FrequentItemSet> {
		public int compare(FrequentItemSet o1, FrequentItemSet o2) {
			// compare size
			Collection<Item> items = o1.getItems();
			Collection<Item> hisItems = o2.getItems();
			if (items.size() < hisItems.size()) {
				return -1;
			} else if (items.size() > hisItems.size()) {
				return 1;
			} else {
				// compare items
				Iterator<Item> iterator = hisItems.iterator();
				for (Item myCurrentItem : items) {
					int relation = myCurrentItem.toString().compareTo(iterator.next().toString());
					if (relation != 0) {
						return relation;
					}
				}
				// equal sets
				return 0;
			}
		}
		
	}
	
	private class TupelComparator implements Comparator<Tupel<FrequentItemSet, Iterator<FrequentItemSet>>> {

		public int compare(Tupel<FrequentItemSet, Iterator<FrequentItemSet>> o1, Tupel<FrequentItemSet, Iterator<FrequentItemSet>> o2) {
			FrequencyIgnoringSetComparator comparator = new FrequencyIgnoringSetComparator();
			return comparator.compare(o1.getFirst(), o2.getFirst());
		}
		
	}
	public FrequentItemSetUnificator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
//		double exampleSetSize = getInput(ExampleSet.class).size();
//		double minDifference = getParameterAsDouble(PARAMETER_MINIMAL_DIFFERENCE);
//		double minSupport = getParameterAsDouble(PARAMETER_MINIMAL_SUPPORT); 
		ArrayList<FrequentItemSets> sets = new ArrayList<FrequentItemSets>();
		try {
			int i = 0;
			while (true) {
				FrequentItemSets set = getInput(FrequentItemSets.class, 0);
				set.sortSets(new FrequencyIgnoringSetComparator());
				sets.add(set);
				i++;
			}
		} catch (MissingIOObjectException e) {
		}

		ArrayList<Tupel<FrequentItemSet, Iterator<FrequentItemSet>>> iteratorTupels = new ArrayList<Tupel<FrequentItemSet, Iterator<FrequentItemSet>>>(2);
		for (FrequentItemSets classSets: sets) {
			Iterator<FrequentItemSet> iterator = classSets.iterator();
			iteratorTupels.add(new Tupel<FrequentItemSet, Iterator<FrequentItemSet>>(iterator.next(), iterator));
		}
		// running through itterators
		while(haveNext(iteratorTupels)) {
			// filling set to test if all frequent item sets are equall
			Set<FrequentItemSet> currentSets = new TreeSet<FrequentItemSet>(new FrequencyIgnoringSetComparator());
			for (Tupel<FrequentItemSet, Iterator<FrequentItemSet>> tupel: iteratorTupels) {
				currentSets.add(tupel.getFirst());
			}
			if (currentSets.size() == 1) {
				// not unique: deletion
				ArrayList<Tupel<FrequentItemSet, Iterator<FrequentItemSet>>> newTupels = new ArrayList<Tupel<FrequentItemSet, Iterator<FrequentItemSet>>>(2);
				for (Tupel<FrequentItemSet, Iterator<FrequentItemSet>> tupel: iteratorTupels) {
					Iterator<FrequentItemSet> currentIterator = tupel.getSecond();
					currentIterator.remove();
					if (currentIterator.hasNext())
						newTupels.add(new Tupel<FrequentItemSet, Iterator<FrequentItemSet>>(currentIterator.next(), currentIterator));
				}
				iteratorTupels = newTupels;
			} else {
				// unique: no deletion but forward smallest iterator
				Collections.sort(iteratorTupels, new TupelComparator());
				Iterator<FrequentItemSet> currentIterator = iteratorTupels.get(0).getSecond();
				if (currentIterator.hasNext())
					iteratorTupels.add(new Tupel<FrequentItemSet, Iterator<FrequentItemSet>>(currentIterator.next(), currentIterator));
				iteratorTupels.remove(0);
			}
		}
		IOObject[] objects = new IOObject[sets.size()];
		int i = 0;
		for (FrequentItemSets currentSets: sets) {
			objects[i] = currentSets;
			i++;
		}

		return (objects);
	}

	private boolean haveNext(ArrayList<Tupel<FrequentItemSet, Iterator<FrequentItemSet>>> iterators) {
		boolean hasNext = iterators.size() > 0;
		for (Tupel<FrequentItemSet, Iterator<FrequentItemSet>> iterator: iterators) {
			hasNext = hasNext || iterator.getSecond().hasNext();
		}
		return hasNext;
	}

	public Class[] getInputClasses() {
		return new Class[] {FrequentItemSets.class, ExampleSet.class};
	}

	public Class[] getOutputClasses() {
		return new Class[] {FrequentItemSets.class};
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameters = super.getParameterTypes();
//		parameters.add(new ParameterTypeDouble(PARAMETER_MINIMAL_DIFFERENCE, "specifies the support threshold to regard two itemsets as different", 0, Double.POSITIVE_INFINITY, 0.2));
//		parameters.add(new ParameterTypeDouble(PARAMETER_MINIMAL_SUPPORT, "specifies the support threshold to keep an itemset", 0, Double.POSITIVE_INFINITY, 0.3));
		
		return parameters;
	}
}
