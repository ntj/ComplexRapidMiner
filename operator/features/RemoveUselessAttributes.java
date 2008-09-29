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
package com.rapidminer.operator.features;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.set.AttributeWeightedExampleSet;


/**
 * This individual operator removes all attributes from the example set which
 * has weight 0 or the same minimum and maximum values.
 * 
 * @author Ingo Mierswa
 * @version $Id: RemoveUselessFeatures.java,v 2.12 2006/03/27 13:21:58
 *          ingomierswa Exp $
 */
public class RemoveUselessAttributes extends IndividualOperator {

	public List<Individual> operate(Individual individual) throws Exception {
		AttributeWeightedExampleSet exampleSet = individual.getExampleSet();
		AttributeWeightedExampleSet clone = (AttributeWeightedExampleSet) exampleSet.clone();
		clone.recalculateAllAttributeStatistics();

		Iterator<Attribute> i = clone.getAttributes().iterator();
		while (i.hasNext()) {
			Attribute attribute = i.next();
			double weight = clone.getWeight(attribute);
			if (weight == 0.0d) {
				i.remove();
			} else if (!attribute.isNominal()) {
                double min = clone.getStatistics(attribute, Statistics.MINIMUM);
                double max = clone.getStatistics(attribute, Statistics.MAXIMUM);
				if (min == max) {
					// remove constant attributes if they are 0 or 1
					if ((min == 0.0d) || (max == 1.0d))
						i.remove();
				}
			}
		}

		LinkedList<Individual> l = new LinkedList<Individual>();
		if (clone.getNumberOfUsedAttributes() > 0) {
			l.add(new Individual(clone));
		} else {
			exampleSet.getLog().logWarning("No attributes left after removing useless attributes! Using original example set.");
			l.add(new Individual(exampleSet));
		}
		return l;
	}
}
