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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeParser;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.AbstractExampleTable;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.generator.GenerationException;
import com.rapidminer.tools.RandomGenerator;


/**
 * If the example set contain two equivalent attributes, the longer
 * representation is removed. The length is calculated as the number of nested
 * brackets. The equivalency probe is not done by structural comparison. The
 * attribute values of the equations in question are randomly sampled and the
 * equation results compared. If the difference is less than <i>epsilon</i> for
 * <i>k</i> trials, the equations are probably equivalent. At least they
 * produce similar values. <br/>
 * 
 * The values of the attributes are sampled in the range of the minimum and
 * maximum values of the attribute. This ensures equivalency or at least very
 * similar values for the definition range in question. Therefore a
 * {@link MemoryExampleTable} is constructed and filled with random values. Then
 * a {@link AttributeParser} is used to construct the attributes values.
 * 
 * @author Ingo Mierswa
 * @version $Id: EquivalentAttributeRemoval.java,v 2.17 2006/03/27 13:21:58
 *          ingomierswa Exp $
 */
public class EquivalentAttributeRemoval extends IndividualOperator {

	/**
	 * Indicates the number of examples which should be randomly generated to
	 * check equivalency.
	 */
	private int numberOfSamples = 5;

	/**
	 * If the difference is smaller than epsilon, the attributes are considered
	 * as equivalent.
	 */
	private double epsilon = 0.05d;

	/** Recalculates attribute statistics before sampling. */
	private boolean recalculateAttributeStatistics = false;

    /** The random generator for the example values. */
    private RandomGenerator random;
    
	/** Creates a new equivalent attribute removal population operator. */
	public EquivalentAttributeRemoval(int numberOfSamples, double epsilon, boolean recalculateAttributeStatistics, RandomGenerator random) {
		this.numberOfSamples = numberOfSamples;
		this.epsilon = epsilon;
		this.recalculateAttributeStatistics = recalculateAttributeStatistics;
        this.random = random;
	}

	public List<Individual> operate(Individual individual) {
		AttributeWeightedExampleSet exampleSet = individual.getExampleSet();
		if (recalculateAttributeStatistics)
			exampleSet.recalculateAllAttributeStatistics();
		Attribute[] allAttributes = exampleSet.getExampleTable().getAttributes();
		List<Attribute> simpleAttributesList = new ArrayList<Attribute>();
		for (int i = 0; i < allAttributes.length; i++) {
			if ((allAttributes[i] != null) && (!allAttributes[i].getConstruction().isGenerated()))
				simpleAttributesList.add(allAttributes[i]);
		}
        
        

		Map<String, Attribute> removeMap = new HashMap<String, Attribute>();
		Attribute[] attributeArray = exampleSet.getAttributes().createRegularAttributeArray();
		for (int i = 0; i < attributeArray.length; i++) {
			for (int j = i + 1; j < attributeArray.length; j++) {
				Attribute att1 = attributeArray[i];
				Attribute att2 = attributeArray[j];
				if (att1.getConstruction().equals(att2.getConstruction())) {
					removeMap.put(att2.getName(), att2);
				} else {
					AbstractExampleTable exampleTable = new MemoryExampleTable(simpleAttributesList, new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.'), numberOfSamples);
					try {
						// create parser
						AttributeParser parser = new AttributeParser(exampleTable);
						
						

						// create data set and attributes to check
						Tools.fillTableWithRandomValues(exampleTable, exampleSet, random);
						ExampleSet randomSet = new SimpleExampleSet(exampleTable, new LinkedList<Attribute>());
						parser.generateAttribute(randomSet.getLog(), att1.getConstruction().getDescription(false));
						parser.generateAttribute(randomSet.getLog(), att2.getConstruction().getDescription(false));
						
						// add longer attribute to remove map if equivalent
						if (equivalent(randomSet)) {
							int depth1 = att1.getConstruction().getDepth();
							int depth2 = att2.getConstruction().getDepth();
							if (depth1 > depth2)
								removeMap.put(att1.getName(), att1);
							else
								removeMap.put(att2.getName(), att2);
						}
					} catch (GenerationException e) {
						exampleSet.getLog().logWarning("Cannot generate test attribute: " + e.getMessage() + ". We just keep both attributes for sure...");
					}
				}
			}
		}

		Iterator i = removeMap.values().iterator();
		while (i.hasNext()) {
			Attribute attribute = (Attribute) i.next();
			exampleSet.getLog().log("Remove equivalent attribute '" + attribute.getName() + "'.");
			exampleSet.getAttributes().remove(attribute);
		}

		List<Individual> l = new LinkedList<Individual>();
		l.add(new Individual(exampleSet));
		return l;
	}

	private boolean equivalent(ExampleSet exampleSet) {
		if (exampleSet.getAttributes().size() < 2) {
			return true;
		} else {
			Iterator<Example> reader = exampleSet.iterator();
			Iterator<Attribute> a = exampleSet.getAttributes().iterator();
			Attribute a1 = a.next();
			Attribute a2 = a.next();
			if (a1.equals(a2))
				return true;
			while (reader.hasNext()) {
				Example example = reader.next();
				if (Math.abs(example.getValue(a1) - example.getValue(a2)) > epsilon)
					return false;
			}
			return true;
		}
	}
}
