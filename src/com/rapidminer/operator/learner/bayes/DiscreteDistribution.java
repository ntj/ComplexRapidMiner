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
package com.rapidminer.operator.learner.bayes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.operator.learner.Distribution;
import com.rapidminer.tools.Tools;


/**
 * DiscreteDistribution is an distribution for nominal values. For probability calculation it counts the frequency of all values and returns this
 * number + 1 of the given value divided by the total number of all examples + the number of different values.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: DiscreteDistribution.java,v 1.2 2007/06/22 22:39:09 ingomierswa Exp $
 */
public class DiscreteDistribution implements Distribution {

	private static final long serialVersionUID = 7573474548080998479L;

	private ArrayList<Double> values= new ArrayList<Double>();

	private ArrayList<Double> occurences= new ArrayList<Double>();

	private int totalOccurences = 0;

	private int totalValueNumber = 0;

	private double totalProbability = 0;

	private Attribute attribute;
	
	public DiscreteDistribution(Attribute attribute, Set<Double> possibleValues, Collection<Double> allValues) {
		this.attribute = attribute;
		this.totalValueNumber = attribute.getMapping().size();

		Iterator<Double> setIterator = possibleValues.iterator();
		while (setIterator.hasNext()) {
			Double value = setIterator.next();
			values.add(value);
			double occurences = Collections.frequency(allValues, value);
			totalOccurences += occurences;
			this.occurences.add(occurences);
		}
		
		for (int i = 0; i < occurences.size(); i++) {
			Double value = occurences.get(i);
			double valueD = value.doubleValue();
			valueD = (valueD + 1) / (totalOccurences + totalValueNumber);
			occurences.set(i, valueD);
			totalProbability += valueD;
		}
	}

	public double getProbability(double x) {
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i).equals(x)) {
				return this.occurences.get(i);
			}
		}
		return (1d - totalProbability) / (totalValueNumber - values.size());
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < values.size(); i++) {
			buffer.append(attribute.getMapping().mapIndex((int)values.get(i).doubleValue()) + "\t");
		}
		buffer.append(Tools.getLineSeparator());
		for (int i = 0; i < values.size(); i++) {
			buffer.append(Tools.formatIntegerIfPossible(occurences.get(i)) + "\t");
		}
		buffer.append(" else " + Tools.formatNumber((1d - totalProbability) / (totalValueNumber - values.size())));
		buffer.append(" (sum: " + Tools.formatIntegerIfPossible(totalOccurences) + ")");
		return buffer.toString();
	}
}
